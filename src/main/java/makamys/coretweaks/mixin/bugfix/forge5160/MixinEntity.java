package makamys.coretweaks.mixin.bugfix.forge5160;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import makamys.coretweaks.ducks.bugfix.IForge5160Entity;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import static makamys.coretweaks.CoreTweaks.LOGGER;

// Adapted from https://github.com/Itaros/minecraft-backport5160/blob/master/src/main/java/ru/itaros/backport5160/Forge5160Transformer.java

/**
 * TODO LIST:
 * 1) Wut? https://github.com/MinecraftForge/MinecraftForge/pull/5160/files#diff-66531a6c0289a9654ce522f047a8827eR78
 * 2) Entity LeashKnot
 * 3) Entity Shulker//DO NOT EXISTS APPARENTLY LOL
 * 4) NetHandlerPlayServer - resync player
 * 5) Wut? https://github.com/MinecraftForge/MinecraftForge/pull/5160/files#diff-3d0fbbd4fa45e8003e514fcac5f2f148R55
 * 6) Wut? MC-117412? https://github.com/MinecraftForge/MinecraftForge/pull/5160/files#diff-496aa22ea49bec02cab69d859142959dR327
 * 7) There is some magic with perWorldStorage = new MapStorage((ISaveHandler)null);. Wut?? https://github.com/MinecraftForge/MinecraftForge/pull/5160/files#diff-0facadd57a1c485eed926033d315a5f0R45
 * 8) Wut? https://github.com/MinecraftForge/MinecraftForge/pull/5160/files#diff-0facadd57a1c485eed926033d315a5f0R268
 */

@Mixin(Entity.class)
public class MixinEntity implements IForge5160Entity {
    
    private boolean crtw$isAddedToWorld;
    private int crtw$worldId;
    
    @Shadow
    public World worldObj;
    @Shadow
    public double posX;
    @Shadow
    public double posZ;
    
    // Here we FORCE game to reregister entity on EVERY move. This ensures it will not get lost(wrong chunk assignment) when moving through chunk boundary
    // Origin: https://github.com/PaperMC/Paper/blob/fd1bd5223a461b6d98280bb8f2d67280a30dd24a/Spigot-Server-Patches/0315-Always-process-chunk-registration-after-moving.patch
    // TODO(makamys): calling this on every field access seems redundant, why not just do it after PUTFIELD?
    // TODO CONVENIENCE: Actually find coordinate setter triplet and insert after to avoid inconsistancies
    // Inserting after posZ assignment(as we called super before)
    @Inject(method = {"setPosition", "moveEntity"}, at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;posZ:D", shift = Shift.AFTER))
    public void onPositionChanged1(CallbackInfo ci) {
        // Exclude update if isAddedToWorld evaluates to false
        // Exclude update if world.isRemote evaluates to true
        if (this.worldObj != null && !this.worldObj.isRemote && crtw$isAddedToWorld(this.worldObj)) {
            // Forces world to recognize the entity on each movement. EWH!!!
            this.worldObj.updateEntityWithOptionalForce((Entity)(Object)this, false);
            // Thank gods it returns nothing
        }
    }
    
    // We need to insert critical accessor call to trick game to load a chunk and then discard
    @Inject(method = "setPositionAndRotation", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setPosition(DDD)V"))
    public void onPositionChanged2(CallbackInfo ci) {
        // Exclude update if world.isRemote evaluates to true
        if (this.worldObj != null && !this.worldObj.isRemote) {
            // CALL FFS!
            // TODO: I am not sure this is correct method, but judging by 4 shift it should be
            this.worldObj.getChunkFromChunkCoords(
                    (int)Math.floor(this.posX) >> 4,
                    (int)Math.floor(this.posZ) >> 4
            );
           // Ignore result. Here we are just teasing the game to force chunkload
        }
    }
    
    // Adding tracking monitoring facilities
    // Here we implement isAddedToWorld accessor to get current status of world presence tracking
    // CoreTweaks: We also make sure the dimension ID matches
    public boolean crtw$isAddedToWorld(World world) {
        return this.crtw$isAddedToWorld && this.crtw$worldId == getWorldId(world);
    }

    // Here we implement method used to set tracking flag
    public void crtw$onAddedToWorld(World world) {
        this.crtw$isAddedToWorld = true;
        this.crtw$worldId = getWorldId(world);
    }

    // Here we implement method used to unset tracking flag
    public void crtw$onRemovedFromWorld(World world) {
        if(getWorldId(world) == this.crtw$worldId) {
            this.crtw$isAddedToWorld = false;
        } else {
            // Some mods might do "1. add to new world; 2. remove from old world" instead of "1. remove from old world; 2. add to new world" 
            LOGGER.debug("Will not track removal from invalid world. Entity is in dimension " + this.crtw$worldId + ", and it's being removed from dimension " + getWorldId(world));
        }
    }
    
    @Unique
    private int getWorldId(World world) {
        return world.provider.dimensionId;
    }
    
}
