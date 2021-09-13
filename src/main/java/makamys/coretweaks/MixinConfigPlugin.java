package makamys.coretweaks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.MixinEnvironment.Phase;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

public class MixinConfigPlugin implements IMixinConfigPlugin {
    
    @Override
    public void onLoad(String mixinPackage) {
        Config.reload();
        //JarDiscovererCache.load();
        //Launch.classLoader.registerTransformer(ASMModParserTransformer.class.getName());
        if(Config.fcOptimizeTextureUpload && MixinEnvironment.getCurrentEnvironment() == MixinEnvironment.getDefaultEnvironment()) {
            Set<String> transformerExceptions = (Set<String>)ObfuscationReflectionHelper.getPrivateValue(LaunchClassLoader.class, Launch.classLoader, "transformerExceptions");
            transformerExceptions.remove("fastcraft");
        }
    }

    @Override
    public String getRefMapperConfig() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<String> getMixins() {
        List<String> mixins = new ArrayList<>();
        
        Phase phase = MixinEnvironment.getCurrentEnvironment().getPhase();
        if(phase == Phase.INIT) {
            if(Config.modDiscovererSkipKnownLibraries) mixins.add("MixinModDiscoverer");
            if(Config.jarDiscovererCache) mixins.add("MixinJarDiscoverer");
            if(Config.fastProgressBar) mixins.add("MixinProgressBar");
            if(Config.fastStepMessageStrip) mixins.add("MixinFMLClientHandler");
        } else if(phase == Phase.DEFAULT) {
            if(Config.clientChunkMap) mixins.add("MixinChunkProviderClient");
            if(Config.crashHandler) mixins.add("MixinMinecraft_CrashHandler");
            if(Config.forceUncapFramerate) mixins.add("MixinMinecraft_SyncTweak");
            if(Config.ofFixUpdateRenderersReturnValue) mixins.add("MixinRenderGlobal");
            if(Config.ofOptimizeWorldRenderer) mixins.add("MixinWorldRenderer");
            if(Config.getPendingBlockUpdates) mixins.add("MixinWorldServer");
            if(Config.restoreTravelSound) mixins.add("MixinNetHandlerPlayClient");
            if(Config.cloudHeightCheck != Config.CloudHeightCheck.UNCHANGED) mixins.add("MixinEntityRenderer_Clouds");
            if(Config.frameProfilerHooks) mixins.addAll(Arrays.asList("MixinEntityRenderer_FrameProfiler",
                                                                        "MixinMinecraft_FrameProfiler"));
            if(Config.fixSmallEntitySwim) mixins.add("MixinEntity");
            if(Config.fcOptimizeTextureUpload) {
                mixins.add("optimization.fastcraft_texture_load.MixinFastcraftTextureUtil");
                mixins.add("optimization.fastcraft_texture_load.MixinTextureUtil");
                mixins.add("optimization.fastcraft_texture_load.MixinTextureMap");
            }
        }
        return mixins;
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
