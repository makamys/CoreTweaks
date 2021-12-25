package makamys.coretweaks;

import java.io.File;
import java.util.Map;

import org.apache.commons.lang3.EnumUtils;

import makamys.coretweaks.util.ConfigDumper;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.config.Configuration;

public class Config {
    
    public static boolean crashHandler;
    public static boolean lightFixStare;
    public static boolean forceUncapFramerate;
    public static boolean ofFixUpdateRenderersReturnValue;
    public static boolean ofUnlockCustomSkyMinRenderDistance;
    public static boolean ofOptimizeWorldRenderer;
    public static boolean fcOptimizeTextureUpload;
    public static boolean getPendingBlockUpdates;
    public static boolean clientChunkMap;
    public static boolean restoreTravelSound;
    public static boolean fixSmallEntitySwim;
    public static boolean forgeModDiscovererSkipKnownLibraries;
    public static boolean jarDiscovererCache;
    public static boolean forgeFastProgressBar;
    public static boolean forgeFastStepMessageStrip;
    public static boolean frameProfilerHooks;
    public static CloudHeightCheck cloudHeightCheck;
    public static boolean fixDisplayListDelete;
    public static boolean fixHeightmapRange;
    public static boolean fixDoubleEat;
    public static boolean forgeFastDeobfuscationRemapper;
    public static float minFarPlaneDistance;
    
    public static boolean coreTweaksCommand;
    public static String profilerMethods;
    
    public static boolean threadedTextureLoader;
    public static TransformerCache transformerCache;
    public static boolean fastFolderTexturePack;

    public static int threadedTextureLoaderThreadCount;

    public static String badTransformers;
    public static String badClasses;
    public static String modFilesToIgnore;
    public static int recentCacheSize;
    public static int verbosity;
    public static boolean forgeBarProfiler;
    public static boolean crasher;
    public static boolean serverRunTimePrinter;
    
    public static String[] transformersToCache;
    
    public static enum CloudHeightCheck {
    	UNCHANGED,
    	VARIABLE_CORRECTED,
    	ALWAYS_TRANSPARENT,
    	ALWAYS_OPAQUE
    }
    
    public static enum TransformerCache {
        NONE,
        LITE,
        FULL
    }
    
