package de.jakob.lotm.attachments;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncCorruptionPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class CorruptionComponent {

    private float corruption = 0.0f;
    private boolean leakageExempt = false;

    public CorruptionComponent() {
    }

    public float getCorruption() {
        return corruption;
    }

    public void setCorruption(float corruption) {
        this.corruption = corruption;
    }

    public boolean isLeakageExempt() {
        return leakageExempt;
    }

    public void setLeakageExempt(boolean leakageExempt) {
        this.leakageExempt = leakageExempt;
    }

    public void setCorruptionAndSync(float corruption, LivingEntity entity) {
        this.corruption = Math.clamp(corruption, 0.0f, 1.0f);

        if (entity instanceof ServerPlayer player) {
            PacketHandler.sendToPlayer(player, new SyncCorruptionPacket(this.corruption, entity.getId()));
        }
    }

    public void increaseCorruptionAndSync(float amount, LivingEntity entity) {
        // Block positive corruption gains for original sefirot owners inside their own dimension
        if (amount > 0 && entity instanceof ServerPlayer sp && sp.server != null) {
            SefirotData sd = SefirotData.get(sp.server);
            String ownedSef = sd.getClaimedSefirot(sp.getUUID());
            if (ownedSef != null && !ownedSef.isEmpty()) {
                java.util.UUID firstOwner = sd.getFirstOwner(ownedSef);
                if (firstOwner != null && firstOwner.equals(sp.getUUID())) {
                    net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dim =
                            de.jakob.lotm.beyonders.sefirah.SefirahHandler.getSefirotDimensionKey(ownedSef);
                    if (dim != null && sp.level().dimension().equals(dim)) {
                        return; // shielded — no corruption gain inside own sefirot
                    }
                }
            }
        }
        this.corruption += amount;

        if (this.corruption > 1.0f) this.corruption = 1.0f;
        else if (this.corruption < 0.0f) this.corruption = 0.0f;

        if (entity instanceof ServerPlayer player) {
            PacketHandler.sendToPlayer(player, new SyncCorruptionPacket(this.corruption, entity.getId()));
        }
    }

    public void decreaseCorruptionAndSync(float amount, LivingEntity entity) {
        increaseCorruptionAndSync(-amount, entity);
    }

    public static final IAttachmentSerializer<CompoundTag, CorruptionComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public CorruptionComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    CorruptionComponent component = new CorruptionComponent();
                    component.corruption = tag.getFloat("corruption");
                    component.leakageExempt = tag.getBoolean("leakageExempt");
                    return component;
                }

                @Override
                public CompoundTag write(CorruptionComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    tag.putFloat("corruption", component.corruption);
                    tag.putBoolean("leakageExempt", component.leakageExempt);
                    return tag;
                }
            };
}
