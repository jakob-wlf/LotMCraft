package de.jakob.lotm.abilities.fool.miracle_creation;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.GiantLightningEntity;
import de.jakob.lotm.entity.custom.MeteorEntity;
import de.jakob.lotm.entity.custom.TornadoEntity;
import de.jakob.lotm.entity.custom.VolcanoEntity;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.DarknessEffectPacket;
import de.jakob.lotm.network.packets.toClient.HotGroundEffectPacket;
import de.jakob.lotm.network.packets.toClient.OpenCoordinateScreenForTeleportationPacket;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class MiracleHandler {


    public static void performMiracle(String miracleId, ServerLevel level, LivingEntity caster) {
        switch (miracleId) {
            case "summon_village" -> summonStructure(level, caster, "village_plains", "minecraft");
            case "summon_end_city" -> summonStructure(level, caster, "end_city", "minecraft");
            case "summon_pillager_outpost" -> summonStructure(level, caster, "pillager_outpost", "minecraft");
            case "summon_desert_temple" -> summonStructure(level, caster, "desert_pyramid", "minecraft");
            case "summon_evernight_church" -> summonStructure(level, caster, "evernight_church", LOTMCraft.MOD_ID);
            case "summon_meteor" -> summonMeteor(level, caster);
            case "summon_tornados" -> summonTornados(level, caster);
            case "summon_volcano" -> summonVolcano(level, caster);
            case "summon_lightning" -> summonLightning(level, caster);
            case "reverse_gravity" -> reverseGravity(level, caster);
            case "darkness" -> summonDarkness(level, caster);
            case "make_ground_hot" -> makeGroundHot(level, caster);
            case "slow_time" -> slowTime(level, caster);
        }
    }

    private static void summonLightning(ServerLevel level, LivingEntity caster) {
        Vec3 targetLoc = AbilityUtil.getTargetLocation(caster, 70, 2, true);
        for(int i = 0; i < 35; i++) {
            BlockState state = level.getBlockState(BlockPos.containing(targetLoc.subtract(0, 1, 0)));
            if(state.getCollisionShape(level, BlockPos.containing(targetLoc)).isEmpty())
                targetLoc = targetLoc.subtract(0, 1, 0);
        }

        GiantLightningEntity lightning = new GiantLightningEntity(level, caster, targetLoc, 50, 6, DamageLookup.lookupDamage(2, .85) * BeyonderData.getMultiplier(caster), BeyonderData.isGriefingEnabled(caster), 13, 200, 0x6522a8);
        level.addFreshEntity(lightning);
    }

    private static void slowTime(ServerLevel level, LivingEntity caster) {
        Vec3 center = caster.position();

        EffectManager.playEffect(EffectManager.Effect.MIRACLE, center.x, center.y, center.z, level);

        ServerScheduler.scheduleForDuration(0, 1, 20 * 20, () -> {
            AbilityUtil.getAllNearbyEntities(caster, level, center, 70).forEach(e -> {
                e.setDeltaMovement(0, 0, 0);
                e.hurtMarked = true;
            });
        }, level);
    }

    private static void makeGroundHot(ServerLevel level, LivingEntity caster) {
        Vec3 center = caster.position();

        // Affect entities
        ServerScheduler.scheduleForDuration(0, 4, 20 * 15, () -> {
            AbilityUtil.getNearbyEntities(caster, level, center, 70).forEach(e -> {
                if(AbilityUtil.distanceToGround(level, e) <= 1.4) {
                    e.hurt(level.damageSources().onFire(), (float) (DamageLookup.lookupDps(2, .7, 4, 25) * BeyonderData.getMultiplier(caster)));
                    e.setRemainingFireTicks(40);
                }
            });
        });

        List<BlockPos> affectedBlocks = AbilityUtil.getBlocksInEllipsoid((ServerLevel) level, center, 70, 15, true, false, true)
                .stream().filter(blockPos -> !level.getBlockState(blockPos).isAir()).toList();

        // Sort blocks by distance from center for spreading effect
        List<BlockPos> sortedBlocks = affectedBlocks.stream()
                .sorted(Comparator.comparingDouble(pos -> pos.distSqr(BlockPos.containing(center))))
                .toList();

        // Group blocks into waves based on distance
        int currentIndex = 0;
        int waveNumber = 0;

        while (currentIndex < sortedBlocks.size()) {
            // Calculate wave size - increases as we go outward
            int waveSize = Math.min(1 + (waveNumber * 2), sortedBlocks.size() - currentIndex);

            // Get blocks for this wave
            List<BlockPos> waveBlocks = new ArrayList<>(sortedBlocks.subList(currentIndex, currentIndex + waveSize));

            // Schedule this wave to turn black
            int delay = waveNumber; // 1 tick per wave
            final int currentWave = waveNumber;

            ServerScheduler.scheduleDelayed(delay, () -> {
                // Send packet to all players in the level
                HotGroundEffectPacket packet = new HotGroundEffectPacket(waveBlocks, false, currentWave);
                PacketHandler.sendToAllPlayersInSameLevel(packet, (ServerLevel) level);
            }, (ServerLevel) level);

            currentIndex += waveSize;
            waveNumber++;
        }

        // Calculate total spread time based on number of waves
        int totalSpreadTime = waveNumber;

        int restorationTime = (20 * 10) + totalSpreadTime;

        ServerScheduler.scheduleDelayed(restorationTime, () -> {
            // Send restoration packet to clients
            HotGroundEffectPacket packet = new HotGroundEffectPacket(sortedBlocks, true, 0);
            PacketHandler.sendToAllPlayersInSameLevel(packet, (ServerLevel) level);

            // Force block updates server-side to ensure sync
            sortedBlocks.forEach(b -> {
                level.sendBlockUpdated(b, level.getBlockState(b), level.getBlockState(b), Block.UPDATE_ALL);
            });
        }, (ServerLevel) level);
    }

    private static void summonDarkness(ServerLevel level, LivingEntity caster) {
        Vec3 center = caster.position();

        // Affect entities
        ServerScheduler.scheduleForDuration(0, 4, 20 * 15, () -> {
            AbilityUtil.addPotionEffectToNearbyEntities((ServerLevel) level, caster, 70,
                    center, new MobEffectInstance(MobEffects.BLINDNESS, 20 * 10, 5, false, false, false));

            AbilityUtil.getNearbyEntities(caster, (ServerLevel) level, center, 70).forEach(e -> {
                SanityComponent sanityComponent = e.getData(ModAttachments.SANITY_COMPONENT);
                sanityComponent.increaseSanityAndSync(-.0025f, e);
            });
        });

        List<BlockPos> affectedBlocks = AbilityUtil.getBlocksInEllipsoid((ServerLevel) level, center, 70, 15, true, false, true)
                .stream().filter(blockPos -> !level.getBlockState(blockPos).isAir()).toList();

        // Sort blocks by distance from center for spreading effect
        List<BlockPos> sortedBlocks = affectedBlocks.stream()
                .sorted(Comparator.comparingDouble(pos -> pos.distSqr(BlockPos.containing(center))))
                .toList();

        // Group blocks into waves based on distance
        int currentIndex = 0;
        int waveNumber = 0;

        while (currentIndex < sortedBlocks.size()) {
            // Calculate wave size - increases as we go outward
            int waveSize = Math.min(1 + (waveNumber * 2), sortedBlocks.size() - currentIndex);

            // Get blocks for this wave
            List<BlockPos> waveBlocks = new ArrayList<>(sortedBlocks.subList(currentIndex, currentIndex + waveSize));

            // Schedule this wave to turn black
            int delay = waveNumber; // 1 tick per wave
            final int currentWave = waveNumber;

            ServerScheduler.scheduleDelayed(delay, () -> {
                // Send packet to all players in the level
                DarknessEffectPacket packet = new DarknessEffectPacket(waveBlocks, false, currentWave);
                PacketHandler.sendToAllPlayersInSameLevel(packet, (ServerLevel) level);
            }, (ServerLevel) level);

            currentIndex += waveSize;
            waveNumber++;
        }

        // Calculate total spread time based on number of waves
        int totalSpreadTime = waveNumber;

        int restorationTime = (20 * 10) + totalSpreadTime;

        ServerScheduler.scheduleDelayed(restorationTime, () -> {
            // Send restoration packet to clients
            DarknessEffectPacket packet = new DarknessEffectPacket(sortedBlocks, true, 0);
            PacketHandler.sendToAllPlayersInSameLevel(packet, (ServerLevel) level);

            // Force block updates server-side to ensure sync
            sortedBlocks.forEach(b -> {
                level.sendBlockUpdated(b, level.getBlockState(b), level.getBlockState(b), Block.UPDATE_ALL);
            });
        }, (ServerLevel) level);

    }

    private static void reverseGravity(ServerLevel level, LivingEntity caster) {
        Vec3 centerPos = caster.position();

        EffectManager.playEffect(EffectManager.Effect.MIRACLE, centerPos.x, centerPos.y, centerPos.z, level);

        HashSet<LivingEntity> affectedEntities = new HashSet<>();

        ServerScheduler.scheduleForDuration(0, 2, 20 * 30, () -> {
            AbilityUtil.getAllNearbyEntities(caster, level, centerPos, 60).forEach(e -> {
                if(!(e instanceof LivingEntity living)) {
                    e.setDeltaMovement(0, .2, 0);
                    e.hurtMarked = true;
                    return;
                }

                AttributeInstance attribute =  living.getAttribute(Attributes.GRAVITY);
                if(attribute == null) {
                    e.setDeltaMovement(0, .2, 0);
                    e.hurtMarked = true;
                    return;
                }

                if(!affectedEntities.contains(living) && ! attribute.hasModifier(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "reverse_gravity"))) {
                    affectedEntities.add(living);
                    attribute.addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "reverse_gravity"), -2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                }
            });
        }, () -> {
            for(LivingEntity e : affectedEntities) {
                AttributeInstance attribute =  e.getAttribute(Attributes.GRAVITY);
                if(attribute != null && attribute.hasModifier(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "reverse_gravity"))) {
                    attribute.removeModifier(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "reverse_gravity"));
                }
            }
        }, level);
    }

    private static void summonVolcano(ServerLevel level, LivingEntity caster) {
        Vec3 targetPos = AbilityUtil.getTargetLocation(caster, 60, 2);

        EffectManager.playEffect(EffectManager.Effect.MIRACLE, targetPos.x, targetPos.y, targetPos.z, level);

        VolcanoEntity volcano = new VolcanoEntity(level, targetPos, (float) DamageLookup.lookupDamage(2, .5) * (float) BeyonderData.getMultiplier(caster), caster);
        level.addFreshEntity(volcano);
    }

    private static void summonTornados(ServerLevel level, LivingEntity caster) {
        LivingEntity target = AbilityUtil.getTargetEntity(caster, 12, 3);

        Vec3 pos = AbilityUtil.getTargetLocation(caster, 12, 2);

        TornadoEntity tornado = target == null ? new TornadoEntity(ModEntities.TORNADO.get(), level, .15f, (float) DamageLookup.lookupDamage(2, .75) * (float) BeyonderData.getMultiplier(caster), caster) : new TornadoEntity(ModEntities.TORNADO.get(), level, .15f, 32.5f * (float) BeyonderData.getMultiplier(caster), caster, target);
        tornado.setPos(pos);
        level.addFreshEntity(tornado);

        for(int i = 0; i < 5; i++) {
            TornadoEntity additionalTornado = target == null || (new Random()).nextInt(4) != 0 ? new TornadoEntity(ModEntities.TORNADO.get(), level, .15f, (float) DamageLookup.lookupDamage(2, .75) * (float) BeyonderData.getMultiplier(caster), caster) : new TornadoEntity(ModEntities.TORNADO.get(), level, .15f, (float) DamageLookup.lookupDamage(2, .75) * (float) BeyonderData.getMultiplier(caster), caster, target);
            Vec3 randomOffset = new Vec3((level.random.nextDouble() - 0.5) * 40, 3, (level.random.nextDouble() - 0.5) * 40);
            additionalTornado.setPos(pos.add(randomOffset));
            level.addFreshEntity(additionalTornado);
        }
    }


    private static void summonMeteor(ServerLevel level, LivingEntity caster) {
        Vec3 targetLoc = AbilityUtil.getTargetLocation(caster, 85, 3);


        EffectManager.playEffect(EffectManager.Effect.MIRACLE, targetLoc.x, targetLoc.y, targetLoc.z, level);

        MeteorEntity meteor = new MeteorEntity(level, 3.25f,  (float) DamageLookup.lookupDamage(2, 1) * (float) BeyonderData.getMultiplier(caster), 6, caster, BeyonderData.isGriefingEnabled(caster), 20, 34);
        meteor.setPosition(targetLoc);
        level.addFreshEntity(meteor);
    }

    private static void summonStructure(ServerLevel level, LivingEntity caster, String structureName, String namespace) {
        BlockPos pos = caster.blockPosition();

        // Get the structure from registry
        ResourceKey<Structure> structureKey = ResourceKey.create(
                Registries.STRUCTURE,
                ResourceLocation.fromNamespaceAndPath(namespace, structureName)
        );

        Optional<Holder.Reference<Structure>> structureHolder = level.registryAccess()
                .registryOrThrow(Registries.STRUCTURE)
                .getHolder(structureKey);

        if (structureHolder.isEmpty()) {
            System.err.println("Could not find structure: " + structureName);
            return;
        }

        Structure structure = structureHolder.get().value();
        ChunkGenerator chunkGenerator = level.getChunkSource().getGenerator();

        // Generate the structure
        StructureStart structureStart = structure.generate(
                level.registryAccess(),
                chunkGenerator,
                chunkGenerator.getBiomeSource(),
                level.getChunkSource().randomState(),
                level.getStructureManager(),
                level.getSeed(),
                new ChunkPos(pos),
                0,
                level,
                (biome) -> true
        );

        if (!structureStart.isValid()) {
            System.err.println("Failed to generate valid " + structureName + " structure");
            return;
        }

        EffectManager.playEffect(EffectManager.Effect.MIRACLE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, level);

        // Calculate the chunk range needed
        BoundingBox boundingBox = structureStart.getBoundingBox();
        ChunkPos minChunk = new ChunkPos(
                SectionPos.blockToSectionCoord(boundingBox.minX()),
                SectionPos.blockToSectionCoord(boundingBox.minZ())
        );
        ChunkPos maxChunk = new ChunkPos(
                SectionPos.blockToSectionCoord(boundingBox.maxX()),
                SectionPos.blockToSectionCoord(boundingBox.maxZ())
        );

        // Load all chunks in the structure's area
        ChunkPos.rangeClosed(minChunk, maxChunk).forEach((chunkPos) -> {
            level.getChunk(chunkPos.x, chunkPos.z);
        });

        // Place the structure in all chunks
        ChunkPos.rangeClosed(minChunk, maxChunk).forEach((chunkPos) -> {
            structureStart.placeInChunk(
                    level,
                    level.structureManager(),
                    chunkGenerator,
                    level.getRandom(),
                    new BoundingBox(
                            chunkPos.getMinBlockX(),
                            level.getMinBuildHeight(),
                            chunkPos.getMinBlockZ(),
                            chunkPos.getMaxBlockX(),
                            level.getMaxBuildHeight(),
                            chunkPos.getMaxBlockZ()
                    ),
                    chunkPos
            );
        });

        System.out.println("Successfully generated " + structureName + " at " + pos);

        // Mark chunks for saving
        ChunkPos.rangeClosed(minChunk, maxChunk).forEach((chunkPos) -> {
            level.getChunk(chunkPos.x, chunkPos.z).setUnsaved(true);
        });
    }
}