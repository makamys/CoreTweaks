package makamys.coretweaks.mixin.bugfix.smallentityswim;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import makamys.coretweaks.util.AxisAlignedBBHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;

@Mixin(Entity.class)
public class MixinEntity {
	
	@Redirect(method = "handleWaterMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/AxisAlignedBB;contract(DDD)Lnet/minecraft/util/AxisAlignedBB;"))
	public AxisAlignedBB redirectContract(AxisAlignedBB dis, double dx, double dy, double dz) {
		return AxisAlignedBBHelper.ensureNonNegativeDimensions(dis.contract(dx, dy, dz));
	}
}
