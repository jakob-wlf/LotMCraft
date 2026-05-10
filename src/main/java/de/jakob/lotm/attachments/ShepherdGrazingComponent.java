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

    public static final class ManifestedSoulData {
        private final String soulId;
        private final String entityUuid;

        public ManifestedSoulData(String soulId, UUID entityUuid) {
            this.soulId = soulId;
            this.entityUuid = entityUuid == null ? "" : entityUuid.toString();
        }

        public String soulId() {
            return soulId;
        }

        public UUID entityUuid() {
            return entityUuid.isEmpty() ? null : UUID.fromString(entityUuid);
        }

        public CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putString("SoulId", soulId);
            tag.putString("EntityUuid", entityUuid);
            return tag;
        }

        public static ManifestedSoulData fromTag(CompoundTag tag) {
            return new ManifestedSoulData(
                    tag.getString("SoulId"),
                    tag.getString("EntityUuid").isEmpty() ? null : UUID.fromString(tag.getString("EntityUuid"))
            );
        }
    }

    private final ArrayList<GrazedSoulData> souls = new ArrayList<>();
    private final ArrayList<ManifestedSoulData> manifestedSouls = new ArrayList<>();
    private final ArrayList<String> activeSoulIds = new ArrayList<>();
    private String activeSoulId = "";
    private float permanentSanityCap = 1.0f;

    public ArrayList<GrazedSoulData> getSouls() {
        return souls;
    }

    public void addSoul(GrazedSoulData soul) {
        souls.add(soul);
        if (activeSoulId.isEmpty()) {
            activeSoulId = soul.soulId();
            activeSoulIds.add(soul.soulId());
        }
    }

    public void removeSoul(String soulId) {
        souls.removeIf(soul -> Objects.equals(soul.soulId(), soulId));
        removeManifestedSoul(soulId);
        if (Objects.equals(activeSoulId, soulId)) {
            activeSoulIds.remove(soulId);
            activeSoulId = activeSoulIds.isEmpty() ? (souls.isEmpty() ? "" : souls.get(0).soulId()) : activeSoulIds.get(0);
            if (!activeSoulId.isEmpty() && !activeSoulIds.contains(activeSoulId)) {
                activeSoulIds.add(0, activeSoulId);
            }
        } else {
            activeSoulIds.remove(soulId);
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
        if (this.activeSoulId.isEmpty()) {
            if (!activeSoulIds.isEmpty()) {
                activeSoulIds.clear();
            }
            return;
        }

        activeSoulIds.remove(this.activeSoulId);
        activeSoulIds.add(0, this.activeSoulId);
    }

    public String getActiveSoulId() {
        return activeSoulId;
    }

    public ArrayList<String> getActiveSoulIds() {
        return new ArrayList<>(activeSoulIds);
    }

    public ArrayList<GrazedSoulData> getActiveSouls() {
        ArrayList<GrazedSoulData> activeSouls = new ArrayList<>();
        for (String soulId : activeSoulIds) {
            GrazedSoulData soul = getSoul(soulId);
            if (soul != null) {
                activeSouls.add(soul);
            }
        }
        return activeSouls;
    }

    public int getActiveSoulCount() {
        return activeSoulIds.size();
    }

    public boolean isSoulActive(String soulId) {
        return activeSoulIds.contains(soulId);
    }

    public void ensureActiveSoul(String soulId, int maxActiveSouls) {
        if (soulId == null || soulId.isEmpty()) {
            return;
        }

        activeSoulIds.remove(soulId);
        activeSoulIds.add(0, soulId);
        while (activeSoulIds.size() > Math.max(1, maxActiveSouls)) {
            activeSoulIds.remove(activeSoulIds.size() - 1);
        }
        activeSoulId = activeSoulIds.get(0);
    }

    public boolean toggleActiveSoul(String soulId, int maxActiveSouls) {
        if (soulId == null || soulId.isEmpty()) {
            return false;
        }

        if (activeSoulIds.contains(soulId)) {
            if (activeSoulIds.size() <= 1) {
                return false;
            }
            activeSoulIds.remove(soulId);
            if (Objects.equals(activeSoulId, soulId)) {
                activeSoulId = activeSoulIds.get(0);
            }
            return false;
        }

        if (activeSoulIds.size() >= Math.max(1, maxActiveSouls)) {
            return false;
        }

        activeSoulIds.add(soulId);
        if (activeSoulId.isEmpty()) {
            activeSoulId = soulId;
        }
        return true;
    }

    public boolean trimActiveSouls(int maxActiveSouls) {
        int clampedMax = Math.max(1, maxActiveSouls);
        boolean changed = false;
        while (activeSoulIds.size() > clampedMax) {
            activeSoulIds.remove(activeSoulIds.size() - 1);
            changed = true;
        }
        if (activeSoulIds.isEmpty() && !souls.isEmpty()) {
            activeSoulIds.add(souls.get(0).soulId());
            changed = true;
        }
        String newPrimary = activeSoulIds.isEmpty() ? "" : activeSoulIds.get(0);
        if (!Objects.equals(activeSoulId, newPrimary)) {
            activeSoulId = newPrimary;
            changed = true;
        }
        return changed;
    }

    public String getManifestedSoulId() {
        return manifestedSouls.isEmpty() ? "" : manifestedSouls.get(0).soulId();
    }

    public void setManifestedSoulId(String manifestedSoulId) {
        if (manifestedSoulId == null || manifestedSoulId.isEmpty()) {
            manifestedSouls.clear();
            return;
        }

        UUID existingUuid = getManifestedEntityUuid();
        manifestedSouls.clear();
        manifestedSouls.add(new ManifestedSoulData(manifestedSoulId, existingUuid));
    }

    public UUID getManifestedEntityUuid() {
        return manifestedSouls.isEmpty() ? null : manifestedSouls.get(0).entityUuid();
    }

    public void setManifestedEntityUuid(UUID manifestedEntityUuid) {
        if (manifestedSouls.isEmpty()) {
            if (manifestedEntityUuid != null) {
                manifestedSouls.add(new ManifestedSoulData("", manifestedEntityUuid));
            }
            return;
        }

        ManifestedSoulData first = manifestedSouls.get(0);
        manifestedSouls.set(0, new ManifestedSoulData(first.soulId(), manifestedEntityUuid));
    }

    public boolean hasManifestedSoul() {
        return !manifestedSouls.isEmpty();
    }

    public ArrayList<ManifestedSoulData> getManifestedSouls() {
        return new ArrayList<>(manifestedSouls);
    }

    public int getManifestedSoulCount() {
        return manifestedSouls.size();
    }

    public boolean isSoulManifested(String soulId) {
        return manifestedSouls.stream().anyMatch(data -> Objects.equals(data.soulId(), soulId));
    }

    public UUID getManifestedEntityUuid(String soulId) {
        for (ManifestedSoulData data : manifestedSouls) {
            if (Objects.equals(data.soulId(), soulId)) {
                return data.entityUuid();
            }
        }
        return null;
    }

    public void manifestSoul(String soulId, UUID entityUuid) {
        manifestedSouls.removeIf(data -> Objects.equals(data.soulId(), soulId) || Objects.equals(data.entityUuid(), entityUuid));
        manifestedSouls.add(new ManifestedSoulData(soulId, entityUuid));
    }

    public void removeManifestedSoul(String soulId) {
        manifestedSouls.removeIf(data -> Objects.equals(data.soulId(), soulId));
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
        ListTag manifestedSoulsTag = new ListTag();
        for (ManifestedSoulData data : manifestedSouls) {
            manifestedSoulsTag.add(data.toTag());
        }
        tag.put("Souls", soulsTag);
        tag.put("ManifestedSouls", manifestedSoulsTag);
        tag.putString("ActiveSoulId", activeSoulId);
        ListTag activeSoulIdsTag = new ListTag();
        for (String soulId : activeSoulIds) {
            CompoundTag soulIdTag = new CompoundTag();
            soulIdTag.putString("SoulId", soulId);
            activeSoulIdsTag.add(soulIdTag);
        }
        tag.put("ActiveSoulIds", activeSoulIdsTag);
        tag.putFloat("PermanentSanityCap", permanentSanityCap);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        souls.clear();
        manifestedSouls.clear();
        ListTag soulsTag = tag.getList("Souls", Tag.TAG_COMPOUND);
        for (int i = 0; i < soulsTag.size(); i++) {
            souls.add(GrazedSoulData.fromTag(soulsTag.getCompound(i)));
        }
        if (tag.contains("ManifestedSouls", Tag.TAG_LIST)) {
            ListTag manifestedSoulsTag = tag.getList("ManifestedSouls", Tag.TAG_COMPOUND);
            for (int i = 0; i < manifestedSoulsTag.size(); i++) {
                manifestedSouls.add(ManifestedSoulData.fromTag(manifestedSoulsTag.getCompound(i)));
            }
        } else {
            String manifestedSoulId = tag.getString("ManifestedSoulId");
            String manifestedEntityUuid = tag.getString("ManifestedEntityUuid");
            if (!manifestedSoulId.isEmpty() && !manifestedEntityUuid.isEmpty()) {
                manifestedSouls.add(new ManifestedSoulData(manifestedSoulId, UUID.fromString(manifestedEntityUuid)));
            }
        }
        activeSoulId = tag.getString("ActiveSoulId");
        activeSoulIds.clear();
        if (tag.contains("ActiveSoulIds", Tag.TAG_LIST)) {
            ListTag activeSoulIdsTag = tag.getList("ActiveSoulIds", Tag.TAG_COMPOUND);
            for (int i = 0; i < activeSoulIdsTag.size(); i++) {
                String soulId = activeSoulIdsTag.getCompound(i).getString("SoulId");
                if (!soulId.isEmpty()) {
                    activeSoulIds.add(soulId);
                }
            }
        } else if (!activeSoulId.isEmpty()) {
            activeSoulIds.add(activeSoulId);
        }
        activeSoulIds.removeIf(soulId -> getSoul(soulId) == null);
        if (!activeSoulId.isEmpty() && !activeSoulIds.contains(activeSoulId) && getSoul(activeSoulId) != null) {
            activeSoulIds.add(0, activeSoulId);
        }
        if (activeSoulIds.isEmpty() && !souls.isEmpty()) {
            activeSoulId = souls.get(0).soulId();
            activeSoulIds.add(activeSoulId);
        } else if (!activeSoulIds.isEmpty()) {
            activeSoulId = activeSoulIds.get(0);
        }
        permanentSanityCap = tag.contains("PermanentSanityCap") ? tag.getFloat("PermanentSanityCap") : 1.0f;
    }
}
