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
            int buttonX, buttonY, buttonWidth, buttonHeight;
            GuiButton newWorld = ((List<GuiButton>)event.buttonList).stream().filter(b -> b.id == 1).findFirst().orElse(null);
            if(newWorld != null) {
                // We found the single player button, squash it and put the continue button in the leftover space.
                int originalWidth = newWorld.width;
                newWorld.width = originalWidth / 3 * 2 - 2;
                buttonX = newWorld.xPosition + originalWidth / 3 * 2 + 2;
                buttonY = newWorld.yPosition;
                buttonWidth = originalWidth - (originalWidth / 3 * 2 + 2);
                buttonHeight = newWorld.height;
            } else {
                // Something is interfering (probably CustomMainMenu.) Let's just put it to the right of the single player button's original position.
                int originalX = event.gui.width / 2 - 100;
                int originalY = event.gui.height / 4 + 48;
                int originalWidth = 200;
                int originalHeight = 20;
                buttonX = originalX + originalWidth + 4;
                buttonY = originalY;
                buttonWidth = originalWidth - (originalWidth / 3 * 2 + 2);
                buttonHeight = originalHeight;
            }
            loadLastWorldButton = new GuiButton(UPDATES_BUTTON_ID, buttonX, buttonY, buttonWidth, buttonHeight, I18n.format("gui.coretweaks.menu.continue", new Object[0]));
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
