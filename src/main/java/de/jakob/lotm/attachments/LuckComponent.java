package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

public class LuckComponent implements INBTSerializable<CompoundTag> {

    private int luck = 0;

    public int getLuck() {
        return luck;
    }

    public void setLuck(int luck) {
        this.luck = luck;
        if(this.luck > 3000) {
            this.luck = 3000;
        }
        if(this.luck < -6360) {
            this.luck = -6360;
        }
    }

    public void addLuck(int amount) {
        this.luck += amount;
        if(luck > 3000) {
            luck = 3000;
        }
        if(luck < -6360) {
            luck = -6360;
        }
    }

    public void addLuckWithMax(int amount, int max) {
        if (amount >= 0 && this.luck + amount > max) {
            this.luck = max;
        } else {
            this.luck += amount;
        }
    }

    public void addLuckWithMin(int amount, int min) {
        if (amount < 0 && this.luck + amount < min) {
            this.luck = -min;
        } else {
            this.luck += amount;
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
