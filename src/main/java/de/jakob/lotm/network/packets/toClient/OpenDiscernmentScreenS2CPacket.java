package de.jakob.lotm.network.packets.toClient;

import com.mojang.datafixers.util.Pair;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record OpenDiscernmentScreenS2CPacket(List<Pair<String, Integer>> saved) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenDiscernmentScreenS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_discernment_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, Pair<String, Integer>> PAIR_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    Pair::getFirst,

                    ByteBufCodecs.INT,
                    Pair::getSecond,

                    Pair::of
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenDiscernmentScreenS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.collection(ArrayList::new, PAIR_CODEC),
                    OpenDiscernmentScreenS2CPacket::saved,

                    OpenDiscernmentScreenS2CPacket::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenDiscernmentScreenS2CPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientHandler.handleDiscernmentScreenPacket(payload);
        });
    }
}
