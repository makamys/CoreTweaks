package makamys.toomanycrashes;

import java.io.File;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.config.Configuration;

public class Config {
    
    public static boolean crashHandler;
    public static boolean getPendingBlockUpdates;
    public static boolean clientChunkMap;
    public static boolean restoreTravelSound;
    
    public static boolean TMCCommand;
    public static boolean printActive;
    
    public static int spikeThreshold;
    
    public static void reload() {
        Configuration config = new Configuration(new File(Launch.minecraftHome, "config/toomanycrashes.cfg"));
        
        config.load();
        crashHandler = config.get("Tweaks", "crashHandler", true, "Handle crashes without exiting the game. May cause graphical glitches in the newly loaded game!").getBoolean();
        restoreTravelSound = config.get("Tweaks", "restoreTravelSound", true, "Restore interdimensional travel sound (travel.ogg). Fixes MC-233, fixed in 1.9").getBoolean();
        getPendingBlockUpdates = config.get("Optimizations", "getPendingBlockUpdates", true).getBoolean();
        clientChunkMap = config.get("Optimizations", "clientChunkMap", true).getBoolean();
        TMCCommand = config.get("Misc", "TMCCommand", true).getBoolean();
        spikeThreshold = config.get("Misc", "spikeThreshold", 30).getInt();
        printActive = config.get("Misc", "printActive", true).getBoolean();
        
        if(config.hasChanged()) {
            config.save();
        }
    }
    
}
