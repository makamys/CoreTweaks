package makamys.coretweaks;

import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;

public interface IModEventListener {
    
    default void onServerAboutToStart(FMLServerAboutToStartEvent event) {};
    
}
