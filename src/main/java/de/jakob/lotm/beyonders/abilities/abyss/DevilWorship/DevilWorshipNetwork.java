package de.jakob.lotm.beyonders.abilities.abyss.DevilWorship;

import de.jakob.lotm.LOTMCraft;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DevilWorshipNetwork {
    public record PlayerEntry(String uuid, String name) {
        public static final StreamCodec<ByteBuf, PlayerEntry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, PlayerEntry::uuid,
                ByteBufCodecs.STRING_UTF8, PlayerEntry::name,
                PlayerEntry::new);
    }
    public record OpenSummonScreen(int pleasure, String tribute, List<PlayerEntry> players, List<String> pathways)
            implements CustomPacketPayload {
        public static final Type<OpenSummonScreen> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_devil_summon_screen"));
        public static final StreamCodec<FriendlyByteBuf, OpenSummonScreen> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, OpenSummonScreen::pleasure,
                ByteBufCodecs.STRING_UTF8, OpenSummonScreen::tribute,
                PlayerEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), OpenSummonScreen::players,
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), OpenSummonScreen::pathways,
                OpenSummonScreen::new);
        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
        public static void handle(OpenSummonScreen packet, IPayloadContext context) {context.enqueueWork(() -> {if (context.flow().getReceptionSide().isClient()) {DevilWorshipScreen.open(packet.pleasure(), packet.tribute(), packet.players(), packet.pathways());}});}
    }
    public record ChooseMission(int mission, String target, int seekReward, String seekPathway, int seekSequence)
            implements CustomPacketPayload {
        public static final Type<ChooseMission> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "choose_devil_mission"));
        public static final StreamCodec<FriendlyByteBuf, ChooseMission> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, ChooseMission::mission,
                ByteBufCodecs.STRING_UTF8, ChooseMission::target,
                ByteBufCodecs.VAR_INT, ChooseMission::seekReward,
                ByteBufCodecs.STRING_UTF8, ChooseMission::seekPathway,
                ByteBufCodecs.VAR_INT, ChooseMission::seekSequence,
                ChooseMission::new);
        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
        public static void handle(ChooseMission packet, IPayloadContext context) {
            context.enqueueWork(() -> {if (context.player() instanceof ServerPlayer player) {DevilWorshipAbility.handleChoice(player, packet.mission(), packet.target(), packet.seekReward(), packet.seekPathway(), packet.seekSequence());}});
        }
    }
}