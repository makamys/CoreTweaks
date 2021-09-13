package makamys.coretweaks.diagnostics;

import cpw.mods.fml.common.ProgressManager.ProgressBar;
import makamys.coretweaks.Config;

public class FMLBarProfiler {
    
    private static FMLBarProfiler instance;
    
    public void onPush(ProgressBar bar) {
        System.out.println("push " + bar2str(bar));
    }
    
    public void onPop(ProgressBar bar) {
        System.out.println("pop " + bar2str(bar));
    }
    
    public void onStep(ProgressBar bar) {
        System.out.println("step " + bar2str(bar));
    }
    
    private static String bar2str(ProgressBar bar) {
        return bar.getTitle() + " - " + bar.getMessage() + " (" + bar.getStep() + "/" + bar.getSteps() + ")";
    }
    
    public static FMLBarProfiler instance() {
        return instance == null ? (instance = new FMLBarProfiler()) : instance;
    }
    
    public static boolean isActive() {
        return Config.fmlBarProfiler;
    }
    
}
