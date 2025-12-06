package de.jakob.lotm.attachments;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncFogPacket;
import de.jakob.lotm.network.packets.toClient.SyncSanityPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class SanityComponent {

    private float sanity = 1.0f;

    public SanityComponent() {
    }

    public float getSanity() {
        return sanity;
    }

    public void setSanity(float sanity) {
        this.sanity = sanity;
    }

    public void setSanityAndSync(float sanity, LivingEntity entity) {
        this.sanity = sanity;
        if(!(entity instanceof ServerPlayer player)) return;
        PacketHandler.sendToPlayer(player, new SyncSanityPacket(sanity));
    }

    public static final IAttachmentSerializer<CompoundTag, SanityComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public SanityComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider lookup) {
                    SanityComponent component = new SanityComponent();
                    component.sanity = tag.getFloat("sanity");
                    return component;
                }

                @Override
                public CompoundTag write(SanityComponent component, HolderLookup.Provider lookup) {
                    CompoundTag tag = new CompoundTag();
                    tag.putFloat("sanity", component.sanity);
                    return tag;
                }
            };
}
