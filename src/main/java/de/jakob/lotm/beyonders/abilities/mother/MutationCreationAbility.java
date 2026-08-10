package de.jakob.lotm.beyonders.abilities.mother;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MutationCreationAbility extends Ability {
    public MutationCreationAbility(String id) {
        super(id, 4);

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(6, 7, 8, 10, 12));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(4000f, 1700f, 1100f, 625f, 550f));

        baseDamage = 1; //needed only to hook into multiplier
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("mother", 4));
    }

    @Override
    public float getSpiritualityCost() {
        return 800;
    }

    private final DustParticleOptions dustGreen = new DustParticleOptions(
            new Vector3f(68 / 255f, 110 / 255f, 76 / 255f),
            2
    );

    private final DustParticleOptions dustWhite = new DustParticleOptions(
            new Vector3f(138 / 255f, 189 / 255f, 147 / 255f),
            2
    );

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, baseDistance, 2);
        if(target == null) return;

        float targetHeight = target.getBbHeight();
        Vec3 targetPos = target.position().add(0, targetHeight / 2, 0);
        ParticleUtil.spawnParticles(serverLevel, dustGreen, targetPos, 200, .5, targetHeight / 2, .5, 0);
        ParticleUtil.spawnParticles(serverLevel, dustWhite, targetPos, 200, .5, targetHeight / 2, .5, 0);

        if(!target.hasEffect(ModEffects.MUTATED)) {
            int duration = 20 * getDuration(entity, target);
            int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

            target.addEffect(new MobEffectInstance(ModEffects.MUTATED, duration, getDamage(entitySeq)));
        }
        else {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.mutation_creation.already_afflicted").withColor(0x8abd93));
        }

    }

    private int getDamage(int seq){
        return (int) (baseDamage * switch (seq){
                    case 4 -> 1;
                    case 3 -> 1;
                    case 2 -> 2;
                    case 1 -> 2;
                    case 0 -> 3;
                    default -> 0;
                });
    }

    private int getDuration(LivingEntity entity, LivingEntity target) {
        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        int targetSeq = BeyonderData.getSequence(target);

        if(targetSeq < entitySeq)
            return 1;
        else if(entitySeq < targetSeq)
            return 30;
        else
            return 5;
    }
}
