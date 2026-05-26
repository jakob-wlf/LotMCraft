package de.jakob.lotm.network.packets.toServer;

import com.mojang.datafixers.util.Pair;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.PureIdealismUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DiscernmentSelectedPacket(Pair<String, Integer> pair) implements CustomPacketPayload {
    public static final Type<DiscernmentSelectedPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "discernment_selected"));

    public static final StreamCodec<RegistryFriendlyByteBuf, Pair<String, Integer>> PAIR_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    Pair::getFirst,

                    ByteBufCodecs.INT,
                    Pair::getSecond,

                    Pair::of
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, DiscernmentSelectedPacket> STREAM_CODEC =
            StreamCodec.composite(
                    PAIR_CODEC,
                    DiscernmentSelectedPacket::pair,

                    DiscernmentSelectedPacket::new
            );


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DiscernmentSelectedPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();

            PureIdealismUtil.startDiscernment(player, packet.pair.getFirst(), packet.pair.getSecond());
        });
    }
}
