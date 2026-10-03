package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ApotheosisComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.UniquenessComponent;
import de.jakob.lotm.gamerule.ModGameRules;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncApotheosisPacket;
import de.jakob.lotm.util.BeyonderData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Sent by the client when the player clicks the "Begin Apotheosis" button in the Apotheosis tab.
 * Server validates conditions and initiates apotheosis.
 */
public record RequestUniquenessApotheosisPacket() implements CustomPacketPayload {

    /** Kills required for apotheosis - must match client-side check in IntrospectScreen. */
    public static final int KILLS_REQUIRED_FOR_APOTHEOSIS = 500;

    public static final Type<RequestUniquenessApotheosisPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "request_uniqueness_apotheosis"));

    public static final StreamCodec<ByteBuf, RequestUniquenessApotheosisPacket> STREAM_CODEC =
            StreamCodec.unit(new RequestUniquenessApotheosisPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestUniquenessApotheosisPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isServerbound() && context.player() instanceof ServerPlayer player) {
                handleApotheosisRequest(player);
            }
        });
    }

    private static void handleApotheosisRequest(ServerPlayer player) {
        UniquenessComponent comp = player.getData(ModAttachments.UNIQUENESS_COMPONENT);

        if (!comp.hasUniqueness()) {
            player.displayClientMessage(
                    Component.translatable("lotm.uniqueness.fail"),
                    true
            );
            return;
        }

        String pathway = comp.getUniquenessPathway();
        if (pathway.isEmpty() || !pathway.equalsIgnoreCase(BeyonderData.getPathway(player))) {
            player.displayClientMessage(
                    Component.translatable("lotm.uniqueness.fail"),
                    true
            );
            return;
        }

        int charStack = BeyonderData.getCurrentCharStack(player);
        int requiredStack = player.serverLevel().getGameRules().getInt(ModGameRules.CHARSTACK_REQUIRED_FOR_APOTHEOSIS);
        int killCount = comp.getKillCount();

        if (charStack < requiredStack || killCount < KILLS_REQUIRED_FOR_APOTHEOSIS) {
            player.displayClientMessage(
                    Component.translatable("lotm.uniqueness.fail"),
                    true
            );
            return;
        }

        if (!player.serverLevel().dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) {
            player.displayClientMessage(
                    Component.translatable("lotm.uniqueness.overworld"),
                    true
            );
            return;
        }

        // Start apotheosis
        ApotheosisComponent apotheosis = player.getData(ModAttachments.APOTHEOSIS_COMPONENT);
        if (apotheosis.getApotheosisTicksLeft() > 0) return;

        int color = BeyonderData.pathwayInfos.get(pathway).color();
        player.level().players().forEach(p ->
                p.displayClientMessage(
                        Component.literal(player.getName().getString())
                                .append(Component.translatable("lotm.uniqueness.start"))
                                .withColor(color),
                        false
                )
        );

        apotheosis.setPathway(pathway);
        apotheosis.setApotheosisTicksLeftAndSync(20 * 60 * 5, (ServerLevel) player.level(), player);

        player.level().players().forEach(p -> p.playSound(SoundEvents.WITHER_SPAWN));
        player.closeContainer();
    }
}
