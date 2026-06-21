package de.jakob.lotm.beyonders.abilities.door;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.entity.custom.ability_entities.door_pathway.MysticalDoorEntity;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.OpenCoordinateScreenPacket;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.TeleportationUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TeleportationAuthorityAbility extends SelectableAbility {
    public TeleportationAuthorityAbility(String id) {
        super(id, 8);
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.teleportation_authority.self", "ability.lotmcraft.teleportation_authority.self_and_nearby", "ability.lotmcraft.teleportation_authority.targets", "ability.lotmcraft.teleportation_authority.banish"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if(level.isClientSide) {
            return;
        }

        if(!(entity instanceof ServerPlayer)) selectedAbility = 3;

        switch (selectedAbility) {
            case 0 -> PacketHandler.sendToPlayer((ServerPlayer) entity, new OpenCoordinateScreenPacket("teleportation_authority_self"));
            case 1 -> PacketHandler.sendToPlayer((ServerPlayer) entity, new OpenCoordinateScreenPacket("teleportation_authority_self_and_nearby"));
            case 2 -> PacketHandler.sendToPlayer((ServerPlayer) entity, new OpenCoordinateScreenPacket("teleportation_authority_targets"));
            case 3 -> banishTargets((ServerLevel) level, entity);
        }
    }

    private void banishTargets(ServerLevel level, LivingEntity entity) {
        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 30, 2, true, true);
        List<LivingEntity> targets = AbilityUtil.getNearbyEntities(entity, level, targetLoc, 14);

        ParticleUtil.spawnParticles(level, ModParticles.STAR.get(), targetLoc, 900, 8, .2, 8, .075);
        EffectManager.playEffect(EffectManager.Effect.BANISHMENT, targetLoc.x, targetLoc.y, targetLoc.z, level);
        level.playSound(null, BlockPos.containing(targetLoc), SoundEvents.ENDERMAN_TELEPORT, entity.getSoundSource(), 1, 1);

        ServerLevel banishLevel = selectRandomLevel(level);
        Vec3 banishLoc = generateRandomLocationInLevel(banishLevel);

        for(int i = 0; i < 40; i++) {
            MysticalDoorEntity door = new MysticalDoorEntity(level, targetLoc.add((level.random.nextDouble() - .5) * 16, (level.random.nextDouble() - .5) * 4, (level.random.nextDouble() - .5) * 16), level.random.nextInt(5) + 1, 1, 34);
            door.setYBodyRot(level.random.nextFloat() * 360);
            level.addFreshEntity(door);
        }

        int casterSequence = BeyonderData.getSequence(entity);
        for (LivingEntity target : targets) {
            int targetSequence = BeyonderData.getSequence(target);
            if(targetSequence > casterSequence)
                target.teleportTo(banishLevel, banishLoc.x, banishLoc.y, banishLoc.z, Set.of(), target.getYRot(), target.getXRot());
            else if(!AbilityUtil.isTargetSignificantlyStronger(entity, target)) {
                ServerScheduler.scheduleDelayed(20 * 5, () -> {
                    if(target.position().distanceTo(targetLoc) < 14) {
                        target.teleportTo(banishLevel, banishLoc.x, banishLoc.y, banishLoc.z, Set.of(), target.getYRot(), target.getXRot());
                    }
                });
            }
        }
    }

    private Vec3 generateRandomLocationInLevel(ServerLevel banishLevel) {
        RandomSource random = banishLevel.random;
        double x = random.nextDouble() * banishLevel.getWorldBorder().getSize() - banishLevel.getWorldBorder().getSize() / 2;
        double z = random.nextDouble() * banishLevel.getWorldBorder().getSize() - banishLevel.getWorldBorder().getSize() / 2;
        double y = banishLevel.getHeight(Heightmap.Types.WORLD_SURFACE, (int)x, (int)z) + 8;
        if(y <= 10) y = 100;

        return banishLevel.getWorldBorder().clampToBounds(new Vec3(x, y, z)).getCenter();
    }

    private ServerLevel selectRandomLevel(ServerLevel level) {
         return switch (level.random.nextInt(3)) {
            case 0 -> level.getServer().getLevel(ResourceKey.create(Registries.DIMENSION,
                        ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "space")));
            case 1 -> level.getServer().getLevel(ResourceKey.create(Registries.DIMENSION,
                        ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "spirit_world")));
            default -> level;
        };
    }

    public static void teleportSelf(ServerPlayer player, Vec3 coordinates) {
        teleportEntities(player.serverLevel(), coordinates, List.of(player), player.getSoundSource());
    }

    public static void teleportSelfAndOthers(ServerPlayer player, Vec3 coordinates) {
        List<LivingEntity> teleportEntities = AbilityUtil.getNearbyEntities(null, player.serverLevel(), player.position(), 20);

        ParticleUtil.spawnParticles(player.serverLevel(), ModParticles.STAR.get(), player.position(), 900, 8, .2, 8, .075);

        teleportEntities(player.serverLevel(), coordinates, teleportEntities, player.getSoundSource());
    }

    public static void teleportTargets(ServerPlayer player, Vec3 coordinates) {
        Vec3 targetLoc = AbilityUtil.getTargetLocation(player, 30, 2, true, true);
        List<LivingEntity> targets = AbilityUtil.getNearbyEntities(player, player.serverLevel(), targetLoc, 9);

        ParticleUtil.spawnParticles(player.serverLevel(), ModParticles.STAR.get(), targetLoc, 900, 8, .2, 8, .075);

        teleportEntities(player.serverLevel(), coordinates, targets, player.getSoundSource());
    }

    public static void teleportEntities(ServerLevel level, Vec3 coordinates, List<LivingEntity> targets, SoundSource soundSource) {
        coordinates = TeleportationUtil.clampToBorder(level, coordinates);

        for(LivingEntity entity : targets) {
            EffectManager.playEffect(EffectManager.Effect.TELEPORTATION, entity.getX(), entity.getY(), entity.getZ(), level);
            level.playSound(null, BlockPos.containing(entity.position()), SoundEvents.ENDERMAN_TELEPORT, entity.getSoundSource(), 1, 1);
            ParticleUtil.spawnSphereParticles(level, ParticleTypes.ENCHANTED_HIT, entity.position().add(0, entity.getEyeHeight() / 2, 0), 3, 400);
            ParticleUtil.spawnSphereParticles(level, ParticleTypes.END_ROD, entity.position().add(0, entity.getEyeHeight() / 2, 0), 2, 200);

            entity.teleportTo(coordinates.x, coordinates.y, coordinates.z);
        }

        level.playSound(null, BlockPos.containing(coordinates), SoundEvents.ENDERMAN_TELEPORT, soundSource, 1, 1);
        EffectManager.playEffect(EffectManager.Effect.TELEPORTATION, coordinates.x, coordinates.y, coordinates.z, level);
        ParticleUtil.spawnSphereParticles(level, ParticleTypes.ENCHANTED_HIT, coordinates.add(0, .9, 0), 3, 400);
        ParticleUtil.spawnSphereParticles(level, ParticleTypes.END_ROD, coordinates.add(0, .9, 0), 2, 200);
    }


    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of("door", 2);
    }

    @Override
    protected float getSpiritualityCost() {
        return 5000;
    }
}
