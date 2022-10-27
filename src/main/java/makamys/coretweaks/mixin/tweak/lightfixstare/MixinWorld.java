package makamys.coretweaks.mixin.tweak.lightfixstare;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import makamys.coretweaks.tweak.LightFixStare;
import net.minecraft.world.World;

@Mixin(World.class)
public abstract class MixinWorld {
    
    @Inject(method = "setActivePlayerChunksAndCheckLight", at = @At("TAIL"))
    private void postPlayerCheckLight(CallbackInfo ci) {
        LightFixStare.postPlayerCheckLight((World)(Object)this);
    }
}
