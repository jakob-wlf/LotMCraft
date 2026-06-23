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

public record HybridMobSyncS2CPacket(int entityId, CompoundTag hybridData) implements CustomPacketPayload {
    
    public static final Type<HybridMobSyncS2CPacket> TYPE = 
        new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "hybrid_mob_sync"));

    public static final StreamCodec<FriendlyByteBuf, HybridMobSyncS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    HybridMobSyncS2CPacket::entityId,
                    ByteBufCodecs.COMPOUND_TAG,
                    HybridMobSyncS2CPacket::hybridData,
                    HybridMobSyncS2CPacket::new
            );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(HybridMobSyncS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = context.player().level().getEntity(packet.entityId());
            
            if(entity instanceof LivingEntity living) {
                living.getPersistentData().put("HybridMobData", packet.hybridData());
                living.refreshDimensions();
            }
        });
    }
}