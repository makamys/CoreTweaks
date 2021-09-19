package makamys.coretweaks.tweak;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class LightFixStare {
    
    public static void postPlayerCheckLight(World world) {
        if (!world.playerEntities.isEmpty()) {
            int var1 = world.rand.nextInt(world.playerEntities.size());
            EntityPlayer var2 = (EntityPlayer)world.playerEntities.get(var1);
            MovingObjectPosition hit = var2.rayTrace(1000, 0);
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
