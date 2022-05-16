package makamys.coretweaks.tweak;

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
            MovingObjectPosition hit = rayTrace(player, 1000);
            int posX = hit.blockX;
            int posY = hit.blockY;
            int posZ = hit.blockZ;
            int var3 = posX + world.rand.nextInt(11) - 5;
            int var4 = posY + world.rand.nextInt(11) - 5;
            int var5 = posZ + world.rand.nextInt(11) - 5;
            world.func_147451_t(var3, var4, var5);
        }
    }
    
    private static MovingObjectPosition rayTrace(EntityLivingBase entity, double reach) {
        Vec3 vec3 = Vec3.createVectorHelper(entity.posX, entity.posY, entity.posZ);
        Vec3 vec31 = entity.getLook(1);
        Vec3 vec32 = vec3.addVector(vec31.xCoord * reach, vec31.yCoord * reach, vec31.zCoord * reach);
        return entity.worldObj.func_147447_a(vec3, vec32, true, false, true);
    }
    
}
