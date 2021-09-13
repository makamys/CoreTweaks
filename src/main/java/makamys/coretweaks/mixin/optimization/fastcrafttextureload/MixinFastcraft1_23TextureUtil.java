package makamys.coretweaks.mixin.optimization.fastcrafttextureload;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import makamys.coretweaks.CoreTweaks;

/* According to the javadoc of `targets`, specifying this (public) class via a string is supposed to cause an exception during runtime.
 * It only causes a warning though, and compilation is way easier this way, so *shrug* */
@Mixin(targets = "fastcraft.ab", remap = false)
abstract class MixinFastcraft1_23TextureUtil {
    
    @Redirect(method = "a([[IIIIIZZ)Z", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glGetInteger(I)I"))
    private static int redirectGetInteger(int param) {
        if(CoreTweaks.isStitching && param == GL11.GL_TEXTURE_BINDING_2D) {
            assert GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D) == CoreTweaks.boundTexture;
            return CoreTweaks.boundTexture;
        } else {
            return GL11.glGetInteger(param);
        }
    }
}