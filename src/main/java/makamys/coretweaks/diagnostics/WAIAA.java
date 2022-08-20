package makamys.coretweaks.diagnostics;

import static makamys.coretweaks.command.CoreTweaksCommand.*;
import static net.minecraft.util.EnumChatFormatting.*;

import makamys.coretweaks.command.CoreTweaksCommand;
import makamys.coretweaks.command.ISubCommand;
import makamys.coretweaks.util.MCUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
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
                int rayLength = Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16 * 2;
                MovingObjectPosition posLiquid = MCUtil.rayTrace(elb, rayLength, true);
                MovingObjectPosition posNoLiquid = MCUtil.rayTrace(elb, 100000000, false);
                
                sender.addChatMessage(new ChatComponentText(HELP_EMPHASIS_COLOR + "You are looking at"));
                printRayTraceResult(sender, posLiquid);
                
                if(!posLiquid.toString().equals(posNoLiquid.toString())) {
                    sender.addChatMessage(new ChatComponentText(HELP_EMPHASIS_COLOR + "+ Under liquid:"));
                    printRayTraceResult(sender, posNoLiquid);
                }
            } else {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Caller is a " + sender.getClass() + ", expected an EntityLivingBase subclass."));
            }
        }

        private void printRayTraceResult(ICommandSender sender, MovingObjectPosition pos) {
            Block block = sender.getEntityWorld().getBlock(pos.blockX, pos.blockY, pos.blockZ);
            int chunkX = pos.blockX >> 4;
            int chunkY = pos.blockY >> 4;
            int chunkZ = pos.blockZ >> 4;
            
            sender.addChatMessage(new ChatComponentText("Position: (" + pos.blockX + ", " + pos.blockY + ", " + pos.blockZ + ")"));
            sender.addChatMessage(new ChatComponentText("Subchunk: (" + chunkX + ", " + chunkY + ", " + chunkZ + ") Corner: (" + (chunkX * 16) + ", " + (chunkY * 16) + ", " + (chunkZ * 16) + ")"));
            sender.addChatMessage(new ChatComponentText("Block: " + block.getClass().getName()));       
        }
    }
    
}
