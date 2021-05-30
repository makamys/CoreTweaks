package makamys.toomanycrashes.command;

import makamys.toomanycrashes.FrameTimer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class TMCCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "tmc";
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
            if(args[0].equals("frametimer")) {
                if(args.length == 2) {
                    switch(args[1]) {
                        case "start": {
                            if(FrameTimer.instance.isStarted()) {
                                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Already started frame timer"));
                            } else {
                                FrameTimer.instance.start();
                                sender.addChatMessage(new ChatComponentText("Started frame timer"));
                            }
                            return;
                        }
                        case "stop": {
                            if(!FrameTimer.instance.isStarted()) {
                                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Frame timer is not running"));
                            } else {
                                if(FrameTimer.instance.stop()) {
                                    sender.addChatMessage(new ChatComponentText("Stopped frame timer"));
                                } else {
                                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Failed to save frame times, see log for details"));
                                }
                            }
                            
                            return;
                        }
                    }
                }
            }
        }
        
        throw new WrongUsageException("tmc (frametimer (start|stop))", new Object[0]);
    }
    
}
