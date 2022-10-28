package makamys.coretweaks;

import static makamys.coretweaks.util.AnnotationBasedConfigHelper.*;
import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.io.File;

import makamys.coretweaks.util.AnnotationBasedConfigHelper;
import makamys.coretweaks.util.ConfigDumper;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.config.Configuration;

public class Config {
    
    @ConfigBoolean(cat="Tweaks", def=true, com="Lets you survive crashes without the game exiting, usually. Not compatible with other mods that do the same thing.")
    public static boolean crashHandler;
    @ConfigBoolean(cat="Tweaks", def=true, com="Causes lighting updates around the block the player is looking at. A workaround for lighting errors that lets you fix them by staring at them. Useful in the Nether.")
    public static boolean lightFixStare;
    @ConfigBoolean(cat="Tweaks", def=false, com="EXPERIMENTAL: Uncaps framerate even when framelimiter is enabled. The framerate limit will only be used to decide how much time to spend updating chunks each frame. Vanilla Beta 1.7.3 behavior. It seems to make things worse though, at least with OptiFine.")
    public static boolean forceUncapFramerate;
    @ConfigBoolean(cat="Tweaks", def=false, com="Fixes OptiFine's implementation of updateRenderers returning the opposite value of what it should (probably a bug). Only effective when framerate limiter is enabled. Speeds up chunk updates significantly, and increases framerate when there aren't many chunk updates. However, during heavy chunk updating (e.g. when loading a world) it decreases the framerate as a side effect of not being as lazy.")
    public static boolean ofFixUpdateRenderersReturnValue;
    @ConfigBoolean(cat="Tweaks", def=true, com="Allows custom sky rendering in OptiFine D6 when using a render distance lower than 8.")
    public static boolean ofUnlockCustomSkyMinRenderDistance;
    @ConfigFloat(cat="Tweaks", def=180f, min=-1f, max=Float.MAX_VALUE, com="The distance of the view fustrum's far plane will be clamped above this value. Setting this to 180 or higher fixes clipping in OptiFine's custom skybox that happens when using lower render distances. Set this to a negative value to disable this tweak.")
    public static float minFarPlaneDistance;
    @ConfigBoolean(cat="Tweaks", def=false, com="Disables fog. Simple as.")
    public static boolean disableFog;
    @ConfigBoolean(cat="Tweaks", def=false, com="Uncap max length for world name and world seed in the world creation GUI. (By default, it's capped at 32.)")
    public static boolean uncapCreateWorldGuiTextFieldLength;
    @ConfigBoolean(cat="Tweaks", def=false, com="Add a button to the main menu that loads the last played world.")
    public static boolean mainMenuContinueButton;
            
    @ConfigBoolean(cat="Tweaks", def=true, com="Pause some ticks after auto-loaded world is loaded.\nDelaying the pausing can be useful because some initialization like chunk updates won't happen while the game is paused.")
    public static boolean autoLoadPauseOnWorldEntry;
    @ConfigBoolean(cat="Tweaks", def=true, com="Ding once auto-loaded world is loaded.")
    public static boolean autoLoadDingOnWorldEntry;
    @ConfigInt(cat="Tweaks", def=20, min=0, max=Integer.MAX_VALUE, com="How many ticks to wait before pausing an auto-loaded world.")
    public static int autoLoadPauseWaitLength;
            
    @ConfigBoolean(cat="Bugfixes", def=true, com="Restore interdimensional travel sound (travel.ogg). Fixes MC-233, fixed in 1.9")
    public static boolean restoreTravelSound;
    @ConfigBoolean(cat="Bugfixes", def=true, com="Fixes bug in entity swimming code resulting in small entities (ones with hitboxes less than 0.8 units tall, such as DMod's foxes) being prone to drowning.")
    public static boolean fixSmallEntitySwim;
    @ConfigEnum(cat="Tweaks", def="ALWAYS_TRANSPARENT", clazz=CloudHeightCheck.class, com="Lets you tweak the condition used to decide whether to render opaque or transparent clouds.\n" + 
            "* UNCHANGED: Don't change anything\n" +
            "* VARIABLE_CORRECTED: Keep vanilla behavior of rendering clouds as opaque when the player is below them and transparent otherwise, but with the turning point corrected to match the cloud height even when the world provider has a different cloud height than 128. Also provides a fix for OptiFine's bug where clouds disappear when the player is between Y=128 and the cloud height level when they are raised.\n" +
            "* ALWAYS_TRANSPARENT: Always render clouds as transparent (how it is in b1.7.3 and 1.15+)\n" + 
            "* ALWAYS_OPAQUE: Always render clouds as opaque")
    public static CloudHeightCheck cloudHeightCheck;
    @ConfigBoolean(cat="Bugfixes", def=true, com="Fixes graphical glitches that happen after recovering from a game crash, caused by world renderer display lists getting deleted but never reallocated. From 1.12.")
    public static boolean fixDisplayListDelete;
    @ConfigBoolean(cat="Bugfixes", def=true, com="Fixes heightmap calculation not including the top layer of 16x16x16 regions, causing lighting errors (MC-7508)")
    public static boolean fixHeightmapRange;
    @ConfigBoolean(cat="Bugfixes", def=true, com="Fixes an extra food item sometimes getting silently consumed (MC-849)")
    public static boolean fixDoubleEat;
    @ConfigBoolean(cat="Bugfixes", def=false, com="Fixes crash when certain invalid URLs appear in chat. Incompatible with Hodgepodge 1.6.14 and higher, which already does this.")
    public static boolean fixForgeChatLinkCrash;
            
