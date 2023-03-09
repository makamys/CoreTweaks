package makamys.coretweaks.ducks.bugfix;

import net.minecraft.world.World;

public interface IForge5160Entity {
    
    public boolean crtw$isAddedToWorld(World world);

    public void crtw$onAddedToWorld(World world);

    public void crtw$onRemovedFromWorld(World world);
    
}
