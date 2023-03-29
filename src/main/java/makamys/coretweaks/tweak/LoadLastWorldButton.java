package makamys.coretweaks.tweak;

import java.util.List;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import makamys.coretweaks.IModEventListener;
import makamys.coretweaks.util.MCUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.common.MinecraftForge;

public class LoadLastWorldButton implements IModEventListener {
    
    public static LoadLastWorldButton instance;
    
    GuiButton loadLastWorldButton;
    private static final int UPDATES_BUTTON_ID = -2026964516;
    
    @Override
    public void onInit(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void onGui(InitGuiEvent.Post event) {
        if(event.gui instanceof GuiMainMenu) {
            GuiButton newWorld = ((List<GuiButton>)event.buttonList).stream().filter(b -> b.id == 1).findFirst().get();
            int originalWidth = newWorld.width;
            newWorld.width = originalWidth / 3 * 2 - 2;
            loadLastWorldButton = new GuiButton(UPDATES_BUTTON_ID, newWorld.xPosition + originalWidth / 3 * 2 + 2, newWorld.yPosition, originalWidth - (originalWidth / 3 * 2 + 2), newWorld.height, I18n.format("gui.coretweaks.menu.continue", new Object[0]));
            event.buttonList.add(loadLastWorldButton);
        }
    }
        
    @SubscribeEvent
    public void onActionPerformed(ActionPerformedEvent.Post event) {
        if(event.button == loadLastWorldButton) {
            MCUtil.tryToLoadWorld(null);
        }
    }
    
}
