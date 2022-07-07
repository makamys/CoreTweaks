package makamys.coretweaks.mixin.diagnostics.wireframe;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import makamys.coretweaks.Config;
import net.minecraft.client.renderer.EntityRenderer;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
    
    @Inject(method = "renderWorld", at = @At("HEAD"))
    private void preRenderWorld(CallbackInfo ci) {
        if(Config.wireframe) {
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        }
    }
    
    @Inject(method = "renderWorld", at = @At("RETURN"))
    private void postRenderWorld(CallbackInfo ci) {
        if(Config.wireframe) {
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        }
    }
    
}
