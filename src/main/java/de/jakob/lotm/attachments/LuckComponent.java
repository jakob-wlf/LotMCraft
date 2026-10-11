package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

public class LuckComponent implements INBTSerializable<CompoundTag> {

    private int luck = 0;

    public int getLuck() {
        return luck;
    }

    public void setLuck(int luck) {
        this.luck = Mth.clamp(luck, -6360, 3000);
    }

    public void addLuck(int amount) {
        setLuck(getLuck() + amount);
    }

    public void addLuckWithMax(int amount, int max) {
        if (amount >= 0 && getLuck() + amount > max) {
            setLuck(max);
        } else {
            setLuck(getLuck() + amount);
        }
    }

    public void addLuckWithMin(int amount, int min) {
        if (amount < 0 && getLuck() + amount < min) {
            setLuck(min);
        } else {
            setLuck(getLuck() + amount);
        }
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("luck", luck);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        if (compoundTag.contains("luck")) {
            this.luck = compoundTag.getInt("luck");
        }
    }
}
