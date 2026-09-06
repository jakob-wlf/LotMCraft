package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.util.helper.AbilityBarHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record SyncAbilityBarPacket(List<String> abilities) implements CustomPacketPayload {

    public static final Type<SyncAbilityBarPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_ability_bar"));

    public static final StreamCodec<ByteBuf, SyncAbilityBarPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
            SyncAbilityBarPacket::abilities,
            SyncAbilityBarPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncAbilityBarPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            AbilityBarHelper.setAbilities(ClientHandler.getPlayer(), new ArrayList<>(packet.abilities));
        });
    }
}