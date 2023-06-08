package arekkuusu.betterhurttimer.mixin;

import arekkuusu.betterhurttimer.BHTConfig;
import arekkuusu.betterhurttimer.api.capability.Hurt;
import arekkuusu.betterhurttimer.api.capability.HurtProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.LazyOptional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class DamageArmorMixin {

    //Forge Compliant
    @Redirect(method = "hurt", at = @At(target = "Lnet/minecraft/world/entity/LivingEntity;hurtCurrentlyUsedShield(F)V", value = "INVOKE"), require = 0)
    public void damageShield(LivingEntity entity, float damage) {
        LazyOptional<Hurt> optional = HurtProvider.hurt(entity);
        if (optional.isPresent()) {
            Hurt capability = optional.orElseThrow(UnsupportedOperationException::new);
            if (capability.getTicksToShieldDamage() > 0) {
                if (Double.compare(Math.max(0, capability.getLastShieldDamage() + BHTConfig.Runtime.DamageFrames.nextAttackDamageDifference), damage) < 0) {
                    hurtCurrentlyUsedShield((float) (damage - capability.getLastShieldDamage()));
                    capability.setLastShieldDamage((int) damage);
                }
            } else {
                hurtCurrentlyUsedShield(damage);
                capability.setLastShieldDamage((int) damage);
                capability.setTicksToShieldDamage(BHTConfig.Runtime.DamageFrames.shieldResistantTime);
            }
        } else {
            hurtCurrentlyUsedShield(damage);
        }
    }

    @Redirect(method = "getDamageAfterArmorAbsorb", at = @At(target = "Lnet/minecraft/world/entity/LivingEntity;hurtArmor(Lnet/minecraft/world/damagesource/DamageSource;F)V", value = "INVOKE"), require = 0)
    public void damageArmor(LivingEntity entity, DamageSource source, float damage) {
        LazyOptional<Hurt> optional = HurtProvider.hurt(entity);
        if (optional.isPresent()) {
            Hurt capability = optional.orElseThrow(UnsupportedOperationException::new);
            if (capability.getTicksToArmorDamage() > 0) {
                if (Double.compare(Math.max(0, capability.getLastArmorDamage() + BHTConfig.Runtime.DamageFrames.nextAttackDamageDifference), damage) < 0) {
                    hurtArmor(source, (float) (damage - capability.getLastArmorDamage()));
                    capability.setLastArmorDamage((int) damage);
                }
            } else {
                hurtArmor(source, damage);
                capability.setLastArmorDamage((int) damage);
                capability.setTicksToArmorDamage(BHTConfig.Runtime.DamageFrames.armorResistantTime);
            }
        } else {
            hurtArmor(source, damage);
        }
    }
    //Forge Compliant

    @Shadow
    protected abstract void hurtArmor(DamageSource arg, float f);

    @Shadow
    protected abstract void hurtCurrentlyUsedShield(float f);
}
