package de.jakob.lotm.network.packets;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.SelectableAbilityItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AbilitySelectionPacket(Item item, int selectedAbility) implements CustomPacketPayload {
    public static final Type<AbilitySelectionPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "select_ability"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AbilitySelectionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.ITEM),
            AbilitySelectionPacket::item,
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
            updatePlayerAbilitySelection(player, packet.selectedAbility(), packet.item());
        });
    }

    private static void updatePlayerAbilitySelection(ServerPlayer player, int selectedAbility, Item item) {
        if (item instanceof SelectableAbilityItem selectableItem) {
            selectableItem.setSelectedAbility(player, selectedAbility);
        }
    }
}