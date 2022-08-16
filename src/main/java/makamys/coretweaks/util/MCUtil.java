package makamys.coretweaks.util;

import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import cpw.mods.fml.client.FMLClientHandler;
import makamys.coretweaks.JVMArgs;
import net.minecraft.client.LoadingScreenRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
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
	
	/** EntityLivingBase#rayTrace but it works on servers. */
    public static MovingObjectPosition rayTrace(EntityLivingBase entity, double reach) {
        Vec3 vec3 = Vec3.createVectorHelper(entity.posX, entity.posY, entity.posZ);
        Vec3 vec31 = entity.getLook(1);
        Vec3 vec32 = vec3.addVector(vec31.xCoord * reach, vec31.yCoord * reach, vec31.zCoord * reach);
        return entity.worldObj.func_147447_a(vec3, vec32, true, false, true);
    }
	
}
