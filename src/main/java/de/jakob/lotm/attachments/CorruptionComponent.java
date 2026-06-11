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

    public CorruptionComponent() {
    }

    public float getCorruption() {
        return corruption;
    }

    public void setCorruption(float corruption) {
        this.corruption = corruption;
    }

    public void setCorruptionAndSync(float corruption, LivingEntity entity) {
        this.corruption = Math.clamp(corruption, 0.0f, 1.0f);

        if (entity instanceof ServerPlayer player) {
            PacketHandler.sendToPlayer(player, new SyncCorruptionPacket(this.corruption, entity.getId()));
        }
    }

    public void increaseCorruptionAndSync(float amount, LivingEntity entity) {
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
                    return component;
                }

                @Override
                public CompoundTag write(CorruptionComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    tag.putFloat("corruption", component.corruption);
                    return tag;
                }
            };
}
