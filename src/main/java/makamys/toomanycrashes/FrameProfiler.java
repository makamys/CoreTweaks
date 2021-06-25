package makamys.toomanycrashes;

import java.io.File;
import java.io.IOException;

import static makamys.toomanycrashes.FrameProfiler.Entry.*;

public class FrameProfiler {
    
    public static FrameProfiler instance = new FrameProfiler();
    TableBuilder<Entry, Long> tb;
    
    private boolean started = false;
    
    enum Entry {
    	FRAME_START
    }
    
    
    private void addEntry(Entry type, long value) {
    	tb.set(type, value);
    }
    
    private void addEntry(Entry type) {
    	addEntry(type, System.nanoTime());
    }
    
    public void onFrameStart() {
        if(started) {
        	tb.endRow();
            addEntry(FRAME_START);
        }
    }
    
    public void start() {
    	tb = new TableBuilder<>();
        started = true;
    }
    
    private boolean dumpProfilingResults() {
    	try {
    		tb.writeToCSV(new File("frameprofiler-" + System.currentTimeMillis() + ".csv"));
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
