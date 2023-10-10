package makamys.coretweaks.optimization.transformercache.lite;

import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.IClassTransformer;

public class CachedNameTransformerProxy extends CachedTransformerProxy implements IClassTransformer, IClassNameTransformer {
    public CachedNameTransformerProxy(IClassTransformer original) {
        super(original);
    }

    @Override
    public String unmapClassName(String name) {
        return ((IClassNameTransformer)original).unmapClassName(name);
    }

    @Override
    public String remapClassName(String name) {
        return ((IClassNameTransformer)original).remapClassName(name);
    }

    public static CachedTransformerProxy of(IClassTransformer transformer) throws Exception {
        Class<?> cls = CachedTransformerProxyGenerator.generate(CachedNameTransformerProxy.class, transformer.getClass());
        return (CachedTransformerProxy)cls.getConstructor(IClassTransformer.class).newInstance(transformer);
    }
}