    public static void reload() {
        Configuration config = new Configuration(new File(Launch.minecraftHome, "config/coretweaks.cfg"));
        
        config.load();
        crashHandler = config.getBoolean("crashHandler", "Tweaks", true, "Lets you survive crashes without the game exiting, usually. Not compatible with BetterCrashes.");
        lightFixStare = config.getBoolean("lightFixStare", "Tweaks", true, "Causes lighting updates around the block the player is looking at. A workaround for lighting errors that lets you fix them by staring at them. Useful in the Nether.");
        forceUncapFramerate = config.getBoolean("forceUncapFramerate", "Tweaks", false, "Uncaps framerate even when framelimiter is enabled. The framerate limit will only be used to decide how much time to spend updating chunks each frame. Vanilla Beta 1.7.3 behavior. It seems to make things worse though, at least with OptiFine.");
        ofFixUpdateRenderersReturnValue = config.getBoolean("ofFixUpdateRenderersReturnValue", "Tweaks", false, "Fixes OptiFine's implementation of updateRenderers returning the opposite value of what it should (probably a bug). Only effective when framerate limiter is enabled. Speeds up chunk updates significantly, and increases framerate when there aren't many chunk updates. However, during heavy chunk updating (e.g. when loading a world) it decreases the framerate as a side effect of not being as lazy.");
        ofUnlockCustomSkyMinRenderDistance = config.getBoolean("ofUnlockCustomSkyMinRenderDistance", "Tweaks", true, "Allows custom sky rendering in OptiFine D6 when using a render distance lower than 8.");
        minFarPlaneDistance = config.getFloat("minFarPlaneDistance", "Tweaks", 180f, -1f, Float.MAX_VALUE, "The distance of the view fustrum's far plane will be clamped above this value. Setting this to 180 or higher fixes clipping in OptiFine's custom skybox that happens when using lower render distances. Set this to a negative value to disable this tweak.");
        
        restoreTravelSound = config.getBoolean("restoreTravelSound", "Bugfixes", true, "Restore interdimensional travel sound (travel.ogg). Fixes MC-233, fixed in 1.9");
        fixSmallEntitySwim = config.getBoolean("fixSmallEntitySwim", "Bugfixes", true, "Fixes bug in entity swimming code resulting in small entities (ones with hitboxes less than 0.8 units tall, such as DMod's foxes) being prone to drowning.");
        cloudHeightCheck = CloudHeightCheck.valueOf(config.get("Tweaks", "cloudHeightCheck", CloudHeightCheck.ALWAYS_TRANSPARENT.toString(),
                "Lets you tweak the condition used to decide whether to render opaque or transparent clouds.\n" + 
                "UNCHANGED: Don't change anything\n" +
                "VARIABLE_CORRECTED: Keep vanilla behavior of rendering clouds as opaque when the player is below them and transparent otherwise, but with the turning point corrected to match the cloud height even when the world provider has a different cloud height than 128. Also provides a fix for OptiFine's bug where clouds disappear when the player is between Y=128 and the cloud height level when they are raised.\n" +
                "ALWAYS_TRANSPARENT: Always render clouds as transparent (how it is in b1.7.3 and 1.15+)\n" + 
                "ALWAYS_OPAQUE: Always render clouds as opaque",
                EnumUtils.getEnumMap(CloudHeightCheck.class).keySet().toArray(new String[]{})).getString());
        fixDisplayListDelete = config.getBoolean("fixDisplayListDelete", "Bugfixes", true, "Fixes graphical glitches that happen after recovering from a game crash, caused by world renderer display lists getting deleted but never reallocated. From 1.12.");
        fixHeightmapRange = config.getBoolean("fixHeightmapRange", "Bugfixes", true, "Fixes heightmap calculation not including the top layer of 16x16x16 regions, causing lighting errors (MC-7508)");
        fixDoubleEat = config.getBoolean("fixDoubleEat", "Bugfixes", true, "Fixes an extra food item sometimes getting silently consumed (MC-849)");
        
        getPendingBlockUpdates = config.getBoolean("getPendingBlockUpdates", "Optimizations", true, "Optimizes WorldServer#getPendingBlockUpdates. OptiFine also does this, but this won't have an effect when OF is present, so there's no conflict.");
        clientChunkMap = config.getBoolean("clientChunkMap", "Optimizations", false, "Faster implementation of ChunkProviderClient#chunkMapping. From 1.16 (I don't know when exactly it was added). Might be a little buggy (it should only cause client-side errors though).");
        forgeModDiscovererSkipKnownLibraries = config.getBoolean("forgeModDiscovererSkipKnownLibraries", "Optimizations", true, "Skip over known libraries during Forge mod discovery. From Forge 1.12 (added in 1.9)");
        jarDiscovererCache = config.getBoolean("jarDiscovererCache", "Optimizations", true, "Cache jar discoverer results. Not compatible with FoamFix's memory leak fix (which by the way is unnecessary since this doubles as that).");
        forgeFastProgressBar = config.getBoolean("forgeFastProgressBar", "Optimizations", true, "Don't update progress bar on steps. Only active if splash is disabled.");
        forgeFastStepMessageStrip = config.getBoolean("forgeFastStepMessageStrip", "Optimizations", true, "Don't strip unusual characters from bar step messages. Only active if splash is disabled.");
        forgeFastDeobfuscationRemapper = config.getBoolean("forgeFastDeobfuscationRemapper", "Optimizations", true, "Reduces the unnecessary work FMLDeobfuscationRemapper does when we are in a deobfuscated (i.e. development) environment.");
        ofOptimizeWorldRenderer = config.getBoolean("ofOptimizeWorldRenderer", "Optimizations", true, "Replaces the reflection OptiFine uses to access Forge methods in WorldRenderer#updateRenderer with direct calls to those methods. Small speedup during chunk updates.");
        fcOptimizeTextureUpload = config.getBoolean("fcOptimizeTextureUpload", "Optimizations", true, "Removes the call to GL11#getInteger in FastCraft's texture upload handler during texture stitching and uses a cached value instead. Fixes the slowness of texture stitching that happens when OptiFine and FastCraft are both present, and mipmapping is enabled.");
        
        coreTweaksCommand = config.getBoolean("coreTweaksCommand", "Diagnostics", true, "Enables /coretweaks command");
        profilerMethods = config.getString("profilerMethods", "Diagnostics", "", "Comma-separated list of methods to profile. The results will be written to ./coretweaks/out/profiler-<timestamp>.csv. Currently only the call count is measured. Method names have the syntax of `<canonical class name>.<method name>`, like `some.package.SomeClass.method`.");
        frameProfilerHooks = config.getBoolean("frameProfilerHooks", "Diagnostics", false, "Insert hooks that lets the frame profiler profile various parts of frame rendering. Highly recommended if you are using the frame profiler.");
        forgeBarProfiler = config.getBoolean("forgeBarProfiler", "Diagnostics", false, "Creates a report of how long each step of startup loading took in ./coretweaks/out/fml_bar_profiler.csv.");
        crasher = config.getBoolean("crasher", "Diagnostics", false, "Enables debug feature that crashes the game when pressing certain key combinations.");
        serverRunTimePrinter = config.getBoolean("serverRunTimePrinter", "Diagnostics", false, "Prints server run time.");
        
        threadedTextureLoader = config.getBoolean("threadedTextureLoader", "Optimizations", false,
                "Use multi-threaded texture loading when stitching textures? Placebo.");
        transformerCache = getEnum(config, "transformerCache", "Optimizations", TransformerCache.LITE, "The type of transformer caching to use.\n"
                + "NONE: None\n"
                + "LITE: Cache individual transformations of select transformers. Reduces startup time. Relatively safe.\n"
                + "FULL: Cache the entire transformer chain. Reduces startup time further, but breaks with many things.");
        fastFolderTexturePack = config.getBoolean("fastFolderTexturePack", "Optimizations", true, 
                "Use the optimization that speeds up loading folder resource packs?");
        
        threadedTextureLoaderThreadCount = config.getInt("threadedTextureLoaderThreadCount", "Optimizations", 0, 0, Integer.MAX_VALUE,
                "How many threads to use for loading textures? (0: auto (all cores))");
        
        config.setCategoryComment("transformer_cache_full", 
                "Options for the full caching class transformer. (only appliable if it's enabled)");
        badTransformers = config.getString("badTransformers", "transformer_cache_full",
                "org.spongepowered.asm.mixin.transformer.Proxy,appeng.transformer.asm.ApiRepairer,com.mumfrey.liteloader.transformers.ClassOverlayTransformer+",
                "Comma-separated list of transformers for which the view of the transformer chain should be restored.\n" + 
                "\n" + 
                "The caching class transformer replaces the transformer chain with just itself.\n" + 
                "This creates conflicts with certain other transformers which also access the transformer chain,\n" +
                "which can result in the game crashing.\n" +
                "To solve this, our transformer will restore the view of the transformer chain while these transformers are running.\n" + 
                "\n" + 
                "How to find bad transformers? If you see another transformer's name in your crash log,\n" +
                "or see its name in one of the iterator stack traces printed in debug mode,\n" +
                "adding it to this list may solve the problem.\n");
        badClasses = config.getString("badClasses", "transformer_cache_full", "net.eq2online.macros.permissions.MacroModPermissions", 
                "Sometimes caching classes can cause problems. Classes in this list will not be cached.\n");
        modFilesToIgnore = config.getString("modFilesToIgnore", "transformer_cache_full", "CMD files.jar", 
                "Comma-separated list of mod files to ignore modifications of when deciding if a cache rebuild\n" +
                "should be triggered.\n" +
                "If your cache keeps getting rebuilt even though you haven't changed any mods, look for deranged\n" +
                "mod files and add them to this list.");
        recentCacheSize = config.getInt("recentCacheSize", "transformer_cache_full", 512, -1, Integer.MAX_VALUE, 
                "Cached class bytecode is removed from memory after being used, but the most recent N are kept around\n" +
                "because the same class is often transformed more than once. This option sets the value of that N.\n" +
                "(Set to -1 to keep class bytecode in RAM forever)");
        verbosity = config.getInt("verbosity", "transformer_cache_full", 1, 0, 2,
                "0: Only print the essential messages.\n" +
                "1: Print when the cache gets saved.\n" +
                "2: Debug mode. Turn this on to log a bunch of stuff that can help find the cause of a crash.");
        
        config.setCategoryComment("transformer_cache_lite", 
                "Options for the lite caching class transformer. (only appliable if it's enabled)");
        transformersToCache = config.getStringList("transformersToCache", "transformer_cache_lite", new String[]{"cpw.mods.fml.common.asm.transformers.DeobfuscationTransformer", "codechicken.core.asm.MCPDeobfuscationTransformer", "net.minecraftforge.classloading.FluidIdTransformer", "cpw.mods.fml.common.asm.transformers.SideTransformer", "cpw.mods.fml.common.asm.transformers.TerminalTransformer"}, "Canonical class names of the transformers that should be cached.");
        
        if(ConfigDumper.ENABLED) {
            ConfigDumper.dumpConfig(config);
        }
        
        if(config.hasChanged()) {
            config.save();
        }
    }
    
    // TODO move this to MCLib
    private static <E extends Enum> E getEnum(Configuration config, String propName, String propCat, E propDefault, String propComment) {
        return getEnum(config, propName, propCat, propDefault, propComment, false);
    }
    
    private static <E extends Enum> E getEnum(Configuration config, String propName, String propCat, E propDefault, String propComment, boolean lowerCase) {
        Map enumMap = EnumUtils.getEnumMap(propDefault.getClass());
        String[] valuesStr = (String[])enumMap.keySet().toArray(new String[]{});
        String defaultString = propDefault.toString();
        if(lowerCase) defaultString = defaultString.toLowerCase();
        return (E)enumMap.get(config.getString(propName, propCat, defaultString, propComment, valuesStr).toUpperCase());
    }
    
}
