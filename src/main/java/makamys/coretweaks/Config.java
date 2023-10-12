package makamys.coretweaks;

import static makamys.coretweaks.util.AnnotationBasedConfigHelper.*;
import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cpw.mods.fml.common.versioning.ComparableVersion;
import makamys.coretweaks.util.AnnotationBasedConfigHelper;
import makamys.coretweaks.util.ConfigDumper;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;

public class Config {
    
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    
    @ConfigWrappedEnum(cat="Tweaks", def=TRUE, com="Lets you survive crashes without the game exiting, usually.\nCompatibility note: Not compatible BetterCrashes and CrashGuard, which do the same thing (this feature will be disabled).")
    public static FeatureSetting crashHandler;
    @ConfigWrappedEnum(cat="Tweaks", def=TRUE, com="Causes lighting updates around the block the player is looking at. A workaround for lighting errors that lets you fix them by staring at them. Useful in the Nether.")
    public static FeatureSetting lightFixStare;
    @ConfigWrappedEnum(cat="Tweaks", def=FALSE, com="EXPERIMENTAL: Uncaps framerate even when framelimiter is enabled. The framerate limit will only be used to decide how much time to spend updating chunks each frame. Vanilla Beta 1.7.3 behavior. It seems to make things worse though, at least with OptiFine.")
    public static FeatureSetting forceUncapFramerate;
    @ConfigWrappedEnum(cat="Tweaks.Mods", def=FALSE, com="Fixes OptiFine's implementation of updateRenderers returning the opposite value of what it should (probably a bug). Only effective when framerate limiter is enabled. Speeds up chunk updates significantly, and increases framerate when there aren't many chunk updates. However, during heavy chunk updating (e.g. when loading a world) it decreases the framerate as a side effect of not being as lazy.")
    public static FeatureSetting ofFixUpdateRenderersReturnValue;
    @ConfigWrappedEnum(cat="Tweaks.Mods", def=TRUE, com="Allows custom sky rendering in OptiFine D6 when using a render distance lower than 8.")
    public static FeatureSetting ofUnlockCustomSkyMinRenderDistance;
    @ConfigWrappedEnum(cat="Tweaks", def=TRUE, com="If enabled, the distance of the view fustrum's far plane will be clamped above `minFarPlaneDistance`. Setting it to 180 or higher fixes clipping in OptiFine's custom skybox that happens when using lower render distances.")
    public static FeatureSetting clampFarPlaneDistance;
    @ConfigFloat(cat="Tweaks.clampFarPlaneDistance", def=180f, min=0f, max=Float.MAX_VALUE, com="See `clampFarPlaneDistance`.")
    public static float minFarPlaneDistance;
    @ConfigWrappedEnum(cat="Tweaks", def=FALSE, com="Disables fog. Simple as.")
    public static FeatureSetting disableFog;
    @ConfigWrappedEnum(cat="Tweaks", def=FALSE, com="Uncap max length for world name and world seed in the world creation GUI. (By default, it's capped at 32.)")
    public static FeatureSetting uncapCreateWorldGuiTextFieldLength;
    @ConfigWrappedEnum(cat="Tweaks", def=FALSE, com="Add a button to the main menu that loads the last played world. (Button ID: -2026964516)")
    public static FeatureSetting mainMenuContinueButton;
    @ConfigWrappedEnum(cat="Tweaks", def=TRUE, com="In vanilla, the player can't sprint for longer than 30 seconds. This tweak increases this limit to be virtually infinite. Fixes MC-4839, fixed in 1.9")
    public static FeatureSetting extendSprintTimeLimit;
    
