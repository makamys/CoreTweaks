package makamys.coretweaks.util;

import java.io.File;
import java.lang.reflect.Field;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.launchwrapper.Launch;

public class Util {
	
	private static Field ofCloudsHeight_F;
	public static Object Reflector_ForgeBlock_hasTileEntity;
	public static Object Reflector_ForgeBlock_canRenderInPass;
	
	static {
		try {
			ofCloudsHeight_F = GameSettings.class.getDeclaredField("ofCloudsHeight");
			System.out.println("Found ofCloudsHeight field, assuming OptiFine is present");
			
			Class<?> reflector = Launch.classLoader.findClass("Reflector");
			Field hasTileEntityF = reflector.getField("ForgeBlock_hasTileEntity");
			Reflector_ForgeBlock_hasTileEntity = hasTileEntityF.get(null);
			
			Field canRenderInPassF = reflector.getField("ForgeBlock_canRenderInPass");
			Reflector_ForgeBlock_canRenderInPass = canRenderInPassF.get(null);
			
			System.out.println("Found OptiFine fields, assuming OptiFine is present");
		} catch (NoSuchFieldException | SecurityException | ClassNotFoundException | IllegalArgumentException | IllegalAccessException e) {
			System.out.println("Couldn't find OptiFine fields (" + e.getMessage() + "), assuming OptiFine is not present");
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
	
	public static File childFile(File parent, String childName) {
	    parent.mkdirs();
	    return new File(parent, childName);
	}
	
}
