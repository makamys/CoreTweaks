package makamys.coretweaks.optimization.transformercache.light;

import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.IClassTransformer;

public class CachedNameTransformer implements IClassTransformer, IClassNameTransformer {

    private IClassTransformer original;
    
    public CachedNameTransformer(IClassTransformer original) {
        this.original = original;
    }
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        byte[] result = TransformerCache.getCached(this, name, transformedName, basicClass);
        if(result == null) {
            TransformerCache.prePutCached(this, name, transformedName, basicClass);
            result = original.transform(name, transformedName, basicClass);
            TransformerCache.putCached(this, name, transformedName, result);
        }
        return result;
    }

    @Override
    public String unmapClassName(String name) {
        return ((IClassNameTransformer)original).unmapClassName(name);
    }

    @Override
    public String remapClassName(String name) {
        return ((IClassNameTransformer)original).remapClassName(name);
    }

}
