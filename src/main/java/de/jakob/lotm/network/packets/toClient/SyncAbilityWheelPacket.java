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

public record SyncAbilityWheelPacket(List<String> abilities, int selectedAbility) implements CustomPacketPayload {

    public static final Type<SyncAbilityWheelPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_ability_wheel"));

    public static final StreamCodec<ByteBuf, SyncAbilityWheelPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
            SyncAbilityWheelPacket::abilities,
            ByteBufCodecs.VAR_INT,
            SyncAbilityWheelPacket::selectedAbility,
            SyncAbilityWheelPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncAbilityWheelPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientData.setAbilityWheelData(new ArrayList<>(packet.abilities()), packet.selectedAbility());
        });
    }
}