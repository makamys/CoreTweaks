package makamys.coretweaks.diagnostics;

import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.util.Arrays;

public class RenderTickTimePrinter {
    
    private static long frameStart, frameEnd;
    
    private static final int printInterval = 4;
    private static int[] times = new int[printInterval * 2000]; // hope you don't get above 2000 fps!
    private static int timeI;
    
    private static long nextPrintTime;

    public static void preRenderWorld() {
        if(nextPrintTime == 0) {
            nextPrintTime = System.nanoTime();
        }
        
        frameStart = System.nanoTime();
    }
    
    public static void postRenderWorld() {
        frameEnd = System.nanoTime();
        times[timeI++] = (int)(frameEnd - frameStart);
        
        long now = System.nanoTime();
        
        if(now > nextPrintTime || timeI > times.length) {
            Arrays.sort(times, 0, timeI);
            long sum = 0;
            
            for(int i = 0; i < timeI; i++) {
                sum += times[i];
            }
            
            LOGGER.info("Render tick time: median: " + times[timeI / 2] / 1000000.0 + " ms, avg: " + (sum / (double)timeI / 1000000.0) + " ms");
            
            timeI = 0;
            
            while(now > nextPrintTime) {
                nextPrintTime += printInterval * 1000000000l;
            }
        }
    }

}