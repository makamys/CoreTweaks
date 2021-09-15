package makamys.coretweaks.optimization.transformercache.light;

import net.minecraft.launchwrapper.IClassTransformer;

public class CachedTransformer implements IClassTransformer {

    protected IClassTransformer original;
    
    public CachedTransformer(IClassTransformer original) {
        this.original = original;
    }
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        byte[] result = TransformerCache.instance.getCached(original, name, transformedName, basicClass);
        if(result == null) {
            TransformerCache.instance.prePutCached(original, name, transformedName, basicClass);
            result = original.transform(name, transformedName, basicClass);
            TransformerCache.instance.putCached(original, name, transformedName, result);
        }
        return result;
    }

}
