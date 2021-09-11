package makamys.coretweaks.command;

import makamys.coretweaks.FrameProfiler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
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
        if(args.length > 0) {
            if(args[0].equals("frameprofiler")) {
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
                    }
                }
            }
        }
        
        throw new WrongUsageException("coretweaks (frameprofiler (start|stop))", new Object[0]);
    }
    
}
