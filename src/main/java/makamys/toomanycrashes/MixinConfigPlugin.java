package makamys.toomanycrashes;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.minecraft.launchwrapper.Launch;

public class MixinConfigPlugin implements IMixinConfigPlugin {
    
    @Override
    public void onLoad(String mixinPackage) {
        //JarDiscovererCache.load();
        //Launch.classLoader.registerTransformer(ASMModParserTransformer.class.getName());
    }

    @Override
    public String getRefMapperConfig() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        Config.reload();
        
        if(Arrays.asList(
                "makamys.toomanycrashes.mixin.MixinChunkProviderClient"
                ).contains(mixinClassName)){
            return Config.clientChunkMap;
        } else if(Arrays.asList(
                "makamys.toomanycrashes.mixin.MixinMinecraft"
                ).contains(mixinClassName)){
            return Config.crashHandler;
        } else if(Arrays.asList(
                "makamys.toomanycrashes.mixin.MixinWorldServer"
                ).contains(mixinClassName)){
            return Config.getPendingBlockUpdates;
        } else if(Arrays.asList(
                "makamys.toomanycrashes.mixin.MixinNetHandlerPlayClient"
                ).contains(mixinClassName)){
            return Config.restoreTravelSound;
        } else if(Arrays.asList(
                "makamys.toomanycrashes.mixin.MixinModDiscoverer"
                ).contains(mixinClassName)){
            return Config.modDiscovererSkipKnownLibraries;
        } else if(Arrays.asList(
                "makamys.toomanycrashes.mixin.MixinJarDiscoverer"
                ).contains(mixinClassName)){
            return Config.jarDiscovererCache;
        } else if(Arrays.asList(
                "makamys.toomanycrashes.mixin.MixinProgressBar"
                ).contains(mixinClassName)){
            return Config.fastProgressBar;
        } else {
            return true;
        }
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<String> getMixins() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // TODO Auto-generated method stub
        
    }

}
