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
	
	private static Field ofCloudsHeight_F;
	public static Object Reflector_ForgeBlock_hasTileEntity;
	public static Object Reflector_ForgeBlock_canRenderInPass;
	
	static {
		try {
			ofCloudsHeight_F = GameSettings.class.getDeclaredField("ofCloudsHeight");
			LOGGER.info("Found ofCloudsHeight field, assuming OptiFine is present");
			
			Class<?> reflector = Launch.classLoader.findClass("Reflector");
			Field hasTileEntityF = reflector.getField("ForgeBlock_hasTileEntity");
			Reflector_ForgeBlock_hasTileEntity = hasTileEntityF.get(null);
			
			Field canRenderInPassF = reflector.getField("ForgeBlock_canRenderInPass");
			Reflector_ForgeBlock_canRenderInPass = canRenderInPassF.get(null);
			
			LOGGER.info("Found OptiFine fields, assuming OptiFine is present");
		} catch (NoSuchFieldException | SecurityException | ClassNotFoundException | IllegalArgumentException | IllegalAccessException e) {
			LOGGER.info("Couldn't find OptiFine fields (" + e.getMessage() + "), assuming OptiFine is not present");
		} 
	}
	
	public static float getOfCloudsHeight() {
		if(ofCloudsHeight_F != null) {
			try {
				return (float)ofCloudsHeight_F.get(Minecraft.getMinecraft().gameSettings);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return 0;
	}
	
	public static boolean isOptifinePresent() {
		return ofCloudsHeight_F != null;
	}
	
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
    
    /** World#func_147447_a but there is no cap on range. */
    public static MovingObjectPosition rayTrace(World dis, Vec3 origin, Vec3 ray, boolean stopOnLiquid, boolean mysteryFlag1, boolean mysteryFlag2) {
        if (!Double.isNaN(origin.xCoord) && !Double.isNaN(origin.yCoord) && !Double.isNaN(origin.zCoord)) {
            if (!Double.isNaN(ray.xCoord) && !Double.isNaN(ray.yCoord) && !Double.isNaN(ray.zCoord)) {
                int i = MathHelper.floor_double(ray.xCoord);
                int j = MathHelper.floor_double(ray.yCoord);
                int k = MathHelper.floor_double(ray.zCoord);
                int l = MathHelper.floor_double(origin.xCoord);
                int i1 = MathHelper.floor_double(origin.yCoord);
                int j1 = MathHelper.floor_double(origin.zCoord);
                Block block = dis.getBlock(l, i1, j1);
                int k1 = dis.getBlockMetadata(l, i1, j1);

                if ((!mysteryFlag1 || block.getCollisionBoundingBoxFromPool(dis, l, i1, j1) != null) && block.canCollideCheck(k1, stopOnLiquid)) {
                    MovingObjectPosition movingobjectposition = block.collisionRayTrace(dis, l, i1, j1, origin, ray);

                    if (movingobjectposition != null) {
                        return movingobjectposition;
                    }
                }

                MovingObjectPosition movingobjectposition2 = null;

                k1 = (int)(Math.abs(ray.xCoord) + Math.abs(ray.yCoord) + Math.abs(ray.zCoord)) * 2;

                while (k1-- >= 0) {
                    if (Double.isNaN(origin.xCoord) || Double.isNaN(origin.yCoord) || Double.isNaN(origin.zCoord))
                    {
                        return null;
                    }

                    if (l == i && i1 == j && j1 == k) {
                        return mysteryFlag2 ? movingobjectposition2 : null;
                    }

                    boolean flag6 = true;
                    boolean flag3 = true;
                    boolean flag4 = true;
                    double d0 = 999.0D;
                    double d1 = 999.0D;
                    double d2 = 999.0D;

                    if (i > l) {
                        d0 = (double)l + 1.0D;
                    } else if (i < l) {
                        d0 = (double)l + 0.0D;
                    } else {
                        flag6 = false;
                    }

                    if (j > i1) {
                        d1 = (double)i1 + 1.0D;
                    } else if (j < i1) {
                        d1 = (double)i1 + 0.0D;
                    } else {
                        flag3 = false;
                    }

                    if (k > j1) {
                        d2 = (double)j1 + 1.0D;
                    } else if (k < j1) {
                        d2 = (double)j1 + 0.0D;
                    } else {
                        flag4 = false;
                    }

                    double d3 = 999.0D;
                    double d4 = 999.0D;
                    double d5 = 999.0D;
                    double d6 = ray.xCoord - origin.xCoord;
                    double d7 = ray.yCoord - origin.yCoord;
                    double d8 = ray.zCoord - origin.zCoord;

                    if (flag6) {
                        d3 = (d0 - origin.xCoord) / d6;
                    }

                    if (flag3) {
                        d4 = (d1 - origin.yCoord) / d7;
                    }

                    if (flag4) {
                        d5 = (d2 - origin.zCoord) / d8;
                    }

                    boolean flag5 = false;
                    byte b0;

                    if (d3 < d4 && d3 < d5) {
                        if (i > l) {
                            b0 = 4;
                        } else {
                            b0 = 5;
                        }

                        origin.xCoord = d0;
                        origin.yCoord += d7 * d3;
                        origin.zCoord += d8 * d3;
                    } else if (d4 < d5) {
                        if (j > i1) {
                            b0 = 0;
                        } else {
                            b0 = 1;
                        }

                        origin.xCoord += d6 * d4;
                        origin.yCoord = d1;
                        origin.zCoord += d8 * d4;
                    } else {
                        if (k > j1) {
                            b0 = 2;
                        } else {
                            b0 = 3;
                        }

                        origin.xCoord += d6 * d5;
                        origin.yCoord += d7 * d5;
                        origin.zCoord = d2;
                    }

                    Vec3 vec32 = Vec3.createVectorHelper(origin.xCoord, origin.yCoord, origin.zCoord);
                    l = (int)(vec32.xCoord = (double)MathHelper.floor_double(origin.xCoord));

                    if (b0 == 5) {
                        --l;
                        ++vec32.xCoord;
                    }

                    i1 = (int)(vec32.yCoord = (double)MathHelper.floor_double(origin.yCoord));

                    if (b0 == 1) {
                        --i1;
                        ++vec32.yCoord;
                    }

                    j1 = (int)(vec32.zCoord = (double)MathHelper.floor_double(origin.zCoord));

                    if (b0 == 3) {
                        --j1;
                        ++vec32.zCoord;
                    }

                    Block block1 = dis.getBlock(l, i1, j1);
                    int l1 = dis.getBlockMetadata(l, i1, j1);

                    if (!mysteryFlag1 || block1.getCollisionBoundingBoxFromPool(dis, l, i1, j1) != null) {
                        if (block1.canCollideCheck(l1, stopOnLiquid)) {
                            MovingObjectPosition movingobjectposition1 = block1.collisionRayTrace(dis, l, i1, j1, origin, ray);

                            if (movingobjectposition1 != null) {
                                return movingobjectposition1;
                            }
                        } else {
                            movingobjectposition2 = new MovingObjectPosition(l, i1, j1, b0, origin, false);
                        }
                    }
                }
                return mysteryFlag2 ? movingobjectposition2 : null;
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
