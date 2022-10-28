package makamys.coretweaks.tweak;

import makamys.coretweaks.Config;

public class FarPlaneDistanceTweaker {
    
    public static float modifyFarPlane(float original) {
        return Math.max(Config.clampFarPlaneDistance_min, original);
    }
    
}

