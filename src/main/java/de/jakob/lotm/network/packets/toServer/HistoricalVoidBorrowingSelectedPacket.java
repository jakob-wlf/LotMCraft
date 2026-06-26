package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.fool.HistoricalVoidSummoningAbility;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public record HistoricalVoidBorrowingSelectedPacket (String SelectedOption) implements CustomPacketPayload {
    public static final Type<HistoricalVoidBorrowingSelectedPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "historical_void_borrowing_selected"));

    public static final StreamCodec<RegistryFriendlyByteBuf, HistoricalVoidBorrowingSelectedPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    HistoricalVoidBorrowingSelectedPacket::SelectedOption,
                    HistoricalVoidBorrowingSelectedPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(HistoricalVoidBorrowingSelectedPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            if (packet.SelectedOption.equals("Borrow Health")) {
                HistoricalVoidSummoningAbility.historicalVoidBorrowHealth(player, player.serverLevel());
            }
            else if (packet.SelectedOption.equals("Borrow Spirituality")) {
                HistoricalVoidSummoningAbility.historicalVoidBorrowSpirituality(player, player.serverLevel());
            }
            else if (packet.SelectedOption.equals("Borrow Cleansed State")) {
                HistoricalVoidSummoningAbility.historicalVoidBorrowCleansedState(player, player.serverLevel());
            }
            else if (packet.SelectedOption.equals("Borrow Sequence")) {
                HistoricalVoidSummoningAbility.historicalVoidBorrowSequence(player, player.serverLevel());
            }
        });
    }
}

