package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record HybridMobSyncPacket(int entityId, CompoundTag hybridData) implements CustomPacketPayload {
    
    public static final Type<HybridMobSyncPacket> TYPE = 
        new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "hybrid_mob_sync"));

    public static final StreamCodec<FriendlyByteBuf, HybridMobSyncPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    HybridMobSyncPacket::entityId,
                    ByteBufCodecs.COMPOUND_TAG,
                    HybridMobSyncPacket::hybridData,
                    HybridMobSyncPacket::new
            );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(HybridMobSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = context.player().level().getEntity(packet.entityId());
            
            if(entity instanceof LivingEntity living) {
                living.getPersistentData().put("HybridMobData", packet.hybridData());
                living.refreshDimensions();
            }
        });
    }
}