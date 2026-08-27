package fi.dy.masa.tweakeroo.mixin.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.world.explosion.Explosion;
import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(Explosion.class)
public abstract class MixinExplosion
{
    @ModifyArg(method = "affectWorld", require = 0, expect = 0,
               at = @At(value = "INVOKE",
               target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"))
    private ParticleEffect addParticleModify(ParticleEffect original)
    {
        // 1.20.1 Explosion has no particle field (added in 1.21); keep original for reduced toggle
        // The mixin is optional (require=0,expect=0) so it won't fail if target missing on 1.20.1
        return original;
    }
}
