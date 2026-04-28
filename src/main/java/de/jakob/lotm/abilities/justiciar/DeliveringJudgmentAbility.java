package de.jakob.lotm.abilities.justiciar;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DeliveringJudgmentAbility extends Ability {

    public DeliveringJudgmentAbility(String id) {
        super(id, 5f);
        hasOptimalDistance = false;
        canBeCopied = false;
        canBeShared = false;
        canBeReplicated = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("justiciar", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 300;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;

        if (ProhibitionAbility.FAIL_COUNT_BY_ENTITY.isEmpty()) {
            sendFizzle(entity);
            return;
        }

        UUID targetId = ProhibitionAbility.FAIL_COUNT_BY_ENTITY.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);

        if (targetId == null) {
            sendFizzle(entity);
            return;
        }

        LivingEntity target = serverLevel.getEntity(targetId) instanceof LivingEntity le ? le : null;

        if (target == null) {
            sendFizzle(entity);
            return;
        }

        entity.teleportTo(target.getX(), target.getY(), target.getZ());
        EffectManager.playEffect(EffectManager.Effect.WAYPOINT, target.getX(), target.getY(), target.getZ(), serverLevel);

        if (entity instanceof ServerPlayer sp) {
            sp.sendSystemMessage(Component.translatable("ability.lotmcraft.delivering_judgment.arrived")
                    .withStyle(ChatFormatting.GOLD));
        }
    }

    private void sendFizzle(LivingEntity entity) {
        if (entity instanceof ServerPlayer sp) {
            sp.sendSystemMessage(Component.translatable("ability.lotmcraft.delivering_judgment.no_target")
                    .withStyle(ChatFormatting.RED));
        }
    }
}
