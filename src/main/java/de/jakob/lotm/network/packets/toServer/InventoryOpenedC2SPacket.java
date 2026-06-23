package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.item.PotionIngredient;
import de.jakob.lotm.beyonders.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.beyonders.potions.BeyonderPotion;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record InventoryOpenedC2SPacket() implements CustomPacketPayload {
    public static final Type<InventoryOpenedC2SPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "opened_inventory_screen"));

    public static final StreamCodec<FriendlyByteBuf, InventoryOpenedC2SPacket> STREAM_CODEC =
            StreamCodec.unit(new InventoryOpenedC2SPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(InventoryOpenedC2SPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            Inventory inv = player.getInventory();

            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.isEmpty()) continue;

                Item item = stack.getItem();

                if (item instanceof PotionIngredient obj) {
                    for (var path : obj.getPathways()) {
                        if (!BeyonderData.playerMap.check(path, obj.getSequence())) {
                            inv.setItem(i, ItemStack.EMPTY);
                            break;
                        }
                    }
                }

                else if (item instanceof BeyonderPotion potion) {
                    if (!BeyonderData.playerMap.check(
                            potion.getPathway(), potion.getSequence())) {
                        inv.setItem(i, ItemStack.EMPTY);
                    }
                }

                else if (item instanceof BeyonderCharacteristicItem cha) {
                    if (!BeyonderData.playerMap.check(
                            cha.getPathway(), cha.getSequence())) {
                        inv.setItem(i, ItemStack.EMPTY);
                    }
                }
            }

            player.containerMenu.broadcastChanges();
        });
    }
}
