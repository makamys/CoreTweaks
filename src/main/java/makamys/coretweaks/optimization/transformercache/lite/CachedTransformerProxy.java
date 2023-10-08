package makamys.coretweaks.optimization.transformercache.lite;

import makamys.coretweaks.optimization.NonFunctionAlteringWrapper;
import net.minecraft.launchwrapper.IClassTransformer;

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
        return result;
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
