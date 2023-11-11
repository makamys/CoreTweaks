package makamys.coretweaks.mixin.bugfix.intelcolor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.OpenGlHelper;

/** <p>A fix for the Intel rendering issue as originally proposed by TheMasterCaver. Thanks to PheonixVX and TheMasterCaver for finding the reason:
 * <a href="https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/1294926-themastercavers-world?page=13#c294">https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/1294926-themastercavers-world?page=13#c294</a></p>
 * 
 * <p>This fix changes the behavior of all calls to OpenGlHelper#setActiveTexture, so it's somewhat intrusive.</p>
 */
@Mixin(OpenGlHelper.class)
abstract class MixinOpenGlHelper {
    @Inject(method = "setActiveTexture", at = @At(value = "HEAD"))
    private static void setClientActiveTextureToSame(int texture, CallbackInfo ci) {
        OpenGlHelper.setClientActiveTexture(texture);
    }
}
