package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.tyrant.LightningStormAbility;
import de.jakob.lotm.beyonders.sefirah.SefirotAuthorityManager;
import de.jakob.lotm.attachments.EnvisionBlasphemyTriggerData;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncEnvisionTriggerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/**
 * Client → Server: Envisioning > Target > Blasphemy action.
 *
 * Two modes (determined by {@link #instant}):
 *  true  – fire the LEODERO effect on {@link #targetName} immediately.
 *  false – register {@link #triggerWord} so that when the sender next says it in chat
 *          the LEODERO effect fires on {@link #targetName} (one-shot, then cleared).
 */
public record RequestEnvisionBlasphemyPacket(String targetName, String triggerWord, boolean instant)
        implements CustomPacketPayload {

    public static final Type<RequestEnvisionBlasphemyPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "request_envision_blasphemy"));

    public static final StreamCodec<FriendlyByteBuf, RequestEnvisionBlasphemyPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeUtf(pkt.targetName());
                        buf.writeUtf(pkt.triggerWord());
                        buf.writeBoolean(pkt.instant());
                    },
                    buf -> new RequestEnvisionBlasphemyPacket(buf.readUtf(), buf.readUtf(), buf.readBoolean())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    private static final LightningStormAbility LEODERO_STORM =
            new LightningStormAbility("lightning_storm_leodero_envision");

    public static void handle(RequestEnvisionBlasphemyPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sender)) return;

            if (packet.instant()) {
                // Fire immediately on the target
                fireOnPlayer(sender, packet.targetName());
            } else {
                // Register trigger word — persists until changed. One word per caster.
                String word = packet.triggerWord().trim();
                if (word.isEmpty()) return;

                EnvisionBlasphemyTriggerData data = EnvisionBlasphemyTriggerData.get(sender.getServer());
                data.setTrigger(sender.getUUID(), word);

                // Sync current word back to the client
                PacketHandler.sendToPlayer(sender, new SyncEnvisionTriggerPacket(word));

                sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§aEnvisioning trigger set: when anyone says §e\"" + word +
                        "§a\" in chat, LEODERO fires on them (60s cooldown)."));
            }
        });
    }

    /** Fires LEODERO on a named player; sends feedback to {@code sender}. */
    public static void fireOnPlayer(ServerPlayer sender, String targetName) {
        ServerPlayer target = sender.getServer().getPlayerList().getPlayerByName(targetName);
        if (target == null) {
            sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§cTarget player not found: " + targetName));
            return;
        }
        if (SefirotAuthorityManager.blocksEnvisioningTarget(target, sender)) {
            sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§cThat player cannot be targeted by Envisioning."));
            return;
        }
        fireLeoderoOn(target, sender.getUUID(), sender);
    }

    /**
     * Fires the LEODERO lightning storm on {@code victim}.
     * Checks and enforces the 60-second cooldown keyed on {@code casterUUID}.
     * {@code casterForFeedback} may be null (chat-triggered fires have no single feedback target).
     */
    public static void fireLeoderoOn(ServerPlayer victim, UUID casterUUID, ServerPlayer casterForFeedback) {
        EnvisionBlasphemyTriggerData data = EnvisionBlasphemyTriggerData.get(victim.getServer());
        ServerPlayer caster = victim.getServer().getPlayerList().getPlayer(casterUUID);
        if (caster != null && SefirotAuthorityManager.blocksEnvisioningTarget(victim, caster)) {
            if (casterForFeedback != null) {
                casterForFeedback.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§cThat player cannot be targeted by Envisioning."));
            }
            return;
        }

        if (!data.canFire(casterUUID)) {
            long secs = data.getCooldownRemainingSeconds(casterUUID);
            if (casterForFeedback != null) {
                casterForFeedback.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§cLEODERO is on cooldown for " + secs + "s."));
            }
            return;
        }

        data.recordFire(casterUUID);

        if (!(victim.level() instanceof ServerLevel serverLevel)) return;

        net.minecraft.world.entity.decoration.ArmorStand dummy =
                new net.minecraft.world.entity.decoration.ArmorStand(
                        serverLevel, victim.getX(), victim.getY(), victim.getZ());
        dummy.setYRot(victim.getYRot());
        dummy.setXRot(victim.getXRot());
        serverLevel.addFreshEntity(dummy);

        de.jakob.lotm.util.scheduling.ServerScheduler.scheduleDelayed(0, () -> {
            LEODERO_STORM.onAbilityUse(serverLevel, dummy);
            dummy.discard();
        }, serverLevel);

        if (casterForFeedback != null) {
            casterForFeedback.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§bLEODERO! §7Invoked on §f" + victim.getName().getString()));
        }
    }
}