    @ConfigWrappedEnum(cat="Bugfixes", def=TRUE, com="Restore interdimensional travel sound (travel.ogg). Fixes MC-233, fixed in 1.9")
    public static FeatureSetting restoreTravelSound;
    @ConfigWrappedEnum(cat="Bugfixes", def=TRUE, com="Fixes bug in entity swimming code resulting in small entities (ones with hitboxes less than 0.8 units tall, such as DMod's foxes) being prone to drowning.")
    public static FeatureSetting fixSmallEntitySwim;
    @ConfigWrappedEnum(cat="Tweaks", def=TRUE, com="If enabled, the condition used to decide whether to render opaque or transparent clouds will be set based on the value of `cloudHeightCheckMode`.\n" +
            "Compatibility note: Not compatible with NotFine, which has a setting to control the same thing (this feature will be disabled).")
    public static FeatureSetting tweakCloudHeightCheck;
    @ConfigEnum(cat="Tweaks.tweakCloudHeightCheck", def="VARIABLE_CORRECTED", com="Lets you tweak the condition used to decide whether to render opaque or transparent clouds.\n" + 
            "* VARIABLE_CORRECTED: Keep vanilla behavior of rendering clouds as opaque when the player is below them and transparent otherwise, but with the turning point corrected to match the cloud height even when the world provider has a different cloud height than 128. Also provides a fix for OptiFine's bug where clouds disappear when the player is between Y=128 and the cloud height level when they are raised.\n" +
            "* ALWAYS_TRANSPARENT: Always render clouds as transparent (how it is in b1.7.3 and 1.15+)\n" + 
            "* ALWAYS_OPAQUE: Always render clouds as opaque")
    public static CloudHeightCheck cloudHeightCheckMode;
    @ConfigWrappedEnum(cat="Bugfixes", def=TRUE, com="Fixes graphical glitches that happen after recovering from a game crash, caused by world renderer display lists getting deleted but never reallocated. From 1.12.")
    public static FeatureSetting fixDisplayListDelete;
    @ConfigWrappedEnum(cat="Bugfixes", def=TRUE, com="Fixes heightmap calculation not including the top layer of 16x16x16 regions, causing lighting errors (MC-7508)")
    public static FeatureSetting fixHeightmapRange;
    @ConfigWrappedEnum(cat="Bugfixes", def=TRUE, com="Fixes an extra food item sometimes getting silently consumed (MC-849)")
    public static FeatureSetting fixDoubleEat;
    @ConfigWrappedEnum(cat="Bugfixes", def=TRUE, com="Fixes crash when certain invalid URLs appear in chat.\nCompatibility note: Not compatible with `fixUrlDetection` in Hodgepodge 1.6.14 and higher, which does the same (this feature will be disabled).")
    public static FeatureSetting fixForgeChatLinkCrash;
    @ConfigWrappedEnum(cat="Bugfixes", def=TRUE, com="Backports most of https://github.com/MinecraftForge/MinecraftForge/pull/5160, a patch that fixes minecarts duplicating and vanishing.")
    public static FeatureSetting fixEntityTracking;
    @ConfigWrappedEnum(cat="Bugfixes", def=TRUE, com="Fixes click sounds not playing in guis with scroll bars (e.g. the video settings screen.)")
    public static FeatureSetting guiClickSound;
            
