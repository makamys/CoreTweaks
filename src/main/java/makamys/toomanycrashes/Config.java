package makamys.toomanycrashes;

import java.io.File;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.config.Configuration;

public class Config {
    
    public static boolean crashHandler;
    public static boolean getPendingBlockUpdates;
    public static boolean clientChunkMap;
    public static boolean restoreTravelSound;
    public static boolean modDiscovererSkipKnownLibraries;
    public static boolean jarDiscovererCache;
    public static boolean fastProgressBar;
    
    public static boolean TMCCommand;
    public static boolean printActive;
    
    public static int spikeThreshold;
    
    public static void reload() {
        Configuration config = new Configuration(new File(Launch.minecraftHome, "config/toomanycrashes.cfg"));
        
        config.load();
        crashHandler = config.get("Tweaks", "crashHandler", true, "Lets you survive crashes without the game exiting, usually. May cause graphical glitches in the newly loaded game!").getBoolean();
        restoreTravelSound = config.get("Tweaks", "restoreTravelSound", true, "Restore interdimensional travel sound (travel.ogg). Fixes MC-233, fixed in 1.9").getBoolean();
        getPendingBlockUpdates = config.get("Optimizations", "getPendingBlockUpdates", true, "Optimizes WorldServer#getPendingBlockUpdates. OptiFine also does this, but this won't have an effect when OF is present, so there's no conflict.").getBoolean();
        clientChunkMap = config.get("Optimizations", "clientChunkMap", true, "Faster implementation of ChunkProviderClient#chunkMapping. From 1.16 (I don't know when exactly it was added). Might be buggy when travelling between dimensions?").getBoolean();
        modDiscovererSkipKnownLibraries = config.get("Optimizations", "modDiscovererSkipKnownLibraries", true, "Skip over known libraries during Forge mod discovery. From Forge 1.12 (added in 1.9)").getBoolean();
        jarDiscovererCache = config.get("Optimizations", "jarDiscovererCache", true, "Cache jar discoverer results").getBoolean();
        fastProgressBar = config.get("Optimizations", "fastProgressBar", true, "Don't update progress bar on steps").getBoolean();
        TMCCommand = config.get("Misc", "TMCCommand", true).getBoolean();
        spikeThreshold = config.get("Misc", "spikeThreshold", 30).getInt();
        printActive = config.get("Misc", "printActive", true).getBoolean();
        
        if(config.hasChanged()) {
            config.save();
        }
    }
    
}
