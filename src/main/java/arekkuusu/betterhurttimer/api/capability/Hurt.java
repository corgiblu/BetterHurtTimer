package arekkuusu.betterhurttimer.api.capability;

import arekkuusu.betterhurttimer.api.capability.data.AttackInfo;
import arekkuusu.betterhurttimer.api.capability.data.HurtSourceInfo;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import java.util.WeakHashMap;

@AutoRegisterCapability
public class Hurt {
    private Object2ObjectMap<CharSequence, HurtSourceInfo.HurtSourceData> hurtMap = new Object2ObjectArrayMap<>();
    private WeakHashMap<Entity, arekkuusu.betterhurttimer.api.capability.data.AttackInfo> meleeMap = new WeakHashMap<>();
    private int ticksToArmorDamage;
    private int ticksToShieldDamage;
    private double lastArmorDamage;
    private double lastShieldDamage;

    // Getters
    public Object2ObjectMap<CharSequence, HurtSourceInfo.HurtSourceData> getHurtMap() {
        return hurtMap;
    }
    public WeakHashMap<Entity, AttackInfo> getMeleeMap() {
        return meleeMap;
    }
    public double getLastArmorDamage() {
        return lastArmorDamage;
    }
    public double getLastShieldDamage() {
        return lastShieldDamage;
    }
    public int getTicksToArmorDamage() {
        return ticksToArmorDamage;
    }
    public int getTicksToShieldDamage() {
        return ticksToShieldDamage;
    }

    public void copyFrom(Hurt source) {
        this.hurtMap = source.hurtMap;
        this.meleeMap = source.meleeMap;
        this.lastArmorDamage = source.lastArmorDamage;
        this.lastShieldDamage = source.lastShieldDamage;
        this.ticksToArmorDamage = source.ticksToArmorDamage;
        this.ticksToShieldDamage = source.ticksToShieldDamage;
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putInt("ticksToArmorDamage", ticksToArmorDamage);
        nbt.putInt("ticksToShieldDamage", ticksToShieldDamage);
    }

    public void loadNBTData(CompoundTag nbt) {
        ticksToArmorDamage = nbt.getInt("ticksToArmorDamage");
        ticksToShieldDamage = nbt.getInt("ticksToShieldDamage");
    }

    public void tickDownArmorDamage() {
        ticksToArmorDamage = Math.min(ticksToArmorDamage--, 0);
    }

    public void tickDownShieldDamage() {
        ticksToShieldDamage = Math.min(ticksToShieldDamage--, 0);
    }

    public void setLastArmorDamage(int damage) {
        lastArmorDamage = damage;
    }

    public void setLastShieldDamage(int damage) {
        lastShieldDamage = damage;
    }

    public void setTicksToShieldDamage(int ticksToShieldDamage) {
        this.ticksToShieldDamage = ticksToShieldDamage;
    }

    public void setTicksToArmorDamage(int ticksToArmorDamage) {
        this.ticksToArmorDamage = ticksToArmorDamage;
    }
}
