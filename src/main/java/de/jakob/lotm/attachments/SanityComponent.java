package de.jakob.lotm.attachments;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncSanityPacket;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class SanityComponent {

    private float sanity = 1.0f;
    private int virtualPersonaStacks = 0;

    public SanityComponent() {
    }

    public float getSanity() {
        return sanity;
    }

    public void setSanity(float sanity) {
        this.sanity = sanity;
    }

    public void setSanityAndSync(float sanity, LivingEntity entity) {
        this.sanity = Math.clamp(sanity, 0.0f, 1.0f);

        if (entity instanceof ServerPlayer player) {
            PacketHandler.sendToPlayer(player, new SyncSanityPacket(this.sanity, entity.getId()));
        }
    }

    public void increaseSanityAndSync(float amount, LivingEntity entity) {
        if (amount < 0) {
            // Consume one virtual persona stack to fully cancel this sanity reduction
            if (virtualPersonaStacks > 0) {
                virtualPersonaStacks--;
                return;
            }

            if (BeyonderData.isBeyonder(entity)) {
                amount *= (float) BeyonderData.getSanityDecreaseMultiplierForSequence(BeyonderData.getSequence(entity));
            }
        }

        this.sanity += amount;

        if (this.sanity > 1.0f) this.sanity = 1.0f;
        else if (this.sanity < 0.0f) this.sanity = 0.0f;

        if (entity instanceof ServerPlayer player) {
            PacketHandler.sendToPlayer(player, new SyncSanityPacket(sanity, entity.getId()));
        }
    }

    public void decreaseSanityAndSync(float amount, LivingEntity entity) {
        increaseSanityAndSync(-amount, entity);
    }

    public void increaseSanityWithSequenceDifference(float amount, LivingEntity entity, int casterSequence, int targetSequence) {
        if (amount < 0) {
            // Consume one virtual persona stack to fully cancel this sanity reduction
            if (virtualPersonaStacks > 0) {
                virtualPersonaStacks--;
                return;
            }

            if (entity instanceof ServerPlayer player && BeyonderData.isBeyonder(player)) {
                amount *= (float) BeyonderData.getSanityDecreaseMultiplierForSequence(BeyonderData.getSequence(player));
            }
        }

        amount *= getSanityDifferenceMultiplier(casterSequence, targetSequence);

        this.sanity += amount;

        if (this.sanity > 1.0f) this.sanity = 1.0f;
        else if (this.sanity < 0.0f) this.sanity = 0.0f;

        if (entity instanceof ServerPlayer player) {
            PacketHandler.sendToPlayer(player, new SyncSanityPacket(sanity, entity.getId()));
        }
    }

    public void decreaseSanityWithSequenceDifference(float amount, LivingEntity entity, int casterSequence, int targetSequence) {
        increaseSanityWithSequenceDifference(-amount, entity, casterSequence, targetSequence);
    }

    private float getSanityDifferenceMultiplier(int casterSequence, int targetSequence) {
        if(casterSequence < targetSequence) {
            return (targetSequence - casterSequence) * 0.4f + 1;
        }
        else if (casterSequence > targetSequence) {
            return 1.0f / ((casterSequence - targetSequence) * 0.4f + 1);
        }
        else {
            return 1.0f;
        }
    }

    public int getVirtualPersonaStacks() {
        return virtualPersonaStacks;
    }

    public void addVirtualPersonaStack() {
        virtualPersonaStacks++;
    }

    public void setVirtualPersonaStacks(int stacks) {
        this.virtualPersonaStacks = Math.clamp(stacks, 0, 10);
    }

    public static final IAttachmentSerializer<CompoundTag, SanityComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public SanityComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    SanityComponent component = new SanityComponent();
                    component.sanity = tag.getFloat("sanity");
                    component.virtualPersonaStacks = tag.getInt("virtualPersonaStacks");
                    return component;
                }

                @Override
                public CompoundTag write(SanityComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    tag.putFloat("sanity", component.sanity);
                    tag.putInt("virtualPersonaStacks", component.virtualPersonaStacks);
                    return tag;
                }
            };
}
