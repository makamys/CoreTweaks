package makamys.coretweaks.command;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import makamys.coretweaks.diagnostics.FrameProfiler;
import makamys.coretweaks.diagnostics.MethodProfiler;
import makamys.coretweaks.diagnostics.MethodProfiler.ProfileMode;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class CoreTweaksCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "coretweaks";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "";
    }
    
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        EnumChatFormatting helpColor = EnumChatFormatting.BLUE;
        EnumChatFormatting helpUsageColor = EnumChatFormatting.YELLOW;
        EnumChatFormatting helpEmphasisColor = EnumChatFormatting.DARK_AQUA;
        if(args.length > 0) {
            if(args[0].equals("frameprofiler")) {
                String usage = "coretweaks frameprofiler <start|stop|help>";
                if(args.length == 2) {
                    switch(args[1]) {
                        case "start": {
                            if(FrameProfiler.instance.isStarted()) {
                                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Already started frame profiler"));
                            } else {
                                FrameProfiler.instance.start();
                                sender.addChatMessage(new ChatComponentText("Started frame profiler"));
                            }
                            return;
                        }
                        case "stop": {
                            if(!FrameProfiler.instance.isStarted()) {
                                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Frame profiler is not running"));
                            } else {
                                if(FrameProfiler.instance.stop()) {
                                    sender.addChatMessage(new ChatComponentText("Stopped frame profiler"));
                                } else {
                                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Failed to save frame profiler results, see log for details"));
                                }
                            }
                            
                            return;
                        }
                        case "help": {
                            addColoredChatMessage(sender, "Usage: " + usage, helpUsageColor);
                            addColoredChatMessage(sender, "Creates a report about the timing of various parts of the rendering process.", helpColor);
                            addColoredChatMessage(sender, "The report will be written to " + helpEmphasisColor + "./coretweaks/out/frameprofiler.csv" + helpColor + ".", helpColor);
                            addColoredChatMessage(sender, "A useful script for parsing the results of the report can be found at " + helpEmphasisColor + "https://github.com/makamys/CoreTweaks/tree/master/scripts", helpColor, (msg) -> msg.getChatStyle().setChatClickEvent(new ClickEvent(Action.OPEN_URL, "https://github.com/makamys/CoreTweaks/tree/master/scripts")));
                            return;
                        }
                    }
                }
                throw new WrongUsageException(usage, new Object[0]);
            } else if(args[0].equals("methodprofiler")) {
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
                            addColoredChatMessage(sender, "Usage: " + usage, helpUsageColor);
                            addColoredChatMessage(sender, "Creates a report of how many times per frame the methods specified in the " + helpEmphasisColor + "profilerMethods" + helpColor + " config option were called.", helpColor);
                            return;
                        }
                    }
                }
                throw new WrongUsageException(usage, new Object[0]);
            }
        }
        throw new WrongUsageException("coretweaks <frameprofiler|methodprofiler>", new Object[0]);
    }
    
    private static void addColoredChatMessage(ICommandSender sender, String text, EnumChatFormatting color) {
        addColoredChatMessage(sender, text, color, null);
    }
    
    private static void addColoredChatMessage(ICommandSender sender, String text, EnumChatFormatting color, Consumer<ChatComponentText> fixup) {
        ChatComponentText msg = new ChatComponentText(text);
        msg.getChatStyle().setColor(color);
        if(fixup != null) {
            fixup.accept(msg);
        }
        sender.addChatMessage(msg);
    }
    
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, new String[] {"frameprofiler", "methodprofiler"}) : null;
    }
    
}
