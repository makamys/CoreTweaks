package makamys.coretweaks.optimization.transformercache.light;

import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.IClassTransformer;

public class CachedNameTransformer extends CachedTransformer implements IClassTransformer, IClassNameTransformer {
    
    public CachedNameTransformer(IClassTransformer original) {
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

}
