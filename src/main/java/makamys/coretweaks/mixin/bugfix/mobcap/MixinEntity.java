package makamys.coretweaks.mixin.bugfix.mobcap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import makamys.coretweaks.bugfix.MobCapHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;

@Mixin(Entity.class)
public class MixinEntity {

    /** 
     * @author makamys
     * @reason Checking class hierarchy doesn't reliably identify the type of modded mobs.
     * Check what type they registered their spawn with instead, so they count towards the same mob cap type as the type of the spawn cycle during which they spawned.
     */
    @Overwrite(remap = false)
    public boolean isCreatureType(EnumCreatureType type, boolean forSpawnCount) {
        return MobCapHandler.isCreatureType((Entity)(Object)this, type);
    }
}
