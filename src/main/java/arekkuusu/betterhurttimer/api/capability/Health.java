package arekkuusu.betterhurttimer.api.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public class Health {
    private int health;
    private final int MIN_HEALTH = 0;

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void copyFrom(Health source) {
        this.health = source.health;
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putInt("health", health);
    }

    public void loadNBTData(CompoundTag nbt) {
        health = nbt.getInt("health")  ;
    }
}
