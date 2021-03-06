package makamys.coretweaks;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.MixinEnvironment.Phase;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.relauncher.ReflectionHelper;
import makamys.coretweaks.optimization.transformercache.lite.TransformerCache;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import static makamys.coretweaks.CoreTweaks.LOGGER;

public class MixinConfigPlugin implements IMixinConfigPlugin {
    
    @Override
    public void onLoad(String mixinPackage) {
        Config.reload();
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        List<String> mixins = new ArrayList<>();
        
        Phase phase = MixinEnvironment.getCurrentEnvironment().getPhase();
        if(phase == Phase.PREINIT) {
            if(!isForgeSplashEnabled()) {
                if(Config.forgeFastStepMessageStrip) mixins.add("optimization.fmlmessagestrip.MixinFMLClientHandler");
            }
        } else if(phase == Phase.INIT) {
            
        } else if(phase == Phase.DEFAULT) {
            if(Config.transformerCache == Config.TransformerCache.LITE) {
                // At this point the transformer chain is complete, so we can go hook it.
                TransformerCache.instance.init();
            }
            
            if(Config.jarDiscovererCache) {
                // We are now at the end of Launch#launch. FoamFix's transformer has finished constructing, so we can hack it.
                if(disableFoamFixJarDiscovererTransformer()) {
                    mixins.add("optimization.jardiscoverercache.MixinJarDiscoverer");
                }
            }
            
            if(Config.clientChunkMap) mixins.add("optimization.clientchunkmap.MixinChunkProviderClient");
            if(Config.crashHandler) mixins.add("tweak.crashhandler.MixinMinecraft");
            if(Config.lightFixStare) mixins.add("tweak.lightfixstare.MixinWorld");
            if(Config.fixDisplayListDelete) mixins.add("bugfix.displaylistdelete.MixinRenderGlobal");
            if(Config.fixHeightmapRange) mixins.add("bugfix.heightmaprange.MixinChunk");
            if(Config.fixSmallEntitySwim) mixins.add("bugfix.smallentityswim.MixinEntity");
            if(Config.fixForgeChatLinkCrash) mixins.add("bugfix.chatlinkcrash.MixinForgeHooks");
            if(Config.minFarPlaneDistance >= 0f) mixins.add("tweak.farplane.MixinEntityRenderer");
            if(Config.ofUnlockCustomSkyMinRenderDistance) mixins.add("tweak.ofcustomsky.MixinOFD6CustomSky");
            if(Config.disableFog) mixins.add("tweak.disablefog.MixinEntityRenderer");
            if(Config.uncapCreateWorldGuiTextFieldLength) mixins.add("tweak.newworldguimaxlength.MixinGuiCreateWorld");
            if(Config.wireframe) mixins.add("diagnostics.wireframe.MixinEntityRenderer");
            
            if(Config.forceUncapFramerate) mixins.add("tweak.synctweak.MixinMinecraft");
            if(Config.ofFixUpdateRenderersReturnValue) mixins.add("tweak.ofupdaterenderersreturn.MixinRenderGlobal");
            if(Config.ofOptimizeWorldRenderer) mixins.add("optimization.ofupdaterendererreflect.MixinWorldRenderer");
            if(Config.getPendingBlockUpdates) mixins.add("optimization.getpendingblockupdates.MixinWorldServer");
            if(Config.restoreTravelSound) mixins.add("bugfix.restoretravelsound.MixinNetHandlerPlayClient");
            if(Config.cloudHeightCheck != Config.CloudHeightCheck.UNCHANGED) mixins.add("tweak.cloudheightcheck.MixinEntityRenderer");
            if(Config.frameProfilerHooks) mixins.addAll(Arrays.asList("diagnostics.frameprofiler.MixinEntityRenderer",
                                                                        "diagnostics.frameprofiler.MixinMinecraft"));
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
                        // Allow transforming FastCraft
                        Set<String> transformerExceptions = ObfuscationReflectionHelper.getPrivateValue(LaunchClassLoader.class, Launch.classLoader, "transformerExceptions");
                        transformerExceptions.remove("fastcraft");
                        
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static boolean disableFoamFixJarDiscovererTransformer() {
        try {
            Class bugfixModClassTransformerClass = null;
            try {
                bugfixModClassTransformerClass = Class.forName("pl.asie.foamfix.bugfixmod.coremod.BugfixModClassTransformer");
                LOGGER.info("Found BugfixModClassTransformer, applying compatibility hack for FoamFix's jarDiscovererMemoryLeakFix");
                Object instance = bugfixModClassTransformerClass.getField("instance").get(null);
                Map<String, ArrayList> patchers = (Map<String, ArrayList>) ReflectionHelper.getPrivateValue(bugfixModClassTransformerClass, instance, "patchers");
                for(Iterator<Entry<String, ArrayList>> it = patchers.entrySet().iterator(); it.hasNext(); ) {
                    Entry<String, ArrayList> e = it.next();
                    boolean removed = false;
                    
                    for(Iterator<Object> itPatchers = e.getValue().iterator(); itPatchers.hasNext(); ) {
                        Object patcher = itPatchers.next();
                        if(patcher.getClass().getSimpleName().equals("JarDiscovererMemoryLeakFixPatcher")) {
                            LOGGER.trace("Removing patcher " + patcher.getClass().getName() + " for class " + e.getKey());
                            itPatchers.remove();
                            removed = true;
                        }
                    }
                    
                    if(removed && e.getValue().isEmpty()) {
                        LOGGER.trace("Removing patcher list for class " + e.getKey() + " since we emptied it.");
                        it.remove();
                    }
                    
                }
            } catch(ClassNotFoundException e) {
                LOGGER.trace("Couldn't find BugfixModClassTransformer. This is not an error unless FoamFix is actually present.");
            }
        } catch(Exception e) {
            LOGGER.error("Failed to apply compatibility hack for FoamFix's jarDiscovererMemoryLeakFix. CoreTweaks's jar discoverer cache will be disabled. Please disable FoamFix's jarDiscovererMemoryLeakFix to fix the incompatibility.");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean isForgeSplashEnabled() {
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

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

}
