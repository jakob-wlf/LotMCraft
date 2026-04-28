package de.jakob.lotm.abilities.justiciar;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.RingEffectManager;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class IndividualBalanceAbility extends Ability {

    public static final Map<UUID, Long> INDIVIDUALLY_BALANCED = new ConcurrentHashMap<>();


    public IndividualBalanceAbility(String id) {
        super(id, 40f);
        interactionRadius = 20;
        hasOptimalDistance = true;
        optimalDistance = 10f;
        postsUsedAbilityEventManually = true;
        canBeCopied = false;
        canBeShared =false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("justiciar", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 2000;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20, 1.5f);
        if (target == null) return;

        if (!BeyonderData.isBeyonder(target)) {
            if (entity instanceof net.minecraft.server.level.ServerPlayer sp) {
                sp.sendSystemMessage(Component.translatable("ability.lotmcraft.individual_balance.not_beyonder")
                        .withStyle(ChatFormatting.RED));
            }
            return;
        }
        int DURATION = 3600*(int) Math.max(multiplier(entity)/4,1);
        UUID targetId = target.getUUID();
        INDIVIDUALLY_BALANCED.put(targetId, serverLevel.getGameTime() +DURATION );
        ServerScheduler.scheduleDelayed(DURATION, () -> INDIVIDUALLY_BALANCED.remove(targetId));

        // Gold/white ring at target
        RingEffectManager.createRingForAll(target.position(), 3f, 40,
                1.0f, 0.96f, 0.72f, 0.85f, 1.5f, 4f, serverLevel);

        Component broadcast = Component.translatable("ability.lotmcraft.individual_balance.applied_prefix")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(target.getDisplayName().getString())
                        .withStyle(ChatFormatting.WHITE));
        serverLevel.getServer().getPlayerList().getPlayers().forEach(p -> {
            if (p.level().equals(serverLevel) && p.distanceTo(entity) <= 60) {
                p.sendSystemMessage(broadcast);
            }
        });

        if (target instanceof net.minecraft.server.level.ServerPlayer sp) {
            sp.sendSystemMessage(Component.translatable("ability.lotmcraft.individual_balance.sealed")
                    .withStyle(ChatFormatting.RED));
        }

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, target.position(), entity, this, interactionFlags, 3, 20 * 2));
    }
}
