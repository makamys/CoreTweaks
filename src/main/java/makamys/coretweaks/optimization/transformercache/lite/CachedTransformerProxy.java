package makamys.coretweaks.optimization.transformercache.lite;

import lombok.SneakyThrows;
import makamys.coretweaks.optimization.NonFunctionAlteringWrapper;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;

public class CachedTransformerProxy implements IClassTransformer, NonFunctionAlteringWrapper<IClassTransformer> {
    public int runs = 0;
    public int misses = 0;
    
    protected IClassTransformer original;
    private String transformerName;
    
    public CachedTransformerProxy(IClassTransformer original) {
        this.original = original;
        this.transformerName = original.getClass().getCanonicalName();
    }
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        runs++;
        byte[] result = TransformerCache.instance.getCached(transformerName, name, transformedName, basicClass);
        if(result == null) {
            misses++;
            TransformerCache.instance.prePutCached(transformerName, name, transformedName, basicClass);
            result = original.transform(name, transformedName, basicClass);
            TransformerCache.instance.putCached(transformerName, name, transformedName, result);
        }
        return TransformerCache.fromNullableByteArray(result);
    }
    
    public static CachedTransformerProxy of(IClassTransformer transformer) throws Exception {
        Class<?> cls = CachedTransformerProxyGenerator.generate(CachedTransformerProxy.class, transformer.getClass());
        return (CachedTransformerProxy)cls.getConstructor(IClassTransformer.class).newInstance(transformer);
    }
    
    @Override
    public String toString() {
        return "CachedTransformerProxy{" + transformerName + "}";
    }

    @Override
    public IClassTransformer getOriginal() {
        return original;
    }

}
