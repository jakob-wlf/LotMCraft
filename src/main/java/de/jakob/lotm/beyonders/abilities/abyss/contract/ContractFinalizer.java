package de.jakob.lotm.beyonders.abilities.abyss.contract;

import de.jakob.lotm.beyonders.abilities.abyss.contract.handler.ContractSoulHandler;
import de.jakob.lotm.beyonders.abilities.abyss.contract.registry.PendingReleaseRegistry;
import de.jakob.lotm.beyonders.abilities.abyss.contract.registry.SoulOwnershipRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.UUID;

public class ContractFinalizer {

    public static boolean activate(ContractData data) {
        if (!data.hasBothParties()) {
            data.setTermAFulfilled(false);
            data.setTermBFulfilled(false);
            return false;
        }
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return false;
        }
        ServerPlayer partyA = server.getPlayerList().getPlayer(data.getPartyA());
        ServerPlayer partyB = server.getPlayerList().getPlayer(data.getPartyB());
        if (partyA != null) {data.setPartyAName(partyA.getName().getString());}
        if (partyB != null) {data.setPartyBName(partyB.getName().getString());}
        if (!validateSoulBindings(data)) {return false;}
        String kind = data.contractKind();
        if (kind == null || kind.isEmpty()) {kind = "STANDARD";}
        switch (kind) {
            case "KILL" -> {
                if (data.isKillerPartyA()) {
                    data.setKillerName(data.partyAName());
                } else {
                    data.setKillerName(data.partyBName());
                }
                data.setKillPaidOut(false);
                bindSoulTermsIfAny(data);
            }
            case "LIFE_DEATH" -> data.setLifeDeathExpired(false);
            default -> bindSoulTermsIfAny(data);
        }
        data.setActive(true);
        return true;
    }

    private static boolean validateSoulBindings(ContractData data) {
        UUID termASoul = null;
        UUID termBSoul = null;
        if (data.termAType() == ContractData.TermType.SOUL) {
            termASoul = data.getSoulOwner(true);
            if (termASoul == null) {return false;}
        }
        if (data.termBType() == ContractData.TermType.SOUL) {
            termBSoul = data.getSoulOwner(false);
            if (termBSoul == null) {return false;}
        }
        if (SoulOwnershipRegistry.isSoulBound(termASoul)) {
            return false;
        }
        return termBSoul == null || termBSoul.equals(termASoul) || !SoulOwnershipRegistry.isSoulBound(termBSoul);
    }

    private static void bindSoulTermsIfAny(ContractData data) {
        if (data.isIou()) {
            UUID partyA = data.getPartyA();
            if (partyA != null && !SoulOwnershipRegistry.isSoulBound(partyA)) {
                SoulOwnershipRegistry.bind(partyA);
            }
            return;
        }
        if (data.termBType() == ContractData.TermType.RELEASE_SOUL) {
            UUID target = data.getPartyB();
            UUID requester = data.getPartyA();
            if (target != null && requester != null) {
                data.setReleaseTargetName(data.partyBName());
                PendingReleaseRegistry.register(target, requester, currentGameTime());
            }
            return;
        }

        if (data.termAType() == ContractData.TermType.SOUL) {
            UUID soulOwner = data.getSoulOwner(true);
            if (soulOwner != null) {SoulOwnershipRegistry.bind(soulOwner);}
        }
        if (data.termBType() == ContractData.TermType.SOUL) {
            UUID soulOwner = data.getSoulOwner(false);
            if (soulOwner != null) {
                SoulOwnershipRegistry.bind(soulOwner);
                boolean soulForSoul = data.termAType() == ContractData.TermType.SOUL;
                if (soulForSoul) {
                    UUID recipient = soulOwner.equals(data.getPartyA()) ? data.getPartyB() : data.getPartyA();
                    ContractSoulHandler.startDrain(soulOwner, recipient);
                }
            }
        }
    }
    private static long currentGameTime() {
        var server = ServerLifecycleHooks.getCurrentServer();
        return server != null ? server.overworld().getGameTime() : 0L;
    }
    private static void bindLifeDeathResource(ContractData data) {
        if (!data.hasBothParties()) {return;}
        if (data.termAType() == ContractData.TermType.SOUL) {
            UUID soulOwner = data.getSoulOwner(true);
            if (soulOwner != null && !SoulOwnershipRegistry.isSoulBound(soulOwner)) {SoulOwnershipRegistry.bind(soulOwner);}
        }
        if (data.termBType() == ContractData.TermType.SOUL) {
            UUID soulOwner = data.getSoulOwner(false);
            if (soulOwner != null && !SoulOwnershipRegistry.isSoulBound(soulOwner)) {
                SoulOwnershipRegistry.bind(soulOwner);
            }
        }
    }
}