package arekkuusu.betterhurttimer.api.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HurtProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<Hurt> HURT_LIMITER = CapabilityManager.get(new CapabilityToken<Hurt>() {});
    private Hurt hurt = null;
    private final LazyOptional<Hurt> optional = LazyOptional.of(this::createHurt);

    public static LazyOptional<Hurt> hurt(@Nullable Entity entity) {
        return entity != null ? entity.getCapability(HURT_LIMITER, null) : LazyOptional.empty();
    }

    private Hurt createHurt() {
        if (hurt == null) {
            hurt = new Hurt();
        }

        return hurt;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == HURT_LIMITER) {
            return optional.cast();
        }

        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createHurt().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createHurt().loadNBTData(nbt);
    }
}
