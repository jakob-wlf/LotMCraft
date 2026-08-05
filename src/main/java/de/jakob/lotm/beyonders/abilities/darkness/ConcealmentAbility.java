package de.jakob.lotm.beyonders.abilities.darkness;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.sound.ModSounds;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.TemporaryChunkLoader;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class ConcealmentAbility extends SelectableAbility {
    public ConcealmentAbility(String id) {
        super(id, 5);
        this.canBeCopied = false;
        canBeReplicated = false;
        canBeUsedInArtifact = false;
        autoClear = false;
        cannotBeStolen = true;
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("darkness", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 6000;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.concealment.surroundings",
                "ability.lotmcraft.concealment.enter_concealed_area",
                "ability.lotmcraft.concealment.conceal_thoughts"

        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {

        if(!(entity instanceof Player)) abilityIndex = 0;
        switch(abilityIndex) {
            case 0 -> concealSurroundings(level, entity);
            case 1 -> enterConcealedArea(level, entity);
            case 2 -> concealThoughts(level,entity);
        }
    }

    private void enterConcealedArea(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // Only works for server players
        if(!(entity instanceof ServerPlayer serverPlayer)) {
            return;
        }

        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "concealment_world"));
        ServerLevel concealedLevel = serverLevel.getServer().getLevel(dimension);
        if (concealedLevel == null) {
            return;
        }

        // Check if player is currently in the concealed world
        boolean isInConcealedWorld = serverLevel.dimension().equals(dimension);

        ServerLevel targetLevel;
        if (isInConcealedWorld) {
            // Teleport back to overworld
            targetLevel = serverLevel.getServer().getLevel(Level.OVERWORLD);
        } else {
            // Teleport to concealed world
            targetLevel = concealedLevel;
        }

        if (targetLevel == null) {
            return;
        }

        BlockPos currentPos = entity.blockPosition();
        double x = entity.getX();
        double z = entity.getZ();
        int startY = currentPos.getY();

        BlockPos safePos = findSafePosition(targetLevel, x, startY, z, isInConcealedWorld);

        serverPlayer.teleportTo(targetLevel,
                safePos.getX() + 0.5,
                safePos.getY(),
                safePos.getZ() + 0.5,
                serverPlayer.getYRot(),
                serverPlayer.getXRot()
        );

        this.clearArtifactScaling(entity);
    }

    private BlockPos findSafePosition(ServerLevel level, double x, int startY, double z, boolean searchUpFirst) {
        int blockX = (int) Math.floor(x);
        int blockZ = (int) Math.floor(z);

        int searchY = Math.max(level.getMinBuildHeight(), Math.min(level.getMaxBuildHeight() - 3, startY));

        if (searchUpFirst) {
            for (int y = searchY; y < level.getMaxBuildHeight() - 2; y++) {
                BlockPos checkPos = new BlockPos(blockX, y, blockZ);
                BlockPos belowPos = checkPos.below();
                BlockPos abovePos = checkPos.above();

                if (isSafePosition(level, checkPos, belowPos, abovePos)) {
                    return checkPos;
                }
            }

            for (int y = searchY - 1; y >= level.getMinBuildHeight() + 1; y--) {
                BlockPos checkPos = new BlockPos(blockX, y, blockZ);
                BlockPos belowPos = checkPos.below();
                BlockPos abovePos = checkPos.above();

                if (isSafePosition(level, checkPos, belowPos, abovePos)) {
                    return checkPos;
                }
            }
        } else {
            for (int y = searchY; y >= level.getMinBuildHeight() + 1; y--) {
                BlockPos checkPos = new BlockPos(blockX, y, blockZ);
                BlockPos belowPos = checkPos.below();
                BlockPos abovePos = checkPos.above();

                if (isSafePosition(level, checkPos, belowPos, abovePos)) {
                    return checkPos;
                }
            }

            for (int y = searchY + 1; y < level.getMaxBuildHeight() - 2; y++) {
                BlockPos checkPos = new BlockPos(blockX, y, blockZ);
                BlockPos belowPos = checkPos.below();
                BlockPos abovePos = checkPos.above();

                if (isSafePosition(level, checkPos, belowPos, abovePos)) {
                    return checkPos;
                }
            }
        }

        return findHighestSafePosition(level, blockX, blockZ);
    }

    private boolean isSafePosition(ServerLevel level, BlockPos pos, BlockPos below, BlockPos above) {
        BlockState belowState = level.getBlockState(below);
        BlockState currentState = level.getBlockState(pos);
        BlockState aboveState = level.getBlockState(above);

        // Need solid ground below
        boolean hasSolidGround = !belowState.isAir() &&
                belowState.isSolidRender(level, below) &&
                belowState.isCollisionShapeFullBlock(level, below);

        // Need air at position and above (2 blocks of air for player)
        boolean hasSpace = currentState.isAir() && aboveState.isAir();

        return hasSolidGround && hasSpace;
    }

    private BlockPos findHighestSafePosition(ServerLevel level, int x, int z) {
        // Search from top down for the first solid block
        for (int y = level.getMaxBuildHeight() - 3; y >= level.getMinBuildHeight(); y--) {
            BlockPos checkPos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(checkPos);

            if (!state.isAir() && state.isSolidRender(level, checkPos)) {
                // Found solid ground, check if there's space above
                BlockPos teleportPos = checkPos.above();
                BlockPos twoAbove = teleportPos.above();

                if (level.getBlockState(teleportPos).isAir() &&
                        level.getBlockState(twoAbove).isAir()) {
                    return teleportPos;
                }
            }
        }

        return new BlockPos(x, 65, z);
    }

    private void concealSurroundings(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        boolean isInConcealmentWorld = serverLevel.dimension().equals(ModDimensions.CONCEALMENT_WORLD_DIMENSION_KEY);

        ServerLevel destinationLevel;

        if (isInConcealmentWorld) {
            destinationLevel = serverLevel.getServer().getLevel(Level.OVERWORLD);
        } else {
            destinationLevel = serverLevel.getServer().getLevel(ModDimensions.CONCEALMENT_WORLD_DIMENSION_KEY);
        }

        if(destinationLevel == null) {
            return;
        }

        EffectManager.playEffect(EffectManager.Effect.CONCEALMENT, entity.getX(), entity.getY(), entity.getZ(), serverLevel, entity);

        AtomicDouble radius = new AtomicDouble(2 * (int) (Math.max(multiplier(entity)/2,1)));
        Vec3 finalTargetLoc = entity.position();

        final HashSet<BlockPos> processedBlocks = new HashSet<>();

        ServerScheduler.scheduleForDuration(0, 2, 20 * 5* (int) (Math.max(multiplier(entity)/2,1)), () -> {
            if(BeyonderData.isGriefingEnabled(entity)) {
                AbilityUtil.getBlocksInSphereRadius(serverLevel, finalTargetLoc, radius.get(), true, false, false).forEach(blockPos -> {
                    if(blockPos.getY() < entity.position().y) return;

                    if(processedBlocks.contains(blockPos)) return;

                    BlockState blockState = serverLevel.getBlockState(blockPos);

                    if(blockState.getDestroySpeed(serverLevel, blockPos) < 0) {
                        processedBlocks.add(blockPos);
                        return;
                    }

                    if(blockState.isAir()) {
                        processedBlocks.add(blockPos);
                        return;
                    }

                    destinationLevel.setBlockAndUpdate(blockPos, blockState);

                    serverLevel.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());

                    processedBlocks.add(blockPos);
                });
            }

            AbilityUtil.getNearbyEntities(entity, serverLevel, finalTargetLoc, radius.get()).forEach(targetEntity -> {
                int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

                if(AbilityUtil.isTargetSignificantlyStronger(entitySeq, BeyonderData.getSequence(targetEntity))) return;

                if(targetEntity.getPersistentData().contains("recently_concealed") && targetEntity.getPersistentData().getBoolean("recently_concealed")) return;

                Vec3 originalPos = targetEntity.position();

                BlockPos safePos = findSafePosition(destinationLevel, targetEntity.getX(), targetEntity.blockPosition().getY(), targetEntity.getZ(), false);

                TemporaryChunkLoader.forceChunksTemporarily(destinationLevel, safePos.getX(), safePos.getZ(), 10, 20 * 10);

                targetEntity.teleportTo(destinationLevel,
                        safePos.getX() + 0.5,
                        safePos.getY(),
                        safePos.getZ() + 0.5,
                        Set.of(),
                        targetEntity.getYRot(),
                        targetEntity.getXRot()
                );

                targetEntity.getPersistentData().putBoolean("recently_concealed", true);

                LivingEntity teleportedEntity = (LivingEntity) destinationLevel.getEntity(targetEntity.getUUID());

                if(teleportedEntity == null) return;

                if(AbilityUtil.isTargetSignificantlyWeaker(entitySeq, BeyonderData.getSequence(teleportedEntity))) {
                    teleportedEntity.setHealth(1);
                    if(!(targetEntity instanceof Player))
                        return;
                }

                int returnTime = AbilityUtil.isTargetSignificantlyWeaker(entitySeq, BeyonderData.getSequence(teleportedEntity)) ? 20 * 60 * 2 :
                        BeyonderData.getSequence(teleportedEntity) < entitySeq ? 20 * 5 : 20 * 25;

                ServerScheduler.scheduleDelayed(returnTime, () -> {
                    teleportedEntity.teleportTo(serverLevel,
                            originalPos.x() + 0.5,
                            originalPos.y(),
                            originalPos.z() + 0.5,
                            Set.of(),
                            teleportedEntity.getYRot(),
                            teleportedEntity.getXRot()
                    );
                });

                ServerScheduler.scheduleDelayed(returnTime + 20 * 8, () -> {
                    if (teleportedEntity.getPersistentData().contains("recently_concealed"))
                        teleportedEntity.getPersistentData().putBoolean("recently_concealed", false);
                });
            });

            radius.addAndGet(0.5);
        }, () -> this.clearArtifactScaling(entity), serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(finalTargetLoc, serverLevel)));
    }

    private static final HashSet<UUID> thoughtconcealedEntities = new HashSet<>();
    private void concealThoughts(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if(level.isClientSide)
            return;

        float multiplier = multiplier(entity);
        level.playSound(null, BlockPos.containing(entity.position()), ModSounds.MIDNIGHT_POEM.get(), SoundSource.BLOCKS, 1, 1);
        LivingEntity targetEntity = AbilityUtil.getTargetEntity(entity, 16*(int) Math.max(multiplier/2,1), 2);
        if(targetEntity == null)
            return;

        if(thoughtconcealedEntities.contains(targetEntity.getUUID())) {
            if(entity instanceof ServerPlayer player) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.literal("Thoughts of the target are already concealed.").withColor(0xFFff124d));
                player.connection.send(packet);
            }
            return;
        }
        thoughtconcealedEntities.add(targetEntity.getUUID());
        float multiplier_target = multiplier(targetEntity);
        int duration = 0;

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        int targetEntitySeq = BeyonderData.getSequence(targetEntity);
        if(entitySeq < targetEntitySeq) {
            duration = 20 * 25*(int) Math.max(multiplier/2,1);
        }else if (entitySeq > targetEntitySeq){
            if (!BeyonderData.getPathway(targetEntity).equals("darkness")){
                duration = 80*(int) Math.max(multiplier/2,1);
            };
        }else{
            duration = 20 * 10*(int) Math.max(multiplier/2,1)/  (int) multiplier_target;
        };

        if(!BeyonderData.isBeyonder(targetEntity) || targetEntitySeq > entitySeq-1 ) {
            if(targetEntity instanceof Mob) {
                ((Mob) targetEntity).setNoAi(true);
                ServerScheduler.scheduleDelayed(duration, () -> ((Mob) targetEntity).setNoAi(false));
            }
            if(BeyonderData.isBeyonder(targetEntity)) {
                DisabledAbilitiesComponent component = targetEntity.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
                component.disableAbilityUsageForTime("concealed_thoughts", duration, targetEntity);
            }
        }

        Location loc = new Location(targetEntity.position(), targetEntity.level());

        int finalDuration = duration;

        ServerScheduler.scheduleForDuration(0, 5,duration, () -> {
            targetEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 10, false, false, false));
            targetEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20, 10, false, false, false));
            targetEntity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 20, 10, false, false, false));
            targetEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 4, false, false, false));
            targetEntity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 5, false, false, false));
            targetEntity.setOnGround(true);
            var pos = targetEntity.position();
            targetEntity.setDeltaMovement(new Vec3(0, 0, 0));
            targetEntity.hurtMarked = true;

            targetEntity.teleportTo(pos.x, pos.y, pos.z);

            loc.setLevel(targetEntity.level());
            loc.setPosition(targetEntity.position());
        }, null, (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));


        ServerScheduler.scheduleDelayed(duration, () -> thoughtconcealedEntities.remove(targetEntity.getUUID()));
    }

}