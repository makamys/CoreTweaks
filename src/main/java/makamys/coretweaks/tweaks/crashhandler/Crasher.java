package makamys.coretweaks.tweaks.crashhandler;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import makamys.coretweaks.IModEventListener;

public class Crasher implements IModEventListener {
    
    public static Crasher instance;
    
    @Override
    public void onServerAboutToStart(FMLServerAboutToStartEvent event) {
        if(Keyboard.isKeyDown(Keyboard.KEY_F3) && Keyboard.isKeyDown(Keyboard.KEY_1)) {
            throw new RuntimeException("Test exception in ServerAboutToStart");
        }
    }
    
}
