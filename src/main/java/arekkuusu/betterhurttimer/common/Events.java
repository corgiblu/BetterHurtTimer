package arekkuusu.betterhurttimer.common;

import arekkuusu.betterhurttimer.BHT;
import arekkuusu.betterhurttimer.BHTConfig;
import arekkuusu.betterhurttimer.api.BHTAPI;
import arekkuusu.betterhurttimer.api.capability.data.AttackInfo;
import arekkuusu.betterhurttimer.api.capability.Capabilities;
import arekkuusu.betterhurttimer.api.capability.data.HurtSourceInfo.HurtSourceData;
import arekkuusu.betterhurttimer.api.event.PreLivingAttackEvent;
import arekkuusu.betterhurttimer.api.event.PreLivingKnockBackEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = BHT.MOD_ID)
public class Events {

    public static boolean onAttackEntityOverride = true;
    public static int maxHurtResistantTime = 20;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityUpdate(LivingEvent.LivingUpdateEvent event) {
        if (isClientWorld(event.getEntity())) return;
        Capabilities.hurt(event.getEntity()).ifPresent(capability -> {
            //Source Damage i-Frames
            if (!capability.hurtMap.isEmpty()) {
                capability.hurtMap.forEach((s, data) -> {
                    ++data.lastHurtTick;
                    if (data.tick > 0) {
                        --data.tick;
                    }
                    if (data.info.doFrames && data.tick == 0 && !data.canApply) {
                        Events.onAttackEntityOverride = false;
                        data.apply(event.getEntity());
                        Events.onAttackEntityOverride = true;
                    }
                });
            }
            //Melee i-Frames
            if (!capability.meleeMap.isEmpty()) {
                capability.meleeMap.forEach((e, a) -> a.ticksSinceLastMelee++);
            }
            //Armor i-Frames
            if (capability.ticksToArmorDamage > 0) {
                --capability.ticksToArmorDamage;
            } else {
                capability.lastArmorDamage = 0;
            }
            //Shield i-Frames
            if (capability.ticksToShieldDamage > 0) {
                --capability.ticksToShieldDamage;
            } else {
                capability.lastShieldDamage = 0;
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttackEntityFromPre(PreLivingAttackEvent event) {
        if (isClientWorld(event.getEntityLiving())) return;
        if (!Events.onAttackEntityOverride) return;
        DamageSource source = event.getSource();
        if (Events.isAttack(source)) return; //If my source is melee, return

        EntityLivingBase entity = event.getEntityLiving();
        HurtSourceData data = BHTAPI.get(entity, source);
        data.damageSource = source; //Last source to do the damage gets the kill
        if (data.tick == 0 && data.canApply) {
            data.trigger();
        }

        if (data.info.doFrames) {
            if (data.lastHurtTick < data.info.waitTime) {
                data.accumulate(event.getAmount());
                event.setCanceled(true);
            }
        } else if (data.tick != 0) {
            float lastAmount = event.getAmount();
            if (data.lastHurtTick < data.info.waitTime) {
                if (Double.compare(Math.max(0, data.lastHurtAmount + BHTConfig.CONFIG.damageFrames.nextAttackDamageDifference), event.getAmount()) < 0) {
                    event.setAmount(lastAmount - Math.max(0, data.lastHurtAmount));
                    data.lastHurtAmount = lastAmount;
                } else {
                    event.setCanceled(true);
                }
            } else {
                data.lastHurtAmount = lastAmount;
            }
        } else {
            data.canApply = true;
        }
        data.lastHurtTick = 0;
    }

    public static final Function<Entity, AttackInfo> INFO_FUNCTION = u -> new AttackInfo(42);

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityAttack(LivingAttackEvent event) {
        if (isClientWorld(event.getEntity())) return;
        DamageSource source = event.getSource();
        if (!(source.getImmediateSource() instanceof EntityLivingBase) || event.getAmount() <= 0) return;
        if (!Events.isAttack(source)) return;
        Entity target = event.getEntity();
        Entity attacker = source.getImmediateSource();
        Capabilities.hurt(attacker).ifPresent(capability -> {
            double maxHurtResistantTime = Events.getHurtResistantTime(target);
            double attackerAttackSpeed = Events.getAttackSpeed(attacker);
            double threshold = Events.getThreshold(attacker);
            //Calculate last hurt time required
            int ticksSinceLastHurt = (int) ((float) maxHurtResistantTime * (attackerAttackSpeed * threshold));
            final AttackInfo attackInfo = capability.meleeMap.computeIfAbsent(target, INFO_FUNCTION);
            int ticksSinceLastMelee = attackInfo.ticksSinceLastMelee;
            if (ticksSinceLastMelee < ticksSinceLastHurt) {
                event.setCanceled(true);
            } else {
                attackInfo.ticksSinceLastMelee = 0;
            }
        });
    }

    public static double getHurtResistantTime(Entity entity) {
        return entity instanceof EntityLivingBase ?
                ((EntityLivingBase) entity).maxHurtResistantTime
                : Events.maxHurtResistantTime;
    }

    public static double getAttackSpeed(Entity entity) {
        double attackSpeed = SharedMonsterAttributes.ATTACK_SPEED.getDefaultValue();
        IAttributeInstance attribute = null;
        if (entity instanceof EntityLivingBase) {
            attribute = ((EntityLivingBase) entity).getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED);
        }
        if (attribute != null) {
            attackSpeed = attribute.getAttributeValue();
        }
        return 1.2D - (1.2D / (1.2D / (attackSpeed * 1.2) * 20D));
    }

    public static double getThreshold(Entity entity) {
        ResourceLocation location = EntityList.getKey(entity.getClass());
        double threshold = BHTConfig.CONFIG.attackFrames.attackThresholdDefault;
        if (entity instanceof EntityPlayer)
            threshold = BHTConfig.CONFIG.attackFrames.attackThresholdPlayer;
        if (location != null && BHTAPI.ATTACK_THRESHOLD_MAP.containsKey(location))
            threshold = BHTAPI.ATTACK_THRESHOLD_MAP.get(location);
        return threshold;
    }

    public static boolean isAttack(DamageSource source) {
        return Arrays.asList(BHTConfig.CONFIG.attackFrames.attackSources).contains(source.getDamageType());
    }

    @SubscribeEvent()
    public static void onKnockback(PreLivingKnockBackEvent event) {
        if (isClientWorld(event.getEntityLiving())) return;
        if (Arrays.asList(BHTConfig.CONFIG.knockbackFrames.knockbackExemptSource).contains(event.getSource().getDamageType())) {
            event.setCanceled(true);
        }
    }

    public static boolean isClientWorld(Entity entity) {
        return entity.getEntityWorld().isRemote;
    }
}
