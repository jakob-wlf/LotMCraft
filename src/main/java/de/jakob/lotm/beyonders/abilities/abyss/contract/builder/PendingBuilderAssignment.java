package de.jakob.lotm.beyonders.abilities.abyss.contract.builder;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.abyss.contract.ContractAbility;
import de.jakob.lotm.beyonders.abilities.abyss.contract.ContractData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class PendingBuilderAssignment {
    private record Pending(ContractBuilderConverter.Result result, boolean needsA, boolean needsB, ServerPlayer assignedA, ServerPlayer assignedB) {}
    private static final Map<UUID, Pending> pending = new ConcurrentHashMap<>();
    public static void begin(ServerPlayer caster, ContractBuilderConverter.Result result, int signMode) {
        ServerPlayer a = signMode == 1 ? caster : null;
        ServerPlayer b = signMode == 2 ? caster : null;
        boolean needA = a == null;
        boolean needB = b == null;
        pending.put(Objects.requireNonNull(caster).getUUID(), new Pending(result, needA, needB, a, b));
    }

    public static void finalizeAndPropose(ServerPlayer caster, ContractBuilderConverter.Result result, ServerPlayer a, ServerPlayer b) {
        if (a == null || b == null) return;
        ContractData data = result.data;
        data.setPartyA(a.getUUID());
        data.setPartyB(b.getUUID());
        data.setPartyAName(a.getName().getString());
        data.setPartyBName(b.getName().getString());
        if (data.isKillContract()) {data.setKillerName(data.isKillerPartyA() ? a.getName().getString() : b.getName().getString());
            if (data.isKillContract()) data.setKillerName(data.isKillerPartyA() ? a.getName().getString() : b.getName().getString());
        }
        if (data.hasSoulTermA()) data.setReleaseTargetName(data.getSoulOwnerName(true));
        if (data.hasSoulTermB()) data.setReleaseTargetName(data.getSoulOwnerName(false));
        ContractAbility.proposeDirect(caster, a, b, data);
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer caster)) return;
        if (!(event.getTarget() instanceof ServerPlayer target)) return;
        Pending p = pending.get(caster.getUUID());
        if (p == null) return;
        ServerPlayer a = p.assignedA();
        ServerPlayer b = p.assignedB();
        if (p.needsA() && a == null) {
            a = target;
            AbilityUtil.sendActionBar(caster, Component.literal("Party A set to " + target.getName().getString()).withColor(0x9932cc));
        } else if (p.needsB() && b == null) {
            b = target;
            AbilityUtil.sendActionBar(caster, Component.literal("Party B set to " + target.getName().getString()).withColor(0x9932cc));
        } else return;
        if (a != null && b != null) {
            pending.remove(caster.getUUID());
            finalizeAndPropose(caster, p.result(), a, b);
        } else pending.put(caster.getUUID(), new Pending(p.result(), p.needsA(), p.needsB(), a, b));
    }
}