package makamys.coretweaks.mixin.bugfix.intelcolor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;

/** <p>A more lightweight fix for the Intel rendering issue. Thanks to PheonixVX and TheMasterCaver for finding the reason:
 * <a href="https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/1294926-themastercavers-world?page=13#c294">https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/1294926-themastercavers-world?page=13#c294</a></p>
 * 
 * <p>The issue appears to be that GL_CLIENT_ACTIVE_TEXTURE has an undefined value since it doesn't get set prior to the glTexCoordPointer call.
 * So we set it to the default tex unit.</p>
 */
// Note for testing this: the issue only happens if the -XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump JVM flag is set.
@Mixin(Tessellator.class)
abstract class MixinTessellator {
    @Inject(method = "draw", at = @At(value = "HEAD"))
    private void setClientActiveTextureToDefault(CallbackInfoReturnable<Integer> cir) {
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
    }
}
