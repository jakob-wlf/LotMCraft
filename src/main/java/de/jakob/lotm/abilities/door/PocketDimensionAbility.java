package de.jakob.lotm.abilities.door;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.attachments.PocketDimensionData;
import de.jakob.lotm.block.ModBlocks;
import de.jakob.lotm.entity.custom.ReturnPortalEntity;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PocketDimensionAbility extends AbilityItem {
    public PocketDimensionAbility(Properties properties) {
        super(properties, 2);

        canBeCopied = false;
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1000;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof ServerPlayer player)) {
            return;
        }

        // Get or create pocket dimension location for this player
        PocketDimensionData data = PocketDimensionData.get(serverLevel.getServer());
        BlockPos pocketCenter = data.getOrCreatePocketLocation(player.getUUID());

        // Store the return location
        Vec3 returnPos = entity.position();
        ResourceKey<Level> returnDimension = level.dimension();

        // Teleport to Space dimension
        ResourceKey<Level> spaceDimension = ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "space"));
        ServerLevel spaceLevel = serverLevel.getServer().getLevel(spaceDimension);
        if (spaceLevel == null) {
            return;
        }

        // Generate the hollow sphere if first time
        if (data.isFirstVisit(player.getUUID())) {
            generateHollowSphere(spaceLevel, pocketCenter, 22);
            data.markVisited(player.getUUID());
        }

        // Teleport player to center of pocket dimension
        player.teleportTo(spaceLevel,
                pocketCenter.getX() + 0.5,
                120,
                pocketCenter.getZ() + 0.5,
                player.getYRot(),
                player.getXRot());

        // Spawn return portal entity at center
        ReturnPortalEntity portalEntity = new ReturnPortalEntity(spaceLevel,
                new Vec3(pocketCenter.getX() + .5, 120, pocketCenter.getZ() + .5),
                returnPos,
                returnDimension);
        spaceLevel.addFreshEntity(portalEntity);
    }

    private void generateHollowSphere(ServerLevel level, BlockPos center, int radius) {
        List<BlockPos> sphereBlocks = AbilityUtil.getBlocksInSphereRadius(level,
                Vec3.atCenterOf(center), radius, true);

        for (BlockPos pos : sphereBlocks) {
            level.setBlockAndUpdate(pos, ModBlocks.SOLID_VOID.get().defaultBlockState());
        }

        List<BlockPos> lightBlocks = AbilityUtil.getBlocksInSphereRadius(level,
                Vec3.atCenterOf(center), radius - 2, true);

        for (BlockPos pos : lightBlocks) {
            level.setBlockAndUpdate(pos, Blocks.LIGHT.defaultBlockState());
        }

        List<BlockPos> airBlocks = AbilityUtil.getBlocksInSphereRadius(level,
                Vec3.atCenterOf(center), radius - 4, true);

        for (BlockPos pos : airBlocks) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }

        // Add grass floor (10 blocks high from bottom of sphere)
        int floorHeight = 10;
        int sphereBottom = center.getY() - radius + 4; // +4 because we have 4 block thick walls

        for (int x = -radius + 4; x <= radius - 4; x++) {
            for (int z = -radius + 4; z <= radius - 4; z++) {
                BlockPos checkPos = center.offset(x, 0, z);
                // Only place floor if position is inside the sphere
                if (checkPos.distSqr(center) <= (radius - 4) * (radius - 4)) {
                    for (int y = sphereBottom; y < sphereBottom + floorHeight; y++) {
                        BlockPos floorPos = new BlockPos(center.getX() + x, y, center.getZ() + z);
                        if(level.getBlockState(floorPos).is(ModBlocks.SOLID_VOID)) {
                            continue;
                        }
                        if (y == sphereBottom + floorHeight - 1) {
                            level.setBlockAndUpdate(floorPos, Blocks.GRASS_BLOCK.defaultBlockState());
                        } else {
                            level.setBlockAndUpdate(floorPos, Blocks.DIRT.defaultBlockState());
                        }
                    }
                }
            }
        }
    }
}
