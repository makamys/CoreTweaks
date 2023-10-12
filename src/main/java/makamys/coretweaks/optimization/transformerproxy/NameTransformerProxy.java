package makamys.coretweaks.optimization.transformerproxy;

import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.IClassTransformer;

public class NameTransformerProxy extends TransformerProxy implements IClassTransformer, IClassNameTransformer {
    public NameTransformerProxy(IClassTransformer original) {
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

    public static TransformerProxy of(IClassTransformer transformer) throws Exception {
        Class<?> cls = TransformerProxyGenerator.generate(NameTransformerProxy.class, transformer.getClass());
        return (TransformerProxy)cls.getConstructor(IClassTransformer.class).newInstance(transformer);
    }
}
