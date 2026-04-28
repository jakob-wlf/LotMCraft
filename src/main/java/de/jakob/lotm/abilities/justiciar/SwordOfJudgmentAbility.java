package de.jakob.lotm.abilities.justiciar;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.entity.custom.ability_entities.justiciar_pathway.JudgmentSwordEntity;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class SwordOfJudgmentAbility extends Ability {

    private static final double BASE_CHANCE = 0.40;
    private static final double CHANCE_PER_FAIL = 0.10;

    public SwordOfJudgmentAbility(String id) {
        super(id, 5f);
        interactionRadius = 20;
        hasOptimalDistance = true;
        optimalDistance = 10f;
        postsUsedAbilityEventManually = true;
        canBeCopied = true;
        canBeUsedByNPC = false;
        canBeReplicated = true;
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("justiciar", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 400;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 2030*(int) Math.max(multiplier(entity)/4,1), 1.5f);
        if (target == null) return;

        int failCount = ProhibitionAbility.FAIL_COUNT_BY_ENTITY.getOrDefault(target.getUUID(), 0);
        double successChance = Math.min(1.0, BASE_CHANCE + failCount * CHANCE_PER_FAIL);

        if (random.nextDouble() >= successChance) {
            if (entity instanceof ServerPlayer sp) {
                sp.sendSystemMessage(Component.translatable("ability.lotmcraft.sword_of_judgment.deflected")
                        .withStyle(ChatFormatting.RED));
            }
            return;
        }

        // Clear fail count — judgment has been delivered
        ProhibitionAbility.FAIL_COUNT_BY_ENTITY.remove(target.getUUID());

        // Spawn the sword 15 blocks above the target
        float damage = target.getMaxHealth() * 0.4f;
        Vec3 spawnPos = target.position().add(0, 15, 0);
        JudgmentSwordEntity sword = new JudgmentSwordEntity(serverLevel, spawnPos, damage, entity, target, this);
        serverLevel.addFreshEntity(sword);

        Component message = Component.translatable("ability.lotmcraft.sword_of_judgment.delivered_prefix")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(target.getDisplayName().getString())
                        .withStyle(ChatFormatting.WHITE));
        serverLevel.getServer().getPlayerList().getPlayers().forEach(p -> {
            if (p.level().equals(serverLevel) && p.distanceTo(entity) <= 60) {
                p.sendSystemMessage(message);
            }
        });
    }
}