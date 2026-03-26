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

public record SyncCopiedAbilitiesPacket(List<String> abilityIds, List<String> copyTypes, List<Integer> remainingUses) implements CustomPacketPayload {

    public static final Type<SyncCopiedAbilitiesPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_copied_abilities"));

    public static final StreamCodec<ByteBuf, SyncCopiedAbilitiesPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
            SyncCopiedAbilitiesPacket::abilityIds,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
            SyncCopiedAbilitiesPacket::copyTypes,
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()),
            SyncCopiedAbilitiesPacket::remainingUses,
            SyncCopiedAbilitiesPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncCopiedAbilitiesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientData.setCopiedAbilityData(
                    new ArrayList<>(packet.abilityIds()),
                    new ArrayList<>(packet.copyTypes()),
                    new ArrayList<>(packet.remainingUses())
            );
        });
    }
}
