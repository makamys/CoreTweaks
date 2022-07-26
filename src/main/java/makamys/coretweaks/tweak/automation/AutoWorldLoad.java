package makamys.coretweaks.tweak.automation;

import static makamys.coretweaks.CoreTweaks.LOGGER;
import static makamys.coretweaks.tweak.automation.AutoWorldLoad.PauseStatus.*;

import net.minecraft.client.LoadingScreenRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.SaveFormatComparator;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.lwjgl.opengl.Display;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import makamys.coretweaks.Config;
import makamys.coretweaks.IModEventListener;
import makamys.coretweaks.JVMArgs;

public class AutoWorldLoad implements IModEventListener {
	
	public static AutoWorldLoad instance;
	
	public enum PauseStatus { NONE, ENQUEUED, PAUSED, FINISHED }
    
    private int timesSeenMainMenu = 0;
    private int timesWentIngame = 0;
    
    private PauseStatus pauseStatus = NONE;
    private int pauseWait = 0;
    
    private boolean guiChanged = false;
    
    private Minecraft mc;
    
    private boolean worldLoadFailed;
    
    @Override
    public void onInit(FMLInitializationEvent event) {
    	mc = Minecraft.getMinecraft();
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
    }
    
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event){
        if(event.phase == TickEvent.Phase.START) {
        	if(pauseStatus == ENQUEUED && pauseWait++ >= Config.pauseWaitLength) {
        		mc.displayInGameMenu();
        		pauseStatus = PAUSED;
        	} else if(pauseStatus == PAUSED && Display.isActive()) {
        		mc.displayGuiScreen(null);
        		pauseStatus = FINISHED;
        	}
        	
        	if(guiChanged) {
            	onGuiChanged(mc.currentScreen);
        	}
        }
    }
    
    @SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event) {
    	guiChanged = true;
    }
    
    private void onGuiChanged(GuiScreen gui) {
    	if(JVMArgs.LAUNCH_WORLD != null && !worldLoadFailed) {
	    	if(gui instanceof GuiMainMenu && timesSeenMainMenu++ == 0) {
	    		tryToLoadWorld();
	    	} else if(gui instanceof GuiMainMenu) {
	    		LOGGER.debug("times seen main menu: " + timesSeenMainMenu);
	    	}
	    	if(gui == null && timesWentIngame++ == 0 && !Display.isActive()) {
	    		if(Config.dingOnWorldEntry) {
	    			mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("random.orb"), 1.0F));
	    		}
	    		if(Config.pauseOnWorldEntry) {
	    			pauseStatus = ENQUEUED;
	    		}
	    	}
    	}
    }
    
    private void tryToLoadWorld() {
		if(JVMArgs.LAUNCH_WORLD != null) {
			ISaveFormat saveLoader = mc.getSaveLoader();
    		try {
    			Optional<SaveFormatComparator> saveOpt;
    			
    			List<SaveFormatComparator> saveList = (List<SaveFormatComparator>)saveLoader.getSaveList();
				
    			if(!JVMArgs.LAUNCH_WORLD.isEmpty()) {
					saveOpt = saveList.stream()
							.filter(s -> s.getFileName().equals(JVMArgs.LAUNCH_WORLD)).findFirst();
    			} else {
    				if(saveList != null && !saveList.isEmpty()) {
    					Collections.sort(saveList);
    					saveOpt = Optional.of(saveList.get(0));
    				} else {
    					saveOpt = Optional.empty();
    				}
    			}
				if(saveOpt.isPresent()) {
					SaveFormatComparator save = (SaveFormatComparator)saveOpt.get();
					if(mc.loadingScreen == null) {
						mc.loadingScreen = new LoadingScreenRenderer(mc);
					}
    				FMLClientHandler.instance().tryLoadExistingWorld(null, save.getFileName(), save.getDisplayName());
				} else {
					LOGGER.error("Couldn't find a suitable world to load");
					worldLoadFailed = true;
				}
			} catch (Exception e) {
				LOGGER.error("Failed to load world on startup");
				e.printStackTrace();
			}
		}
    }
}
