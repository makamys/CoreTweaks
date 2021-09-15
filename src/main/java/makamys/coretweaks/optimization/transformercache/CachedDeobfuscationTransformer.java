package makamys.coretweaks.optimization.transformercache;

import cpw.mods.fml.common.asm.transformers.DeobfuscationTransformer;
import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.IClassTransformer;

public class CachedDeobfuscationTransformer implements IClassTransformer, IClassNameTransformer {

    private DeobfuscationTransformer original;
    
    public CachedDeobfuscationTransformer(DeobfuscationTransformer original) {
        this.original = original;
    }
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        return original.transform(name, transformedName, basicClass);
    }

    @Override
    public String unmapClassName(String name) {
        return original.unmapClassName(name);
    }

    @Override
    public String remapClassName(String name) {
        return original.remapClassName(name);
    }

}
