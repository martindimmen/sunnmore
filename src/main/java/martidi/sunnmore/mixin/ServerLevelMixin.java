package martidi.sunnmore.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import martidi.sunnmore.jopa.JopaTravel;

/**
 * Protects the JOPA lobby the way spawn protection does: mayInteract is what vanilla asks before a
 * player breaks a block, places one or empties a bucket.
 */
@Mixin(ServerLevel.class)
abstract class ServerLevelMixin {
	@Inject(method = "mayInteract", at = @At("HEAD"), cancellable = true)
	private void sunnmore$protectJopaLobby(Entity entity, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (JopaTravel.isProtected((ServerLevel) (Object) this, entity, pos)) {
			cir.setReturnValue(false);
		}
	}
}
