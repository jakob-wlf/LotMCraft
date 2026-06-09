package de.jakob.lotm.abilities.darkness;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SwordOfDarknessAbility extends Ability {
    public SwordOfDarknessAbility(String id) {
        super(id, 8, "darkness");
        this.canBeCopied = false;
        autoClear = false;
        canBeShared = false;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            ClientHandler.applyCameraShakeToPlayersInRadius(2, 30, (ClientLevel) level, entity.position(), 60);
            return;
        }

        AbilityUtil.addPotionEffectToNearbyEntities(serverLevel, null, 25, entity.position(), new MobEffectInstance(MobEffects.BLINDNESS, 20, 0, false, false, false));

        Vec3 startLoc = entity.getEyePosition().add(entity.getLookAngle().scale(1.5));
        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 60, 4);
        Vec3 direction = targetLoc.subtract(startLoc).normalize();

        double offsetRight = random.nextDouble(2, 5) * (random.nextBoolean() ? 1 : -1);

        Vec3 slashStart = VectorUtil.getRelativePosition(startLoc, direction, 0, offsetRight, 8);
        Vec3 slashEnd = VectorUtil.getRelativePosition(startLoc, direction, 0, -offsetRight, -8);

        AtomicDouble distance = new AtomicDouble(0);
        ServerScheduler.scheduleForDuration(0, 1, 20 * 6, () -> {
            for (int j = 0; j < 3; j++) {
                Vec3 currentSlashStart = slashStart.add(direction.scale(distance.get()));
                Vec3 currentSlashEnd = slashEnd.add(direction.scale(distance.get()));

                Vec3 slashDirection = currentSlashEnd.subtract(currentSlashStart).normalize();
                double slashLength = currentSlashStart.distanceTo(currentSlashEnd);
                for(double i = 0; i < slashLength; i += 0.25) {
                    Vec3 point = currentSlashStart.add(slashDirection.scale(i));

                    // Sword of Darkness is weakened by Wall of Light (purification)
                    Location pointLoc = new Location(point, level);
                    int seq = AbilityUtil.getSeqWithArt(entity, this);
                    boolean purified = InteractionHandler.isInteractionPossible(pointLoc, "purification", seq);
                    float damageMult = purified ? 0.5f : 1f;
                    float multiplier = multiplier(entity);
                    float actualsequence =  seq<=1? 1: 3;
                    ParticleUtil.spawnParticles(serverLevel, ModParticles.BLACK.get(), point, 3, 0.2, 0);
                    AbilityUtil.damageNearbyEntities(serverLevel, entity, 3, Math.max(multiplier/2,1) * DamageLookup.lookupDamage((int) actualsequence, 1.2) * damageMult, point, true, false, false, 10, ModDamageTypes.source(level, ModDamageTypes.DARKNESS_GENERIC, entity));
                    //Additional effects
                    AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), 20*Math.max(multiplier/2, 1)).forEach(e -> {
                        SanityComponent sanityComponent = e.getData(ModAttachments.SANITY_COMPONENT);
                        sanityComponent.increaseSanityAndSync(-0.000163f*(int) multiplier, e);
                        if (seq <= 1)
                        {
                            //Eternal rest part
                            int actualDuration = 20*5* (int) multiplier;
                            e.addEffect(new MobEffectInstance(ModEffects.ASLEEP, actualDuration, 1, false, false, true));
                            e.addEffect(new MobEffectInstance(MobEffects.DARKNESS, actualDuration, 5, false, false, false));
                            e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, actualDuration, 4, false, false, false));
                            e.setDeltaMovement(Vec3.ZERO);
                            e.setOnGround(true);
                            var pos = e.position();
                            e.setDeltaMovement(new Vec3(0, 0, 0));
                            e.hurtMarked = true;

                            e.teleportTo(pos.x, pos.y, pos.z);
                            // Despair part
                            Location eLoc = new Location(e.position(), serverLevel);
                            boolean hasMorale = InteractionHandler.isInteractionPossibleForEntity(eLoc, "morale_boost", seq, e);
                            int duration = hasMorale? 20*2:20*4;
                            BeyonderData.addModifierWithTimeLimit(e, "sword_of_darkness_multiplier_reduction", 0.3, duration);
                        };
                    });
                    if(BeyonderData.isGriefingEnabled(entity)) {
                        if(serverLevel.getBlockState(BlockPos.containing(point)).getDestroySpeed(level, BlockPos.containing(point)) < 0)
                            continue;
                        serverLevel.setBlockAndUpdate(BlockPos.containing(point), Blocks.AIR.defaultBlockState());
                    }
                }

                distance.addAndGet(.5);
            }
        }, () -> clearArtifactScaling(entity), serverLevel, () -> AbilityUtil.getTimeInArea(entity, new de.jakob.lotm.util.data.Location(entity.position(), serverLevel)));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("darkness", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 3000;
    }
}