    @ConfigBoolean(cat="Optimizations", def=true, com="Optimizes WorldServer#getPendingBlockUpdates. OptiFine also does this, but this won't have an effect when OF is present, so there's no conflict.")
    public static boolean getPendingBlockUpdates;
    @ConfigBoolean(cat="Optimizations", def=false, com="(WIP) Faster implementation of ChunkProviderClient#chunkMapping. From 1.16 (I don't know when exactly it was added). Might be a little buggy (it should only cause client-side errors though).")
    public static boolean clientChunkMap;
    @ConfigBoolean(cat="Optimizations", def=true, com="Skip over known libraries during Forge mod discovery. From Forge 1.12 (added in 1.9)")
    public static boolean forgeModDiscovererSkipKnownLibraries;
    @ConfigBoolean(cat="Optimizations", def=true, com="Cache jar discoverer results (and fix a memory leak as a nice bonus).")
    public static boolean jarDiscovererCache;
    @ConfigBoolean(cat="Optimizations", def=true, com="Don't update progress bar on steps. Only active if splash is disabled.")
    public static boolean forgeFastProgressBar;
    @ConfigBoolean(cat="Optimizations", def=true, com="Don't strip unusual characters from bar step messages. Only active if splash is disabled.")
    public static boolean forgeFastStepMessageStrip;
    @ConfigBoolean(cat="Optimizations", def=true, com="Reduces the unnecessary work FMLDeobfuscationRemapper does when we are in a deobfuscated (i.e. development) environment.")
    public static boolean forgeFastDeobfuscationRemapper;
    @ConfigBoolean(cat="Optimizations", def=true, com="Replaces the reflection OptiFine uses to access Forge methods in WorldRenderer#updateRenderer with direct calls to those methods. Small speedup during chunk updates.")
    public static boolean ofOptimizeWorldRenderer;
    @ConfigBoolean(cat="Optimizations", def=true, com="Removes the call to GL11#getInteger in FastCraft's texture upload handler during texture stitching and uses a cached value instead. Fixes the slowness of texture stitching that happens when OptiFine and FastCraft are both present, and mipmapping is enabled.")
    public static boolean fcOptimizeTextureUpload;
    @ConfigBoolean(cat="Optimizations", def=true, com="Sets TCP_NODELAY to true, reducing network latency in multiplayer. Works on server as well as client. From Minecraft 1.12 (added in 1.8.1).")
    public static boolean tcpNoDelay;
            
    @ConfigBoolean(cat="Diagnostics", def=true, com="Enables the /coretweaks command, used to access various diagnostics. Invoke it in-game for additional information.")
    public static boolean coreTweaksCommand;
    @ConfigString(cat="Diagnostics", def="", com="Comma-separated list of methods to profile. The results will be written to ./coretweaks/out/profiler-<timestamp>.csv. Currently only the call count is measured. Method names have the syntax of `<canonical class name>.<method name>`, like `some.package.SomeClass.method`.")
    public static String profilerMethods;
    @ConfigBoolean(cat="Diagnostics", def=false, com="Automatically start frame profiler as soon as the game starts.")
    public static boolean frameProfilerStartEnabled;
    @ConfigBoolean(cat="Diagnostics", def=false, com="Insert hooks that lets the frame profiler profile various parts of frame rendering. If this is disabled, the frame profiler will only be able to show very limited information.")
    public static boolean frameProfilerHooks;
    @ConfigBoolean(cat="Diagnostics", def=false, com="Print render tick times to log periodically.")
    public static boolean frameProfilerPrint;
    @ConfigBoolean(cat="Diagnostics", def=false, com="Creates a report of how long each step of startup loading took in ./coretweaks/out/fml_bar_profiler.csv.")
    public static boolean forgeBarProfiler;
    @ConfigBoolean(cat="Diagnostics", def=false, com="Enables debug feature that crashes the game when pressing certain key combinations.")
    public static boolean crasher;
    @ConfigBoolean(cat="Diagnostics", def=false, com="Prints server run time.")
    public static boolean serverRunTimePrinter;
    @ConfigBoolean(cat="Diagnostics", def=false, com="Render world in wireframe mode. Tip: If this is enabled when the game is started, you will be able to toggle it without restarting the game, only the world needs to be reloaded.")
    public static boolean wireframe;
            
