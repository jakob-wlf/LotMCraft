package de.jakob.lotm.abilities.tyrant;

import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.attachments.ActiveShaderComponent;
import de.jakob.lotm.attachments.FogComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.TornadoEntity;
import de.jakob.lotm.entity.custom.VolcanoEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.*;

public class CalamityCreationAbility extends SelectableAbility {
    public CalamityCreationAbility(String id) {
        super(id, 25);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1200;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.calamity_creation.volcano", "ability.lotmcraft.calamity_creation.snow_storm", "ability.lotmcraft.calamity_creation.drought", "ability.lotmcraft.calamity_creation.tornados"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        switch (abilityIndex) {
            case 0 -> createVolcano(serverLevel, entity);
            case 1 -> createSnowStorm(serverLevel, entity);
            case 2 -> createDrought(serverLevel, entity);
            case 3 -> createTornados(serverLevel, entity);
        }
    }

    private void createVolcano(ServerLevel serverLevel, LivingEntity entity) {
        Vec3 targetPos = AbilityUtil.getTargetLocation(entity, 60, 2);

        VolcanoEntity volcano = new VolcanoEntity(serverLevel, targetPos, (float) DamageLookup.lookupDamage(2, .5) * (float) multiplier(entity), entity);
        serverLevel.addFreshEntity(volcano);

    }

    private final DustParticleOptions blizzardDust = new DustParticleOptions(new Vector3f(202 / 255f, 232 / 255f, 235 / 255f), 10.0f);

    private void createSnowStorm(ServerLevel serverLevel, LivingEntity entity) {
        Vec3 startPos = entity.position();
        boolean griefing = BeyonderData.isGriefingEnabled(entity);

        List<BlockPos> snowSphereBlocks = griefing
                ? AbilityUtil.getBlocksInSphereRadius(serverLevel, startPos, 60, true, false, false)
                : Collections.emptyList();

        List<BlockPos> snowLayerPositions = snowSphereBlocks.stream()
                .filter(b -> serverLevel.getBlockState(b).isAir()
                        && !serverLevel.getBlockState(b.below()).isAir())
                .toList();

        List<BlockPos> snowPositions = snowSphereBlocks.stream()
                .filter(b -> !serverLevel.getBlockState(b).isAir()) // mimic excludeEmpty = true
                .filter(b -> serverLevel.isEmptyBlock(b.above()))    // mimic onlyExposed = true
                .toList();


        ServerScheduler.scheduleForDuration(0, 4, 20 * 30, () -> {
            // Damage and Effects
            AbilityUtil.damageNearbyEntities(serverLevel, entity, 60, DamageLookup.lookupDps(2, .8, 4, 30) * (float) multiplier(entity), startPos, true, false);
            AbilityUtil.addPotionEffectToNearbyEntities(serverLevel, entity, 60, startPos,
                    new MobEffectInstance(MobEffects.WEAKNESS, 20 * 5, 1, false, false, false),
                    new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 5, 7, false, false, false));

            // Particles and shader
            for(Player player : AbilityUtil.getNearbyEntities(null, serverLevel, startPos, 60, true)
                    .stream()
                    .filter(e -> e instanceof Player)
                    .map(e -> (Player)e)
                    .toList()) {

                FogComponent fogComponent = player.getData(ModAttachments.FOG_COMPONENT);
                fogComponent.setActiveAndSync(true, player);
                fogComponent.setFogIndexAndSync(FogComponent.FOG_TYPE.BLIZZARD, player);

                ParticleUtil.spawnParticles(serverLevel, ParticleTypes.SNOWFLAKE, player.position(), 60, 5, 2, 5, .05);
                ParticleUtil.spawnParticles(serverLevel, blizzardDust, player.position(), 200, 6, 3, 6, .05);

                ActiveShaderComponent component = player.getData(ModAttachments.SHADER_COMPONENT);
                component.setShaderActiveAndSync(true, player);
                component.setShaderIndexAndSync(ActiveShaderComponent.SHADERTYPE.BLIZZARD, player);
            }

            // Weather Change
            if(serverLevel.isRaining()) {
                serverLevel.setWeatherParameters(6000, 0, false, false);
            }

            // Block Changes
            if(griefing) {
                for (int i = 0; i < 120; i++) {
                    if (!snowLayerPositions.isEmpty()) {
                        BlockPos snowPos = snowLayerPositions.get(serverLevel.random.nextInt(snowLayerPositions.size()));
                        serverLevel.setBlockAndUpdate(snowPos, Blocks.SNOW.defaultBlockState());
                    }
                }

                for (int i = 0; i < 15; i++) {
                    if (!snowPositions.isEmpty()) {
                        BlockPos snowPos = snowPositions.get(serverLevel.random.nextInt(snowPositions.size()));
                        serverLevel.setBlockAndUpdate(snowPos, Blocks.SNOW_BLOCK.defaultBlockState());
                    }
                }
            }
        });
    }

    private final DustParticleOptions droughtDust = new DustParticleOptions(new Vector3f(217 / 255f, 121 / 255f, 65 / 255f), 10.0f);

    private void createDrought(ServerLevel serverLevel, LivingEntity entity) {
        Vec3 startPos = entity.position();
        boolean griefing = BeyonderData.isGriefingEnabled(entity);

        List<BlockPos> sphereBlocks = griefing
                ? AbilityUtil.getBlocksInSphereRadius(serverLevel, startPos, 65, true, false, false)
                : Collections.emptyList();

        List<BlockPos> firePositions = sphereBlocks.stream()
                .filter(b -> serverLevel.getBlockState(b).isAir()
                        && !serverLevel.getBlockState(b.below()).isAir())
                .toList();

        List<BlockPos> sandPositions = sphereBlocks.stream()
                .filter(b -> !serverLevel.getBlockState(b).isAir()) // mimic excludeEmpty = true
                .filter(b -> serverLevel.isEmptyBlock(b.above()))    // mimic onlyExposed = true
                .toList();

        ServerScheduler.scheduleForDuration(0, 4, 20 * 30, () -> {
            // Damage and Effects
            AbilityUtil.damageNearbyEntities(serverLevel, entity, 90, DamageLookup.lookupDps(2, .8, 4, 30) * (float) multiplier(entity), startPos, true, false, 20 * 10);
            AbilityUtil.addPotionEffectToNearbyEntities(serverLevel, entity, 90, startPos,
                    new MobEffectInstance(MobEffects.WEAKNESS, 20 * 5, 1, false, false, false),
                    new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 5, 4, false, false, false));

            // Particles and shader
            for(Player player : AbilityUtil.getNearbyEntities(null, serverLevel, startPos, 90, true)
                    .stream()
                    .filter(e -> e instanceof Player)
                    .map(e -> (Player)e)
                    .toList()) {

                FogComponent fogComponent = player.getData(ModAttachments.FOG_COMPONENT);
                fogComponent.setActiveAndSync(true, player);
                fogComponent.setFogIndexAndSync(FogComponent.FOG_TYPE.DROUGHT, player);

                ParticleUtil.spawnParticles(serverLevel, ParticleTypes.FLAME, player.position(), 90, 6, 3, 6, .05);
                ParticleUtil.spawnParticles(serverLevel, droughtDust, player.position(), 200, 8, 3, 8, .05);

                ActiveShaderComponent component = player.getData(ModAttachments.SHADER_COMPONENT);
                component.setShaderActiveAndSync(true, player);
                component.setShaderIndexAndSync(ActiveShaderComponent.SHADERTYPE.DROUGHT, player);
            }

            // Weather Change
            if(serverLevel.isRaining()) {
                serverLevel.setWeatherParameters(6000, 0, false, false);
            }

            // Block Changes
            if(griefing) {
                for (int i = 0; i < 15; i++) {
                    if (!firePositions.isEmpty()) {
                        BlockPos firePos = firePositions.get(serverLevel.random.nextInt(firePositions.size()));
                        serverLevel.setBlockAndUpdate(firePos, Blocks.FIRE.defaultBlockState());
                    }
                }

                for (int i = 0; i < 50; i++) {
                    if (!sandPositions.isEmpty()) {
                        BlockPos sandPos = sandPositions.get(serverLevel.random.nextInt(sandPositions.size()));
                        serverLevel.setBlockAndUpdate(sandPos, Blocks.SAND.defaultBlockState());
                    }
                }

                for (int i = 0; i < 18; i++) {
                    if (!sandPositions.isEmpty()) {
                        BlockPos sandPos = sandPositions.get(serverLevel.random.nextInt(sandPositions.size()));
                        serverLevel.setBlockAndUpdate(sandPos, Blocks.SANDSTONE.defaultBlockState());
                    }
                }
            }
        });
    }

    private void createTornados(ServerLevel serverLevel, LivingEntity entity) {
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 12, 3);

        Vec3 pos = AbilityUtil.getTargetLocation(entity, 12, 2);

        TornadoEntity tornado = target == null ? new TornadoEntity(ModEntities.TORNADO.get(), serverLevel, .15f, (float) DamageLookup.lookupDamage(2, .775) * (float) multiplier(entity), entity) : new TornadoEntity(ModEntities.TORNADO.get(), serverLevel, .15f, 32.5f * (float) multiplier(entity), entity, target);
        tornado.setPos(pos);
        serverLevel.addFreshEntity(tornado);

        for(int i = 0; i < 30; i++) {
            TornadoEntity additionalTornado = target == null || random.nextInt(4) != 0 ? new TornadoEntity(ModEntities.TORNADO.get(), serverLevel, .15f, 17f, entity) : new TornadoEntity(ModEntities.TORNADO.get(), serverLevel, .15f, 17f, entity, target);
            Vec3 randomOffset = new Vec3((serverLevel.random.nextDouble() - 0.5) * 120, 3, (serverLevel.random.nextDouble() - 0.5) * 120);
            additionalTornado.setPos(pos.add(randomOffset));
            serverLevel.addFreshEntity(additionalTornado);
        }
    }
}
