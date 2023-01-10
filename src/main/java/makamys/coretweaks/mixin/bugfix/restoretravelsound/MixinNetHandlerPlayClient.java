package makamys.coretweaks.mixin.bugfix.restoretravelsound;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.util.ResourceLocation;

@Mixin(NetHandlerPlayClient.class)
abstract class MixinNetHandlerPlayClient implements INetHandlerPlayClient {
    
    @Shadow
    private Random avRandomizer;
    @Shadow
    private Minecraft gameController;
    
    private boolean soundPending = false;
    
    @Inject(method = "handleRespawn", at = @At("HEAD"))
    public void preHandleRespawn(S07PacketRespawn packet, CallbackInfo ci) {
        int dimension = packet.func_149082_c();
        if (dimension != gameController.thePlayer.dimension && !gameController.thePlayer.isDead) {
            soundPending = true;
        }
    }
    
    @Inject(method = "handleRespawn", at = @At("RETURN"))
    public void postHandleRespawn(S07PacketRespawn packet, CallbackInfo ci) {
        if(soundPending) {
            gameController.getSoundHandler().playSound(
                    PositionedSoundRecord.func_147674_a(new ResourceLocation("portal.travel"), avRandomizer.nextFloat() * 0.4F + 0.8F));
            soundPending = false;
        }
    }
}
