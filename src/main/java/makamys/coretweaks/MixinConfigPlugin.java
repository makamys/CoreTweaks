package makamys.coretweaks;

import static makamys.coretweaks.CoreTweaks.LOGGER;

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

import com.esotericsoftware.kryo.kryo5.util.Util;

import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.relauncher.ReflectionHelper;
import makamys.coretweaks.optimization.transformercache.lite.TransformerCache;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

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
                if(Config.forgeFastStepMessageStrip.isActive()) mixins.add("optimization.fmlmessagestrip.MixinFMLClientHandler");
            }
        } else if(phase == Phase.INIT) {
            
        } else if(phase == Phase.DEFAULT) {
            if(Config.transformerCache.isActive() && Config.transformerCacheMode == Config.TransformerCache.LITE && Config.lateLiteTransformerCache) {
                // At this point the transformer chain is complete, so we can go hook it.
                TransformerCache.instance.init(true);
            }
            if(Config.jarDiscovererCache.isActive()) {
                // We are now at the end of Launch#launch. FoamFix's transformer has finished constructing, so we can hack it.
                if(disableFoamFixJarDiscovererTransformer()) {
                    mixins.add("optimization.jardiscoverercache.MixinJarDiscoverer");
                }
            }
            
            if(Config.clientChunkMap.isActive()) mixins.add("optimization.clientchunkmap.MixinChunkProviderClient");
            if(Config.crashHandler.isActive()) mixins.add("tweak.crashhandler.MixinMinecraft");
            if(Config.lightFixStare.isActive()) mixins.add("tweak.lightfixstare.MixinWorld");
            if(Config.fixDisplayListDelete.isActive()) mixins.add("bugfix.displaylistdelete.MixinRenderGlobal");
            if(Config.fixHeightmapRange.isActive()) mixins.add("bugfix.heightmaprange.MixinChunk");
            if(Config.fixSmallEntitySwim.isActive()) mixins.add("bugfix.smallentityswim.MixinEntity");
            if(Config.fixForgeChatLinkCrash.isActive()) mixins.add("bugfix.chatlinkcrash.MixinForgeHooks");
            if(Config.clampFarPlaneDistance.isActive()) mixins.add("tweak.farplane.MixinEntityRenderer");
            if(Config.disableFog.isActive()) mixins.add("tweak.disablefog.MixinEntityRenderer");
            if(Config.uncapCreateWorldGuiTextFieldLength.isActive()) mixins.add("tweak.newworldguimaxlength.MixinGuiCreateWorld");
            if(Config.extendSprintTimeLimit.isActive()) mixins.add("tweak.extendsprint.MixinEntityPlayerSP");
            if(Config.fixEntityTracking.isActive()) {
                mixins.add("bugfix.forge5160.MixinChunk");
                mixins.add("bugfix.forge5160.MixinEntity");
                mixins.add("bugfix.forge5160.MixinWorld");
            }
            if(Config.guiClickSound.isActive()) {
                mixins.add("bugfix.guiclicksound.MixinGuiListExtended");
            }
            if(Config.fixIntelRendering.isActive()) {
                mixins.add(!Config.useAlternateIntelRenderingFix ? "bugfix.intelcolor.MixinTessellator" : "bugfix.intelcolor.MixinOpenGlHelper");
            }
            if(Config.useSpawnTypeForMobCap.isActive()) {
                mixins.add("bugfix.mobcap.MixinEntity");
            }
            
            if(Config.forceUncapFramerate.isActive()) mixins.add("tweak.synctweak.MixinMinecraft");
            if(Config.optimizeGetPendingBlockUpdates.isActive()) mixins.add("optimization.getpendingblockupdates.MixinWorldServer");
            if(Config.restoreTravelSound.isActive()) mixins.add("bugfix.restoretravelsound.MixinNetHandlerPlayClient");
            if(Config.tweakCloudHeightCheck.isActive()) mixins.add("tweak.cloudheightcheck.MixinEntityRenderer");
            
            if(Compat.isOptifinePresent()) {
                if(Config.ofFixUpdateRenderersReturnValue.isActive()) mixins.add("tweak.ofupdaterenderersreturn.MixinRenderGlobal");
                if(Config.ofOptimizeWorldRenderer.isActive()) mixins.add("optimization.ofupdaterendererreflect.MixinWorldRenderer");
                if(Config.ofUnlockCustomSkyMinRenderDistance.isActive()) mixins.add("tweak.ofcustomsky.MixinOFD6CustomSky");
            }
            
            if(Config.fcOptimizeTextureUpload.isActive()) {
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
            if(Config.threadedTextureLoader.isActive()) {
                mixins.add("optimization.threadedtextureloader.ITextureMap");
                mixins.add("optimization.threadedtextureloader.MixinTextureMap");
            }
            if(Config.fastFolderResourcePack.isActive()) {
                mixins.add("optimization.folderresourcepack.MixinFolderResourcePack");
            }
            if(Config.fastDefaultResourcePack.isActive()) {
                mixins.add("optimization.defaultresourcepack.MixinDefaultResourcePack");
            }
            if(Config.tcpNoDelay.isActive()) {
                mixins.add("optimization.tcpnodelay.MixinChannelInitializers");
            }
            
            if(Config.enhanceMapStorageErrors.isActive()) {
                mixins.add("diagnostics.enhancemapstorageerrors.MixinMapStorage");
            }
            if(Config.detectDataWatcherIdConflicts.isActive()) {
                mixins.add("diagnostics.detectdatawatcherconflict.MixinDataWatcher");
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
                LOGGER.info("Found BugfixModClassTransformer, applying compatibility hack to forcibly disable FoamFix's jarDiscovererMemoryLeakFix");
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
