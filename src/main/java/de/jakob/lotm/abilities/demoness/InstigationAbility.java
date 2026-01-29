package de.jakob.lotm.abilities.demoness;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InstigationAbility extends Ability {
    private final HashMap<UUID, LivingEntity> targets = new HashMap<>();

    public InstigationAbility(String id) {
        super(id, 1);

        canBeUsedByNPC = false;
        canBeCopied = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("demoness", 8));
    }

    @Override
    public float getSpiritualityCost() {
        return 40;
    }

    private final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(133 / 255f, 28 / 255f, 214 / 255f),
            2f
    );

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20, 2);
        if(target == null) {
            if(entity instanceof ServerPlayer player) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.translatable("item.lotmcraft.instigation_ability.not_valid_mob").withColor(0xFF68dff7));
                player.connection.send(packet);
            }
            return;
        }

        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.SMOKE, target.getEyePosition().add(0, .2, 0), 40, .4);
        ParticleUtil.spawnParticles((ServerLevel) level, dust, target.getEyePosition().add(0, .2, 0), 40, .4);

        if(!targets.containsKey(entity.getUUID())) {
            targets.put(entity.getUUID(), target);
        } else {
            LivingEntity previousTarget = targets.get(entity.getUUID());
            targets.remove(entity.getUUID());
            if(previousTarget == target) {
                if(entity instanceof ServerPlayer player) {
                    ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.translatable("item.lotmcraft.instigation_ability.cannot_target_same_mob").withColor(0xFF68dff7));
                    player.connection.send(packet);
                }
                return;
            }
            if(!previousTarget.isAlive()) {
                targets.put(entity.getUUID(), target);
                return;
            }

            if(target instanceof Mob mob) {
                addAttackGoalIfAbsent(mob);
                mob.setTarget(previousTarget);
            }
            if(previousTarget instanceof Mob mob) {
                addAttackGoalIfAbsent(mob);
                mob.setTarget(target);
            }
        }
    }

    private void addAttackGoalIfAbsent(Mob mob) {
        AttributeInstance attackAttribute = mob.getAttribute(Attributes.ATTACK_DAMAGE);

        if (attackAttribute == null) {
            return;
        }

        attackAttribute.setBaseValue(5);

        boolean hasAttackGoal = mob.goalSelector.getAvailableGoals().stream()
                .map(WrappedGoal::getGoal)
                .anyMatch(goal -> goal instanceof MeleeAttackGoal);

        boolean hasTargetGoal = mob.targetSelector.getAvailableGoals().stream()
                .map(WrappedGoal::getGoal)
                .anyMatch(goal -> goal instanceof NearestAttackableTargetGoal);

        if (!hasAttackGoal && mob instanceof PathfinderMob pathfinderMob) {
            mob.goalSelector.addGoal(4, new MeleeAttackGoal(pathfinderMob, 1.2D, true));
        }

        if (!hasTargetGoal) {
            mob.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(mob, LivingEntity.class, true));
        }
    }
}
