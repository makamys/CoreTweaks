package makamys.coretweaks.mixin.tweak.ofupdaterenderersreturn;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import makamys.coretweaks.util.OFUtil;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.EntityLivingBase;

@Mixin(RenderGlobal.class)
abstract class MixinRenderGlobal {

    @Inject(method = "updateRenderers", at = @At("RETURN"), cancellable = true)
    public void postUpdateRenderers(EntityLivingBase elb, boolean bool, CallbackInfoReturnable<Boolean> cir) {
        if(OFUtil.isOptifinePresent()) {
            // OptiFine has it backwards for some reason
            cir.setReturnValue(!cir.getReturnValueZ());
        }
    }
    
}