    //@ConfigBoolean(cat="Optimizations", def=false, com="Use multi-threaded texture loading when stitching textures? Placebo.")
    public static boolean threadedTextureLoader;
    @ConfigEnum(cat="Optimizations", def="LITE", clazz=TransformerCache.class, com="The type of transformer caching to use.\n"
                    + "* NONE: None\n"
                    + "* LITE: Cache individual transformations of select transformers. Reduces startup time. Safe.\n"
                    + "* FULL: Cache the entire transformer chain. Reduces startup time further, but breaks with many mods.")
    public static TransformerCache transformerCache;
    @ConfigBoolean(cat="Optimizations", def=true, com="Cache the file paths contained in folder resource packs. Fixes the immense slowdown they add to the loading of large modpacks.")
    public static boolean fastFolderTexturePack;
            
    //@ConfigInt(cat="Optimizations", def=0, min=0, max=Integer.MAX_VALUE, com="How many threads to use for loading textures? (0: auto (all cores))")
    public static int threadedTextureLoaderThreadCount;

    @ConfigString(cat="transformer_cache_full", def="org.spongepowered.asm.mixin.transformer.Proxy,appeng.transformer.asm.ApiRepairer,com.mumfrey.liteloader.transformers.ClassOverlayTransformer+",
            com="Comma-separated list of transformers for which the view of the transformer chain should be restored.\n" + 
            "\n" + 
            "The caching class transformer replaces the transformer chain with just itself. This creates conflicts with certain other transformers which also access the transformer chain, which can result in the game crashing.\n" +
            "To solve this, our transformer will restore the view of the transformer chain while these transformers are running.\n" + 
            "\n" + 
            "How to find bad transformers? If you see another transformer's name in your crash log, or see its name in one of the iterator stack traces printed in debug mode, adding it to this list may solve the problem.\n")
    public static String badTransformers;
    @ConfigString(cat="transformer_cache_full", def="net.eq2online.macros.permissions.MacroModPermissions", 
            com="Sometimes caching classes can cause problems. Classes in this list will not be cached.\n")
    public static String badClasses;
    @ConfigString(cat="transformer_cache_full", def="CMD files.jar", 
            com="Comma-separated list of mod files to ignore modifications of when deciding if a cache rebuild should be triggered.\n" +
            "If your cache keeps getting rebuilt even though you haven't changed any mods, look for deranged mod files and add them to this list.")
    public static String modFilesToIgnore;
    @ConfigInt(cat="transformer_cache_full", def=512, min=-1, max=Integer.MAX_VALUE, 
            com="Cached class bytecode is removed from memory after being used, but the most recent N are kept around because the same class is often transformed more than once. This option sets the value of that N.\n" +
            "(Set to -1 to keep class bytecode in RAM forever)")
    public static int recentCacheSize;
    @ConfigInt(cat="transformer_cache_full", def=1, min=0, max=2,
            com="* 0: Only print the essential messages.\n" +
            "* 1: Print when the cache gets saved.\n" +
            "* 2: Debug mode. Turn this on to log a bunch of stuff that can help find the cause of a crash.")
    public static int verbosity;
    
    @ConfigStringList(cat="transformer_cache_lite", def={"cpw.mods.fml.common.asm.transformers.DeobfuscationTransformer", "codechicken.core.asm.MCPDeobfuscationTransformer", "net.minecraftforge.classloading.FluidIdTransformer", "cpw.mods.fml.common.asm.transformers.SideTransformer", "cpw.mods.fml.common.asm.transformers.TerminalTransformer"}, com="Canonical class names of the transformers that should be cached.")
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
    
    private static AnnotationBasedConfigHelper configHelper = new AnnotationBasedConfigHelper(Config.class, LOGGER);
    
    public static void reload() {
        Configuration config = new Configuration(new File(Launch.minecraftHome, "config/coretweaks.cfg"));
        
        config.load();
        configHelper.loadFields(config);
        
        config.addCustomCategoryComment("Tweaks", "In addition to these settings, there are some tweaks that are activated via JVM flags:\n" +
        "* -Dcoretweaks.launchWorld=WORLD : Automatically loads the world with the folder name WORLD once the main menu is reached. WORLD can be left blank, in this case the most recently played world will be loaded. Hold down shift when the main menu appears to cancel the automatic loading.\n" +
        "* -Dcoretweaks.launchMinimized : Launch Minecraft minimized. Only implemented on Windows at the moment.\n" +
        "* -Dcoretweaks.launchOnDesktop=NUMBER : Launch Minecraft on the virtual desktop with ordinal NUMBER. Only implemented on Linux at the moment. xprop has to be installed for it to work. Only tested on Openbox.");
        
        config.setCategoryComment("transformer_cache_full", 
                "Options for the full caching class transformer. (only appliable if it's enabled)");
        
        config.setCategoryComment("transformer_cache_lite", 
                "Options for the lite caching class transformer. (only appliable if it's enabled)");
        
        if(ConfigDumper.ENABLED) {
            ConfigDumper.dumpConfig(config);
        }
        
        if(config.hasChanged()) {
            config.save();
        }
    }
    
}
