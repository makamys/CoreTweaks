package makamys.coretweaks.mixin;

import java.io.InputStream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cpw.mods.fml.common.discovery.asm.ASMModParser;
import makamys.coretweaks.ducks.IChunkProviderClient;
import makamys.coretweaks.optimization.ClientChunkMap;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.world.World;

@Mixin(ASMModParser.class)
abstract class MixinASMModParser {
    @Inject(method = "<init>*", at = @At("HEAD"), cancellable = true)
    public void preConstructed(InputStream is, CallbackInfo ci) {
        ci.cancel();
    }
}
