package martidi.sunnmore.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import martidi.sunnmore.glenn.GlenndermanEntity;

/** Glenn doesn't give off the purple portal particles every Enderman has; he has his own (see GlenndermanEntity). */
@Mixin(Enderman.class)
abstract class EndermanMixin {
	@WrapWithCondition(
		method = "aiStep",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"
		)
	)
	private boolean sunnmore$skipPortalParticlesForGlenn(
		Level level, ParticleOptions particle, double x, double y, double z, double xd, double yd, double zd
	) {
		return !((Object) this instanceof GlenndermanEntity);
	}
}
