package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AbilitySelectionC2SPacket(String abilityId, int selectedAbility) implements CustomPacketPayload {
    public static final Type<AbilitySelectionC2SPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "select_ability"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AbilitySelectionC2SPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            AbilitySelectionC2SPacket::abilityId,
            ByteBufCodecs.INT,
            AbilitySelectionC2SPacket::selectedAbility,
            AbilitySelectionC2SPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AbilitySelectionC2SPacket packet, IPayloadContext context) {
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