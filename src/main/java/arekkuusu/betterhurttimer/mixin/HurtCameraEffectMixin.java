package arekkuusu.betterhurttimer.mixin;

import arekkuusu.betterhurttimer.BHTConfig;
import arekkuusu.betterhurttimer.client.HurtRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class HurtCameraEffectMixin {

    @Inject(method = "bobHurt", at = @At(target = "Lnet/minecraft/world/entity/LivingEntity;hurtTime:I", value = "FIELD", ordinal = 0), cancellable = true)
    private void hurtCameraEffect(PoseStack poseStack, float partialTicks, CallbackInfo info) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> thing(info));
    }

    private void thing(CallbackInfo info) {
        if (!BHTConfig.Runtime.Rendering.doHurtCameraEffect || (HurtRenderer.preHurtRender > 0)) {
            info.cancel();
        }
    }
}
