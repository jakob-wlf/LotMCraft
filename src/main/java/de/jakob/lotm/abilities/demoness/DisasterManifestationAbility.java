package de.jakob.lotm.abilities.demoness;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.attachments.ActiveShaderComponent;
import de.jakob.lotm.attachments.FogComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.MeteorEntity;
import de.jakob.lotm.entity.custom.TornadoEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisasterManifestationAbility extends SelectableAbilityItem {
    public DisasterManifestationAbility(Properties properties) {
        super(properties, 4);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("demoness", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1200;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.disaster_manifestation.meteor", "ability.lotmcraft.disaster_manifestation.ice_age", "ability.lotmcraft.disaster_manifestation.tornados"};
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        switch (abilityIndex) {
            case 0 -> spawnMeteor(serverLevel, entity);
            case 1 -> iceAge(serverLevel, entity);
            case 2 -> createTornados(serverLevel, entity);
        }
    }

    private void iceAge(ServerLevel serverLevel, LivingEntity entity) {
        AtomicDouble radius = new AtomicDouble(0.5);
        Vec3 startPos = entity.position();

        boolean griefing = BeyonderData.isGriefingEnabled(entity);

        ServerScheduler.scheduleForDuration(0, 1, 110, () -> {
            AbilityUtil.getBlocksInSphereRadius(serverLevel, startPos, radius.get(), true, true, false).forEach(b -> {
                BlockState state = serverLevel.getBlockState(b);
                BlockState aboveState = serverLevel.getBlockState(b.above());
                if(!state.is(Blocks.PACKED_ICE) && aboveState.getCollisionShape(serverLevel, b.above()).isEmpty()) {
                    ParticleUtil.spawnParticles(serverLevel, ParticleTypes.SNOWFLAKE, b.getCenter().add(0, 1, 0), 1, .4);
                }
                if(griefing) {
                    serverLevel.setBlockAndUpdate(b, Blocks.PACKED_ICE.defaultBlockState());
                }
            });

            AbilityUtil.damageNearbyEntities(serverLevel, entity, radius.get() - 1.5, radius.get() + 1.5, 50.5f * multiplier(entity), startPos, true, false, false, 0);

            // Particles and shader
            for(Player player : AbilityUtil.getNearbyEntities(null, serverLevel, startPos, radius.get(), true)
                    .stream()
                    .filter(e -> e instanceof Player)
                    .map(e -> (Player)e)
                    .toList()) {

                if(player != entity) {
                    FogComponent fogComponent = player.getData(ModAttachments.FOG_COMPONENT);
                    fogComponent.setActiveAndSync(true, player);
                    fogComponent.setFogIndexAndSync(FogComponent.FOG_TYPE.BLIZZARD, player);
                }

                ActiveShaderComponent component = player.getData(ModAttachments.SHADER_COMPONENT);
                component.setShaderActiveAndSync(true, player);
                component.setShaderIndexAndSync(ActiveShaderComponent.SHADERTYPE.BLIZZARD, player);
            }

            radius.addAndGet(.5);
        });
    }

    private void spawnMeteor(ServerLevel serverLevel, LivingEntity entity) {
        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 85, 3);

        MeteorEntity meteor = new MeteorEntity(serverLevel, 3.25f, 75.5f * (float) multiplier(entity), 6, entity, BeyonderData.isGriefingEnabled(entity), 18, 30);
        meteor.setPosition(targetLoc);
        serverLevel.addFreshEntity(meteor);
    }

    private void createTornados(ServerLevel serverLevel, LivingEntity entity) {
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 12, 3);

        Vec3 pos = AbilityUtil.getTargetLocation(entity, 12, 2);

        TornadoEntity tornado = target == null ? new TornadoEntity(ModEntities.TORNADO.get(), serverLevel, .15f,32.5f * (float) multiplier(entity), entity) : new TornadoEntity(ModEntities.TORNADO.get(), serverLevel, .15f, 32.5f * (float) multiplier(entity), entity, target);
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
