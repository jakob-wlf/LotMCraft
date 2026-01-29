package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.SelectableAbility;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AbilitySelectionPacket(String abilityId, int selectedAbility) implements CustomPacketPayload {
    public static final Type<AbilitySelectionPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "select_ability"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AbilitySelectionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            AbilitySelectionPacket::abilityId,
            ByteBufCodecs.INT,
            AbilitySelectionPacket::selectedAbility,
            AbilitySelectionPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AbilitySelectionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            Ability ability = LOTMCraft.abilityHandler.getById(packet.abilityId());
            if(!(ability instanceof SelectableAbility selectableAbility)) {
                return;
            }
            updatePlayerAbilitySelection(player, packet.selectedAbility(), selectableAbility);
        });
    }

    private static void updatePlayerAbilitySelection(ServerPlayer player, int selectedAbility, SelectableAbility ability) {
        ability.setSelectedAbility(player, selectedAbility);
    }
}