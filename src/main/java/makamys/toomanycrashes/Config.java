package makamys.toomanycrashes;

import java.io.File;

import org.apache.commons.lang3.EnumUtils;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.config.Configuration;

public class Config {
    
    public static boolean crashHandler;
    public static boolean forceUncapFramerate;
    public static boolean ofFixUpdateRenderersReturnValue;
    public static boolean ofOptimizeWorldRenderer;
    public static boolean getPendingBlockUpdates;
    public static boolean clientChunkMap;
    public static boolean restoreTravelSound;
    public static boolean modDiscovererSkipKnownLibraries;
    public static boolean jarDiscovererCache;
    public static boolean fastProgressBar;
    public static boolean fastStepMessageStrip;
    public static CloudHeightCheck cloudHeightCheck;
    
    public static boolean TMCCommand;
    public static boolean printActive;
    public static String methodsToProfile;
    
    public static int spikeThreshold;
    
    
    public static enum CloudHeightCheck {
    	UNCHANGED,
    	VARIABLE_CORRECTED,
    	ALWAYS_TRANSPARENT,
    	ALWAYS_OPAQUE
    }
    
    public static void reload() {
        Configuration config = new Configuration(new File(Launch.minecraftHome, "config/toomanycrashes.cfg"));
        
        config.load();
        crashHandler = config.get("Tweaks", "crashHandler", false, "Lets you survive crashes without the game exiting, usually. May cause graphical glitches after the crash, so I only recommend enabling it in test/dev sessions.").getBoolean();
        forceUncapFramerate = config.get("Tweaks", "forceUncapFramerate", false, "Uncaps framerate even when framelimiter is enabled. The framerate limit will only be used to decide how much time to spend updating chunks each frame. Vanilla Beta 1.7.3 behavior. It seems to make things worse though, at least with OptiFine.").getBoolean();
        ofFixUpdateRenderersReturnValue = config.get("Tweaks", "ofFixUpdateRenderersReturnValue", true, "Fixes OptiFine's implementation of updateRenderers returning the opposite value of what it should (probably a bug). Only effective when framerate limiter is enabled. Speeds up chunk updates significantly, and increases framerate when there aren't many chunk updates. However, during heavy chunk updating (e.g. when loading a world) it decreases the framerate as a side effect of not being as lazy.").getBoolean();
        ofOptimizeWorldRenderer = config.get("Optimizations", "ofOptimizeWorldRenderer", true, "Replaces the reflection OptiFine uses to access Forge methods in WorldRenderer#updateRenderer with direct calls to those methods. Small speedup during chunk updates.").getBoolean();
        restoreTravelSound = config.get("Tweaks", "restoreTravelSound", true, "Restore interdimensional travel sound (travel.ogg). Fixes MC-233, fixed in 1.9").getBoolean();
        getPendingBlockUpdates = config.get("Optimizations", "getPendingBlockUpdates", true, "Optimizes WorldServer#getPendingBlockUpdates. OptiFine also does this, but this won't have an effect when OF is present, so there's no conflict.").getBoolean();
        clientChunkMap = config.get("Optimizations", "clientChunkMap", false, "Faster implementation of ChunkProviderClient#chunkMapping. From 1.16 (I don't know when exactly it was added). Might be a little buggy (it should only cause client-side errors though).").getBoolean();
        modDiscovererSkipKnownLibraries = config.get("Optimizations", "modDiscovererSkipKnownLibraries", true, "Skip over known libraries during Forge mod discovery. From Forge 1.12 (added in 1.9)").getBoolean();
        jarDiscovererCache = config.get("Optimizations", "jarDiscovererCache", true, "Cache jar discoverer results").getBoolean();
        fastProgressBar = config.get("Optimizations", "fastProgressBar", true, "Don't update progress bar on steps").getBoolean();
        fastStepMessageStrip = config.get("Optimizations", "fastStepMessageStrip", true).getBoolean();
        TMCCommand = config.get("Misc", "TMCCommand", true).getBoolean();
        spikeThreshold = config.get("Misc", "spikeThreshold", 30).getInt();
        printActive = config.get("Misc", "printActive", true).getBoolean();
        methodsToProfile = config.get("Misc", "methodsToProfile", "").getString();
        cloudHeightCheck = CloudHeightCheck.valueOf(config.get("Tweaks", "cloudHeightCheck", CloudHeightCheck.VARIABLE_CORRECTED.toString(),
        		"Lets you tweak the condition used to decide whether to render opaque or transparent clouds.\n" + 
		        "UNCHANGED: Don't change anything\n" +
				"VARIABLE_CORRECTED: Keep vanilla behavior of rendering clouds as opaque when the player is below them and transparent otherwise, but with the turning point corrected to match the cloud height even when the world provider has a different cloud height than 128. Also provides a fix for OptiFine's bug where clouds disappear when the player is between Y=128 and the cloud height level when they are raised.\n" +
		        "ALWAYS_TRANSPARENT: Always render clouds as transparent (how it is in b1.7.3 and 1.15+)\n" + 
				"ALWAYS_OPAQUE: Always render clouds as opaque",
				EnumUtils.getEnumMap(CloudHeightCheck.class).keySet().toArray(new String[]{})).getString());
        
        if(config.hasChanged()) {
            config.save();
        }
    }
    
}
