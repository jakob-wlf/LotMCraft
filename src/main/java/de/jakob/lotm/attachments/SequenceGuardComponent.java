package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import java.util.*;

public class SequenceGuardComponent {
    public static final long PUNISHMENT_MS = 24L * 60 * 60 * 1000;
    public static final long PROVOCATION_MS = 60L * 60 * 1000;
    private final Set<UUID> allowedToKill = new HashSet<>();
    private final Set<UUID> consentedAttackers = new HashSet<>();
    private final Map<UUID, Long> provokers = new HashMap<>();
    private final Map<UUID, Long> provoked = new HashMap<>();
    private boolean sequenceGuard = true;
    private long guardDisabledUntil = 0L;
    private int totalKills = 0;

    public int getTotalKills() {
        return this.totalKills;
    }

    public void addTotalKills(int totalKills) {
        this.totalKills += totalKills;
    }

    public void resetTotalKills() {
        this.totalKills = 0;
    }


    public boolean isSequenceGuard() { return sequenceGuard; }

    public void setSequenceGuard(boolean v) { this.sequenceGuard = v; }

    public boolean isGuardActive() {
        return sequenceGuard && System.currentTimeMillis() >= guardDisabledUntil;
    }


    public void punish() { guardDisabledUntil = System.currentTimeMillis() + PUNISHMENT_MS; }

    public void clearPunishment() { guardDisabledUntil = 0L; }

    public long getGuardDisabledUntil() { return guardDisabledUntil; }


    public Set<UUID> getAllowedToKill() { return allowedToKill; }

    public void addTargetUuid(UUID uuid) { allowedToKill.add(uuid); }

    public void removeTargetUuid(UUID uuid) { allowedToKill.remove(uuid); }


    public Set<UUID> getConsentedAttackers() { return consentedAttackers; }

    public boolean hasConsentAttackers(UUID uuid) {
        return this.consentedAttackers.contains(uuid);
    }

    public void addConsentAttackers(UUID uuid) {
        this.consentedAttackers.add(uuid);
    }

    public void removeConsentAttackers(UUID uuid) {
        this.consentedAttackers.remove(uuid);
    }

    public void clearConsentAttackers() {
        this.consentedAttackers.clear();
    }


    public void addProvoker(UUID uuid) {
        provokers.put(uuid, System.currentTimeMillis() + PROVOCATION_MS);
    }

    public Set<UUID> getProvokers() {
        provokers.values().removeIf(expiry -> System.currentTimeMillis() > expiry);
        return new HashSet<>(provokers.keySet());
    }

    public void clearProvokers() { provokers.clear(); }

    public boolean ProvokedBy(UUID uuid) {
        if (provokers.get(uuid) == null) return false;
        if (System.currentTimeMillis() > provokers.get(uuid)) { provokers.remove(uuid); return false; }
        return true;
    }


    public void addProvoked(UUID uuid) {
        provoked.put(uuid, System.currentTimeMillis() + PROVOCATION_MS);
    }

    public Set<UUID> getProvoked() {
        provoked.values().removeIf(expiry -> System.currentTimeMillis() > expiry);
        return new HashSet<>(provoked.keySet());
    }

    public boolean hasProvoked(UUID uuid) {
        Long expiry = provoked.get(uuid);
        if (expiry == null) return false;
        if (System.currentTimeMillis() > expiry) { provoked.remove(uuid); return false; }
        return true;
    }

    public void removeProvoker(UUID uuid) { provokers.remove(uuid); }

    public void clearProvoked() { provoked.clear(); }



    public static final IAttachmentSerializer<CompoundTag, SequenceGuardComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public SequenceGuardComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    var component = new SequenceGuardComponent();

                    if (tag.contains("allowed_to_kill", Tag.TAG_LIST)) {
                        ListTag list = tag.getList("allowed_to_kill", Tag.TAG_COMPOUND);
                        for (int i = 0; i < list.size(); i++) component.allowedToKill.add(list.getCompound(i).getUUID("uuid"));
                    }

                    if (tag.contains("consented_attackers", Tag.TAG_LIST)) {
                        ListTag list = tag.getList("consented_attackers", Tag.TAG_COMPOUND);
                        for (int i = 0; i < list.size(); i++) component.consentedAttackers.add(list.getCompound(i).getUUID("uuid"));
                    }

                    if (tag.contains("provokers", Tag.TAG_LIST)) {
                        ListTag list = tag.getList("provokers", Tag.TAG_COMPOUND);
                        long now = System.currentTimeMillis();
                        for (int i = 0; i < list.size(); i++) {
                            CompoundTag comp = list.getCompound(i);
                            long expiry = comp.getLong("expiry");
                            if (expiry > now) component.provokers.put(comp.getUUID("uuid"), expiry);
                        }
                    }

                    if (tag.contains("provoked", Tag.TAG_LIST)) {
                        ListTag list = tag.getList("provoked", Tag.TAG_COMPOUND);
                        long now = System.currentTimeMillis();
                        for (int i = 0; i < list.size(); i++) {
                            CompoundTag comp = list.getCompound(i);
                            long expiry = comp.getLong("expiry");
                            if (expiry > now) component.provoked.put(comp.getUUID("uuid"), expiry);
                        }
                    }

                    component.sequenceGuard = !tag.contains("sequence_guard") || tag.getBoolean("sequence_guard");
                    component.guardDisabledUntil = tag.getLong("guard_disabled_until");
                    component.totalKills = tag.getInt("total_kills");
                    return component;
                }

                @Override
                public CompoundTag write(SequenceGuardComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();

                    ListTag allowed = new ListTag();
                    for (UUID uuid : component.allowedToKill) {
                        CompoundTag comp = new CompoundTag();
                        comp.putUUID("uuid", uuid);
                        allowed.add(comp);
                    }
                    tag.put("allowed_to_kill", allowed);

                    ListTag consented = new ListTag();
                    for (UUID uuid : component.consentedAttackers) {
                        CompoundTag comp = new CompoundTag();
                        comp.putUUID("uuid", uuid);
                        consented.add(comp);
                    }
                    tag.put("consented_attackers", consented);

                    ListTag list = new ListTag();
                    component.provokers.forEach((uuid, expiry) -> {
                        CompoundTag comp = new CompoundTag();
                        comp.putUUID("uuid", uuid);
                        comp.putLong("expiry", expiry);
                        list.add(comp);
                    });
                    tag.put("provokers", list);

                    ListTag provokedList = new ListTag();
                    component.provoked.forEach((uuid, expiry) -> {
                        CompoundTag comp = new CompoundTag();
                        comp.putUUID("uuid", uuid);
                        comp.putLong("expiry", expiry);
                        provokedList.add(comp);
                    });
                    tag.put("provoked", provokedList);

                    tag.putBoolean("sequence_guard", component.sequenceGuard);
                    tag.putLong("guard_disabled_until", component.guardDisabledUntil);
                    tag.putInt("total_kills", component.totalKills);
                    return tag;
                }
            };
}