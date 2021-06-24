package makamys.toomanycrashes;

import java.lang.reflect.Field;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;

public class Util {
	
	private static Field ofCloudsHeight_F;
	
	static {
		try {
			ofCloudsHeight_F = GameSettings.class.getDeclaredField("ofCloudsHeight");
			System.out.println("Found ofCloudsHeight field, assuming OptiFine is present");
		} catch (NoSuchFieldException | SecurityException e) {
			System.out.println("Couldn't find ofCloudsHeight field (" + e.getMessage() + "), assuming OptiFine is not present");
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
	
}
