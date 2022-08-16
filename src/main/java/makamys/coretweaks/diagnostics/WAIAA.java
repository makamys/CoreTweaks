package makamys.coretweaks.diagnostics;

import static makamys.coretweaks.command.CoreTweaksCommand.*;
import static net.minecraft.util.EnumChatFormatting.*;

import makamys.coretweaks.command.CoreTweaksCommand;
import makamys.coretweaks.command.ISubCommand;
import makamys.coretweaks.util.MCUtil;
import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;

public class WAIAA {
    
    public static WAIAA instance;
    
    public WAIAA() {
        CoreTweaksCommand.registerSubCommand("waiaa", new WAIAASubCommand());
    }
    
    private static class WAIAASubCommand implements ISubCommand {
        
        @Override
        public void processCommand(ICommandSender sender, String[] args) {
            if(sender instanceof EntityLivingBase) {
                EntityLivingBase elb = (EntityLivingBase)sender;
                // Vanilla's ray trace method is limited at 200 steps...
                MovingObjectPosition pos = MCUtil.rayTrace(elb, 100000000);
                Block block = sender.getEntityWorld().getBlock(pos.blockX, pos.blockY, pos.blockZ);
                sender.addChatMessage(new ChatComponentText(HELP_EMPHASIS_COLOR + "You are looking at"));
                sender.addChatMessage(new ChatComponentText("" + HELP_USAGE_COLOR + "Position: " + HELP_COLOR + BOLD + pos.blockX + ", " + pos.blockY + ", " + pos.blockZ));
                sender.addChatMessage(new ChatComponentText("" + HELP_USAGE_COLOR + "Block: " + HELP_COLOR + BOLD + block.getClass().getName()));
            } else {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Caller is a " + sender.getClass() + ", expected an EntityLivingBase subclass."));
            }
        }
    }
    
}
