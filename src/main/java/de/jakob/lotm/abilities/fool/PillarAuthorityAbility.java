package de.jakob.lotm.abilities.fool;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.hanged.HangedEffectUtil;
import de.jakob.lotm.attachments.AbilityCooldownComponent;
import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.LordOfMysteriesUtil;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;

public class PillarAuthorityAbility extends SelectableAbility {
    private static final Map<String, Integer> REQUIREMENTS = Map.of(LordOfMysteriesUtil.PATHWAY_ID, LordOfMysteriesUtil.SEQUENCE);
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LOTMCraft.MOD_ID, "textures/abilities/concealed_domain_ability.png");

    public PillarAuthorityAbility(String id) {
        super(id, 12.0f, "space", "history", "fooling", "error");
        canBeCopied = false;
        canBeReplicated = false;
        canBeShared = false;
        autoClear = false;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.pillar_authority.spirit_world_seal",
                "ability.lotmcraft.pillar_authority.space_time_labyrinth",
                "ability.lotmcraft.pillar_authority.miracle_rewrite"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        switch (selectedAbility) {
            case 0 -> castSpiritWorldSeal(serverLevel, entity);
            case 1 -> castSpaceTimeLabyrinth(serverLevel, entity);
            case 2 -> castMiracleRewrite(serverLevel, entity);
            default -> castSpiritWorldSeal(serverLevel, entity);
        }
    }

    private void castSpiritWorldSeal(ServerLevel level, LivingEntity entity) {
        Vec3 center = AbilityUtil.getTargetLocation(entity, 48, 1.7f);
        double radius = 18.0;
        float pulseDamage = (float) (DamageLookup.lookupDamage(10, 1.6f) * multiplier(entity));

        level.playSound(null, center.x, center.y, center.z, SoundEvents.END_PORTAL_SPAWN, SoundSource.HOSTILE, 1.8f, 0.55f);
        HangedEffectUtil.spawnShadowBurst(level, center, 2.5, 70);

        ServerScheduler.scheduleForDuration(0, 5, 120, () -> {
            HangedEffectUtil.spawnShadowField(level, center, 10.0, entity.tickCount / 5);
            List<LivingEntity> victims = level.getEntitiesOfClass(
                    LivingEntity.class,
                    new AABB(center.x - radius, center.y - 8, center.z - radius, center.x + radius, center.y + 8, center.z + radius),
                    victim -> victim != entity && victim.isAlive() && victim.distanceToSqr(center) <= radius * radius
            );

            for (LivingEntity victim : victims) {
                DisabledAbilitiesComponent disabled = victim.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
                disabled.disableAbilityUsageForTime("pillar_authority_spirit_world", 15, victim);
                victim.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 30, 1, false, false, true));
                victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0, false, false, true));
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 4, false, false, true));
                victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 30, 3, false, false, true));
                victim.hurt(ModDamageTypes.source(level, ModDamageTypes.DARKNESS_GENERIC, entity), Math.max(5.0f, pulseDamage * 0.22f));
            }

            AABB projectileBox = new AABB(center.x - radius, center.y - 8, center.z - radius, center.x + radius, center.y + 8, center.z + radius);
            for (Projectile projectile : level.getEntitiesOfClass(Projectile.class, projectileBox, projectile -> projectile.getOwner() != entity)) {
                HangedEffectUtil.spawnShadowBurst(level, projectile.position(), 0.6, 14);
                projectile.discard();
            }
        }, level);
    }

    private void castSpaceTimeLabyrinth(ServerLevel level, LivingEntity entity) {
        Vec3 center = AbilityUtil.getTargetLocation(entity, 40, 1.7f);
        double radius = 16.0;
        float pulseDamage = (float) (DamageLookup.lookupDamage(10, 1.4f) * multiplier(entity));

        level.playSound(null, center.x, center.y, center.z, SoundEvents.PORTAL_TRAVEL, SoundSource.HOSTILE, 1.8f, 0.65f);
        HangedEffectUtil.spawnShadowBurst(level, center, 2.0, 60);

        ServerScheduler.scheduleForDuration(0, 6, 120, () -> {
            HangedEffectUtil.spawnShadowField(level, center, 9.0, entity.tickCount / 6);
            List<LivingEntity> victims = level.getEntitiesOfClass(
                    LivingEntity.class,
                    new AABB(center.x - radius, center.y - 8, center.z - radius, center.x + radius, center.y + 8, center.z + radius),
                    victim -> victim != entity && victim.isAlive() && victim.distanceToSqr(center) <= radius * radius
            );

            for (LivingEntity victim : victims) {
                double angle = level.random.nextDouble() * Math.PI * 2.0;
                double distance = 4.0 + level.random.nextDouble() * 6.0;
                double targetX = center.x + Math.cos(angle) * distance;
                double targetZ = center.z + Math.sin(angle) * distance;
                victim.teleportTo(targetX, center.y, targetZ);
                victim.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 40, 1, false, false, true));
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 3, false, false, true));
                victim.hurt(ModDamageTypes.source(level, ModDamageTypes.DARKNESS_GENERIC, entity), Math.max(4.0f, pulseDamage * 0.18f));
            }
        }, level);
    }

    private void castMiracleRewrite(ServerLevel level, LivingEntity entity) {
        Vec3 center = entity.position().add(0, entity.getBbHeight() * 0.5, 0);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.8f, 0.85f);
        HangedEffectUtil.spawnShadowBurst(level, center, 1.8, 54);

        entity.setHealth(entity.getMaxHealth());
        entity.removeAllEffects();
        if (entity instanceof ServerPlayer player) {
            AbilityCooldownComponent cooldowns = player.getData(ModAttachments.COOLDOWN_COMPONENT);
            cooldowns.removeAllCooldowns();
            BeyonderData.setSpirituality(player, BeyonderData.getMaxSpirituality(BeyonderData.getPathway(player), BeyonderData.getSequence(player), player));
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.pillar_authority.miracle_rewrite_used")
                    .withColor(BeyonderData.pathwayInfos.get(LordOfMysteriesUtil.PATHWAY_ID).color()));
        }

        List<LivingEntity> enemies = AbilityUtil.getNearbyEntities(entity, level, entity.position(), 12.0, false);
        for (LivingEntity enemy : enemies) {
            enemy.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0, false, false, true));
            enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2, false, false, true));
            enemy.hurt(ModDamageTypes.source(level, ModDamageTypes.DARKNESS_GENERIC, entity),
                    (float) (DamageLookup.lookupDamage(10, 1.2f) * multiplier(entity) * 0.6f));
        }
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return REQUIREMENTS;
    }

    @Override
    protected float getSpiritualityCost() {
        return 12000.0f;
    }

    @Override
    public ResourceLocation getTextureLocation() {
        return TEXTURE;
    }
}
