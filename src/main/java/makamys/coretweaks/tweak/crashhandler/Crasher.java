package makamys.coretweaks.tweak.crashhandler;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import makamys.coretweaks.IModEventListener;
import makamys.coretweaks.util.KeyboardUtil;
import net.minecraft.client.renderer.Tessellator;

public class Crasher implements IModEventListener {
    
    public static Crasher instance;
    
    private static final boolean CRASH_EVERY_CLIENT_TICK = Boolean.parseBoolean(System.getProperty("coretweaks.crasher.crashEveryClientTick", "false"));
    
    @Override
    public void onPreInit(FMLPreInitializationEvent event) {
        FMLCommonHandler.instance().bus().register(this);
    }
    
    @Override
    public void onServerAboutToStart(FMLServerAboutToStartEvent event) {
        if(Keyboard.isKeyDown(Keyboard.KEY_F3) && Keyboard.isKeyDown(Keyboard.KEY_1)) {
            throw new RuntimeException("Test exception in ServerAboutToStart");
        }
    }
    
    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        if(Keyboard.isKeyDown(Keyboard.KEY_F3) && Keyboard.isKeyDown(Keyboard.KEY_T) && !KeyboardUtil.wasKeyDown(Keyboard.KEY_T)) {
            Tessellator.instance.startDrawingQuads();
            throw new RuntimeException("Test exception during tessellation");
        }
        
        if(CRASH_EVERY_CLIENT_TICK) {
            throw new RuntimeException("Test exception in onClientTick");
        }
    }
    
}
