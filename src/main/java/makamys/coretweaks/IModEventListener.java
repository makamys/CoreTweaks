package makamys.coretweaks;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;

public interface IModEventListener {
    
    default void onPreInit(FMLPreInitializationEvent event) {};
    default void onInit(FMLInitializationEvent event) {};
    default void onPostInit(FMLPostInitializationEvent event) {};
    
    default void onServerAboutToStart(FMLServerAboutToStartEvent event) {};
    default void onServerStarting(FMLServerStartingEvent event) {};
    default void onServerStarted(FMLServerStartedEvent event) {};
    default void onServerStopping(FMLServerStoppingEvent event) {};
    default void onServerStopped(FMLServerStoppedEvent event) {};
    
    default void onShutdown() {};
    
}
