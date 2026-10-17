package de.jakob.lotm.beyonders.abilities.abyss.contract;

import de.jakob.lotm.beyonders.abilities.abyss.contract.handler.ContractAssignmentHandler;
import de.jakob.lotm.beyonders.abilities.abyss.contract.handler.ContractSoulHandler;
import de.jakob.lotm.beyonders.abilities.abyss.contract.menu.ContractDisplay;
import de.jakob.lotm.beyonders.abilities.abyss.contract.menu.ContractDisplayBuilder;
import de.jakob.lotm.beyonders.abilities.abyss.contract.registry.PendingReleaseRegistry;
import de.jakob.lotm.beyonders.abilities.abyss.contract.registry.SoulOwnershipRegistry;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ContractItem extends Item {
    private static final String NBT_KEY = "lotm_contract";
    public ContractItem(Properties properties) {
        super(properties);
    }
    public static ItemStack create(ContractData data) {
        ItemStack stack = new ItemStack(de.jakob.lotm.item.ModItems.CONTRACT_ITEM.get());
        CompoundTag wrapper = new CompoundTag();
        wrapper.put(NBT_KEY, data.toTag());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(wrapper));
        return stack;
    }
    public static ContractData readData(ItemStack stack) {
        CompoundTag wrapper = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return ContractData.of(wrapper.getCompound(NBT_KEY));
    }
    public static void writeData(ItemStack stack, ContractData data) {
        CompoundTag wrapper = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        wrapper.put(NBT_KEY, data.toTag());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(wrapper));
    }
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.pass(stack);
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResultHolder.fail(stack);
        ContractData data = readData(stack);
        if (!data.isActive()) return InteractionResultHolder.fail(stack);
        if (!data.hasBothParties()) return InteractionResultHolder.fail(stack);
        // LIFE / DEATH CONTRACTS, Handled completely here for Party A and Party B anyone else falls through to the intruder logic below.
        boolean isPartyA = serverPlayer.getUUID().equals(data.getPartyA());
        boolean isPartyB = serverPlayer.getUUID().equals(data.getPartyB());
        if (data.isLifeDeathContract() && (isPartyA || isPartyB)) {
            if (!data.lifeDeathExpired()) {
                AbilityUtil.sendActionBar(serverPlayer, Component.literal("The life-bond is still active.").withColor(0x9932cc));
                return InteractionResultHolder.fail(stack);
            }
            // Find which term holds the fulfilled soul
            boolean soulIsTermA = data.termAType() == ContractData.TermType.SOUL && data.termAFulfilled();
            boolean soulIsTermB = data.termBType() == ContractData.TermType.SOUL && data.termBFulfilled();
            if (!soulIsTermA && !soulIsTermB) return InteractionResultHolder.fail(stack);   // item contract
            UUID soulOwner = soulIsTermA ? data.getSoulOwner(true) : data.getSoulOwner(false);
            if (soulOwner == null) return InteractionResultHolder.fail(stack);
            // No ownership check either party may use it
            // The soul always drains from its owner to the OTHER party
            UUID receiver = soulOwner.equals(data.getPartyA()) ? data.getPartyB() : data.getPartyA();
            ContractSoulHandler.startDrain(soulOwner, receiver);
            SoulOwnershipRegistry.release(soulOwner);
            data.setComplete(true);
            data.setActive(false);
            writeData(stack, data);
            stack.shrink(1);
            AbilityUtil.sendActionBar(serverPlayer, Component.literal("The soul is claimed by the contract.").withColor(0x9932cc));
            return InteractionResultHolder.success(stack);
        }

        // INTRUDERS (anyone who isn't Party A)
        boolean isOwner = isPartyA;
        if (!isOwner) {
            int intruderSeq = BeyonderData.getSequence(serverPlayer);
            boolean strongEnough = intruderSeq <= data.casterSeqAtCreation() + 1;
            if (strongEnough) {
                voidContract(data, (ServerLevel) level);
                stack.shrink(stack.getCount());
                AbilityUtil.sendActionBar(serverPlayer, Component.literal("The contract unravels.").withColor(0x9932cc));
            }
            else {AbilityUtil.sendActionBar(serverPlayer, Component.literal("You lack the strength to unmake this.").withColor(0xFFff124d));}
            return InteractionResultHolder.success(stack);
        }
        // PARTY A: curse
        if (player.isShiftKeyDown()) {
            tryCurseThroughContract(serverPlayer, data, stack, (ServerLevel) level);
            return InteractionResultHolder.success(stack);
        }
        // IOU
        if (data.isIou() && serverPlayer.getUUID().equals(data.getPartyB()) && !data.termBFulfilled()) {
            ContractSoulHandler.consumeSoul(serverPlayer, data.getPartyA());
            data.setComplete(true);
            data.setActive(false);
            writeData(stack, data);
            stack.shrink(1);
            return InteractionResultHolder.success(stack);
        }
        // STANDARD SOUL CONTRACT
        if (data.termAType() == ContractData.TermType.SOUL && data.termAFulfilled() && otherTermsFulfilled(data, true) && !data.isComplete()) {
            ServerPlayer owner = Objects.requireNonNull(level.getServer()).getPlayerList().getPlayer(data.getPartyB());
            if (owner == null) {
                AbilityUtil.sendActionBar(serverPlayer, Component.literal("The Abyss Rejects your offer.").withColor(0xFFff124d));
                return InteractionResultHolder.fail(stack);
            }
            ContractSoulHandler.consumeSoul(serverPlayer, data.getPartyB());
            data.setComplete(true);
            data.setActive(false);
            writeData(stack, data);
            stack.shrink(1);
        }
        // KILL CONTRACT
        if (data.isKillContract() && data.termAType() == ContractData.TermType.SOUL) {
            UUID killerUUID = data.isKillerPartyA() ? data.getPartyA() : data.getPartyB();
            if (!serverPlayer.getUUID().equals(killerUUID)) {
                AbilityUtil.sendActionBar(serverPlayer, Component.literal("Only the contracted killer can claim this soul.").withColor(0xFFff124d));
                return InteractionResultHolder.fail(stack);
            }
            ContractSoulHandler.consumeSoul(serverPlayer, data.getPartyB());
            data.setComplete(true);
            data.setActive(false);
            writeData(stack, data);
            stack.shrink(1);

        } else {
            // Preserve the existing ownership behavior for other contracts.
            UUID soulOwnerUUID = data.getSoulOwner(true);
            if (!serverPlayer.getUUID().equals(soulOwnerUUID)) {
                AbilityUtil.sendActionBar(serverPlayer, Component.literal("This is not your soul to offer.").withColor(0xFFff124d));
                return InteractionResultHolder.fail(stack);
            }
        }
        return InteractionResultHolder.success(stack);
    }
    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, Player player, @NotNull LivingEntity target, @NotNull InteractionHand hand) {
        if (player.level().isClientSide) return InteractionResult.PASS;
        if (!(player instanceof ServerPlayer caster)) return InteractionResult.PASS;
        if (!(target instanceof ServerPlayer targetPlayer)) return InteractionResult.PASS;
        ContractData data = readData(stack);
        if (!data.isActive() || data.hasBothParties()) return InteractionResult.PASS;
        UUID contractUUID = data.contractUUID();
        if (contractUUID == null) return InteractionResult.PASS;
        if (targetPlayer.getUUID().equals(data.getPartyA()) || targetPlayer.getUUID().equals(data.getPartyB())) return InteractionResult.PASS;
        ContractAssignmentHandler.sendRequest(caster, targetPlayer, contractUUID);
        return InteractionResult.SUCCESS;
    }
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        ContractData data = readData(stack);
        ContractDisplay display = ContractDisplayBuilder.build(data);
        tooltip.add(display.title());
        tooltip.addAll(display.lines());
    }
    private void tryCurseThroughContract(ServerPlayer holder, ContractData data, ItemStack stack, ServerLevel level) {
        UUID holderUUID = holder.getUUID();
        UUID victimUUID = null;
        if (holderUUID.equals(data.getPartyA()) && data.termAType() == ContractData.TermType.SOUL && data.termAFulfilled()) {
            victimUUID = data.getPartyB();
        } else if (holderUUID.equals(data.getPartyB()) && data.termBType() == ContractData.TermType.SOUL && data.termBFulfilled()) victimUUID = data.getPartyA();
        if (victimUUID == null) return;
        if (!BeyonderData.isBeyonder(holder) || !"abyss".equals(BeyonderData.getPathway(holder)) || BeyonderData.getSequence(holder) > 3) {
            AbilityUtil.sendActionBar(holder, Component.literal("You lack the voice to reach them yet.").withColor(0xFFff124d));
            return;
        }
        long now = level.getGameTime();
        long cooldownTicks = 20L * 30;
        if (now - data.curseLastUseGameTime() < cooldownTicks) {
            AbilityUtil.sendActionBar(holder, Component.literal("The curse is still cooling.").withColor(0xFFff124d));
            return;
        }
        ServerPlayer victim = level.getServer().getPlayerList().getPlayer(victimUUID);
        if (victim == null) return;
        victim.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 5, 0, false, false));
        victim.hurt(victim.damageSources().magic(), 4f);
        AbilityUtil.sendActionBar(victim, Component.literal("A voice from nowhere curses you.").withColor(0x9932cc));
        data.setCurseLastUseGameTime(now);
        writeData(stack, data);
    }

    private static void voidContract(ContractData data, ServerLevel level) {
        data.setActive(false);
        data.setComplete(false);
        if (data.isIou())SoulOwnershipRegistry.release(data.getPartyA());
        else {
            handleSoulOnVoid(data.getPartyA(), data, level);
            handleSoulOnVoid(data.getPartyB(), data, level);
        }
        var server = level.getServer();
        ServerPlayer partyA = server.getPlayerList().getPlayer(data.getPartyA());
        ServerPlayer partyB = server.getPlayerList().getPlayer(data.getPartyB());
        if (partyA != null) AbilityUtil.sendActionBar(partyA, Component.literal("Your contract has been voided.").withColor(0xFFff124d));
        if (partyB != null) AbilityUtil.sendActionBar(partyB, Component.literal("Your contract has been voided.").withColor(0xFFff124d));
    }
    private static void handleSoulOnVoid(UUID uuid, ContractData data, ServerLevel level) {
        boolean isHolderA = uuid.equals(data.getPartyA()) && data.termAType() == ContractData.TermType.SOUL;
        boolean isHolderB = uuid.equals(data.getPartyB()) && data.termBType() == ContractData.TermType.SOUL;
        if (!isHolderA && !isHolderB) return;
        boolean fulfilled = isHolderA ? data.termAFulfilled() : data.termBFulfilled();
        UUID ownerUUID = isHolderA ? data.getSoulOwner(true) : data.getSoulOwner(false);
        if (ownerUUID == null) return;
        if (!fulfilled) {
            SoulOwnershipRegistry.release(ownerUUID);
            return;
        }
        ContractSoulHandler.cancelDrain(ownerUUID);
        var pendingRelease = PendingReleaseRegistry.claimOldest(ownerUUID);
        if (pendingRelease == null) {
            SoulOwnershipRegistry.release(ownerUUID);
            return;
        }
        ContractSoulHandler.startDrain(ownerUUID, pendingRelease.releaseContractCasterUUID());
        markReleaseTermTransferred(pendingRelease.releaseContractCasterUUID(), ownerUUID, level);
    }
    private static void markReleaseTermTransferred(UUID releaseCasterUUID, UUID soulOwnerUUID, ServerLevel level) {
        ServerPlayer releaseCaster = level.getServer().getPlayerList().getPlayer(releaseCasterUUID);
        if (releaseCaster == null) return;
        for (ItemStack stack : releaseCaster.getInventory().items) {
            if (!(stack.getItem() instanceof ContractItem)) continue;
            ContractData d = readData(stack);
            if (!d.isActive()) continue;
            if (d.termBType() != ContractData.TermType.RELEASE_SOUL) continue;
            ServerPlayer soulOwner = level.getServer().getPlayerList().getPlayer(soulOwnerUUID);
            String soulOwnerName = soulOwner != null ? soulOwner.getName().getString() : "";
            if (!soulOwnerUUID.equals(d.getPartyB()) && !d.releaseTargetName().equalsIgnoreCase(soulOwnerName)) continue;
            d.setTermBFulfilled(true);
            writeData(stack, d);
            if (d.termAFulfilled()) {
                SoulOwnershipRegistry.release(soulOwnerUUID);
                d.setComplete(true);
                d.setActive(false);
                AbilityUtil.sendActionBar(releaseCaster, Component.literal("Both obligations are fulfilled. The contract dissolves.").withColor(0x9932cc));
            }
            return;
        }
    }
    private static boolean otherTermsFulfilled(ContractData data, boolean soulIsTermA) {
        // Kill contracts must pay out before a soul can be sacrificed.
        if (data.isKillContract() && !data.killPaidOut()) return false;
        ContractData.TermType otherType = soulIsTermA ? data.termBType() : data.termAType();
        boolean otherFulfilled = soulIsTermA ? data.termBFulfilled() : data.termAFulfilled();
        if (otherType == ContractData.TermType.SOUL) return true;
        return otherFulfilled;
    }
}