package makamys.coretweaks.tweaks.crashhandler.crashmod;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;

@Mod(modid = "crashmod", version = "0.0")
public class CrashMod {
    
    @EventHandler
    public void onServerAboutToStart(FMLServerAboutToStartEvent event) {
        if(Keyboard.isKeyDown(Keyboard.KEY_F3) && Keyboard.isKeyDown(Keyboard.KEY_1)) {
            throw new RuntimeException("Test exception in ServerAboutToStart");
        }
    }
    
}
