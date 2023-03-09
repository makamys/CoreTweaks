package makamys.coretweaks.mixin.bugfix.forge5160;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import makamys.coretweaks.ducks.bugfix.IForge5160Entity;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

@Mixin(World.class)
public class MixinWorld {
    
    // Here we mark Entity as tracked so when it moves across chunk border it could let it be notified
    @Inject(method = "onEntityAdded", at = @At("RETURN"))
    private void postEntityAdded(Entity entity, CallbackInfo ci) {
        ((IForge5160Entity)entity).crtw$onAddedToWorld((World)(Object)this);
    }
    
    // Here we unmark Entity as tracked so when it moves across chunk border it would be ignored
    @Inject(method = "onEntityRemoved", at = @At("RETURN"))
    private void postEntityRemoved(Entity entity, CallbackInfo ci) {
        ((IForge5160Entity)entity).crtw$onRemovedFromWorld((World)(Object)this);
    }
    
}
