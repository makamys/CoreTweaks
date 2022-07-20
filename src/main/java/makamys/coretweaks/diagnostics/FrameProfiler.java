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
        t_gameLoopStart,
        t_frameStart,
        t_updateRenderersStart,
        t_updateRenderersDeadline,
        t_updateRenderersEnd,
        t_renderWorldEnd,
        t_frameEnd,
        t_syncStart,
        t_syncEnd,
        t_gameLoopEnd,
        chunkUpdates,
        gui
    }
    
    
    private void addEntry(Entry type, Object value) {
    	tb.set(type, value);
    }
    
    private void addEntry(Entry type) {
    	addEntry(type, System.nanoTime());
    }
    
    public void onFrameStart() {
        if(started) {
            addEntry(t_frameStart);
            chunksUpdatedAtFrameStart = WorldRenderer.chunksUpdated;
            GuiScreen screen = Minecraft.getMinecraft().currentScreen;
            addEntry(gui, screen == null ? null : screen.getClass().getName());
        }
        
        if(Config.frameProfilerPrint) {
            RenderTickTimePrinter.preRenderWorld();
        }
    }
    
    public void onFrameEnd() {
        if(started) {
            addEntry(t_frameEnd);
            addEntry(chunkUpdates, WorldRenderer.chunksUpdated - chunksUpdatedAtFrameStart);
        }
        
        if(Config.frameProfilerPrint) {
            RenderTickTimePrinter.postRenderWorld();
        }
    }
    
    public void postRenderWorld(float alpha, long deadline) {
    	if(started) {
	    	addEntry(t_renderWorldEnd);
	        addEntry(t_updateRenderersDeadline, deadline);
    	}
    }
    
    public void preUpdateRenderers() {
        if(started) {
            addEntry(t_updateRenderersStart);
        }
    }
    
    public void postUpdateRenderers() {
    	if(started) {
	    	addEntry(t_updateRenderersEnd);
    	}
    }
    
    public void preSync() {
    	if(started) {
	    	addEntry(t_syncStart);
    	}
    }
    
    public void postSync() {
    	if(started) {
	    	addEntry(t_syncEnd);
    	}
    }
    
    public void preRunGameLoop() {
    	if(started) {
        	tb.endRow();
	    	addEntry(t_gameLoopStart);
    	}
    }
    
    public void postRunGameLoop() {
    	if(started) {
	    	addEntry(t_gameLoopEnd);
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
