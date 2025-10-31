package de.jakob.lotm.network.packets;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.AbilityItemHandler;
import de.jakob.lotm.abilities.ToggleAbilityItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ReceiveAbilityItemsPacket() implements CustomPacketPayload {
    public static final Type<ReceiveAbilityItemsPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "receive_abilities"));

    public static final StreamCodec<FriendlyByteBuf, ReceiveAbilityItemsPacket> STREAM_CODEC =
            StreamCodec.unit(new ReceiveAbilityItemsPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ReceiveAbilityItemsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();

            // Give abilities
            AbilityItemHandler.ITEMS.getEntries().forEach(entry -> {
                if(entry.get() instanceof AbilityItem abilityItem && abilityItem.canUse(player))
                    giveAbility(player, entry.get());
                else if(entry.get() instanceof ToggleAbilityItem toggleAbilityItem && toggleAbilityItem.canUse(player))
                    giveAbility(player, entry.get());
            });
        });
    }

    private static void giveAbility(Player player, ItemLike item) {
        ItemStack ability = new ItemStack(item);
        if (!player.getInventory().contains(ability)) {
            player.getInventory().add(ability);
        }
    }
}