    @ConfigWrappedEnum(cat="Optimizations", def=TRUE, com="Optimizes WorldServer#getPendingBlockUpdates. Speeds up chunk unloading.\nCompatibility note: Not compatible with OptiFine, which does the same thing (this feature will be disabled).")
    public static FeatureSetting optimizeGetPendingBlockUpdates;
    //@ConfigWrappedEnum(cat="Optimizations", def=FALSE, com="(WIP) Faster implementation of ChunkProviderClient#chunkMapping. From 1.16 (I don't know when exactly it was added). Might be a little buggy (it should only cause client-side errors though).")
    public static FeatureSetting clientChunkMap = FeatureSetting.FALSE;
    @ConfigWrappedEnum(cat="Optimizations", def=TRUE, com="Skip over known libraries during Forge mod discovery. From Forge 1.12 (added in 1.9)")
    public static FeatureSetting forgeModDiscovererSkipKnownLibraries;
    @ConfigWrappedEnum(cat="Optimizations", def=TRUE, com="Cache jar discoverer results (and fix a memory leak as a nice bonus).\nCompatibility note: Not compatible with `jarDiscovererMemoryLeakFix` in FoamFix, which is a subset of this fix (we will attempt to disable their feature - if this fails, this feature will be disabled).")
    public static FeatureSetting jarDiscovererCache;
    @ConfigInt(cat="Optimizations.jarDiscovererCache", def=8, min=-1, max=Integer.MAX_VALUE,
            com="Maximum age (in runs) of elements in cache. If an element hasn't been used for more than this many runs, it will be discarded. Set to -1 for no limit.")
    public static int jarDiscovererCacheMaxAge;
    @ConfigWrappedEnum(cat="Optimizations", def=TRUE, com="Don't update progress bar on steps. Only active if splash is disabled.")
    public static FeatureSetting forgeFastProgressBar;
    @ConfigWrappedEnum(cat="Optimizations", def=TRUE, com="Don't strip unusual characters from bar step messages. Only active if splash is disabled.")
    public static FeatureSetting forgeFastStepMessageStrip;
    @ConfigWrappedEnum(cat="Optimizations", def=TRUE, com="Reduces the unnecessary work FMLDeobfuscationRemapper does when we are in a deobfuscated (i.e. development) environment.")
    public static FeatureSetting forgeFastDeobfuscationRemapper;
    @ConfigWrappedEnum(cat="Optimizations.Mods", def=TRUE, com="Replaces the reflection OptiFine uses to access Forge methods in WorldRenderer#updateRenderer with direct calls to those methods. Small speedup during chunk updates.")
    public static FeatureSetting ofOptimizeWorldRenderer;
    @ConfigWrappedEnum(cat="Optimizations.Mods", def=TRUE, com="Removes the call to GL11#getInteger in FastCraft's texture upload handler during texture stitching and uses a cached value instead. Fixes the slowness of texture stitching that happens when OptiFine and FastCraft are both present, and mipmapping is enabled.")
    public static FeatureSetting fcOptimizeTextureUpload;
    @ConfigWrappedEnum(cat="Optimizations", def=TRUE, com="Sets TCP_NODELAY to true, reducing network latency in multiplayer. Works on server as well as client. From Minecraft 1.12 (added in 1.8.1).")
    public static FeatureSetting tcpNoDelay;
    @ConfigWrappedEnum(cat="Optimizations", def=TRUE, com="Exclude libraries from being transformed. Speeds up startup slightly. This will break any mods that try to transform these libraries, though that's fairly unlikely.")
    public static FeatureSetting excludeLibraryTransformation;
    @ConfigStringList(cat="Optimizations.excludeLibraryTransformation", def={
            "com.google.gson.", "joptsimple.", "io.netty.", "gnu.trove.", // vanilla+Forge
            "kotlin." // Forgelin
            }, com="Packages that should be added as class transformer exclusions.", resetOnLoad=true)
    public static StringList excludeLibraryTransformationPackages;
    @ConfigWrappedEnum(cat="Optimizations", def=TRUE, com="Makes Forge's wildcard class transformers (FluidIdTransformer, SideTransformer and TerminalTransformer) more efficient. Might break mods that interact with the transformer list.")
    public static FeatureSetting forgeFastWildcardTransformers;
            
    //@ConfigBoolean(cat="Diagnostics", def=TRUE, com="Enables the /coretweaks command, used to access various diagnostics. Invoke it in-game for additional information.")
    public static FeatureSetting coreTweaksCommand = FeatureSetting.FALSE;
    @ConfigWrappedEnum(cat="Diagnostics", def=TRUE, com="Makes MapStorage's errors more informative.")
    public static FeatureSetting enhanceMapStorageErrors;
            
