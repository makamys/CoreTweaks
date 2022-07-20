package makamys.coretweaks.diagnostics;

import static makamys.coretweaks.diagnostics.FrameProfiler.Entry.*;

import java.io.IOException;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import makamys.coretweaks.Config;
import makamys.coretweaks.CoreTweaks;
import makamys.coretweaks.IModEventListener;
import makamys.coretweaks.util.TableBuilder;
import makamys.coretweaks.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.WorldRenderer;

public class FrameProfiler implements IModEventListener {
    
    public static FrameProfiler instance;
    TableBuilder<Entry, Object> tb;
    
    private boolean started = false;
    
    private int chunksUpdatedAtFrameStart = 0;
    
    enum Entry {
        T_GAMELOOP_START,
        T_FRAME_START,
        T_UPDATERENDERERS_START,
        UPDATERENDERERS_DEADLINE,
        T_UPDATERENDERERS_END,
        T_RENDERWORLD_END,
        T_FRAME_END,
        T_SYNC_START,
        T_SYNC_END,
        T_GAMELOOP_END,
        CHUNK_UPDATES,
        GUI
    }
    
    
    private void addEntry(Entry type, Object value) {
    	tb.set(type, value);
    }
    
    private void addEntry(Entry type) {
    	addEntry(type, System.nanoTime());
    }
    
    public void onFrameStart() {
        if(started) {
            addEntry(T_FRAME_START);
            chunksUpdatedAtFrameStart = WorldRenderer.chunksUpdated;
            GuiScreen gui = Minecraft.getMinecraft().currentScreen;
            addEntry(GUI, gui == null ? null : gui.getClass().getName());
        }
        
        if(Config.frameProfilerPrint) {
            RenderTickTimePrinter.preRenderWorld();
        }
    }
    
    public void onFrameEnd() {
        if(started) {
            addEntry(T_FRAME_END);
            addEntry(CHUNK_UPDATES, WorldRenderer.chunksUpdated - chunksUpdatedAtFrameStart);
        }
        
        if(Config.frameProfilerPrint) {
            RenderTickTimePrinter.postRenderWorld();
        }
    }
    
    public void postRenderWorld(float alpha, long deadline) {
    	if(started) {
	    	addEntry(T_RENDERWORLD_END);
	        addEntry(UPDATERENDERERS_DEADLINE, deadline);
    	}
    }
    
    public void preUpdateRenderers() {
        if(started) {
            addEntry(T_UPDATERENDERERS_START);
        }
    }
    
    public void postUpdateRenderers() {
    	if(started) {
	    	addEntry(T_UPDATERENDERERS_END);
    	}
    }
    
    public void preSync() {
    	if(started) {
	    	addEntry(T_SYNC_START);
    	}
    }
    
    public void postSync() {
    	if(started) {
	    	addEntry(T_SYNC_END);
    	}
    }
    
    public void preRunGameLoop() {
    	if(started) {
        	tb.endRow();
	    	addEntry(T_GAMELOOP_START);
    	}
    }
    
    public void postRunGameLoop() {
    	if(started) {
	    	addEntry(T_GAMELOOP_END);
    	}
    }
    
    public void start() {
    	tb = new TableBuilder<>();
        started = true;
    }
    
    private boolean dumpProfilingResults() {
    	try {
    		tb.writeToCSV(Util.childFile(CoreTweaks.OUT_DIR, "frameprofiler.csv"));
    		return true;
    	} catch(IOException e) {
    		return false;
    	} finally {
    		tb = null;
    	}
    }
    
    public boolean stop() {
        started = false;
        if(tb != null) {
            return dumpProfilingResults();
        }
        return true;
    }
    
    public boolean isStarted() {
        return started;
    }
    
    @Override
    public void onInit(FMLInitializationEvent event) {
        FMLCommonHandler.instance().bus().register(instance);
        
        if(Config.frameProfilerStartEnabled) {
            start();
        }
    }
    
    @Override
    public void onShutdown() {
        if(started) {
            stop();
        }
    }
    
    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if(event.phase == TickEvent.Phase.START) {
            FrameProfiler.instance.onFrameStart();
        } else if(event.phase == TickEvent.Phase.END) {
            FrameProfiler.instance.onFrameEnd();
        }
    }
    
}
