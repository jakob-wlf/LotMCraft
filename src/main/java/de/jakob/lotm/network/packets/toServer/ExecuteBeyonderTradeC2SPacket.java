package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.gui.custom.Trades.BeyonderTradeMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public record ExecuteBeyonderTradeC2SPacket(int npcEntityId, int tradeIndex) implements CustomPacketPayload {

    public static final Type<ExecuteBeyonderTradeC2SPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "execute_beyonder_trade"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ExecuteBeyonderTradeC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ExecuteBeyonderTradeC2SPacket::npcEntityId,
                    ByteBufCodecs.VAR_INT, ExecuteBeyonderTradeC2SPacket::tradeIndex,
                    ExecuteBeyonderTradeC2SPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(TYPE, STREAM_CODEC, ExecuteBeyonderTradeC2SPacket::handle);
    }

    public static void handle(ExecuteBeyonderTradeC2SPacket packet, net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;

            if (!(serverPlayer.containerMenu instanceof BeyonderTradeMenu menu)) return;

            Entity entity = serverPlayer.level().getEntity(packet.npcEntityId());
            if (!(entity instanceof BeyonderNPCEntity npc)) return;

            if (menu.getNpcEntityId() != packet.npcEntityId()) return;

            var trades = npc.getCurrentTrades();
            int tradeIndex = packet.tradeIndex();

            if (tradeIndex < 0 || tradeIndex >= trades.size()) return;

            BeyonderNPCEntity.TradeEntry trade = trades.get(tradeIndex);

            int slotA = BeyonderTradeMenu.getInputSlotAIndex(tradeIndex);
            int slotB = BeyonderTradeMenu.getInputSlotBIndex(tradeIndex);

            ItemStack providedA = menu.getInputSlotsContainer().getItem(slotA);
            ItemStack providedB = menu.getInputSlotsContainer().getItem(slotB);

            if (!matchesCost(providedA, trade.costA())) {
                return;
            }
            if (!trade.costB().isEmpty()) {
                if (!matchesCost(providedB, trade.costB())) {
                    return;
                }
            }

            consumeExact(providedA, trade.costA().getCount());
            menu.getInputSlotsContainer().setItem(slotA, providedA);

            if (!trade.costB().isEmpty()) {
                consumeExact(providedB, trade.costB().getCount());
                menu.getInputSlotsContainer().setItem(slotB, providedB);
            }

            ItemStack result = trade.result().copy();
            BeyonderTradeMenu.giveOrDrop(serverPlayer, result);

            npc.removeTrade(tradeIndex);

            serverPlayer.closeContainer();
        });
    }

    private static boolean matchesCost(ItemStack provided, ItemStack cost) {
        if (cost.isEmpty()) return true;
        if (provided.isEmpty()) return false;
        if (!ItemStack.isSameItemSameComponents(provided, cost)) return false;
        return provided.getCount() >= cost.getCount();
    }

    private static void consumeExact(ItemStack provided, int amount) {
        if (provided.isEmpty()) return;
        provided.shrink(amount);
    }
}