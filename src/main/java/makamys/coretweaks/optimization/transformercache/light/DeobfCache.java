package makamys.coretweaks.optimization.transformercache.light;

import java.util.List;

import cpw.mods.fml.common.asm.transformers.DeobfuscationTransformer;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

public class DeobfCache {
    
    public static void init() {
        LaunchClassLoader lcl = (LaunchClassLoader)Launch.classLoader;
        List<IClassTransformer> transformers = (List<IClassTransformer>)ReflectionHelper.getPrivateValue(LaunchClassLoader.class, lcl, "transformers");
        for(int i = 0; i < transformers.size(); i++) {
            if(transformers.get(i) instanceof DeobfuscationTransformer) {
                System.out.println("Found deobfuscation transformer, hooking it with cached one");
                transformers.set(i, new CachedDeobfuscationTransformer((DeobfuscationTransformer)transformers.get(i)));
            }
        }
    }
    
}
