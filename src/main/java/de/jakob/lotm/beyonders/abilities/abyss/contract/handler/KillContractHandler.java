
package de.jakob.lotm.beyonders.abilities.abyss.contract.handler;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.abyss.contract.ContractData;
import de.jakob.lotm.beyonders.abilities.abyss.contract.ContractItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class KillContractHandler {

    private static final int SCAN_INTERVAL = 20;
    private static int tickCounter;
    private static final List<PendingKill> PENDING_KILLS = new ArrayList<>();
    private record PendingKill(UUID killerUUID, String killerName, String victimName, boolean victimWasPlayer) {}
    private static void log(String message) {
        LOTMCraft.LOGGER.info("[KillContract] {}", message);
    }
    // REGISTER KILLS
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) {return;}
        if (!(victim.level().getServer() instanceof MinecraftServer server)) {return;}
        if (!(event.getSource().getEntity() instanceof ServerPlayer killer)) {return;}
        if (!hasEligibleKillContract(server, killer, victim)) {return;}
        PendingKill pending = new PendingKill(killer.getUUID(), killer.getGameProfile().getName(), victim.getName().getString(), victim instanceof Player);
        synchronized (PENDING_KILLS) {PENDING_KILLS.add(pending);}
        log("Registered pending kill: " + pending.killerName() + " killed " + pending.victimName());
        // Try immediately; the periodic scan retries if necessary.
        processPendingKills(server);
    }
    // PERIODIC RETRY
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (++tickCounter < SCAN_INTERVAL) {return;}
        tickCounter = 0;
        processPendingKills(event.getServer());
    }
    private static void processPendingKills(MinecraftServer server) {
        synchronized (PENDING_KILLS) {Iterator<PendingKill> iterator = PENDING_KILLS.iterator();
            while (iterator.hasNext()) {
                PendingKill pending = iterator.next();
                ServerPlayer killer = server.getPlayerList().getPlayer(pending.killerUUID());
                if (killer == null) {continue;}
                boolean fulfilled = false;
                // Search player's inventory.
                for (ServerPlayer holder : server.getPlayerList().getPlayers()) {
                    if (scanContainer(holder.getInventory(), pending, killer)) {
                        fulfilled = true;
                        break;
                    }
                }
                if (fulfilled) {
                    iterator.remove();
                }
            }
        }
    }
    // FIND A MATCHING CONTRACT
    private static boolean scanContainer(Container container, PendingKill pending, ServerPlayer killer) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!(stack.getItem() instanceof ContractItem)) {continue;}
            ContractData data = ContractItem.readData(stack);
            if (!data.isActive() || !data.isKillContract() || data.killPaidOut() || data.getPartyA() == null || data.getPartyB() == null){continue;}
            if (!data.killerName().equalsIgnoreCase(pending.killerName())) {continue;}
            if (!data.victimName().equalsIgnoreCase(pending.victimName())) {continue;}
            String entityType = data.victimEntityType();
            if ("PLAYER".equals(entityType) && !pending.victimWasPlayer()) {continue;}
            if ("MOB".equals(entityType) && pending.victimWasPlayer()) {continue;}
            if (!"ALL".equals(entityType) && !"PLAYER".equals(entityType) && !"MOB".equals(entityType)) {continue;}
            log("Found matching contract in container.");
            if (payOutKillContract(data, stack, killer)) {
                container.setChanged();
                return true;
            }
        }

        return false;
    }
    // FULFILL CONTRACT
    private static boolean payOutKillContract(ContractData data, ItemStack stack, ServerPlayer killer) {
        if (!data.isActive() || data.killPaidOut()) {return false;}
        ContractData.TermType paymentType = data.termAType();
        switch (paymentType) {
            case ITEM -> {
                Item required;
                try {
                    ResourceLocation itemId = ResourceLocation.parse(data.termAItem());
                    required = BuiltInRegistries.ITEM.get(itemId);
                } catch (Exception exception) {
                    return false;
                }
                if (required == Items.AIR) {
                    return false;
                }
            }

            case SOUL -> {
                UUID payerUUID = data.isKillPayerPartyA() ? data.getPartyA() : data.getPartyB();
                UUID killerUUID = data.isKillerPartyA() ? data.getPartyA() : data.getPartyB();
                if (payerUUID == null || killerUUID == null) {return false;}
                if (payerUUID.equals(killerUUID)) {return false;}
                // Record whose soul is being offered.
                if (data.termAType() == ContractData.TermType.SOUL) {
                    data.setTermASoulOwnerIsPartyA(payerUUID.equals(data.getPartyA()));
                    data.setTermAFulfilled(true);
                } else if (data.termBType() == ContractData.TermType.SOUL) {
                    data.setTermBSoulOwnerIsPartyA(payerUUID.equals(data.getPartyA()));
                    data.setTermBFulfilled(true);
                }
            }
            case DIGEST -> {
                UUID payerUUID = data.isKillPayerPartyA() ? data.getPartyA() : data.getPartyB();
                UUID killerUUID = data.isKillerPartyA() ? data.getPartyA() : data.getPartyB();
                if (payerUUID == null || killerUUID == null || payerUUID.equals(killerUUID)) {
                    return false;
                }
                ServerPlayer payer = killer.server.getPlayerList().getPlayer(payerUUID);
                ServerPlayer receiver = killer.server.getPlayerList().getPlayer(killerUUID);
                // Both must be online. The pending kill is kept and retried.
                if (payer == null || receiver == null) {return false;}
                DigestTransferHandler.Result result = DigestTransferHandler.transfer(payer, receiver, data.termACount());
                if (result != DigestTransferHandler.Result.DONE) {
                    return false;
                }
                data.setTermAFulfilled(true);
            }
            case RELEASE_SOUL, NONE -> {
                return false;
            }

        }
        // The kill requirement is satisfied.
        data.setKillPaidOut(true);
        // Persist both state changes together.
        ContractItem.writeData(stack, data);
        // Only remove the contract when every obligation is fulfilled.
        boolean allTermsFulfilled = data.termAFulfilled() && data.termBFulfilled();
        if (allTermsFulfilled) {
            data.setComplete(true);
            data.setActive(false);
            ContractItem.writeData(stack, data);
            stack.shrink(1);
        } else {
        }
        return true;
    }
    private static int countItem(ServerPlayer player, Item item) {
        int total = 0;
        for (ItemStack stack : player.getInventory().items) {if (stack.getItem() == item) {total += stack.getCount();}}
        for (ItemStack stack : player.getInventory().offhand) {if (stack.getItem() == item) {total += stack.getCount();}}
        return total;
    }
    private static boolean hasEligibleKillContract(MinecraftServer server, ServerPlayer killer, LivingEntity victim) {
        String killerName = killer.getGameProfile().getName();
        String victimName = victim.getName().getString();
        boolean victimWasPlayer = victim instanceof Player;
        for (ServerPlayer holder : server.getPlayerList().getPlayers()) {
            Container container = holder.getInventory();
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = container.getItem(i);
                if (!(stack.getItem() instanceof ContractItem)) {continue;}
                ContractData data = ContractItem.readData(stack);
                // Contract must be active and both parties must exist.
                if (!data.isActive() || !data.isKillContract() || data.killPaidOut() || data.getPartyA() == null || data.getPartyB() == null) {
                    continue;
                }
                if (!data.killerName().equalsIgnoreCase(killerName) || !data.victimName().equalsIgnoreCase(victimName)) {
                    continue;
                }
                String entityType = data.victimEntityType();
                if ("PLAYER".equals(entityType) && !victimWasPlayer) {continue;}
                if ("MOB".equals(entityType) && victimWasPlayer) {continue;}
                if (!"ALL".equals(entityType) && !"PLAYER".equals(entityType) && !"MOB".equals(entityType)) {continue;}
                return true;
            }
        }
        return false;
    }
}