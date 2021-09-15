package makamys.coretweaks.optimization.transformercache.light;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cpw.mods.fml.common.asm.transformers.DeobfuscationTransformer;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

public class TransformerCache {
    
    public static TransformerCache instance;
    
    public static void init() {
        instance = new TransformerCache();
    }
    
    public TransformerCache() {
        hookClassLoader();
    }

    private void hookClassLoader() {
        LaunchClassLoader lcl = (LaunchClassLoader)Launch.classLoader;
        List<IClassTransformer> transformers = (List<IClassTransformer>)ReflectionHelper.getPrivateValue(LaunchClassLoader.class, lcl, "transformers");
        for(int i = 0; i < transformers.size(); i++) {
            if(transformers.get(i) instanceof DeobfuscationTransformer) {
                System.out.println("Found deobfuscation transformer, hooking it with cached one");
                transformers.set(i, new CachedNameTransformer((DeobfuscationTransformer)transformers.get(i)));
            }
        }
    }

    public byte[] getCached(IClassTransformer transformer, String name, String transformedName, byte[] basicClass) {
        // TODO Auto-generated method stub
        return null;
    }

    public void prePutCached(IClassTransformer transformer, String name, String transformedName, byte[] basicClass) {
        // TODO Auto-generated method stub
        
    }

    public void putCached(IClassTransformer transformer, String name, String transformedName, byte[] result) {
        // TODO Auto-generated method stub
        
    }
}
