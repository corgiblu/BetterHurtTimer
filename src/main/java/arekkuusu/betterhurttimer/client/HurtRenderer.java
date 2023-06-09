package arekkuusu.betterhurttimer.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;

public class HurtRenderer {
    // holi :)
    // a que hora sales por el pan? :3
    public static int preHurtRender;

    public static void setPreHurtTime(LivingEntity entity) {
        if(entity == Minecraft.getInstance().cameraEntity) {
            HurtRenderer.preHurtRender = entity.hurtTime;
        }
    }
}