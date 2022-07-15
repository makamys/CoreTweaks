package makamys.coretweaks.diagnostics;

import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import makamys.coretweaks.Config;
import makamys.coretweaks.CoreTweaks;
import makamys.coretweaks.util.Util;

public class MethodProfiler {
    
    public static MethodProfiler instance = new MethodProfiler();
    
    public Map<String, List<String>> targets = new HashMap<>();
    public List<MethodInstrumentationData> methodInstrumentationDatas = new ArrayList<>();
    
    private ProfileMode profileMode = ProfileMode.NONE;
    private Writer out;
    
    public void init() {
        if(Config.profilerMethods.isEmpty()) return;
        
        for(String methodStr : Config.profilerMethods.split(",")) {
            int lastDot = methodStr.lastIndexOf('.');
            String clazz = methodStr.substring(0, lastDot);
            String method = methodStr.substring(lastDot + 1);
            List<String> classTargets = targets.get(clazz);
            if(classTargets == null) {
                classTargets = new ArrayList<>();
                targets.put(clazz, classTargets);
            }
            
            classTargets.add(method);
        }
        
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    FileUtils.write(Util.childFile(CoreTweaks.OUT_DIR, "method-profiler.json"), String.join("\n", methodInstrumentationDatas.stream().map(d -> d.getDump()).collect(Collectors.toList())));
                } catch (IOException e) {
                    LOGGER.error("Failed to write profiler data");
                    e.printStackTrace();
                }
            }}, "CoreTweaks profiler save thread"));
    }
    
    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if(event.phase == TickEvent.Phase.START) {
            if(profileMode == ProfileMode.PER_FRAME) {
                for(MethodInstrumentationData mid : methodInstrumentationDatas) {
                    writeLine(mid.getFullName() + " calls this frame: " + mid.callsThisFrame);
                    mid.callsThisFrame = 0;
                }
            }
        }
    }
    
    private void writeLine(String str) {
        try {
            out.write(str + "\n");
        } catch (IOException e) {
            LOGGER.error("Error writing to method profiler file writer.");
            e.printStackTrace();
            LOGGER.warn("Terminating method profiling due to error.");
            profileMode = ProfileMode.NONE;
            try {
                out.close();
            } catch (IOException e1) {
                LOGGER.error("Failed to close method profiler file writer.");
                e1.printStackTrace();
            }
        }
    }
    
    public boolean isStarted() {
        return profileMode != ProfileMode.NONE;
    }

    public boolean start(ProfileMode mode) {
        try {
            out = new FileWriter(Util.childFile(CoreTweaks.OUT_DIR, "method-profiler-trace.log"));
            profileMode = mode;
            return true;
        } catch (IOException e) {
            LOGGER.error("Failed to initialize method profiler file writer");
            e.printStackTrace();
        }
        return false;
    }

    public boolean stop() {
        profileMode = ProfileMode.NONE;
        try {
            out.close();
            return true;
        } catch (IOException e) {
            LOGGER.error("Failed to close method profiler file writer");
            e.printStackTrace();
        }
        return false;
    }
    
    public static void preTargetCalled(int id) {
        MethodProfiler.instance.handlePreTargetCalled(id);
    }
    
    public void handlePreTargetCalled(int id) {
        MethodInstrumentationData data = methodInstrumentationDatas.get(id);
        data.calls++;
        
        if(profileMode == ProfileMode.PER_FRAME) {
            data.callsThisFrame++;
        }
    }
    
    public static boolean isActive() {
        return !Config.profilerMethods.isEmpty();
    }
    
    public static class MethodInstrumentationData {
        String owner;
        String name;
        String desc;
        int calls;
        
        int callsThisFrame;
        
        public MethodInstrumentationData(String owner, String name, String desc) {
            this.owner = owner;
            this.name = name;
            this.desc = desc;
        }
        
        public String getDump() {
            return "\"" + owner + ";" + name + desc + "\": {\n" +
                    "  calls: " + calls + "\n"
                    + "}\n";
        }
        
        public String getFullName() {
            return owner + ";" + name + desc;
        }
    }
    
    public static enum ProfileMode {
        NONE, PER_FRAME
    }
    
}
