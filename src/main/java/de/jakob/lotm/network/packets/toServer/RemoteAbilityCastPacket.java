package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.attachments.AbilityWheelComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record RemoteAbilityCastPacket(UUID targetUUID) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RemoteAbilityCastPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "remote_ability_cast"));

    public static final StreamCodec<FriendlyByteBuf, RemoteAbilityCastPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> buf.writeUUID(pkt.targetUUID()),
                    buf -> new RemoteAbilityCastPacket(buf.readUUID())
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RemoteAbilityCastPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                AbilityWheelComponent component = serverPlayer.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);

                if (component.getAbilities().isEmpty()) {
                    return;
                }

                int selectedIndex = component.getSelectedAbility();
                if (selectedIndex >= 0 && selectedIndex < component.getAbilities().size()) {
                    String entry = component.getAbilities().get(selectedIndex);
                    String abilityId = entry.split(":")[0];
                    Ability ability = LOTMCraft.abilityHandler.getById(abilityId);

                    if (ability != null && serverPlayer.level() instanceof ServerLevel serverLevel) {
                        // Set the remote target for targeting methods in AbilityUtil
                        AbilityUtil.setRemoteCastTargetUUID(packet.targetUUID());
                        
                        try {
                            if (ability instanceof SelectableAbility selectableAbility) {
                                int subIndex = getIndex(entry);
                                if (subIndex != -1) {
                                    selectableAbility.addSubAbilityOverride(serverPlayer, subIndex);
                                }
                            }

                            // Use the ability - usually this calls getTargetEntity/Location internally
                            ability.useAbility(serverLevel, serverPlayer, true, true, true);
                        } finally {
                            // Always clear the remote target
                            AbilityUtil.clearRemoteCastTargetUUID();
                        }
                    }
                }
            }
        });
    }

    private static int getIndex(String s) {
        String[] parts = s.split(":");
        if (parts.length < 2) return -1;
        try {
            return Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
