package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.data.ClientData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record SyncCopiedAbilitiesS2CPacket(List<String> abilityIds, List<String> copyTypes, List<Integer> remainingUses) implements CustomPacketPayload {

    public static final Type<SyncCopiedAbilitiesS2CPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_copied_abilities"));

    public static final StreamCodec<ByteBuf, SyncCopiedAbilitiesS2CPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
            SyncCopiedAbilitiesS2CPacket::abilityIds,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
            SyncCopiedAbilitiesS2CPacket::copyTypes,
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()),
            SyncCopiedAbilitiesS2CPacket::remainingUses,
            SyncCopiedAbilitiesS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncCopiedAbilitiesS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientData.setCopiedAbilityData(
                    new ArrayList<>(packet.abilityIds()),
                    new ArrayList<>(packet.copyTypes()),
                    new ArrayList<>(packet.remainingUses())
            );
        });
    }
}
