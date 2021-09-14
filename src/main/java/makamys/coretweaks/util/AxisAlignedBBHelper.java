package makamys.coretweaks.util;

import net.minecraft.util.AxisAlignedBB;

public class AxisAlignedBBHelper {
	
	public static AxisAlignedBB ensureNonNegativeDimensions(AxisAlignedBB dis) {
		if(dis.minX > dis.maxX) {
			dis.minX = dis.maxX = (dis.maxX + dis.minX) / 2.0; 
		}
		if(dis.minY > dis.maxY) {
			dis.minY = dis.maxY = (dis.maxY + dis.minY) / 2.0; 
		}
		if(dis.minZ > dis.maxZ) {
			dis.minZ = dis.maxZ = (dis.maxZ + dis.minZ) / 2.0; 
		}
		
		return dis;
	}
	
}
