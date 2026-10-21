package de.jakob.lotm.beyonders.abilities.black_emperor;

import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.events.custom.TargetEntityEvent;
import de.jakob.lotm.events.custom.TargetLocationEvent;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
@EventBusSubscriber
public class DistortionAbility extends ToggleAbility {
    public DistortionAbility(String id) {
        super(id);
        hasDynamicSpirituality = true;
        hasDynamicCooldown = true;

        dynamicCooldown = new LinkedList<>(List.of(7, 7, 7, 7, 7, 6, 6));
        dynamicSpirituality = new LinkedList<>(List.of(1000f, 700f, 600f, 400f, 300f, 100f, 80f));
    }

    private final int[] duration = new int[] {6, 5, 5, 4, 4, 3, 3};
    public static final String DISTORT_TAG = "DISTORTION";
    public static final int PROJECTILE_RANGE = 5;
    private static final double DISTORT_BLEND = 0.5;
    private static final double AWAY_BIAS     = 0.7;

    private final int MOVEMENT_RANGE = 7;
    private final int LANGUAGE_RANGE = 10;
    public static final LinkedList<Integer> dynamicDistortCost = new LinkedList<>(List.of(1000, 700, 600, 400, 300, 100, 80));


    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of("black_emperor", 6);
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }


    @Override
    public void tick(Level level, LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player) || !(level instanceof ServerLevel serverLevel)) return;

        Vec3 playerPos = player.position();
        Vec3 eyePos    = player.getEyePosition();

        // Distort Projectiles
        double rangeSq = PROJECTILE_RANGE * PROJECTILE_RANGE;
        for (Entity e : AbilityUtil.getAllNearbyEntities(entity, serverLevel, eyePos, PROJECTILE_RANGE)) {
            if (e instanceof Projectile projectile) {
                attemptProjectileDistort(player, projectile, playerPos, rangeSq);
            }
        }

        // Distort Player movement toward you to be away
        double moveRangeSq = MOVEMENT_RANGE * MOVEMENT_RANGE;
        for (LivingEntity target : AbilityUtil.getNearbyEntities(entity, serverLevel, eyePos, MOVEMENT_RANGE)) {
            if (!isInFieldOfView(player, target, 30.0)) continue;
            attemptMovementDistort(player, target, playerPos, moveRangeSq);
        }

    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if(!(entity instanceof ServerPlayer player) || !(level instanceof ServerLevel serverLevel)) return;
        player.getPersistentData().putBoolean(DISTORT_TAG, true);
        ServerScheduler.scheduleDelayed(duration[BeyonderData.getSequence(entity)]*20, () -> {
            cancel(serverLevel, player);
        });
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        entity.getPersistentData().remove(DISTORT_TAG);
    }


    private static boolean isInFieldOfView(ServerPlayer player, LivingEntity target, double halfAngleDegrees) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 toTarget = target.getEyePosition().subtract(player.getEyePosition());
        if (toTarget.lengthSqr() < 1.0E-6) return true;
        toTarget = toTarget.normalize();

        double dot = look.dot(toTarget);
        double cosHalfAngle = Math.cos(Math.toRadians(halfAngleDegrees));

        return dot >= cosHalfAngle;
    }

    public static boolean affordDistort(LivingEntity entity, int cost){
        if(BeyonderData.getSpirituality(entity) >= cost) {
            BeyonderData.reduceSpirituality(entity, cost);
            return true;
        }
        return false;
    }

    private static void attemptProjectileDistort(ServerPlayer player, Projectile projectile, Vec3 playerPos, double rangeSq) {
        if(!(projectile.getOwner() instanceof LivingEntity owner)) return;
        if (projectile.getOwner() == player) return;
        if (!canDistort(player, owner)) return;
        if (projectile.position().distanceToSqr(playerPos) > rangeSq) return;

        Vec3 motion = projectile.getDeltaMovement();
        double speed = motion.length();
        if (speed < 0.01) return;

        long seed = projectile.getUUID().getMostSignificantBits()
                ^ projectile.getUUID().getLeastSignificantBits();
        RandomSource projRand = RandomSource.create(seed);
        Vec3 randomDir = new Vec3(
                projRand.nextGaussian(),
                projRand.nextGaussian(),
                projRand.nextGaussian()
        ).normalize();

        Vec3 away = projectile.position().subtract(playerPos);
        if (away.lengthSqr() < 0.01) return;
        away = away.normalize();

        Vec3 targetDir = randomDir.scale(1 - AWAY_BIAS)
                .add(away.scale(AWAY_BIAS));
        if (targetDir.lengthSqr() < 1.0E-4) targetDir = away;
        targetDir = targetDir.normalize();

        Vec3 blended = motion.normalize().scale(1 - DISTORT_BLEND)
                .add(targetDir.scale(DISTORT_BLEND));
        Vec3 newDir = blended.lengthSqr() < 1.0E-4 ? targetDir : blended.normalize();

        projectile.setDeltaMovement(newDir.scale(speed));
        projectile.hurtMarked = true;
    }

    private void attemptMovementDistort(ServerPlayer distorter, LivingEntity target,
                                        Vec3 playerPos, double moveRangeSq) {
        if (target == distorter) return;
        if (target.position().distanceToSqr(playerPos) > moveRangeSq) return;
        if (!canDistort(distorter, target)) return;
        if(!affordDistort(distorter, dynamicDistortCost.get(BeyonderData.getSequence(distorter)))) return;
        target.addEffect(new MobEffectInstance(
                ModEffects.REVERSED_CONTROLS,
                40,
                1,
                false,
                false,
                false
        ));

        if (target instanceof Mob mob && mob.getTarget() == distorter) {
            mob.setTarget(null);
        }
    }


    private static boolean attemptDistort(LivingEntity entity, LivingEntity target) {
        if (target instanceof ServerPlayer || target instanceof BeyonderNPCEntity) {
            return canDistort(entity, target) && affordDistort(entity, dynamicDistortCost.get(BeyonderData.getSequence(entity)));
        }
        return false;
    }

    public static boolean canDistort(LivingEntity entity, LivingEntity target) {
        RandomSource random = RandomSource.create();
        if(!entity.getPersistentData().getBoolean(DISTORT_TAG)) return false;
        if(!BeyonderData.isBeyonder(target)) return true;
        double chance = random.nextDouble();
        return (chance < EloquenceAbility.calculateMultiplier(BeyonderData.getSequence(entity), BeyonderData.getSequence(target)));
    }


    @SubscribeEvent
    public static void onLocation(TargetLocationEvent event) {
        Vec3 startPos  = event.getSourceEntity().position();
        Vec3 targetPos = event.getTargetLocation();

        if (!(event.getSourceEntity().level() instanceof ServerLevel level)) return;

        double rangeSq = PROJECTILE_RANGE * PROJECTILE_RANGE;

        AABB searchBox = new AABB(startPos, targetPos).inflate(PROJECTILE_RANGE);
        List<Entity> entities = level.getEntities(
                event.getSourceEntity(),
                searchBox,
                e -> e.getPersistentData().getBoolean(DISTORT_TAG)
        );
        if (entities.isEmpty()) return;

        Vec3 pathVec    = targetPos.subtract(startPos);
        double pathLenSq = pathVec.lengthSqr();
        if (pathLenSq < 1.0E-6) return;

        Vec3 pathDir = pathVec.normalize();

        Vec3 worldUp = new Vec3(0, 1, 0);
        Vec3 right   = pathDir.cross(worldUp);
        if (right.lengthSqr() < 1.0E-4) {
            right = new Vec3(1, 0, 0);
        }
        right = right.normalize();
        Vec3 left = right.scale(-1);
        Vec3 up   = worldUp;

        Vec3 deflectDir = switch (level.random.nextInt(3)) {
            case 0 -> left;
            case 1 -> right;
            default -> up;
        };

        for (Entity entity : entities) {
            if(!(entity instanceof LivingEntity livingEntity)) return;
            if (entity.position().distanceToSqr(targetPos) <= rangeSq) {
                double pushDist = 5.0 + level.random.nextDouble();
                if(attemptDistort(livingEntity, event.getSourceEntity())) event.setTargetLocation(entity.position().add(deflectDir.scale(pushDist)));
                return;
            }
        }

        for (Entity entity : entities) {
            if(!(entity instanceof LivingEntity livingEntity)) return;
            Vec3 point = entity.position();

            double t = point.subtract(startPos).dot(pathVec) / pathLenSq;
            t = Mth.clamp(t, 0.0, 1.0);
            Vec3 closest = startPos.add(pathVec.scale(t));

            if (point.distanceToSqr(closest) <= rangeSq) {
                double pushDist = 5.0 + level.random.nextDouble();
                if(attemptDistort(livingEntity, event.getSourceEntity())) event.setTargetLocation(entity.position().add(deflectDir.scale(pushDist)));
                return;
            }
        }
    }



    @SubscribeEvent
    public static void onPlayerTarget(TargetEntityEvent event) {
        if(!(event.getTargetEntity() instanceof ServerPlayer serverPlayer)) return;
        if(canDistort(event.getTargetEntity(), event.getTargetEntity())) event.setTargetEntity(null);
    }
}
