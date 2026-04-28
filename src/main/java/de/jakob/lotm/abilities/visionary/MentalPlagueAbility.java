package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.abilities.visionary.passives.MetaAwarenessAbility;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class MentalPlagueAbility extends Ability {
    public MentalPlagueAbility(String id) {
        super(id, 30, "plague");
        canBeCopied = false;
        canBeReplicated = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 4));
    }

    @Override
    public float getSpiritualityCost() {
        return 2400;
    }

    private final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(250 / 255f, 201 / 255f, 102 / 255f),
            1f
    );

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 30* (int) Math.max(multiplier(entity)/4,1), 2);
        if(target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.mental_plague.no_target").withColor(0xf5ca7f));
            return;
        }

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        int targetSeq = BeyonderData.getSequence(target);
        if(BeyonderData.getPathway(target).equals("visionary") && targetSeq < entitySeq){
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.dream_traversal.failed").withColor(0xFFff124d));

            if(targetSeq <= 1 && target instanceof ServerPlayer targetPlayer && entity instanceof ServerPlayer entityPlayer){
                MetaAwarenessAbility.onDivined(entityPlayer, targetPlayer);
            }

            return;
        }

        if(AbilityUtil.isTargetSignificantlyStronger(entitySeq, targetSeq)) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.mental_plague.target_too_strong").withColor(0xf5ca7f));
            return;
        }

        // Mental Plague is weakened by purification
        Location targetLoc = new Location(target.position(), level);
        int seq = AbilityUtil.getSeqWithArt(entity, this);
        int duration = InteractionHandler.isInteractionPossible(targetLoc, "purification", seq) ? 20 * 60 * 2 : 20 * 60 * 10;

        target.addEffect(new MobEffectInstance(ModEffects.MENTAL_PLAGUE, duration, 1+ (int) Math.max(multiplier(entity)/4,1), false, false, false));
        // ParticleUtil.spawnParticles((ServerLevel) target.level(), dust, target.getEyePosition(), 200, .4);
    }
}
