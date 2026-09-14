package de.jakob.lotm.beyonders.abilities.fool;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.MarionetteOwnerComponent;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.helper.marionettes.MarionetteComponent;
import de.jakob.lotm.util.helper.marionettes.MarionetteUtils;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class PuppeteeringAbility extends Ability {

    private final HashMap<UUID, LivingEntity> entitiesBeingManipulated = new HashMap<>();

    private final DustParticleOptions particleOptions = new DustParticleOptions(new Vector3f(.4f, .4f, .4f), 1.35f);


    public PuppeteeringAbility(String id) {
        super(id, 1);
        canBeUsedByNPC = false;

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(1, 1, 1, 2, 2, 3));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(17500f, 7000f, 3800f, 2500f, 2275f, 1660f));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("fool", 5));
    }

    @Override
    public float getSpiritualityCost() {
        return 40;
    }

    private int getManipulationDistance(int sequence) {
        return switch (sequence) {
            default -> 7;
            case 4 -> 70;
            case 3 -> 90;
            case 2 -> 150;
            case 1 -> 500;
            case 0 -> 1000;
        };
    }

    private int getMaxPuppetCount(int sequence) {
        return switch (sequence) {
            default -> 3;
            case 4 -> 25;
            case 3 -> 50;
            case 2 -> 150;
            case 1 -> 250;
            case 0 -> 500;
        };
    }


    private int getManipulationTimeBySequenceAndSequenceDifference(int sequence, int targetSequence) {
        if (sequence == 5) {
            if (targetSequence < 5) return -1;
            int targetClamped = Math.min(targetSequence, 10);
            return 2400 - (20 * 20) * (targetClamped - 5); // so against seq10 9 8 7 6 5 its -> 20s 40s 60s 80s 100s 120s
        }

        if (sequence == 4) {
            if (targetSequence < 3) return -1;
            if (targetSequence == 3) return 20 * 90;
            return 20 * (30 >> (Math.min(targetSequence, 5) - 4));
        }

        if (sequence == 3) {
            if (targetSequence < 3) return -1;
            return 20 * (40 >> (Math.min(targetSequence, 5) - 3));
        }

        if (sequence == 2) {
            if (targetSequence == 0) return -1;
            if (targetSequence == 1) return 20 * 120;
            return 20 * (50 >> (Math.min(targetSequence, 5) - 2));
        }

        if (sequence == 1) {
            if (targetSequence == 0) return -1;
            return 20 * (60 >> (Math.min(targetSequence, 5) - 1));
        }

        if (sequence == 0) {
            if (targetSequence == 0) return 20 * 120;
            return 20 * (16 >> (Math.min(targetSequence, 5) - 1));
        }

        return -1;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        if(entitiesBeingManipulated.containsKey(entity.getUUID())) {
            entitiesBeingManipulated.remove(entity.getUUID());
            return;
        }

        int sequence = AbilityUtil.getSeqWithArt(entity, this);

        if(!BeyonderData.isBeyonder(entity) || sequence < 0 || sequence > 9)
            return;


        LivingEntity target = AbilityUtil.getTargetEntity(entity, baseDistance, 3);
        if(target == null || target == entity || target instanceof Phantom) {
            if(entity instanceof ServerPlayer player) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.translatable("ability.lotmcraft.puppeteering.no_entity_found").withColor(0xFFff124d));
                player.connection.send(packet);
            }
            return;
        }
        int targetSequence = BeyonderData.getSequence(target);
        int time = getManipulationTimeBySequenceAndSequenceDifference(sequence, targetSequence);

        if(BeyonderData.isBeyonder(target)) {
            if (time < 0) {
                entity.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 8, 5, false, false, false));
                return;
            }
        }

        SanityComponent sanityComponent = target.getData(ModAttachments.SANITY_COMPONENT);
        if (sanityComponent.getSanity() < 0.8f) {
            time = (int) (time * (0.15f + sanityComponent.getSanity()));
        }

        entitiesBeingManipulated.put(entity.getUUID(), target);

        AtomicBoolean stopped = new AtomicBoolean(false);

        String pathway = BeyonderData.getPathway(target);
        if (LOTMCraft.abilityHandler.getById("divination_ability").hasAbility(target) || (pathway.equals("wheel_of_fortune") && targetSequence <= 5) || targetSequence <= 3) {
            if(target instanceof Mob mob) {
                mob.setTarget(entity);
            }
        }

        Vec3 startTemp = entity.getEyePosition().add(entity.getLookAngle().normalize());
        Vec3 endTemp = target.getEyePosition();

        final Vec3 perp1 = VectorUtil.getRandomPerpendicular(endTemp.subtract(startTemp));
        final Vec3 perp2 = VectorUtil.getRandomPerpendicular(endTemp.subtract(startTemp));
        final Vec3 perp3 = VectorUtil.getRandomPerpendicular(endTemp.subtract(startTemp));

        AtomicDouble health = new AtomicDouble(target.getHealth());
        AtomicDouble casterHealth = new AtomicDouble(entity.getHealth());
        AtomicDouble elapsedTicks = new AtomicDouble(0.0);

        int finalTime = time;
        ServerScheduler.scheduleForDuration(0, 1, time, () -> {
            if(stopped.get()) {
                return;
            }

            if(!entitiesBeingManipulated.containsKey(entity.getUUID())) {
                entitiesBeingManipulated.remove(entity.getUUID());
                stopped.set(true);
                return;
            }

            double currentTick = elapsedTicks.addAndGet(1.0);
            float progress = (float) currentTick / finalTime;

            if(!target.isAlive() || target.isRemoved() || target.level() != level) {
                entitiesBeingManipulated.remove(entity.getUUID());
                stopped.set(true);
                return;
            }

            if(target.distanceTo(entity) >= getManipulationDistance(sequence) * 1.75f) {
                entitiesBeingManipulated.remove(entity.getUUID());
                stopped.set(true);
                return;
            }

            if(target.getHealth() < (health.get() * 0.8)) {
                entitiesBeingManipulated.remove(entity.getUUID());
                stopped.set(true);
                return;
            }

            if(entity.getHealth() < (casterHealth.get() * 0.6)) {
                entitiesBeingManipulated.remove(entity.getUUID());
                stopped.set(true);
                return;
            }

            if(entity.hasEffect(ModEffects.LOOSING_CONTROL)){
                entitiesBeingManipulated.remove(entity.getUUID());
                stopped.set(true);
                return;
            }

            Vec3 start = VectorUtil.getRelativePosition(entity.getEyePosition(), new Vec3(entity.getLookAngle().x, 0, entity.getLookAngle().z), .1, .35, -.5);
            Vec3 end = target.getEyePosition();

            for(int i = 0; i < 3; i++) {
                double right = i == 0 ? -2 : (i == 1 ? 1.4 : 2.2);
                double up = i == 2 ? -.2 : (i == 1 ? 0 : 1.2);
                Vec3 perp = i == 0 ? perp1 : (i == 1 ? perp2 : perp3);
                Vec3 startLoc = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(), 0, right, up);

                float distance = (float) end.distanceTo(startLoc);
                float bezierSteps = .025f;

                int maxPoints = Math.max(2, Math.min(10, (int) Math.ceil(distance * 1.5)));

                List<Vec3> points = VectorUtil.createBezierCurve(startLoc, end, perp, bezierSteps, random.nextInt(1, maxPoints + 1));

                for(Vec3 point : points) {
                    ParticleUtil.spawnParticles((ServerLevel) level, particleOptions, point, 1, 0, 0, 0, 0);
                }
            }
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 4, false, false, false));
            if (progress >= 0.20f) {
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 5, false, false, false));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 5, false, false, false));
            }
            if (progress >= 0.60f) {
                target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 10, false, false, false));
                // every 5 seconds - lose abilities for 2 seconds

                if (currentTick % 100 == 0) {
                    DisabledAbilitiesComponent disabledComponent = target.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
                    disabledComponent.disableAbilityUsageForTime("puppeteering_ability_" + entity.getUUID(), 20, target);
                }
            }

            health.set(target.getHealth());
        }, () -> {
            if(stopped.get()) {
                return;
            }
            entitiesBeingManipulated.remove(entity.getUUID());

            MarionetteComponent component = entity.getData(ModAttachments.MARIONETTE_COMPONENT.get());

            MarionetteOwnerComponent data = entity.getData(ModAttachments.MARIONETTE_OWNER_COMPONENT);
            if(entity instanceof Player player && !component.isMarionette() && data.getMarionettes().size() < getMaxPuppetCount(sequence)) {
                turnIntoMarionette(target, player);
            }
            else {
                target.hurt(target.damageSources().generic(), Float.MAX_VALUE);
            }
        }, (ServerLevel) level);
    }

    private void turnIntoMarionette(LivingEntity target, Player player) {
        if(target instanceof Player) {
            Vec3 pos = target.position();
            if(BeyonderData.isBeyonder(target)) {
                int sequence = BeyonderData.getSequence(target);
                String pathway = BeyonderData.getPathway(target);
                target.hurt(target.damageSources().generic(), Float.MAX_VALUE);
                target = new BeyonderNPCEntity(ModEntities.BEYONDER_NPC.get(), target.level(), false, pathway, sequence);
            }
            else {
                target.hurt(target.damageSources().generic(), Float.MAX_VALUE);
                target = new BeyonderNPCEntity(ModEntities.BEYONDER_NPC.get(), target.level(), false, "none", 10);
            }

            target.setPos(pos);
            target.level().addFreshEntity(target);
        }
        target.setHealth(target.getMaxHealth());
        if(target instanceof Mob mob) {
            mob.setTarget(null);
            mob.getNavigation().stop();
        }
        if (MarionetteUtils.turnEntityIntoMarionette(target, player)) {
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.puppeteering.entity_turned").withColor(0xa26fc9));
        } else {
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.puppeteering.entity_turned_failed").withColor(0xa26fc9));
        }
    }
}