    //@ConfigBoolean(cat="Optimizations", def=FALSE, com="Use multi-threaded texture loading when stitching textures? Placebo.")
    public static FeatureSetting threadedTextureLoader = FeatureSetting.FALSE;
    @ConfigWrappedEnum(cat="Optimizations", def=TRUE, com="Enable class transformer cache.")
    public static FeatureSetting transformerCache;
    @ConfigEnum(cat="Optimizations.transformerCache", def="LITE", com="The type of transformer caching to use.\n"
                    + "* LITE: Cache individual transformations of select transformers. Reduces startup time. Safe.\n"
                    + "* FULL: Cache the entire transformer chain. Reduces startup time further, but breaks with many mods.")
    public static TransformerCache transformerCacheMode;
    @ConfigWrappedEnum(cat="Optimizations", def=TRUE, com="Cache the file paths contained in folder resource packs. Eliminates the immense slowdown they add to the loading of large modpacks.")
    public static FeatureSetting fastFolderResourcePack;
    @ConfigWrappedEnum(cat="Optimizations", def=TRUE, com="Cache the folders contained in DefaultResourcePacks. Makes the biggest difference in dev environments and on slow file systems (looking at you Windows.)")
    public static FeatureSetting fastDefaultResourcePack;
            
    //@ConfigInt(cat="Optimizations", def=0, min=0, max=Integer.MAX_VALUE, com="How many threads to use for loading textures? (0: auto (all cores))")
    public static int threadedTextureLoaderThreadCount;

    @ConfigString(cat="Optimizations.transformerCache.full", def="org.spongepowered.asm.mixin.transformer.Proxy,appeng.transformer.asm.ApiRepairer,com.mumfrey.liteloader.transformers.ClassOverlayTransformer+",
            com="Comma-separated list of transformers for which the view of the transformer chain should be restored.\n" + 
            "\n" + 
            "The caching class transformer replaces the transformer chain with just itself. This creates conflicts with certain other transformers which also access the transformer chain, which can result in the game crashing.\n" +
            "To solve this, our transformer will restore the view of the transformer chain while these transformers are running.\n" + 
            "\n" + 
            "How to find bad transformers? If you see another transformer's name in your crash log, or see its name in one of the iterator stack traces printed in debug mode, adding it to this list may solve the problem.\n")
    public static String badTransformers;
    @ConfigString(cat="Optimizations.transformerCache.full", def="net.eq2online.macros.permissions.MacroModPermissions", 
            com="Sometimes caching classes can cause problems. Classes in this list will not be cached.\n")
    public static String badClasses;
    @ConfigString(cat="Optimizations.transformerCache.full", def="CMD files.jar", 
            com="Comma-separated list of mod files to ignore modifications of when deciding if a cache rebuild should be triggered.\n" +
            "If your cache keeps getting rebuilt even though you haven't changed any mods, look for deranged mod files and add them to this list.")
    public static String modFilesToIgnore;
    @ConfigInt(cat="Optimizations.transformerCache.full", def=512, min=-1, max=Integer.MAX_VALUE, 
            com="Cached class bytecode is removed from memory after being used, but the most recent N are kept around because the same class is often transformed more than once. This option sets the value of that N.\n" +
            "(Set to -1 to keep class bytecode in RAM forever)")
    public static int recentCacheSize;
    @ConfigInt(cat="Optimizations.transformerCache.full", def=1, min=0, max=2,
            com="* 0: Only print the essential messages.\n" +
            "* 1: Print when the cache gets saved.\n" +
            "* 2: Debug mode. Turn this on to log a bunch of stuff that can help find the cause of a crash.")
    public static int verbosity;
    
    @ConfigStringList(cat="Optimizations.transformerCache.lite", def={"cpw.mods.fml.common.asm.transformers.DeobfuscationTransformer", "codechicken.core.asm.MCPDeobfuscationTransformer", "net.minecraftforge.classloading.FluidIdTransformer", "cpw.mods.fml.common.asm.transformers.SideTransformer", "cpw.mods.fml.common.asm.transformers.TerminalTransformer"}, com="Canonical class names of the transformers that should be cached.")
    public static StringList transformersToCache;
    @ConfigInt(cat="Optimizations.transformerCache.lite", def=128, min=-1, max=Integer.MAX_VALUE,
            com="Maximum size (in MB) of cache. If the cache grows larger than this, the least recently used entries will be discarded. Set to -1 for no limit.")
    public static int liteTransformerCacheMaxSizeMB;
    @ConfigBoolean(cat="Optimizations.transformerCache.lite", def=false,
            com="Initialize later using a safer method. Might improve compatibility at the cost of not caching early transformations.")
    public static boolean lateLiteTransformerCache;
    @ConfigBoolean(cat="Optimizations.transformerCache.lite", def=true,
            com="Only store the difference made by a transformation rather than the entire result.\nGreatly reduces cache size (by ~40-70%) but makes transformation slightly (~25%) slower in the cached case and ~50-100% slower when the cache is first created.\nDisable this if you want to maximize speed at the cost of increased disk and memory usage.")
    public static boolean useDiffsInTransformerCache;
    
