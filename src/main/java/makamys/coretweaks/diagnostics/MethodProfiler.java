package makamys.coretweaks.diagnostics;

import static makamys.coretweaks.CoreTweaks.LOGGER;
import static makamys.coretweaks.command.CoreTweaksCommand.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import makamys.coretweaks.Config;
import makamys.coretweaks.CoreTweaks;
import makamys.coretweaks.command.CoreTweaksCommand;
import makamys.coretweaks.command.ISubCommand;
import makamys.coretweaks.util.Util;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class MethodProfiler {
    
    public static MethodProfiler instance = new MethodProfiler();
    
    public Map<String, List<String>> targets = new HashMap<>();
    public List<MethodInstrumentationData> methodInstrumentationDatas = new ArrayList<>();
    
    private ProfileMode profileMode = ProfileMode.NONE;
    private Writer out;
    
    public void init() {
        if(!isActive()) return;
        
        CoreTweaksCommand.registerSubCommand("methodprofiler", new MethodProfilerSubCommand());
        
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
                    FileUtils.write(Util.childFile(CoreTweaks.OUT_DIR, "method-profiler.json"), String.join("\n", methodInstrumentationDatas.stream().filter(d -> !d.disabled).map(d -> d.getDump()).collect(Collectors.toList())));
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
                    if(!mid.disabled) {
                        writeLine(mid.getFullName() + " calls this frame: " + mid.callsThisFrame);
                        mid.callsThisFrame = 0;
                    }
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
        if(!data.disabled) {
            data.calls++;
            
            if(profileMode == ProfileMode.PER_FRAME) {
                data.callsThisFrame++;
            }
        }
    }
    
    public void clearDatasForClass(String transformedName) {
        String internalName = transformedName.replaceAll("\\.", "/");
        for(MethodInstrumentationData mid : methodInstrumentationDatas) {
            if(mid.owner.equals(internalName)) {
                mid.disabled = true;
            }
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
        
        boolean disabled;
        
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
    
    private static class MethodProfilerSubCommand implements ISubCommand {
        
        @Override
        public void processCommand(ICommandSender sender, String[] args) {
            String usage = "coretweaks methodprofiler <start|stop|help>";
            if(args.length >= 2) {
                switch(args[1]) {
                    case "start": {
                        if(MethodProfiler.instance.isStarted()) {
                            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Already started method profiler"));
                        } else {
                            List<String> validValues = Arrays.stream(MethodProfiler.ProfileMode.values()).map(v -> v.name())
                                    .collect(Collectors.toList());
                            if(args.length == 3) {
                                String modeStr = args[2];
                                
                                if(validValues.contains(modeStr.toUpperCase())) {
                                    MethodProfiler.ProfileMode mode = MethodProfiler.ProfileMode.valueOf(modeStr.toUpperCase());
                                    if(mode != ProfileMode.NONE) {
                                        if(MethodProfiler.instance.start(mode)) {
                                            sender.addChatMessage(new ChatComponentText("Started method profiler"));
                                        } else {
                                            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Failed to start method profiler, see log for details"));
                                        }
                                    }
                                } else {
                                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Invalid mode: " + modeStr));
                                }
                                
                            } else {
                                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Need a mode. Valid ones are: " + validValues.stream().filter(v -> !v.equals(MethodProfiler.ProfileMode.NONE.name())).collect(Collectors.toList())));
                                throw new WrongUsageException("coretweaks methodprofiler start <mode>", new Object[0]);
                            }
                        }
                        return;
                    }
                    case "stop": {
                        if(!MethodProfiler.instance.isStarted()) {
                            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Method profiler is not running"));
                        } else {
                            if(MethodProfiler.instance.stop()) {
                                sender.addChatMessage(new ChatComponentText("Stopped method profiler"));
                            } else {
                                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Failed to save method profiler results, see log for details"));
                            }
                        }
                        
                        return;
                    }
                    case "help": {
                        addColoredChatMessage(sender, "Usage: " + usage, HELP_USAGE_COLOR);
                        addColoredChatMessage(sender, "Creates a report of how many times per frame the methods specified in the " + HELP_EMPHASIS_COLOR + "profilerMethods" + HELP_COLOR + " config option were called.", HELP_COLOR);
                        return;
                    }
                }
            }
            throw new WrongUsageException(usage, new Object[0]);
        }
    }
}
