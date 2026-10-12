package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class BloodlineComponent implements INBTSerializable<CompoundTag> {
    public static final StreamCodec<RegistryFriendlyByteBuf, BloodlineComponent> STREAM_CODEC =
            StreamCodec.of(
                    (buf, component) -> buf.writeUtf(component.getBloodline()),
                    buf -> {
                        BloodlineComponent component = new BloodlineComponent();
                        component.setBloodline(buf.readUtf());
                        return component;
                    }
            );
    private String bloodline = "";
    public String getBloodline() { return bloodline;}
    public void setBloodline(String bloodline) { this.bloodline = bloodline == null ? "" : bloodline;}
    public boolean hasBloodline() { return !bloodline.isEmpty();}
    public void clear() {bloodline = "";}
    public BloodlineComponent copy() {
        BloodlineComponent copy = new BloodlineComponent();
        copy.bloodline = bloodline;
        return copy;
    }
    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Bloodline", bloodline);
        return tag;
    }
    @Override
    public void deserializeNBT(HolderLookup.Provider provider,CompoundTag tag ) {
        bloodline = tag.contains("Bloodline", Tag.TAG_STRING) ? tag.getString("Bloodline") : "";
    }
}