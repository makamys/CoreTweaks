package makamys.coretweaks.mixin.optimization.tcpnodelay;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import io.netty.channel.ChannelInitializer;

@Mixin(targets = {"net.minecraft.network.NetworkSystem$1", "net.minecraft.network.NetworkManager$2", "net.minecraft.client.network.OldServerPinger$2"}, remap = false)
public abstract class MixinChannelInitializers extends ChannelInitializer {
    
    @ModifyArg(method = "initChannel", at = @At(value = "INVOKE", target = "Ljava/lang/Boolean;valueOf(Z)Ljava/lang/Boolean;"))
    private boolean setTCPNoDelay(boolean original) {
        return true;
    }
    
    
}
