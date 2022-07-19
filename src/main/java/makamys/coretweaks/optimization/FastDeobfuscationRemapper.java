package makamys.coretweaks.optimization;

import java.io.IOException;
import java.util.Map;

import com.google.common.collect.Maps;

import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import makamys.coretweaks.Config;
import makamys.coretweaks.util.ProxyMap;
import net.minecraft.launchwrapper.Launch;

public class FastDeobfuscationRemapper {
    
    private static void hook(FMLDeobfuscatingRemapper remapper) {
        ObfuscationReflectionHelper.setPrivateValue(FMLDeobfuscatingRemapper.class, remapper, new ProxyMap<String,Map<String,String>>(Maps.newHashMap()) {
            
            private Map<String, String> emptyMap = Maps.newHashMap();
            
            @Override
            public boolean containsKey(Object key) {
                return true;
            }
            
            @Override
            public Map<String, String> get(Object key) {
                return emptyMap;
            }
            
        }, "fieldDescriptions");
    }

    public static boolean isActive() {
        return Config.forgeFastDeobfuscationRemapper && isDeobfuscated();
    }

    public static void init() {
        hook(FMLDeobfuscatingRemapper.INSTANCE);
    }
    
    private static boolean isDeobfuscated() {
        try {
            return Launch.classLoader.getClassBytes("net.minecraft.world.World") != null;
        } catch(IOException e) {
            return false;
        }
    }
    
}
