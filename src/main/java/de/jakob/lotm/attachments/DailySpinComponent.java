package de.jakob.lotm.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

/**
 * Tracks when a player last claimed their daily spin,
 * and when they can next use the Sell Your Soul wheel.
 */
public class DailySpinComponent {

    private static final long SOUL_COOLDOWN_MS = 0L; // no cooldown

    private long lastSpinEpochDay  = -1L;
    private long sellSoulReadyTime = 0L;  // epoch millis when sell-soul becomes available again

    public DailySpinComponent() {}

    // ── Daily Spin ─────────────────────────────────────────────────────────────

    /** True when the player has not yet claimed their spin for today (UTC). */
    public boolean canSpin() {
        long today = System.currentTimeMillis() / 86400000L;
        return lastSpinEpochDay != today;
    }

    /** Records the spin for today. */
    public void markSpun() {
        this.lastSpinEpochDay = System.currentTimeMillis() / 86400000L;
    }

    // ── Sell Your Soul ─────────────────────────────────────────────────────────

    public boolean canSellSoul() {
        return System.currentTimeMillis() >= sellSoulReadyTime;
    }

    /** Returns remaining cooldown in seconds (0 if ready). */
    public long getSellSoulCooldownSeconds() {
        long remaining = sellSoulReadyTime - System.currentTimeMillis();
        return remaining > 0 ? remaining / 1000L : 0L;
    }

    public void markSellSoulUsed() {
        this.sellSoulReadyTime = System.currentTimeMillis() + SOUL_COOLDOWN_MS;
    }

    public long getSellSoulReadyTime() {
        return sellSoulReadyTime;
    }

    public static final IAttachmentSerializer<CompoundTag, DailySpinComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public DailySpinComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
                    DailySpinComponent c = new DailySpinComponent();
                    c.lastSpinEpochDay  = tag.getLong("lastSpinEpochDay");
                    c.sellSoulReadyTime = tag.getLong("sellSoulReadyTime");
                    return c;
                }

                @Override
                public CompoundTag write(DailySpinComponent c, HolderLookup.Provider provider) {
                    CompoundTag tag = new CompoundTag();
                    tag.putLong("lastSpinEpochDay",  c.lastSpinEpochDay);
                    tag.putLong("sellSoulReadyTime", c.sellSoulReadyTime);
                    return tag;
                }
            };
}
