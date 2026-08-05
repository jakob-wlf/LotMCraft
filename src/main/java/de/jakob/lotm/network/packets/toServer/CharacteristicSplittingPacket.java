package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.BeyonderComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.beyonders.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.beyonders.potions.BeyonderCharacteristicItemHandler;
import de.jakob.lotm.util.playerMap.Characteristic;
import de.jakob.lotm.util.playerMap.PlayerMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CharacteristicSplittingPacket(String pathway, int sequence) implements CustomPacketPayload {
    public static final Type<CharacteristicSplittingPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "characteristic_splitting"));

    public static final StreamCodec<FriendlyByteBuf, CharacteristicSplittingPacket> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> {
                buf.writeUtf(pkt.pathway());
                buf.writeInt(pkt.sequence());
            },
            buf -> new CharacteristicSplittingPacket(buf.readUtf(), buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CharacteristicSplittingPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            BeyonderComponent component = player.getData(ModAttachments.BEYONDER_COMPONENT);
            int currentStack = component.getCharacteristicList().stream()
                    .filter(c -> c.pathway().equals(packet.pathway()) && c.sequence() == packet.sequence())
                    .mapToInt(Characteristic::stack)
                    .sum();

            if (currentStack > 0) {
                // If the characteristic being removed is the player's current sequence and they only have 1 stack,
                // we might want to prevent them from "losing" their beyonder status or just let them.
                // Given the description, it's a menu to remove a characteristic and place it in inventory.
                
                component.setCharacteristic(currentStack - 1, packet.sequence(), packet.pathway());
                PlayerMap playerMap = PlayerMap.get(player.serverLevel());
                playerMap.setStack(player, currentStack -1, packet.sequence(), packet.pathway());
                de.jakob.lotm.events.BeyonderDataTickHandler.invalidateCache(player);
                PacketHandler.syncBeyonderDataToPlayer(player);

                BeyonderCharacteristicItem item = BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence(packet.pathway(), packet.sequence());
                if (item != null) {
                    ItemStack stack = new ItemStack(item);
                    if (!player.getInventory().add(stack)) {
                        player.drop(stack, false);
                    }
                }
            }
        });
    }
}
