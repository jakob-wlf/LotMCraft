package de.jakob.lotm.network.packets.toServer;

import com.mojang.datafixers.util.Pair;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.helper.PureIdealismUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DiscernmentSelectedC2SPacket(Pair<String, Integer> pair) implements CustomPacketPayload {
    public static final Type<DiscernmentSelectedC2SPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "discernment_selected"));

    public static final StreamCodec<RegistryFriendlyByteBuf, Pair<String, Integer>> PAIR_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    Pair::getFirst,

                    ByteBufCodecs.INT,
                    Pair::getSecond,

                    Pair::of
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, DiscernmentSelectedC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    PAIR_CODEC,
                    DiscernmentSelectedC2SPacket::pair,

                    DiscernmentSelectedC2SPacket::new
            );


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DiscernmentSelectedC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();

            PureIdealismUtil.startDiscernment(player, packet.pair.getFirst(), packet.pair.getSecond());
        });
    }
}
