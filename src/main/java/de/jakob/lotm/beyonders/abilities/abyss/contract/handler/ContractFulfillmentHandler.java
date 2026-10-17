package de.jakob.lotm.beyonders.abilities.abyss.contract.handler;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.abyss.contract.ContractData;
import de.jakob.lotm.beyonders.abilities.abyss.contract.ContractItem;
import de.jakob.lotm.beyonders.abilities.abyss.contract.registry.SoulOwnershipRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ContractFulfillmentHandler {
    private static final int SCAN_INTERVAL_TICKS = 20 * 5;
    private static int tickCounter = 0;
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (++tickCounter < SCAN_INTERVAL_TICKS) return;
        tickCounter = 0;
        for (ServerPlayer caster : event.getServer().getPlayerList().getPlayers()) {
            var items = caster.getInventory().items;
            for (int i = 0; i < items.size(); i++) {
                ItemStack stack = items.get(i);
                if (!(stack.getItem() instanceof ContractItem)) continue;
                ContractData data = ContractItem.readData(stack);
                if (data.isLifeDeathContract()) continue;
                if (!data.isActive()) continue;
                checkItemTerm(caster, data, true);
                checkItemTerm(caster, data, false);
                checkDigestTerm(caster, data, true);
                checkDigestTerm(caster, data, false);
                boolean involvesSoul = data.termAType() == ContractData.TermType.SOUL || data.termBType() == ContractData.TermType.SOUL;
                if (data.isComplete()) {
                    caster.getInventory().setItem(i, ItemStack.EMPTY);
                    continue;
                }
                if (data.isKillContract()) {
                    boolean hasSoulTerm = data.termAType() == ContractData.TermType.SOUL || data.termBType() == ContractData.TermType.SOUL;
                    if (data.killPaidOut() && data.termAFulfilled() && !hasSoulTerm) {
                        data.setComplete(true);
                        data.setActive(false);
                        caster.getInventory().setItem(i, ItemStack.EMPTY);
                        continue;
                    }
                } else if (!involvesSoul && data.areAllTermsFulfilled()) {
                    caster.getInventory().setItem(i, ItemStack.EMPTY);
                    continue;
                }
                ContractItem.writeData(stack, data);
            }
        }
    }

    private static void checkDigestTerm(ServerPlayer caster, ContractData data, boolean isTermA) {
        if (data.isKillContract()) return;
        ContractData.TermType type = isTermA ? data.termAType() : data.termBType();
        boolean fulfilled = isTermA ? data.termAFulfilled() : data.termBFulfilled();
        if (type != ContractData.TermType.DIGEST || fulfilled) return;
        if (!data.hasBothParties()) return;
        ServerPlayer giver = caster.server.getPlayerList().getPlayer(data.getTermSender(isTermA));
        ServerPlayer receiver = caster.server.getPlayerList().getPlayer(data.getTermRecipient(isTermA));
        if (giver == null || receiver == null) return;
        int percent = isTermA ? data.termACount() : data.termBCount();
        if (DigestTransferHandler.transfer(giver, receiver, percent) != DigestTransferHandler.Result.DONE) {return;}
        if (isTermA) {data.setTermAFulfilled(true);}
        else {data.setTermBFulfilled(true);}
        boolean oppositeIsA = !isTermA;
        ContractData.TermType oppositeType = oppositeIsA ? data.termAType() : data.termBType();
        boolean oppositeFulfilled = oppositeIsA ? data.termAFulfilled() : data.termBFulfilled();
        if (oppositeType == ContractData.TermType.SOUL && !oppositeFulfilled) {
            if (oppositeIsA) {data.setTermAFulfilled(true);}
            else {data.setTermBFulfilled(true);}
            ContractSoulHandler.startDrain(data.getTermSender(oppositeIsA), data.getTermRecipient(oppositeIsA));
        }
    }

    private static void checkItemTerm(ServerPlayer caster, ContractData data, boolean isTermA) {
        boolean alreadyFulfilled = isTermA ? data.termAFulfilled() : data.termBFulfilled();
        if (alreadyFulfilled) return;
        if (!data.hasBothParties()) return;
        if (data.isKillContract()) {
            if (!isTermA) return;
            if (!data.killPaidOut()) return;
        }
        ContractData.TermType type = isTermA ? data.termAType() : data.termBType();
        if (type != ContractData.TermType.ITEM) return;
        String itemId = isTermA ? data.termAItem() : data.termBItem();
        int count = isTermA ? data.termACount() : data.termBCount();
        ServerPlayer sender = caster.server.getPlayerList().getPlayer(data.getTermSender(isTermA));
        ServerPlayer recipient = caster.server.getPlayerList().getPlayer(data.getTermRecipient(isTermA));
        if (sender == null || recipient == null) return;
        Item required;
        try {required = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId));
        } catch (Exception ignored) {
            return;
        }
        if (countItem(recipient, required) < count) return;
        if (isTermA) {data.setTermAFulfilled(true);}
        else {data.setTermBFulfilled(true);}
        if (data.isIou()) {
            SoulOwnershipRegistry.release(data.getPartyA());
            data.setComplete(true);
            data.setActive(false);
            return;
        }
        ContractData.TermType oppositeType = isTermA ? data.termBType() : data.termAType();
        if (oppositeType == ContractData.TermType.SOUL) {
            if (isTermA && !data.termBFulfilled()) {data.setTermBFulfilled(true);}
            else if (!isTermA && !data.termAFulfilled()) {data.setTermAFulfilled(true);}
        }
    }
    private static int countItem(ServerPlayer player, Item item) {
        int total = 0;
        for (ItemStack stack : player.getInventory().items) {if (stack.getItem() == item) {total += stack.getCount();}}
        return total;
    }
}