package de.jakob.lotm.status;

import de.jakob.lotm.attachments.BeyonderComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.UniquenessComponent;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.playerMap.Characteristic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * A point-in-time snapshot of a player's beyonder state:
 * pathway/sequence, characteristic list, uniqueness, and active potion effects.
 */
public final class StatusSnapshot {

    public final String                    pathway;
    public final int                       sequence;
    public final ArrayList<Characteristic> charList;       // deep copy of BeyonderComponent characteristic list
    public final String[]                  pathwayHistory; // copy of BeyonderComponent pathwayHistory[10]
    public final String                    uniquenessPathway;
    public final boolean                   hasUniqueness;
    public final int                       uniquenessKillCount;
    public final List<CompoundTag>         effects;        // serialised MobEffectInstances
    public final long                      captureTimeMs;

    public StatusSnapshot(String pathway, int sequence, ArrayList<Characteristic> charList,
                          String[] pathwayHistory,
                          String uniquenessPathway, boolean hasUniqueness, int uniquenessKillCount,
                          List<CompoundTag> effects, long captureTimeMs) {
        this.pathway             = pathway;
        this.sequence            = sequence;
        this.charList            = charList;
        this.pathwayHistory      = pathwayHistory != null ? pathwayHistory.clone() : new String[10];
        this.uniquenessPathway   = uniquenessPathway;
        this.hasUniqueness       = hasUniqueness;
        this.uniquenessKillCount = uniquenessKillCount;
        this.effects             = effects;
        this.captureTimeMs       = captureTimeMs;
    }

    // ── Capture ───────────────────────────────────────────────────────────────

    public static StatusSnapshot capture(ServerPlayer player) {
        var bc = player.getData(ModAttachments.BEYONDER_COMPONENT);
        var uc = player.getData(ModAttachments.UNIQUENESS_COMPONENT);

        // Deep-copy the characteristic list
        ArrayList<Characteristic> charCopy = new ArrayList<>();
        for (Characteristic c : bc.getCharacteristicList()) {
            charCopy.add(new Characteristic(c.pathway(), c.stack(), c.sequence()));
        }

        // Copy pathway history
        String[] historyCopy = bc.getPathwayHistory().clone();

        List<CompoundTag> effectTags = new ArrayList<>();
        for (MobEffectInstance eff : new ArrayList<>(player.getActiveEffects())) {
            try {
                Tag t = eff.save();
                if (t instanceof CompoundTag ct) effectTags.add(ct);
            } catch (Exception ignored) {}
        }

        return new StatusSnapshot(
                bc.getPathway(),
                bc.getSequence(),
                charCopy,
                historyCopy,
                uc.getUniquenessPathway(),
                uc.hasUniqueness(),
                uc.getKillCount(),
                effectTags,
                System.currentTimeMillis()
        );
    }

    // ── Apply ─────────────────────────────────────────────────────────────────

    /**
     * Restore this snapshot onto {@code player}.
     * Uses skipCheck=true so the Envisioning authority can exceed normal slot caps.
     *
     * <p>Bypasses {@code BeyonderData.clearCharacteristics}/{@code setCharacteristic} to avoid
     * the {@code isBeyonder} guard that silently drops all characteristic writes after
     * {@code clearCharacteristics} empties the list (which sets sequence=10/pathway=none).
     */
    public void applyTo(ServerPlayer player) {
        // 1. Set pathway + sequence via setBeyonder so the player is put into the playerMap,
        //    passive-effect hooks fire, and the initial component state is valid.
        //    clearCharStack=false: we replace the list ourselves below, avoiding a double-clear.
        //    addToPathwayHistory=false: we restore the full history manually in step 3.
        BeyonderData.setBeyonder(player, pathway, sequence, true, false, false, false, false, true);

        // 2. Force-replace charList in BOTH the BeyonderComponent AND the playerMap.
        //    This avoids the isBeyonder guard that fires after clearCharacteristics() makes the
        //    player appear as non-beyonder, silently dropping all subsequent setCharacteristic calls.
        BeyonderData.forceRestoreCharList(player, charList);

        // 3. Restore pathway history (needed for beyonder map & introspect ability lookups).
        BeyonderComponent bc = player.getData(ModAttachments.BEYONDER_COMPONENT);
        if (pathwayHistory != null) {
            bc.setPathwayHistory(pathwayHistory.clone());
        }

        // 4. Uniqueness
        UniquenessComponent uc = player.getData(ModAttachments.UNIQUENESS_COMPONENT);
        uc.setHasUniqueness(hasUniqueness);
        uc.setUniquenessPathway(uniquenessPathway);
        uc.setKillCount(uniquenessKillCount);
        player.setData(ModAttachments.UNIQUENESS_COMPONENT, uc);

        // 5. Effects — clear first, then restore the saved set
        player.removeAllEffects();
        for (CompoundTag tag : effects) {
            MobEffectInstance loaded = MobEffectInstance.load(tag);
            if (loaded != null) player.addEffect(loaded);
        }

        // 6. Final full sync so the client sees the corrected charList, pathway, and history.
        PacketHandler.syncBeyonderDataToPlayer(player);
        PacketHandler.syncUniquenessToPlayer(player);
    }

    // ── NBT ───────────────────────────────────────────────────────────────────

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("pathway",          pathway);
        tag.putInt("sequence",            sequence);

        ListTag cs = new ListTag();
        for (Characteristic c : charList) {
            CompoundTag e = new CompoundTag();
            e.putString(Characteristic.CHAR_PATH,  c.pathway());
            e.putInt(Characteristic.CHAR_SEQ,      c.sequence());
            e.putInt(Characteristic.CHAR_STACK,    c.stack());
            cs.add(e);
        }
        tag.put("charList", cs);

        ListTag hist = new ListTag();
        for (String h : pathwayHistory) hist.add(StringTag.valueOf(h != null ? h : ""));
        tag.put("pathwayHistory", hist);

        tag.putString("uniquenessPathway", uniquenessPathway);
        tag.putBoolean("hasUniqueness",    hasUniqueness);
        tag.putInt("uniquenessKillCount",  uniquenessKillCount);

        ListTag efx = new ListTag();
        efx.addAll(effects);
        tag.put("effects",           efx);
        tag.putLong("captureTimeMs", captureTimeMs);
        return tag;
    }

    public static StatusSnapshot load(CompoundTag tag) {
        ArrayList<Characteristic> chars = new ArrayList<>();
        ListTag cst = tag.getList("charList", Tag.TAG_COMPOUND);
        for (int j = 0; j < cst.size(); j++) {
            try { chars.add(Characteristic.fromNBT(cst.getCompound(j), null)); }
            catch (Exception ignored) {}
        }

        ListTag efxt = tag.getList("effects", Tag.TAG_COMPOUND);
        List<CompoundTag> efx = new ArrayList<>();
        for (int j = 0; j < efxt.size(); j++) efx.add(efxt.getCompound(j));

        String[] hist = new String[10];
        ListTag histTag = tag.getList("pathwayHistory", Tag.TAG_STRING);
        for (int j = 0; j < Math.min(10, histTag.size()); j++) {
            String h = histTag.getString(j);
            hist[j] = h.isEmpty() ? null : h;
        }

        return new StatusSnapshot(
                tag.getString("pathway"),
                tag.getInt("sequence"),
                chars,
                hist,
                tag.getString("uniquenessPathway"),
                tag.getBoolean("hasUniqueness"),
                tag.getInt("uniquenessKillCount"),
                efx,
                tag.getLong("captureTimeMs")
        );
    }
}
