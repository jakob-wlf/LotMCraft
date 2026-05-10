package de.jakob.lotm.abilities.hanged;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CullOfSpiritualFleshAbility extends Ability {
    private static final Map<String, Integer> REQUIREMENTS =
            Map.of(HangedPathwayConstants.PATHWAY_ID, HangedPathwayConstants.SEQUENCE_BLACK_KNIGHT);
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LOTMCraft.MOD_ID, "textures/abilities/sword_of_darkness_ability.png");
    private static final float SPIRITUALITY_COST = 560.0f;
    private static final float MAX_DAMAGE_SCALE_SEQ1 = 7.5f;
    private static final float MAX_AREA_SCALE_SEQ1 = 4.0f;
    private static final float MAX_RANGE_SCALE_SEQ1 = 3.2f;
    private static final float GREATSWORD_DAMAGE = 1.7f;
    private static final float SOUL_REND_DAMAGE_PORTION = 0.42f;

    private static final String NO_TARGET_MESSAGE = "ability.lotmcraft.cull_of_spiritual_flesh.no_target";
    private static final String CLEAVE_MESSAGE = "ability.lotmcraft.cull_of_spiritual_flesh.cleave";

    public CullOfSpiritualFleshAbility(String id) {
        super(id, 12.0f, "corruption", "darkness");
        canBeCopied = false;
        autoClear = false;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        double areaScale = HangedPathwayConstants.scaleDoubleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_BLACK_KNIGHT, 1.0, MAX_AREA_SCALE_SEQ1);
        double rangeScale = HangedPathwayConstants.scaleDoubleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_BLACK_KNIGHT, 1.0, MAX_RANGE_SCALE_SEQ1);
        float damageScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_BLACK_KNIGHT, MAX_DAMAGE_SCALE_SEQ1);
        Vec3 start = entity.getEyePosition().add(entity.getLookAngle().scale(1.2));
        Vec3 target = AbilityUtil.getTargetLocation(entity, (int) Math.max(12, Math.round(12 * rangeScale)), 2.4f);
        Vec3 rawDirection = target.subtract(start);
        if (rawDirection.lengthSqr() < 0.001) {
            AbilityUtil.sendActionBar(entity,
                    Component.translatable(NO_TARGET_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
            clearArtifactScaling(entity);
            return;
        }

        Vec3 direction = rawDirection.normalize();
        double offsetRight = random.nextDouble(2.2, 4.2) * areaScale * (random.nextBoolean() ? 1 : -1);
        Vec3 slashStart = VectorUtil.getRelativePosition(start, direction, 0, offsetRight, 5.2 * areaScale);
        Vec3 slashEnd = VectorUtil.getRelativePosition(start, direction, 0, -offsetRight, -5.2 * areaScale);
        Set<Integer> hitEntities = new HashSet<>();
        Set<BlockPos> brokenBlocks = new HashSet<>();

        HangedRenderEffectUtil.playShadowBlade(serverLevel, entity, slashStart, slashEnd, 14);
        HangedEffectUtil.playDepravityCast(serverLevel, entity.position());
        HangedEffectUtil.spawnDepravityBurst(serverLevel, start, 0.9, 30);
        AbilityUtil.sendActionBar(entity,
                Component.translatable(CLEAVE_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));

        AtomicDouble distance = new AtomicDouble(0.0);
        ServerScheduler.scheduleForDuration(0, 1, 14, () -> {
            Vec3 currentStart = slashStart.add(direction.scale(distance.get()));
            Vec3 currentEnd = slashEnd.add(direction.scale(distance.get()));
            Vec3 center = currentStart.add(currentEnd).scale(0.5);
            HangedEffectUtil.spawnDepravityTrail(serverLevel, currentStart, currentEnd, 0.3);
            HangedEffectUtil.spawnShadowTrail(serverLevel, currentStart, currentEnd, 0.45);

            for (LivingEntity victim : AbilityUtil.getNearbyEntities(entity, serverLevel, center, 3.15 * areaScale)) {
                if (!hitEntities.add(victim.getId())) {
                    continue;
                }

                float damage = (float) (DamageLookup.lookupDamage(7, GREATSWORD_DAMAGE * damageScale) * multiplier(entity));
                victim.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.BEYONDER_GENERIC, entity), damage);
                if (hasDegenerateThoughts(victim)) {
                    ModDamageTypes.trueDamage(victim, Math.max(3.0f, damage * SOUL_REND_DAMAGE_PORTION), serverLevel, entity);
                }

                victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1, false, false, true));
                victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 2, false, false, true));
                victim.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 70, 0, false, false, true));
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2, false, false, true));
                victim.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 70, 1, false, false, true));
                if (victim instanceof Player playerVictim) {
                    playerVictim.getData(ModAttachments.SANITY_COMPONENT).decreaseSanityAndSync(0.025f, playerVictim);
                }
            }

            if (BeyonderData.isGriefingEnabled(entity)) {
                breakSlashBlocks(serverLevel, currentStart, currentEnd, areaScale, brokenBlocks);
            }
            distance.addAndGet(1.35 * areaScale);
        }, () -> clearArtifactScaling(entity), serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), serverLevel)));
    }

    private static boolean hasDegenerateThoughts(LivingEntity target) {
        boolean weakenedMind = target.hasEffect(MobEffects.CONFUSION)
                || target.hasEffect(MobEffects.DARKNESS)
                || target.hasEffect(MobEffects.WEAKNESS)
                || target.hasEffect(MobEffects.POISON)
                || target.hasEffect(MobEffects.WITHER)
                || target.hasEffect(MobEffects.HUNGER);
        if (target instanceof Player player) {
            return weakenedMind || player.getData(ModAttachments.SANITY_COMPONENT).getSanity() < 0.72f;
        }
        return weakenedMind;
    }

    private static void breakSlashBlocks(ServerLevel level, Vec3 start, Vec3 end, double areaScale, Set<BlockPos> brokenBlocks) {
        Vec3 diff = end.subtract(start);
        double distance = diff.length();
        if (distance < 0.001) {
            return;
        }

        Vec3 direction = diff.normalize();
        double radius = Math.max(1.0, 1.25 * areaScale);
        for (double travelled = 0; travelled <= distance; travelled += 0.45) {
            Vec3 point = start.add(direction.scale(travelled));
            BlockPos center = BlockPos.containing(point);
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-(int) radius, -1, -(int) radius), center.offset((int) radius, 1, (int) radius))) {
                if (!brokenBlocks.add(pos.immutable())) {
                    continue;
                }
                if (level.getBlockState(pos).isAir() || level.getBlockState(pos).getDestroySpeed(level, pos) < 0) {
                    continue;
                }
                if (pos.distSqr(center) <= (radius * radius) + 0.5) {
                    level.destroyBlock(pos, false);
                }
            }
        }
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return REQUIREMENTS;
    }

    @Override
    protected float getSpiritualityCost() {
        return SPIRITUALITY_COST;
    }

    @Override
    public ResourceLocation getTextureLocation() {
        return TEXTURE;
    }
}
