package makamys.coretweaks.tweak;

import makamys.coretweaks.util.MCUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class LightFixStare {
    
    public static void postPlayerCheckLight(World world) {
        if (!world.playerEntities.isEmpty()) {
            int playerIndex = world.rand.nextInt(world.playerEntities.size());
            EntityPlayer player = (EntityPlayer)world.playerEntities.get(playerIndex);
            MovingObjectPosition hit = MCUtil.rayTrace(player, 1000);
            int posX = hit.blockX;
            int posY = hit.blockY;
            int posZ = hit.blockZ;
            int var3 = posX + world.rand.nextInt(11) - 5;
            int var4 = posY + world.rand.nextInt(11) - 5;
            int var5 = posZ + world.rand.nextInt(11) - 5;
            world.func_147451_t(var3, var4, var5);
        }
    }
    
}
