package de.jakob.lotm.beyonders.abilities.abyss.contract.handler;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.abyss.contract.ContractData;
import de.jakob.lotm.beyonders.abilities.abyss.contract.ContractItem;
import de.jakob.lotm.beyonders.abilities.abyss.contract.registry.SoulOwnershipRegistry;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class LifeDeathHandler {
    private static final int SCAN_INTERVAL_TICKS = 20;
    private static int tickCounter;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (++tickCounter < SCAN_INTERVAL_TICKS) {return;}
        tickCounter = 0;
        MinecraftServer server = event.getServer();
        long now = server.overworld().getGameTime();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Container inventory = player.getInventory();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack stack = inventory.getItem(i);
                if (!(stack.getItem() instanceof ContractItem)) {continue;}
                ContractData data = ContractItem.readData(stack);
                if (!data.isLifeDeathContract() || !data.isActive() || !data.hasBothParties() || data.isComplete() || data.lifeDeathExpired()) {continue;}
                // Only trusted when every term is fulfilled, so a stray
                // start time from signing can't skip fulfilment.
                if (data.lifeDeathStartGameTime() >= 0 && data.areAllTermsFulfilled()) {
                    long duration = data.lifeDeathDurationTicks();
                    if (duration >= 0 && now - data.lifeDeathStartGameTime() >= duration) {endLifeLink(server, stack, data);}
                    continue;
                }

                //Not started fulfil outstanding terms
                boolean changed = false;
                if (data.termAType() != ContractData.TermType.NONE) {changed |= fulfillTerm(server, data, true);}
                if (data.termBType() != ContractData.TermType.NONE) {changed |= fulfillTerm(server, data, false);}
                if (changed) {ContractItem.writeData(stack, data);}
                if (!data.areAllTermsFulfilled()) {
                    continue;
                }
                //terms done start the link right away
                data.setLifeDeathStartGameTime(now);
                ContractItem.writeData(stack, data);
                notifyParties(server, data, "The life-bond has begun.", 0x9932CC);
            }
        }
    }
    // TERM FULFILMENT
    private static boolean fulfillTerm(MinecraftServer server, ContractData data, boolean termA) {
        ContractData.TermType type = termA ? data.termAType() : data.termBType();
        boolean fulfilled = termA ? data.termAFulfilled() : data.termBFulfilled();
        if (fulfilled) {return false;}
        switch (type) {
            case SOUL -> {
                UUID owner = data.getSoulOwner(termA);
                if (owner == null) {return false;}
                if (SoulOwnershipRegistry.isSoulBound(owner)) {return false;}
                SoulOwnershipRegistry.bind(owner);
                // Never touch the SoulOwnerIsPartyA flags here.
                if (termA) {data.setTermAFulfilled(true);}
                else {data.setTermBFulfilled(true);}
                return true;
            }
            case ITEM -> {
                UUID recipient = data.getTermRecipient(termA);
                String itemId = termA ? data.termAItem() : data.termBItem();
                int count = termA ? data.termACount() : data.termBCount();
                if (!recipientHasItems(server, recipient, itemId, count)) {return false;}
                if (termA) {data.setTermAFulfilled(true);}
                else {data.setTermBFulfilled(true);}
                return true;
            }
            case DIGEST -> {
                ServerPlayer giver = server.getPlayerList().getPlayer(data.getTermSender(termA));
                ServerPlayer receiver = server.getPlayerList().getPlayer(data.getTermRecipient(termA));
                if (giver == null || receiver == null) return false;
                int percent = termA ? data.termACount() : data.termBCount();
                if (DigestTransferHandler.transfer(giver, receiver, percent) != DigestTransferHandler.Result.DONE) {return false;}
                if (termA) data.setTermAFulfilled(true); else data.setTermBFulfilled(true);
                return true;
            }
            default -> {return false;}
        }
    }
    private static boolean recipientHasItems(MinecraftServer server, UUID recipientUUID, String itemId, int requiredCount) {
        if (recipientUUID == null || itemId == null || itemId.isBlank() || requiredCount < 1) {return false;}
        Item required;
        try {required = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId));}
        catch (Exception exception) {return false;}
        if (required == Items.AIR) {return false;}
        ServerPlayer recipient = server.getPlayerList().getPlayer(recipientUUID);
        if (recipient == null) {return false;}
        Container inv = recipient.getInventory();
        int total = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty() && s.getItem() == required) {
                total += s.getCount();
                if (total >= requiredCount) {return true;}
            }
        }
        return false;
    }
    // END OF LINK
    public static void endLifeLink(MinecraftServer server, ItemStack stack, ContractData data) {
        if (!data.hasBothParties() || !data.areAllTermsFulfilled() || data.lifeDeathStartGameTime() < 0 || data.lifeDeathExpired()) {
            return;
        }
        data.setLifeDeathExpired(true);
        data.setComplete(true);
        notifyParties(server, data, "The life-bond has ended.", 0x9932CC);
        if (data.hasAnySoulTerm()) {ContractItem.writeData(stack, data);}
        else {
            data.setActive(false);
            ContractItem.writeData(stack, data);
            stack.shrink(1);
        }
    }
    private static void notifyParties(MinecraftServer server, ContractData data, String message, int color) {
        ServerPlayer partyA = server.getPlayerList().getPlayer(data.getPartyA());
        ServerPlayer partyB = server.getPlayerList().getPlayer(data.getPartyB());
        if (partyA != null) {AbilityUtil.sendActionBar(partyA, Component.literal(message).withColor(color));}
        if (partyB != null) {AbilityUtil.sendActionBar(partyB, Component.literal(message).withColor(color));}
    }
}