package makamys.coretweaks.tweak;

import java.util.List;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import makamys.coretweaks.IModEventListener;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
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
            newWorld.width = 98;
            loadLastWorldButton = new GuiButton(UPDATES_BUTTON_ID, newWorld.xPosition + 102, newWorld.yPosition, newWorld.width, newWorld.height, "Continue");
            event.buttonList.add(loadLastWorldButton);
        }
    }
        
    @SubscribeEvent
    public void onActionPerformed(ActionPerformedEvent event) {
        if(event.button == loadLastWorldButton) {
            System.out.println("click");
        }
    }
    
}
