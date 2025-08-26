package de.jakob.lotm.network.packets;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ReceiveAbilityPacket(ItemLike item) implements CustomPacketPayload {
    public static final Type<ReceiveAbilityPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "receive_ability"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReceiveAbilityPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.ITEM),
            packet -> (Item) packet.item(),
            ReceiveAbilityPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ReceiveAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            ItemLike item = packet.item();
            giveAbility(player, item);
        });
    }

    private static void giveAbility(Player player, ItemLike item) {
        ItemStack ability = new ItemStack(item);
        if (!player.getInventory().contains(ability)) {
            player.getInventory().add(ability);
        }
    }
}