package makamys.coretweaks;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.MixinEnvironment.Phase;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import makamys.coretweaks.optimization.transformercache.lite.TransformerCache;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import static makamys.coretweaks.CoreTweaks.LOGGER;

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
        if(phase == Phase.PREINIT) {
            if(Config.jarDiscovererCache) mixins.add("MixinJarDiscoverer");
            if(!isForgeSplashEnabled()) {
                if(Config.forgeFastProgressBar) mixins.add("MixinProgressBar");
                if(Config.forgeFastStepMessageStrip) mixins.add("MixinFMLClientHandler");
            }
        } else if(phase == Phase.INIT) {
            
        } else if(phase == Phase.DEFAULT) {
            if(Config.transformerCache == Config.TransformerCache.LITE) {
                // At this point the transformer chain is complete, so we can go hook it.
                TransformerCache.instance.init();
            }
            
            if(Config.clientChunkMap) mixins.add("MixinChunkProviderClient");
            if(Config.crashHandler) mixins.add("MixinMinecraft_CrashHandler");
            if(Config.lightFixStare) mixins.add("tweak.lightfixstare.MixinWorld");
            if(Config.fixDisplayListDelete) mixins.add("bugfix.displaylistdelete.MixinRenderGlobal");
            if(Config.fixHeightmapRange) mixins.add("bugfix.heightmaprange.MixinChunk");
            if(Config.fixSmallEntitySwim) mixins.add("MixinEntity");
            if(Config.minFarPlaneDistance >= 0f) mixins.add("tweak.farplane.MixinEntityRenderer");
            if(Config.ofUnlockCustomSkyMinRenderDistance) mixins.add("tweak.ofcustomsky.MixinOFD6CustomSky");
            
            if(Config.forceUncapFramerate) mixins.add("MixinMinecraft_SyncTweak");
            if(Config.ofFixUpdateRenderersReturnValue) mixins.add("MixinRenderGlobal");
            if(Config.ofOptimizeWorldRenderer) mixins.add("MixinWorldRenderer");
            if(Config.getPendingBlockUpdates) mixins.add("MixinWorldServer");
            if(Config.restoreTravelSound) mixins.add("MixinNetHandlerPlayClient");
            if(Config.cloudHeightCheck != Config.CloudHeightCheck.UNCHANGED) mixins.add("MixinEntityRenderer_Clouds");
            if(Config.frameProfilerHooks) mixins.addAll(Arrays.asList("MixinEntityRenderer_FrameProfiler",
                                                                        "MixinMinecraft_FrameProfiler"));
            if(Config.fcOptimizeTextureUpload) {
                String fcVersion = (String)Launch.blackboard.get("fcVersion");
                if(fcVersion != null) {
                    boolean ok = true;
                    switch(fcVersion) {
                    case "1.23":
                        mixins.add("optimization.fastcrafttextureload.MixinFastcraft1_23TextureUtil");
                        break;
                    case "1.25":
                        mixins.add("optimization.fastcrafttextureload.MixinFastcraft1_25TextureUtil");
                        break;
                    default:
                        LOGGER.warn("Unsupported FastCraft version: " + fcVersion + ". fcOptimizeTextureUpload won't work.");
                        ok = false;
                        break;
                    }
                    
                    if(ok) {
                        mixins.add("optimization.fastcrafttextureload.MixinTextureUtil");
                        mixins.add("optimization.fastcrafttextureload.MixinTextureMap");
                    }
                }
            }
            if(Config.threadedTextureLoader) {
                mixins.add("optimization.threadedtextureloader.ITextureMap");
                mixins.add("optimization.threadedtextureloader.MixinTextureMap");
            }
            if(Config.fastFolderTexturePack) {
                mixins.add("optimization.foldertexturepack.MixinFolderResourcePack");
                mixins.add("optimization.foldertexturepack.MixinDefaultResourcePack");
            }
            
        }
        return mixins;
    }

    private static boolean isForgeSplashEnabled() {
        boolean enabled = true;
        File configFile = new File(Launch.minecraftHome, "config/splash.properties");
        if(configFile.exists()) {
            Properties props = new Properties();
            try {
                props.load(new FileReader(configFile));
                enabled = Boolean.parseBoolean((String)props.getOrDefault("enabled", "true"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        return enabled;
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