    public static enum CloudHeightCheck {
        VARIABLE_CORRECTED,
        ALWAYS_TRANSPARENT,
        ALWAYS_OPAQUE
    }
    
    public static enum TransformerCache {
        LITE,
        FULL
    }
    
    @WrappedEnum(enumClass = FeatureSetting.Setting.class, categoryProperty="_enabled")
    public static class FeatureSetting implements IWrappedEnum {
        
        public static enum Setting {
            FALSE, TRUE, FORCE;
        }
        
        private static FeatureSetting FALSE = new FeatureSetting(Setting.FALSE);
        
        private Setting setting;
        
        private boolean disabled;
        
        public FeatureSetting(Setting setting) {
            this.setting = setting;
        }
        
        public FeatureSetting() {
            
        }
        
        public void disable() {
            disabled = true;
        }
        
        public boolean isActive() {
            return setting != Setting.FALSE && (setting == Setting.FORCE || (setting == Setting.TRUE && !disabled));
        }
        
        @Override
        public void setValue(Object newValue, Field field, Configuration config) {
            setting = (Setting)newValue;
            if(!disabled) {
                ConfigWrappedEnum ann = (ConfigWrappedEnum)field.getAnnotation(ConfigWrappedEnum.class);
                if(!config.getBoolean("enable" + capitalize(ann.cat().toLowerCase().split("\\.")[0]), "_categories", true, "Set this to false to disable all features in the '" + ann.cat().toLowerCase() + "' category.") || (setting == Setting.TRUE && Config.shouldDisable(this))) {
                    disable();
                }
            }
        }
        
        @Override
        public Object getValue() {
            return setting;
        }
        
