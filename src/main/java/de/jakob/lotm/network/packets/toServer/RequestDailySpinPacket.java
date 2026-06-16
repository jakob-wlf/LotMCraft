package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.DailySpinComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.entity.custom.uniqueness.UniquenessEntity;
import de.jakob.lotm.gui.custom.DailySpin.DailySpinHandler;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.OpenDailySpinScreenPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

import static de.jakob.lotm.gui.custom.DailySpin.DailySpinHandler.UNIQUENESS_ITEMS;

/** Client → Server: player requests their daily spin. */
public record RequestDailySpinPacket() implements CustomPacketPayload {

    public static final Type<RequestDailySpinPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "request_daily_spin"));

    public static final StreamCodec<ByteBuf, RequestDailySpinPacket> STREAM_CODEC =
            StreamCodec.unit(new RequestDailySpinPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(RequestDailySpinPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isServer()) return;
            if (!(context.player() instanceof ServerPlayer player)) return;

            if (!player.level().getGameRules().getBoolean(de.jakob.lotm.gamerule.ModGameRules.DO_DAILY_SPIN_WHEEL)) {
                player.sendSystemMessage(Component.literal("§cDaily Spin is disabled."));
                return;
            }

            DailySpinComponent component = player.getData(ModAttachments.DAILY_SPIN_COMPONENT);

            if (!component.canSpin()) {
                // Already spun today — show the "already spun" screen
                PacketHandler.sendToPlayer(player, new OpenDailySpinScreenPacket(
                        List.of("✦ Already claimed today! ✦", "Come back tomorrow.", "Come back tomorrow.",
                                "Come back tomorrow.", "Come back tomorrow.", "Come back tomorrow.",
                                "Come back tomorrow.", "Come back tomorrow.", "Come back tomorrow.",
                                "Come back tomorrow.", "Come back tomorrow.", "Come back tomorrow.",
                                "Come back tomorrow.", "Come back tomorrow.", "Come back tomorrow.",
                                "Come back tomorrow.", "Come back tomorrow.", "Come back tomorrow.",
                                "Come back tomorrow.", "Come back tomorrow."),
                        0, false));
                return;
            }

            // Roll reward, give immediately, mark as spun
            DailySpinHandler.SpinResult result = DailySpinHandler.buildDailySpin(player);
            if (!result.reward().isEmpty()) {
                if (UNIQUENESS_ITEMS.contains(result.reward().getItem())){
                    UniquenessEntity uniquenessEntity = new UniquenessEntity(player.level(), player.position(), result.reward().getItem().getDescriptionId());
                    player.level().addFreshEntity(uniquenessEntity);
                    component.markSpun();

                    PacketHandler.sendToPlayer(player, new OpenDailySpinScreenPacket(
                            result.reelNames(), result.landingIndex(), true));
                    return;
                }
                    player.getInventory().add(result.reward().copy());
            }
            component.markSpun();

            PacketHandler.sendToPlayer(player, new OpenDailySpinScreenPacket(
                    result.reelNames(), result.landingIndex(), true));
        });
    }
}
