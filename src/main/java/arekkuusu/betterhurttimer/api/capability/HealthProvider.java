package arekkuusu.betterhurttimer.api.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HealthProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<Health> HEALTH = CapabilityManager.get(new CapabilityToken<Health>() {});
    private Health health = null;
    private final LazyOptional<Health> optional = LazyOptional.of(this::createHealth);
    public static LazyOptional<Health> health(@Nullable Entity entity) {
        return entity != null ? entity.getCapability(HEALTH, null) : LazyOptional.empty();
    }

    private Health createHealth() {
        if (health == null) {
            health = new Health();
        }
        return health;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == HEALTH) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createHealth().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createHealth().loadNBTData(nbt);
    }
}
