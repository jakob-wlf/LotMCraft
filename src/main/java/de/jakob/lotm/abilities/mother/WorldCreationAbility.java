package de.jakob.lotm.abilities.mother;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.attachments.WorldCreationData;
import de.jakob.lotm.block.ModBlocks;
import de.jakob.lotm.entity.custom.ReturnFromNatureEntity;
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

public class WorldCreationAbility extends Ability {
    public WorldCreationAbility(String id) {
        super(id, 2);

        canBeCopied = false;
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("mother", 1));
    }

    @Override
    public float getSpiritualityCost() {
        return 1000;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof ServerPlayer player)) {
            return;
        }

        // Get or create pocket dimension location for this player
        WorldCreationData data = WorldCreationData.get(serverLevel.getServer());
        BlockPos pocketCenter = data.getOrCreatePocketLocation(player.getUUID());

        // Store the return location
        Vec3 returnPos = entity.position();
        ResourceKey<Level> returnDimension = level.dimension();

        // Get Space Level
        ResourceKey<Level> spaceDimension = ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "nature"));
        ServerLevel spaceLevel = serverLevel.getServer().getLevel(spaceDimension);
        if (spaceLevel == null) {
            return;
        }

        if(spaceLevel == level) {
            level.getEntitiesOfClass(ReturnFromNatureEntity.class, entity.getBoundingBox().inflate(200)).stream().findFirst().ifPresent(portal -> {
                if(portal instanceof ReturnFromNatureEntity returnPortal) {
                    returnPortal.teleportPlayerBack(player);
                }
            });
            return;
        }

        if (data.isFirstVisit(player.getUUID())) {
            data.markVisited(player.getUUID());
        }

        // Teleport player to center of pocket dimension
        player.teleportTo(spaceLevel,
                pocketCenter.getX() + 0.5,
                100,
                pocketCenter.getZ() + 0.5,
                player.getYRot(),
                player.getXRot());

        ReturnFromNatureEntity portalEntity = new ReturnFromNatureEntity(spaceLevel,
                new Vec3(pocketCenter.getX() + .5, 100, pocketCenter.getZ() + .5),
                returnPos,
                returnDimension);
        spaceLevel.addFreshEntity(portalEntity);
    }
}
