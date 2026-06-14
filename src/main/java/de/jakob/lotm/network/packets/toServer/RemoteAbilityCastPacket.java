package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.attachments.AbilityWheelComponent;
import de.jakob.lotm.attachments.ModAttachments;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/**
 * Client → Server: the player wants to remotely cast their selected ability at a specific target.
 * Used by the Anchors panel in IntrospectScreen and the Cast button in HonorificNamesScreen.
 *
 * The target UUID is stored in the caster's persistent data under "lotm_remote_ability_target"
 * so individual abilities can optionally read it for directed targeting.
 */
public record RemoteAbilityCastPacket(UUID targetUUID) implements CustomPacketPayload {

    public static final Type<RemoteAbilityCastPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "remote_ability_cast"));

    public static final StreamCodec<ByteBuf, RemoteAbilityCastPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString),
                    RemoteAbilityCastPacket::targetUUID,
                    RemoteAbilityCastPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RemoteAbilityCastPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer caster)) return;
            if (!(caster.level() instanceof ServerLevel serverLevel)) return;
            if (caster.getServer() == null) return;

            // Resolve the target (must be online)
            ServerPlayer target = caster.getServer().getPlayerList().getPlayer(packet.targetUUID());
            if (target == null) return;

            // Store target UUID in caster persistent data so abilities can read it
            caster.getPersistentData().putUUID("lotm_remote_ability_target", packet.targetUUID());

            // Get selected ability from wheel
            AbilityWheelComponent wheel = caster.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
            if (wheel.getAbilities().isEmpty()) return;

            int selectedIndex = wheel.getSelectedAbility();
            if (selectedIndex < 0 || selectedIndex >= wheel.getAbilities().size()) return;

            String abilityId = wheel.getAbilities().get(selectedIndex).split(":")[0];
            Ability ability = LOTMCraft.abilityHandler.getById(abilityId);
            if (ability == null) return;

            ability.useAbility(serverLevel, caster, true, true, true);

            // Clean up remote target hint
            caster.getPersistentData().remove("lotm_remote_ability_target");
        });
    }
}