        public void setValue(Setting newValue) {
            setting = newValue;
        }
        
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof FeatureSetting) {
                FeatureSetting o = (FeatureSetting)obj;
                return setting == o.setting;
            }
            return super.equals(obj);
        }
    }
    
    public static class StringList {
        private String[] listRaw;
        private String[] list;
        
        public StringList(String[] list) {
            set(list);
        }
        
        public String[] getRaw() {
            return listRaw;
        }
        public String[] get() {
            return list;
        }
        public void set(String[] list) {
            listRaw = list;
            this.list = Arrays.copyOfRange(list, list.length > 0 && list[0].equals(":resetOnLoad") ? 1 : 0, list.length);
        }
    }
    
    private static AnnotationBasedConfigHelper configHelper = new AnnotationBasedConfigHelper(Config.class, LOGGER);
    private static boolean firstLoad = true;
    
    public static void reload() {
        File configFile = new File(Launch.minecraftHome, "config/coretweaks.cfg");
        boolean configFileExisted = configFile.exists();
        Configuration config = new Configuration(configFile, CoreTweaks.VERSION);
        
        config.load();
        configHelper.loadFields(config);
        
        config.setCategoryComment("_categories", 
                "In this config file, every feature toggle has the name of '_enabled'.\n" +
                "A feature toggle can be set to the following values. If a different value is provided, the setting will be reverted to the default setting on next start.\n" +
                "* false: Disable the feature\n" +
                "* true: Enable the feature if it doesn't cause an incompatibility\n" +
                "* force: Enable feature unconditionally (for special use cases)\n" +
                "\n" +
                "For convenience, the '_categories' category contains toggles that can be used to disable all features in a category.");
        
        config.setCategoryComment("Optimizations.transformer_cache.full", 
                "Options for the full caching class transformer. (only appliable if it's enabled)");
        
        config.setCategoryComment("Optimizations.transformer_cache.lite", 
                "Options for the lite caching class transformer. (only appliable if it's enabled)");
        
        String loadedVersion = config.getLoadedConfigVersion();
        if(firstLoad && configFileExisted) {
            ConfigMigrator migrator = new ConfigMigrator(config);
            if((loadedVersion == null || (!loadedVersion.startsWith("@") && new ComparableVersion(config.getLoadedConfigVersion()).compareTo(new ComparableVersion("0.3")) < 0))) {
                migrator.migrate_0_2_to_0_3();
                configHelper.saveFields(config);
            }
            if(loadedVersion != null && (!loadedVersion.startsWith("@") && new ComparableVersion(config.getLoadedConfigVersion()).compareTo(new ComparableVersion("0.3.1")) < 0)) {
                migrator.migrate_0_3_0_to_0_3_1(new ComparableVersion(config.getDefinedConfigVersion()).compareTo(new ComparableVersion("0.3.1")) >= 0);
                configHelper.saveFields(config);
            }
            migrator.writeWarnings();
        }
        
        sortCategories(config);
        
        ConfigDumper.dumpConfigIfEnabled(config, CoreTweaks.MODID);
        
        if(config.hasChanged() || !config.getDefinedConfigVersion().equals(config.getLoadedConfigVersion())) {
            config.save();
        }
        
        firstLoad = false;
    }
    
    /** Forge keeps categories in the order they were added. I want them to be sorted alphabetically,
     * with the extra condition that the string 'mods' should come last. */ 
    private static void sortCategories(Configuration config) {
        Field childrenF = null;
        try {
            childrenF = ConfigCategory.class.getDeclaredField("children");
            childrenF.setAccessible(true);
            
            for(String category : config.getCategoryNames()) {
                ConfigCategory cat = config.getCategory(category);
                
                @SuppressWarnings("unchecked")
                List<ConfigCategory> children = (List<ConfigCategory>) childrenF.get(cat);
                
                Collections.sort(children, (catA, catB) -> {
                    String nameA = catA.getName();
                    String nameB = catB.getName();
                    if(nameA.equals("mods") && !nameB.equals("mods")) {
                        return 1;
                    } else if(!nameA.equals("mods") && nameB.equals("mods")) {
                        return -1;
                    } else if(nameA.equals("mods") && nameB.equals("mods")) {
                        return 0;
                    } else {
                        return nameA.compareTo(nameB);
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.info("Failed to sort config categories");
        }
    }
    
    private static boolean shouldDisable(FeatureSetting feature) {
        if(feature == crashHandler) {
            if(Compat.isBetterCrashesPresent()) {
                LOGGER.info("Disabling crash handler because BetterCrashes is present.");
                return true;
            } else if(Compat.isCrashGuardPresent()) {
                LOGGER.info("Disabling crash handler because CrashGuard is present.");
                return true;
            }
        } else if(feature == fixForgeChatLinkCrash) {
            if(Compat.isHodgepodgeChatLinkCrashFixEnabled()) {
                LOGGER.info("Disabling forge chat link crash fix because Hodgepodge has the same fix enabled.");
                return true;
            }
        } else if(feature == fixEntityTracking) {
            if(Compat.isBackport5160Present()) {
                LOGGER.info("Disabling backport of Forge PR #5160 because backport5160 is present.");
                return true;
            }
        } else if(feature == optimizeGetPendingBlockUpdates) {
            if(Compat.isOptifinePresent()) {
                LOGGER.info("Disabling getPendingBlockUpdates optimization because OptiFine is present.");
                return true;
            }
        } else if(feature == tweakCloudHeightCheck) {
            if(Compat.isNotFinePresent()) {
                LOGGER.info("Disabling cloud height check fix because NotFine is present.");
                return true;
            }
        }
        return false;
    }
    
    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
    
}
