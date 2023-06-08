package arekkuusu.betterhurttimer.client;

import arekkuusu.betterhurttimer.BHT;
import arekkuusu.betterhurttimer.BHTConfig;
import arekkuusu.betterhurttimer.api.capability.HealthProvider;
import arekkuusu.betterhurttimer.client.render.effect.DamageParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = BHT.MOD_ID, value = Dist.CLIENT)
public class Events {
    private static Logger logger = BHT.LOG;

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        boolean entityIsLiving = entity instanceof LivingEntity;
        if (entityIsLiving) {
            if (entity.getLevel().isClientSide) {
                event.addCapability(new ResourceLocation(BHT.MOD_ID, "healthcap"), new HealthProvider());
            }
            ((LivingEntity) event.getObject()).attackStrengthTicker = -1;
        }
    }


    @SubscribeEvent
    public static void displayDamage(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity(); //todo check this
        Boolean clientSide = entity.level.isClientSide();
        if (!clientSide || !BHTConfig.Runtime.Rendering.showDamageParticles) return;

        HealthProvider.health(entity).ifPresent(cap -> {
        int currentHealth = (int) Math.ceil(entity.getHealth());
            if (cap.getHealth() != -1 && cap.getHealth() != currentHealth) {
                displayParticle(entity, cap.getHealth() - currentHealth);
            }
            cap.setHealth(currentHealth);
        });
    }

    public static void displayParticle(Entity entity, int damage) {
        if (damage == 0) return;

        ClientLevel world = (ClientLevel) entity.level;
        double motionX = world.random.nextGaussian() * 0.02;
        double motionY = 0.5f;
        double motionZ = world.random.nextGaussian() * 0.02;
        Particle damageIndicator = new DamageParticle(damage, world, entity.getX(), entity.getY() + entity.getBbHeight(), entity.getZ(), motionX, motionY, motionZ);
        Minecraft.getInstance().particleEngine.add(damageIndicator);
    }
}
