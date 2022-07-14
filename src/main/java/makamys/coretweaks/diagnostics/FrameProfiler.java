package makamys.coretweaks.diagnostics;

import static makamys.coretweaks.diagnostics.FrameProfiler.Entry.*;

import java.io.File;
import java.io.IOException;

import makamys.coretweaks.util.Util;
import makamys.coretweaks.Config;
import makamys.coretweaks.CoreTweaks;
import makamys.coretweaks.util.TableBuilder;

public class FrameProfiler {
    
    public static FrameProfiler instance = new FrameProfiler();
    TableBuilder<Entry, Long> tb;
    
    private boolean started = false;
    
    enum Entry {
    	FRAME_START,
    	FRAME_END,
    	RENDERWORLD_END,
    	UPDATERENDERERS_START,
    	UPDATERENDERERS_END,
    	UPDATERENDERERS_DEADLINE,
    	SYNC_START,
    	SYNC_END,
    	GAMELOOP_START,
    	GAMELOOP_END
    }
    
    
    private void addEntry(Entry type, long value) {
    	tb.set(type, value);
    }
    
    private void addEntry(Entry type) {
    	addEntry(type, System.nanoTime());
    }
    
    public void onFrameStart() {
        if(started) {
            addEntry(FRAME_START);
        }
        
        if(Config.frameProfilerPrint) {
            FrameTimePrinter.preRenderWorld();
        }
    }
    
    public void onFrameEnd() {
        if(started) {
            addEntry(FRAME_END);
        }
        
        if(Config.frameProfilerPrint) {
            FrameTimePrinter.postRenderWorld();
        }
    }
    
    public void postRenderWorld(float alpha, long deadline) {
    	if(started) {
	    	addEntry(RENDERWORLD_END);
	        addEntry(UPDATERENDERERS_DEADLINE, deadline);
    	}
    }
    
    public void preUpdateRenderers() {
        if(started) {
            addEntry(UPDATERENDERERS_START);
        }
    }
    
    public void postUpdateRenderers() {
    	if(started) {
	    	addEntry(UPDATERENDERERS_END);
    	}
    }
    
    public void preSync() {
    	if(started) {
	    	addEntry(SYNC_START);
    	}
    }
    
    public void postSync() {
    	if(started) {
	    	addEntry(SYNC_END);
    	}
    }
    
    public void preRunGameLoop() {
    	if(started) {
        	tb.endRow();
	    	addEntry(GAMELOOP_START);
    	}
    }
    
    public void postRunGameLoop() {
    	if(started) {
	    	addEntry(GAMELOOP_END);
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
    
}
