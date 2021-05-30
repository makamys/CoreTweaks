package makamys.toomanycrashes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class FrameTimer {
    
    public static FrameTimer instance = new FrameTimer();
    
    private List<Long> frameTimes = new ArrayList<>();
    private boolean started = false;
    
    public void onFrameStart() {
        if(started) {
            frameTimes.add(System.nanoTime());
        }
    }
    
    public void start() {
        started = true;
    }
    
    private boolean dumpFrameTimes() {
        String dump = "";
        for(long frameTime : frameTimes) {
            dump += frameTime + "\n";
        }
        
        try {
            FileUtils.write(new File("frametimer-" + System.currentTimeMillis() + ".txt"), dump);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    private void analyzeFrameTimes() {
        long last = -1;
        int spikes = 0;
        long sumMs = 0;
        for(long frameTime : frameTimes) {
            if(last != -1) {
                long delta = frameTime - last;
                int deltaMs = (int)(delta / 1_000_000);
                sumMs += deltaMs;
                if(deltaMs > Config.spikeThreshold) {
                    spikes++;
                }
            }
            last = frameTime;
        }
        int deltaCount = (frameTimes.size() - 1);
        System.out.println("spikes (above " + Config.spikeThreshold + " ms): " + spikes + " / " + deltaCount + " (" + (spikes / (double)deltaCount) + ")");
        System.out.println("average: " + (sumMs / (double)deltaCount) + " ms (" + (deltaCount / (sumMs / 1000.0)) + " fps)");
    }
    
    public boolean stop() {
        started = false;
        if(!frameTimes.isEmpty()) {
            analyzeFrameTimes();
            
            frameTimes.clear();
            return dumpFrameTimes();
        }
        return true;
    }
    
    public boolean isStarted() {
        return started;
    }
    
}
