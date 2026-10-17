package de.jakob.lotm.beyonders.abilities.abyss.contract;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.abyss.contract.handler.ContractScreenHandler;
import de.jakob.lotm.beyonders.abilities.abyss.contract.handler.ContractSoulHandler;
import de.jakob.lotm.beyonders.abilities.abyss.contract.registry.PendingReleaseRegistry;
import de.jakob.lotm.beyonders.abilities.abyss.contract.registry.SoulOwnershipRegistry;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ContractAbility extends ToggleAbility {

    public static final Map<UUID, PendingProposal> pendingProposals = new HashMap<>();

    public record PendingProposal(UUID casterUUID, ContractData data) {}
    public ContractAbility(String id) {
        super(id);
        canBeUsedByNPC = false;
        canBeCopied = false;
        canBeReplicated = false;
        cannotBeStolen = true;
        canBeUsedInArtifact = true;
        canBeShared = false;
    }
    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 6));
    }
    @Override
    protected float getSpiritualityCost() {
        return 0;
    }
    @Override
    public void start(Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            return;
        }
        openContractScreen(entity.isShiftKeyDown());
    }
    private static void openContractScreen(boolean showHelp) {
        // client GUI opening is delegated to the client handler.
        ContractScreenHandler.open(showHelp);
    }
    public static void proposeDirect(
            ServerPlayer caster,
            ServerPlayer partyA,
            ServerPlayer partyB,
            ContractData data
    ) {
        data.setPartyA(partyA.getUUID());
        data.setPartyB(partyB.getUUID());

        data.setPartyAName(partyA.getName().getString());
        data.setPartyBName(partyB.getName().getString());
        pendingProposals.put(partyB.getUUID(), new PendingProposal(partyA.getUUID(), data));
        partyB.sendSystemMessage(Component.literal("══════════════════════════════").withColor(0x9932cc));
        partyB.sendSystemMessage(Component.literal("       CONTRACT REQUEST").withColor(0xFFD700));
        partyB.sendSystemMessage(Component.literal(partyA.getName().getString() + " wants you to accept:").withColor(0xFFFFFF));
        partyB.sendSystemMessage(Component.literal(data.describe()).withColor(0xCCCCCC));
        partyB.sendSystemMessage(Component.literal("Type 'accept' or 'decline'").withColor(0x9932cc));
        partyB.sendSystemMessage(Component.literal("══════════════════════════════").withColor(0x9932cc));
        if (!caster.getUUID().equals(partyB.getUUID())) {AbilityUtil.sendActionBar(caster, Component.literal("Proposal sent to " + partyB.getName().getString() + ".").withColor(0x9932cc));
        }
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onChatMessage(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        if (!pendingProposals.containsKey(player.getUUID())) {
            return;
        }
        String msg = event.getRawText().trim().toLowerCase();
        if (msg.equals("accept")) {
            event.setCanceled(true);
            acceptProposal(player);
        } else if (msg.equals("decline")) {
            event.setCanceled(true);
            declineProposal(player);
        }
    }
    private static void acceptProposal(ServerPlayer target) {
        PendingProposal proposal = pendingProposals.remove(target.getUUID());
        if (proposal == null) {
            return;
        }
        ServerPlayer caster = target.server.getPlayerList().getPlayer(proposal.casterUUID());
        if (caster == null) {
            AbilityUtil.sendActionBar(target, Component.literal("The caster is no longer online.").withColor(0xFFff124d));
            return;
        }
        ContractData data = proposal.data();
        data.setPartyA(caster.getUUID());
        data.setPartyB(target.getUUID());
        data.setPartyAName(caster.getName().getString());
        data.setPartyBName(target.getName().getString());
        data.setActive(true);
        data.setCasterSeqAtCreation(BeyonderData.getSequence(caster));
        if (data.isKillContract()) {
            data.setActive(true);
            data.setComplete(false);
            data.setKillPaidOut(false);
            // Do not mark the payment as fulfilled when accepting.
            data.setTermAFulfilled(false);
            data.setTermBFulfilled(false);
            if (data.isKillerPartyA()) {
                data.setKillerName(data.partyAName());
            } else {
                data.setKillerName(data.partyBName());
            }
            var contractItem = ContractItem.create(data);

            if (!caster.getInventory().add(contractItem)) {caster.drop(contractItem, false);}
            AbilityUtil.sendActionBar(caster, Component.literal("Contract formed.").withColor(0x9932cc));
            AbilityUtil.sendActionBar(target, Component.literal("Contract formed.").withColor(0x9932cc));
            return;
        }


        boolean soulForSoul = data.termAType() == ContractData.TermType.SOUL && data.termBType() == ContractData.TermType.SOUL;

        if (data.hasSoulTermA()) {
            UUID soulOwner = data.getSoulOwner(true);
            if (soulOwner == null) {return;}
            if (SoulOwnershipRegistry.isSoulBound(soulOwner)) {
                ServerPlayer owner = target.server.getPlayerList().getPlayer(soulOwner);
                if (owner != null) {AbilityUtil.sendActionBar(owner, Component.literal("Your Soul Belongs to the Abyss").withColor(0xFFff124d));}
                return;
            }
            if (!data.isLifeDeathContract()){SoulOwnershipRegistry.bind(soulOwner);}
            if (soulForSoul) {
                data.setTermAFulfilled(true);
                UUID recipient = data.getTermRecipient(false);
                if (recipient != null) {ContractSoulHandler.startDrain(soulOwner, recipient);}
            }
        }
        if (data.hasSoulTermB()) {
            UUID soulOwner = data.getSoulOwner(false);

            if (soulOwner == null) {return;}
            if (SoulOwnershipRegistry.isSoulBound(soulOwner)) {
                ServerPlayer owner = target.server.getPlayerList().getPlayer(soulOwner);
                if (owner != null) {AbilityUtil.sendActionBar(owner, Component.literal("Your Soul Belongs to the Abyss").withColor(0xFFff124d));}
                return;
            }
            if (!data.isLifeDeathContract())SoulOwnershipRegistry.bind(soulOwner);
            if (soulForSoul) {
                data.setTermBFulfilled(true);
                UUID recipient = data.getTermRecipient(false);
                if (recipient != null) {ContractSoulHandler.startDrain(soulOwner, recipient);}
            }
        }
        if (data.isIou()) {
            UUID soulOwner = caster.getUUID();
            if (SoulOwnershipRegistry.isSoulBound(soulOwner)) {
                AbilityUtil.sendActionBar(caster, Component.literal("Your Soul Belongs to the Abyss").withColor(0xFFff124d));
                return;
            }
            SoulOwnershipRegistry.bind(soulOwner);
        }
        var contractItem = ContractItem.create(data);
        ServerPlayer recipient = data.isIou() ? target : caster;
        if (!recipient.getInventory().add(contractItem)) {
            recipient.drop(contractItem, false);
        }
        AbilityUtil.sendActionBar(caster, Component.literal("Contract formed.").withColor(0x9932cc));
        AbilityUtil.sendActionBar(target, Component.literal("Contract formed.").withColor(0x9932cc));
        if (data.termAType() == ContractData.TermType.RELEASE_SOUL || data.termBType() == ContractData.TermType.RELEASE_SOUL) {
            String releaseTargetName = data.releaseTargetName();
            if (releaseTargetName != null && !releaseTargetName.isBlank()) {
                ServerPlayer releaseTarget = caster.server.getPlayerList().getPlayerByName(releaseTargetName);
                if (releaseTarget != null) {PendingReleaseRegistry.register(releaseTarget.getUUID(), caster.getUUID(), caster.level().getGameTime());
                }
            }
        }
    }
    private static void declineProposal(ServerPlayer target) {
        PendingProposal proposal = pendingProposals.remove(target.getUUID());

        if (proposal == null) {
            return;
        }
        ServerPlayer caster = target.server.getPlayerList().getPlayer(proposal.casterUUID());
        if (caster != null) {
            AbilityUtil.sendActionBar(caster, Component.literal(target.getName().getString() + " declined.").withColor(0xFFff124d));
        }
    }
}