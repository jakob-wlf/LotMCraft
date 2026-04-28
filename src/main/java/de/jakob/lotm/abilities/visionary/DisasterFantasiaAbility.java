package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.abilities.tyrant.TorrentialDownpourAbility;
import de.jakob.lotm.abilities.wheel_of_fortune.calamities.Earthquake;
import de.jakob.lotm.abilities.wheel_of_fortune.calamities.Meteor;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.MeteorEntity;
import de.jakob.lotm.entity.custom.ability_entities.TornadoEntity;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3f;

import java.util.*;

public class DisasterFantasiaAbility extends SelectableAbility {

    private static final Earthquake EARTHQUAKE = new Earthquake();
    private static final Meteor METEOR = new Meteor();
    private static final int METEOR_COUNT = 25;
    private static final double METEOR_RADIUS = 50.0;

    private final DustParticleOptions plagueDust = new DustParticleOptions(new Vector3f(0, 0, 0), 10f);

    public DisasterFantasiaAbility(String id) {
        super(id, 35f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 1));
    }

    @Override
    public float getSpiritualityCost() {
        return 7500;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.disaster_fantasia.earthquake",
                "ability.lotmcraft.disaster_fantasia.meteor",
                "ability.lotmcraft.disaster_fantasia.tornado",
                "ability.lotmcraft.disaster_fantasia.plague"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        Vec3 targetPos = AbilityUtil.getTargetLocation(entity, 150* (int) Math.max(multiplier(entity)/4,1), 3);
        float multiplier = multiplier(entity);
        boolean griefing = BeyonderData.isGriefingEnabled(entity);

        switch (abilityIndex) {
            case 0 -> EARTHQUAKE.spawnCalamity(serverLevel, targetPos, multiplier, griefing, 65* (int) Math.max(multiplier(entity)/4,1), (float) DamageLookup.lookupDps(4, .925, 8, 20) * (int) Math.max(multiplier(entity)/4,1), entity, false);
            case 1 -> spawnMeteorShower(serverLevel, targetPos, multiplier, griefing, entity);
            case 2 -> createTornados(serverLevel, entity);
            case 3 -> createPlague(level, entity);
        }
    }

    private void spawnMeteorShower(ServerLevel level, Vec3 center,
                                          float multiplier, boolean griefing, LivingEntity entity) {
        Random rand = new Random();
        for (int i = 0; i < METEOR_COUNT; i++) {
            ServerScheduler.scheduleDelayed(i * 4, () -> {
                double angle = rand.nextDouble() * 2 * Math.PI;
                double distance = rand.nextDouble() * METEOR_RADIUS;
                double offsetX = Math.cos(angle) * distance;
                double offsetZ = Math.sin(angle) * distance;
                Vec3 meteorPos = new Vec3(center.x + offsetX, center.y, center.z + offsetZ);


                MeteorEntity meteor = new MeteorEntity(level, 2.5f,  (float) DamageLookup.lookupDamage(2, 1)  * (int)Math.max(multiplier(entity)/4,1), 3, entity, griefing, 13, 12);
                meteor.setPosition(meteorPos);
                level.addFreshEntity(meteor);
            }, level, () -> AbilityUtil.getTimeInArea(null, new Location(center, level)));
        }
    }

    private void createTornados(ServerLevel serverLevel, LivingEntity entity) {
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 12* (int)Math.max(multiplier(entity)/4,1), 3);

        Vec3 pos = AbilityUtil.getTargetLocation(entity, 12* (int)Math.max(multiplier(entity)/4,1), 2);

        TornadoEntity tornado = target == null ? new TornadoEntity(ModEntities.TORNADO.get(), serverLevel, .15f, (float) DamageLookup.lookupDamage(2, .35)  * (int)Math.max(multiplier(entity)/4,1), entity) : new TornadoEntity(ModEntities.TORNADO.get(), serverLevel, .15f, (float) DamageLookup.lookupDamage(2, .35)  * (int)Math.max(multiplier(entity)/4,1), entity, target);
        tornado.setPos(pos);
        serverLevel.addFreshEntity(tornado);

        for(int i = 0; i < 30; i++) {
            TornadoEntity additionalTornado = target == null || random.nextInt(4) != 0 ? new TornadoEntity(ModEntities.TORNADO.get(), serverLevel, .15f, (float) DamageLookup.lookupDamage(2, .35)  * (int)Math.max(multiplier(entity)/4,1), entity) : new TornadoEntity(ModEntities.TORNADO.get(), serverLevel, .15f, (float) DamageLookup.lookupDamage(2, .35)  * (int)Math.max(multiplier(entity)/4,1), entity, target);
            Vec3 randomOffset = new Vec3((serverLevel.random.nextDouble() - 0.5) * 120, 3, (serverLevel.random.nextDouble() - 0.5) * 120);
            additionalTornado.setPos(pos.add(randomOffset));
            serverLevel.addFreshEntity(additionalTornado);
        }
    }

    private void createPlague(Level level, LivingEntity entity){
        if(level.isClientSide || !(level instanceof ServerLevel serverLevel))
            return;

        float multiplier = multiplier(entity);

        ServerScheduler.scheduleForDuration(0, 20, 20 * 80, () -> {
            if (entity.level().isClientSide)
                return;

            // Disease is suppressed by purification, cleansing, life aura, or blooming interactions
            Location currentLoc = new Location(entity.position(), entity.level());
            int seq = AbilityUtil.getSeqWithArt(entity, this);
            if(InteractionHandler.isInteractionPossible(currentLoc, "purification", seq) ||
                    InteractionHandler.isInteractionPossible(currentLoc, "cleansing", seq))
                return;

            boolean bloomingNearby = InteractionHandler.isInteractionPossible(currentLoc, "blooming", seq);
            float damageMult = (bloomingNearby) ? 0.4f : 1f;

            ParticleUtil.spawnParticles((ServerLevel) entity.level(), ModParticles.DISEASE.get(), entity.position(), 160, 50, 0.02);
            ParticleUtil.spawnParticles((ServerLevel) entity.level(), plagueDust, entity.position(), 160, 50, 0.02);
            AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) entity.level(), entity, 45*(int) Math.max(multiplier(entity)/4,1), entity.position(), new MobEffectInstance(MobEffects.WITHER, 20, 3, false, false, false));
            AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) entity.level(), entity, 45*(int) Math.max(multiplier(entity)/4,1), entity.position(), new MobEffectInstance(MobEffects.BLINDNESS, 20, 4, false, false, false));
            AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) entity.level(), entity, 45*(int) Math.max(multiplier(entity)/4,1), entity.position(), new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 2, false, false, false));
            AbilityUtil.damageNearbyEntities((ServerLevel) entity.level(), entity, 45*(int) Math.max(multiplier(entity)/4,1), DamageLookup.lookupDps(4, .3, 35, 20) *(int) Math.max(multiplier(entity)/6,1) * damageMult, entity.position(), true, false, true, 0, ModDamageTypes.source(level, ModDamageTypes.DEMONESS_GENERIC, entity));
        }, null, serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));
    }
}