package makamys.coretweaks.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import makamys.coretweaks.Util;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.EntityLivingBase;

@Mixin(RenderGlobal.class)
abstract class MixinRenderGlobal {

    @Inject(method = "updateRenderers", at = @At("RETURN"), cancellable = true)
    public void postUpdateRenderers(EntityLivingBase elb, boolean bool, CallbackInfoReturnable<Boolean> cir) {
    	if(Util.isOptifinePresent()) {
    		// OptiFine has it backwards for some reason
    		cir.setReturnValue(!cir.getReturnValueZ());
    	}
    }
    
}
