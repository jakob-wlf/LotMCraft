package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.DeathImprintData;
import de.jakob.lotm.sefirah.SefirahHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.UUID;

/**
 * Client → Server: River owner sets (or clears) the ability seals for an imprinted player.
 *
 * targetUUID — the player whose abilities are being sealed
 * abilityIds — list of up to 2 ability IDs to seal (empty = clear all seals)
 */
public record SetAbilitySealPacket(String targetUUIDStr, List<String> abilityIds) implements CustomPacketPayload {

    public static final Type<SetAbilitySealPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "set_ability_seal"));

    public static final StreamCodec<ByteBuf, SetAbilitySealPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,                                   SetAbilitySealPacket::targetUUIDStr,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list(2)),      SetAbilitySealPacket::abilityIds,
            SetAbilitySealPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SetAbilitySealPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isServer()) return;
            if (!(context.player() instanceof ServerPlayer owner)) return;

            // Security: only the river owner may seal abilities
            if (!"river_of_eternal_darkness".equals(SefirahHandler.getClaimedSefirot(owner))) return;

            UUID targetUUID;
            try {
                targetUUID = UUID.fromString(packet.targetUUIDStr());
            } catch (IllegalArgumentException ignored) {
                return;
            }

            DeathImprintData data = DeathImprintData.get(owner.server);

            // Security: target must have at least 2 death imprints
            if (data.getImprintCount(targetUUID) < 2) return;

            // Apply seals (capped at 2)
            List<String> toSeal = packet.abilityIds().subList(0, Math.min(2, packet.abilityIds().size()));

            // Only act if the sealed set actually changed — opening the screen and
            // closing without making a selection must not send any message.
            List<String> previouslySealed = data.getSealedAbilities(targetUUID);
            boolean changed = !new java.util.HashSet<>(toSeal).equals(new java.util.HashSet<>(previouslySealed));
            if (!changed) return;

            data.setSealedAbilities(targetUUID, toSeal);

            // Immediately apply / remove the seal in the live target's DisabledAbilitiesComponent
            // (if they are online) so the seal takes effect without requiring a relog.
            ServerPlayer target = owner.server.getPlayerList().getPlayer(targetUUID);
            if (target != null) {
                data.reapplySealedAbilities(target);
            }

            // Notify the target if they are online
            if (target != null) {
                if (toSeal.isEmpty()) {
                    target.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            "§6The River owner has lifted your ability seals."));
                } else {
                    StringBuilder names = new StringBuilder();
                    for (String id : toSeal) {
                        de.jakob.lotm.abilities.core.Ability ab = LOTMCraft.abilityHandler.getById(id);
                        names.append(ab != null ? ab.getName().getString() : id);
                        if (toSeal.indexOf(id) < toSeal.size() - 1) names.append(", ");
                    }
                    target.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            "§4✦ The River of Eternal Darkness has sealed your abilities: §c" + names));
                }
            }

            // Notify the owner
            owner.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§aAbility seals applied."));
        });
    }
}
