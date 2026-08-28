package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.block.entity.RitualisticTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RitualSaveLinesPacket(BlockPos pos, String line1, String line2, String line3) implements CustomPacketPayload {

    public static final Type<RitualSaveLinesPacket> TYPE =
            new Type<>(ResourceLocation_fromNamespaceAndPath());

    public static final StreamCodec<RegistryFriendlyByteBuf, RitualSaveLinesPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, RitualSaveLinesPacket::pos,
            net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8, RitualSaveLinesPacket::line1,
            net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8, RitualSaveLinesPacket::line2,
            net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8, RitualSaveLinesPacket::line3,
            RitualSaveLinesPacket::new
    );

    private static net.minecraft.resources.ResourceLocation ResourceLocation_fromNamespaceAndPath() {
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "ritual_save_lines");
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RitualSaveLinesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getBlockEntity(packet.pos()) instanceof RitualisticTableBlockEntity be) {
                be.setHonorificLines(packet.line1(), packet.line2(), packet.line3());
            }
        });
    }
}