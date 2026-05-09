package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ShepherdGrazingComponent implements INBTSerializable<CompoundTag> {
    public static final class GrazedSoulData {
        private final String soulId;
        private final String displayName;
        private final String pathway;
        private final int sequence;
        private final ArrayList<String> abilityIds;
        private float spirituality;
        private final float maxSpirituality;
        private final boolean manifestable;
        private final CompoundTag soulData;

        public GrazedSoulData(String soulId, String displayName, String pathway, int sequence, List<String> abilityIds,
                              float spirituality, float maxSpirituality, boolean manifestable,
                              CompoundTag soulData) {
            this.soulId = soulId;
            this.displayName = displayName;
            this.pathway = pathway;
            this.sequence = sequence;
            this.abilityIds = new ArrayList<>(abilityIds);
            this.spirituality = spirituality;
            this.maxSpirituality = maxSpirituality;
            this.manifestable = manifestable;
            this.soulData = soulData.copy();
        }

        public String soulId() {
            return soulId;
        }

        public String displayName() {
            return displayName;
        }

        public String pathway() {
            return pathway;
        }

        public int sequence() {
            return sequence;
        }

        public ArrayList<String> abilityIds() {
            return new ArrayList<>(abilityIds);
        }

        public float spirituality() {
            return spirituality;
        }

        public void setSpirituality(float spirituality) {
            this.spirituality = Math.max(0.0f, Math.min(maxSpirituality, spirituality));
        }

        public float maxSpirituality() {
            return maxSpirituality;
        }

        public boolean manifestable() {
            return manifestable;
        }

        public CompoundTag soulData() {
            return soulData.copy();
        }

        public CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putString("SoulId", soulId);
            tag.putString("DisplayName", displayName);
            tag.putString("Pathway", pathway);
            tag.putInt("Sequence", sequence);
            ListTag abilitiesTag = new ListTag();
            for (String abilityId : abilityIds) {
                CompoundTag abilityTag = new CompoundTag();
                abilityTag.putString("AbilityId", abilityId);
                abilitiesTag.add(abilityTag);
            }
            tag.put("AbilityIds", abilitiesTag);
            tag.putFloat("Spirituality", spirituality);
            tag.putFloat("MaxSpirituality", maxSpirituality);
            tag.putBoolean("Manifestable", manifestable);
            tag.put("SoulData", soulData.copy());
            return tag;
        }

        public static GrazedSoulData fromTag(CompoundTag tag) {
            List<String> abilityIds = new ArrayList<>();
            ListTag abilitiesTag = tag.getList("AbilityIds", Tag.TAG_COMPOUND);
            for (int i = 0; i < abilitiesTag.size(); i++) {
                abilityIds.add(abilitiesTag.getCompound(i).getString("AbilityId"));
            }
            return new GrazedSoulData(
                    tag.getString("SoulId"),
                    tag.getString("DisplayName"),
                    tag.getString("Pathway"),
                    tag.getInt("Sequence"),
                    abilityIds,
                    tag.getFloat("Spirituality"),
                    tag.getFloat("MaxSpirituality"),
                    tag.getBoolean("Manifestable"),
                    tag.getCompound("SoulData")
            );
        }
    }

    private final ArrayList<GrazedSoulData> souls = new ArrayList<>();
    private String activeSoulId = "";
    private String manifestedSoulId = "";
    private String manifestedEntityUuid = "";
    private float permanentSanityCap = 1.0f;

    public ArrayList<GrazedSoulData> getSouls() {
        return souls;
    }

    public void addSoul(GrazedSoulData soul) {
        souls.add(soul);
        if (activeSoulId.isEmpty()) {
            activeSoulId = soul.soulId();
        }
    }

    public void removeSoul(String soulId) {
        souls.removeIf(soul -> Objects.equals(soul.soulId(), soulId));
        if (Objects.equals(activeSoulId, soulId)) {
            activeSoulId = souls.isEmpty() ? "" : souls.get(0).soulId();
        }
        if (Objects.equals(manifestedSoulId, soulId)) {
            manifestedSoulId = "";
            manifestedEntityUuid = "";
        }
    }

    public GrazedSoulData getSoul(String soulId) {
        for (GrazedSoulData soul : souls) {
            if (Objects.equals(soul.soulId(), soulId)) {
                return soul;
            }
        }
        return null;
    }

    public GrazedSoulData getActiveSoul() {
        return getSoul(activeSoulId);
    }

    public void setActiveSoulId(String activeSoulId) {
        this.activeSoulId = activeSoulId == null ? "" : activeSoulId;
    }

    public String getActiveSoulId() {
        return activeSoulId;
    }

    public String getManifestedSoulId() {
        return manifestedSoulId;
    }

    public void setManifestedSoulId(String manifestedSoulId) {
        this.manifestedSoulId = manifestedSoulId == null ? "" : manifestedSoulId;
    }

    public UUID getManifestedEntityUuid() {
        return manifestedEntityUuid.isEmpty() ? null : UUID.fromString(manifestedEntityUuid);
    }

    public void setManifestedEntityUuid(UUID manifestedEntityUuid) {
        this.manifestedEntityUuid = manifestedEntityUuid == null ? "" : manifestedEntityUuid.toString();
    }

    public boolean hasManifestedSoul() {
        return !manifestedSoulId.isEmpty() && !manifestedEntityUuid.isEmpty();
    }

    public float getPermanentSanityCap() {
        return permanentSanityCap;
    }

    public void setPermanentSanityCap(float permanentSanityCap) {
        this.permanentSanityCap = Math.max(0.0f, Math.min(1.0f, permanentSanityCap));
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag soulsTag = new ListTag();
        for (GrazedSoulData soul : souls) {
            soulsTag.add(soul.toTag());
        }
        tag.put("Souls", soulsTag);
        tag.putString("ActiveSoulId", activeSoulId);
        tag.putString("ManifestedSoulId", manifestedSoulId);
        tag.putString("ManifestedEntityUuid", manifestedEntityUuid);
        tag.putFloat("PermanentSanityCap", permanentSanityCap);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        souls.clear();
        ListTag soulsTag = tag.getList("Souls", Tag.TAG_COMPOUND);
        for (int i = 0; i < soulsTag.size(); i++) {
            souls.add(GrazedSoulData.fromTag(soulsTag.getCompound(i)));
        }
        activeSoulId = tag.getString("ActiveSoulId");
        manifestedSoulId = tag.getString("ManifestedSoulId");
        manifestedEntityUuid = tag.getString("ManifestedEntityUuid");
        permanentSanityCap = tag.contains("PermanentSanityCap") ? tag.getFloat("PermanentSanityCap") : 1.0f;
    }
}
