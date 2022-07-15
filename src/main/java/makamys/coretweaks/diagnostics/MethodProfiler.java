package makamys.coretweaks.diagnostics;

import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import makamys.coretweaks.Config;
import makamys.coretweaks.CoreTweaks;
import makamys.coretweaks.util.Util;

public class MethodProfiler {
    
    public static MethodProfiler instance = new MethodProfiler();
    
    public Map<String, List<String>> targets = new HashMap<>();
    public List<MethodInstrumentationData> methodInstrumentationDatas = new ArrayList<>();
    
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
    
    public static void preTargetCalled(int id) {
        MethodInstrumentationData data = instance.methodInstrumentationDatas.get(id);
        data.calls++;
    }
    
    public static boolean isActive() {
        return !Config.profilerMethods.isEmpty();
    }
    
    public static class MethodInstrumentationData {
        String owner;
        String name;
        String desc;
        int calls;
        
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
    }
    
}
