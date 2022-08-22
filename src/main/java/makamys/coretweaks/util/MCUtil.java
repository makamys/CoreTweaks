package makamys.coretweaks.util;

import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import cpw.mods.fml.client.FMLClientHandler;
import makamys.coretweaks.JVMArgs;
import net.minecraft.block.Block;
import net.minecraft.client.LoadingScreenRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.SaveFormatComparator;

public class MCUtil {
	
	public static boolean tryToLoadWorld(String worldName) {
	    Minecraft mc = Minecraft.getMinecraft();
        ISaveFormat saveLoader = mc.getSaveLoader();
        try {
            Optional<SaveFormatComparator> saveOpt;
            
            List<SaveFormatComparator> saveList = (List<SaveFormatComparator>)saveLoader.getSaveList();
            
            if(worldName != null && !worldName.isEmpty()) {
                saveOpt = saveList.stream()
                        .filter(s -> s.getFileName().equals(worldName)).findFirst();
            } else {
                if(saveList != null && !saveList.isEmpty()) {
                    Collections.sort(saveList);
                    saveOpt = Optional.of(saveList.get(0));
                } else {
                    saveOpt = Optional.empty();
                }
            }
            if(saveOpt.isPresent()) {
                SaveFormatComparator save = (SaveFormatComparator)saveOpt.get();
                if(mc.loadingScreen == null) {
                    mc.loadingScreen = new LoadingScreenRenderer(mc);
                }
                FMLClientHandler.instance().tryLoadExistingWorld(null, save.getFileName(), save.getDisplayName());
                return true;
            } else {
                LOGGER.error("Couldn't find a suitable world to load");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load world " + worldName);
            e.printStackTrace();
        }
        return false;
    }
    
    /** EntityLivingBase#rayTrace but it works on servers and there is no cap on range. */
    public static MovingObjectPosition rayTrace(EntityLivingBase entity, double reach, boolean stopOnLiquid) {
        Vec3 vec3 = Vec3.createVectorHelper(entity.posX, entity.posY, entity.posZ);
        Vec3 vec31 = entity.getLook(1);
        Vec3 vec32 = vec3.addVector(vec31.xCoord * reach, vec31.yCoord * reach, vec31.zCoord * reach);
        return rayTrace(entity.worldObj, vec3, vec32, stopOnLiquid, false, true);
    }
    
    /** Changes from World#func_147447_a:
     * - There is no cap on range
     * - It does not step into unloaded chunks */
    public static MovingObjectPosition rayTrace(World dis, Vec3 start, Vec3 end, boolean stopOnLiquid, boolean mysteryFlag1, boolean mysteryFlag2) {
        if (!Double.isNaN(start.xCoord) && !Double.isNaN(start.yCoord) && !Double.isNaN(start.zCoord)) {
            if (!Double.isNaN(end.xCoord) && !Double.isNaN(end.yCoord) && !Double.isNaN(end.zCoord)) {
                int endX = MathHelper.floor_double(end.xCoord);
                int endY = MathHelper.floor_double(end.yCoord);
                int endZ = MathHelper.floor_double(end.zCoord);
                int startX = MathHelper.floor_double(start.xCoord);
                int startY = MathHelper.floor_double(start.yCoord);
                int startZ = MathHelper.floor_double(start.zCoord);
                Block startBlock = dis.getBlock(startX, startY, startZ);
                int startMeta = dis.getBlockMetadata(startX, startY, startZ);

                if ((!mysteryFlag1 || startBlock.getCollisionBoundingBoxFromPool(dis, startX, startY, startZ) != null) && startBlock.canCollideCheck(startMeta, stopOnLiquid)) {
                    MovingObjectPosition movingobjectposition = startBlock.collisionRayTrace(dis, startX, startY, startZ, start, end);

                    if (movingobjectposition != null) {
                        return movingobjectposition;
                    }
                }

                MovingObjectPosition result2 = null;

                // This is the only change from vanilla (originally it's 200).
                int stepsLeft = (int)(Math.abs(end.xCoord) + Math.abs(end.yCoord) + Math.abs(end.zCoord)) * 2;

                while (stepsLeft-- >= 0) {
                    if (Double.isNaN(start.xCoord) || Double.isNaN(start.yCoord) || Double.isNaN(start.zCoord))
                    {
                        return null;
                    }

                    if (startX == endX && startY == endY && startZ == endZ) {
                        return mysteryFlag2 ? result2 : null;
                    }

                    boolean moveX = true;
                    boolean moveY = true;
                    boolean moveZ = true;
                    double newX = 999.0D;
                    double newY = 999.0D;
                    double newZ = 999.0D;

                    if (endX > startX) {
                        newX = (double)startX + 1.0D;
                    } else if (endX < startX) {
                        newX = (double)startX + 0.0D;
                    } else {
                        moveX = false;
                    }

                    if (endY > startY) {
                        newY = (double)startY + 1.0D;
                    } else if (endY < startY) {
                        newY = (double)startY + 0.0D;
                    } else {
                        moveY = false;
                    }

                    if (endZ > startZ) {
                        newZ = (double)startZ + 1.0D;
                    } else if (endZ < startZ) {
                        newZ = (double)startZ + 0.0D;
                    } else {
                        moveZ = false;
                    }

                    double rx = 999.0D;
                    double ry = 999.0D;
                    double rz = 999.0D;
                    double dx = end.xCoord - start.xCoord;
                    double dy = end.yCoord - start.yCoord;
                    double dz = end.zCoord - start.zCoord;

                    if (moveX) {
                        rx = (newX - start.xCoord) / dx;
                    }

                    if (moveY) {
                        ry = (newY - start.yCoord) / dy;
                    }

                    if (moveZ) {
                        rz = (newZ - start.zCoord) / dz;
                    }

                    boolean unusedFlag = false;
                    byte dir;

                    if (rx < ry && rx < rz) {
                        if (endX > startX) {
                            dir = 4;
                        } else {
                            dir = 5;
                        }

                        start.xCoord = newX;
                        start.yCoord += dy * rx;
                        start.zCoord += dz * rx;
                    } else if (ry < rz) {
                        if (endY > startY) {
                            dir = 0;
                        } else {
                            dir = 1;
                        }

                        start.xCoord += dx * ry;
                        start.yCoord = newY;
                        start.zCoord += dz * ry;
                    } else {
                        if (endZ > startZ) {
                            dir = 2;
                        } else {
                            dir = 3;
                        }

                        start.xCoord += dx * rz;
                        start.yCoord += dy * rz;
                        start.zCoord = newZ;
                    }

                    Vec3 vec32 = Vec3.createVectorHelper(start.xCoord, start.yCoord, start.zCoord);
                    startX = (int)(vec32.xCoord = (double)MathHelper.floor_double(start.xCoord));

                    if (dir == 5) {
                        --startX;
                        ++vec32.xCoord;
                    }

                    startY = (int)(vec32.yCoord = (double)MathHelper.floor_double(start.yCoord));

                    if (dir == 1) {
                        --startY;
                        ++vec32.yCoord;
                    }

                    startZ = (int)(vec32.zCoord = (double)MathHelper.floor_double(start.zCoord));

                    if (dir == 3) {
                        --startZ;
                        ++vec32.zCoord;
                    }
                    
                    if(!dis.blockExists(startX, startY, startZ)) {
                        break;
                    }
                    
                    Block newStartBlock = dis.getBlock(startX, startY, startZ);
                    int newStartMeta = dis.getBlockMetadata(startX, startY, startZ);

                    if (!mysteryFlag1 || newStartBlock.getCollisionBoundingBoxFromPool(dis, startX, startY, startZ) != null) {
                        if (newStartBlock.canCollideCheck(newStartMeta, stopOnLiquid)) {
                            MovingObjectPosition result1 = newStartBlock.collisionRayTrace(dis, startX, startY, startZ, start, end);

                            if (result1 != null) {
                                return result1;
                            }
                        } else {
                            result2 = new MovingObjectPosition(startX, startY, startZ, dir, start, false);
                        }
                    }
                }
                return mysteryFlag2 ? result2 : null;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
    
    public static MovingObjectPosition rayTrace(EntityLivingBase entity, double reach) {
        return rayTrace(entity, reach, true);
    }
	
}
