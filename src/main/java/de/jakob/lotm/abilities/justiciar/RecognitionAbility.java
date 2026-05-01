package de.jakob.lotm.abilities.justiciar;

import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class RecognitionAbility extends ToggleAbility {

    public RecognitionAbility(String id) {
        super(id);
        canBeCopied = true;
        canBeUsedByNPC = false;
        canBeReplicated = true;
        cannotBeStolen = false;
        doesNotIncreaseDigestion = true;
        canBeUsedInArtifact = true;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("justiciar", 8));
    }

    @Override
    public float getSpiritualityCost() {
        return 0;
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        level.playSound(null, entity.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.5f, 1.6f);
        AbilityUtil.sendActionBar(entity, Component.literal("§6⚖ §eRecognition §6⚖"));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (entity.tickCount % 10 != 0) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 40 * (int) Math.max(multiplier(entity) / 4, 1), 1.5f, true);

        if (target == null) {
            AbilityUtil.sendActionBar(entity, Component.literal("§6⚖ §7No target in sight §6⚖"));
            return;
        }

        int seq = AbilityUtil.getSeqWithArt(entity, this);
        int targetSeq = BeyonderData.getSequence(target);
        String path = BeyonderData.getPathway(target);

        if ((path.equalsIgnoreCase("justiciar") && targetSeq < seq) || seq > targetSeq) {
            AbilityUtil.sendActionBar(entity, Component.literal("§6⚖ §7No target in sight §6⚖"));
            return;
        }

        String name = target.hasCustomName()
                ? target.getCustomName().getString()
                : target.getType().getDescription().getString();

        String pathFormatted = path.substring(0, 1).toUpperCase() + path.substring(1);

        AbilityUtil.sendActionBar(entity, Component.literal(
                "§6⚖ §e" + name + " §6| §fPathway: §e" + pathFormatted + " §6| §fSequence: §e" + targetSeq + " §6⚖"
        ));

        if (entity.tickCount % 20 == 0) {
            level.playSound(null, entity.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3f, 1.8f);
        }
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        clearArtifactScaling(entity);
        level.playSound(null, entity.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.5f, 1.6f);
        AbilityUtil.sendActionBar(entity, Component.literal(""));
    }
}