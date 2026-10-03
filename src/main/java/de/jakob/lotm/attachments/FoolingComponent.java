package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

public class FoolingComponent implements INBTSerializable<CompoundTag> {

    private int ticksRemaining = 0;
    private int stunTicksRemaining = 0;

    public int getTicksRemaining() {
        return ticksRemaining;
    }

    public void setTicksRemaining(int ticks) {
        this.ticksRemaining = Math.max(0, ticks);
    }

    public boolean isFooled() {
        return ticksRemaining > 0;
    }

    public boolean isStunned() {
        return stunTicksRemaining > 0;
    }

    public void applyStun(int ticks) {
        this.stunTicksRemaining = Math.max(this.stunTicksRemaining, ticks);
    }

    public void tick() {
        if (ticksRemaining > 0) ticksRemaining--;
        if (stunTicksRemaining > 0) stunTicksRemaining--;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("fooling_ticks", ticksRemaining);
        tag.putInt("stun_ticks", stunTicksRemaining);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("fooling_ticks")) {
            ticksRemaining = tag.getInt("fooling_ticks");
        }
        if (tag.contains("stun_ticks")) {
            stunTicksRemaining = tag.getInt("stun_ticks");
        }
    }
}
