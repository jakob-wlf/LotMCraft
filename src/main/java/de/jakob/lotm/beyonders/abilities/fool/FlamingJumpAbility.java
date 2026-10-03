package de.jakob.lotm.beyonders.abilities.fool;

import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.gui.custom.flaming_jump.FlamingJumpMenuProvider;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.AbilitySelectionPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.attachments.FlamingJumpData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber
public class FlamingJumpAbility extends SelectableAbility {

    public static final Map<ResourceKey<Level>, Set<BlockPos>> FIRE_MAP = new ConcurrentHashMap<>();

    public FlamingJumpAbility(String id) {
        super(id, 1f);

        canBeUsedByNPC = false;
        this.doesNotIncreaseDigestion = true;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("fool", 7));
    }

    @Override
    public float getSpiritualityCost() {
        return 60;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.flaming_jump_ability.close_flaming_jump",
                "ability.lotmcraft.flaming_jump_ability.far_flaming_jump"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if(level.isClientSide)
            return;

        switch(selectedAbility){
            case 0 -> closeFlamingJump(level, entity);
            case 1 -> farFlamingJump(level, entity);
        }
    }

    @Override
    public void nextAbility(LivingEntity entity){
        if(getAbilityNames().length == 0)
            return;

        if(!selectedAbilities.containsKey(entity.getUUID())) {
            selectedAbilities.put(entity.getUUID(), 0);
        }

        int selectedAbility = selectedAbilities.get(entity.getUUID());
        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        selectedAbility++;
        if(selectedAbility >= getAbilityNames().length) {
            selectedAbility = 0;
        }

        if((entitySeq > 4 && selectedAbility >= 1)){
            selectedAbility = 0;
        }

        selectedAbilities.put(entity.getUUID(), selectedAbility);
        PacketHandler.sendToServer(new AbilitySelectionPacket(getId(), selectedAbility));
    }

    @Override
    public void previousAbility(LivingEntity entity){
        if(getAbilityNames().length == 0)
            return;

        if(!selectedAbilities.containsKey(entity.getUUID())) {
            selectedAbilities.put(entity.getUUID(), 0);
        }

        int selectedAbility = selectedAbilities.get(entity.getUUID());
        selectedAbility--;
        if(selectedAbility <= -1) {
            selectedAbility = getAbilityNames().length - 1;
        }

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        if((entitySeq > 4 && selectedAbility >= 1)){
            selectedAbility = 0;
        }

        selectedAbilities.put(entity.getUUID(), selectedAbility);
        PacketHandler.sendToServer(new AbilitySelectionPacket(getId(), selectedAbility));
    }

    public void closeFlamingJump(Level level, LivingEntity entity) {
        BlockPos block = getSelectedFire(level, entity, true);

        if(block == null) {
            if(entity instanceof ServerPlayer player) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.translatable("lotmcraft.flaming_jump_ability.no_fire_found").withColor(0xFFff124d));
                player.connection.send(packet);
            }
            return;
        }

        entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 3, 1, false, false, false));
        ServerScheduler.scheduleForDuration(0, 1, 20 * 3, () -> entity.setRemainingFireTicks(0));
        entity.teleportTo(block.getCenter().x, block.getCenter().y + .75, block.getCenter().z);

        level.playSound(null, block, SoundEvents.BLAZE_SHOOT, SoundSource.BLOCKS, 1, 1);
        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.FLAME, block.getCenter().add(0, .8, 0), 60, .3, .8, .3, .05);

    }

    public static void farFlamingJump(Level level, LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        List<BlockPos> validFires = getValidFireLocations(serverLevel, player, player.blockPosition());

        player.openMenu(
                new FlamingJumpMenuProvider(validFires),
                buf -> {
                    buf.writeVarInt(validFires.size());
                    for (BlockPos pos : validFires) {
                        buf.writeBlockPos(pos);
                    }
                }
        );
    }

    @Override
    public void onHold(Level level, LivingEntity entity) {
        if(!level.isClientSide)
            return;

        BlockPos selectedFire = getSelectedFire(level, entity, false);
        if(selectedFire == null) {
            return;
        }

        ParticleUtil.spawnParticles((ClientLevel) level, ParticleTypes.FLASH, selectedFire.getCenter(), 20, .1, 0);
    }

    private final Block[] fireBlocks = new Block[]{
            Blocks.FIRE,
            Blocks.SOUL_FIRE,
            Blocks.CAMPFIRE,
            Blocks.SOUL_CAMPFIRE,
            Blocks.LAVA,
            Blocks.LAVA_CAULDRON,
            Blocks.TORCH,
            Blocks.SOUL_TORCH,
            Blocks.WALL_TORCH,
            Blocks.SOUL_WALL_TORCH,
            Blocks.SPAWNER
    };

    public BlockPos getSelectedFire(Level level, LivingEntity entity, boolean checkNearestIfNoneSelected) {
        BlockPos block = AbilityUtil.getTargetBlock(entity, 50, false, true);

        if(level.getBlockState(block).is(Blocks.FIRE))
            return block;

        BlockPos nextBestBlock = AbilityUtil.getBlocksInSphereRadius(level, block.getCenter(), 3.2, true, false, false)
                .stream()
                .filter(b -> {
                    Block blockAtPos = level.getBlockState(b).getBlock();
                    for (Block fireBlock : fireBlocks) {
                        if (blockAtPos == fireBlock) {
                            return true;
                        }
                    }
                    return false;
                })
                .min(Comparator.comparing(b -> b.distToCenterSqr(block.getCenter()))).orElse(null);

        if(nextBestBlock != null) {
            return nextBestBlock;
        }

        LivingEntity burningEntity = AbilityUtil.getTargetEntity(entity, 40, 2.5f, true, true);
        if(burningEntity != null && burningEntity.getRemainingFireTicks() > 0) {
            return burningEntity.blockPosition();
        }

        if(!checkNearestIfNoneSelected) {
            return null;
        }

        return AbilityUtil.getBlocksInSphereRadius(level, entity.position(), 20, true, false, false)
                .stream()
                .filter(b -> {
                    Block blockAtPos = level.getBlockState(b).getBlock();
                    for (Block fireBlock : fireBlocks) {
                        if (blockAtPos == fireBlock) {
                            return true;
                        }
                    }
                    return false;
                })
                .min(Comparator.comparing(b -> b.distToCenterSqr(entity.position()))).orElse(null);
    }


    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            BlockState state = event.getState();

            if (state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)) {
                FIRE_MAP.computeIfAbsent(level.dimension(), k -> ConcurrentHashMap.newKeySet()).add(event.getPos().immutable());
                FlamingJumpData.get(level).setDirty();
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            BlockState state = event.getState();

            if (state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)) {
                Set<BlockPos> fires = FIRE_MAP.get(level.dimension());

                if (fires != null && fires.remove(event.getPos())) {
                    FlamingJumpData.get(level).setDirty();
                }
            }
        }
    }

    public static List<BlockPos> getValidFireLocations(ServerLevel level, LivingEntity entity, BlockPos playerPos) {
        FlamingJumpData.get(level);

        Set<BlockPos> fires = FIRE_MAP.get(level.dimension());
        if (fires == null || fires.isEmpty()) {
            return Collections.emptyList();
        }

        double maxDistSqr = Math.pow(getMaxJumpDistance(BeyonderData.getSequence(entity)), 2);

        List<BlockPos> toRemove = new ArrayList<>();
        List<BlockPos> validFires = new ArrayList<>();

        for (BlockPos pos : fires) {
            // chunk loaded and no longer fire - mark for removal
            if (level.hasChunkAt(pos)) {
                BlockState state = level.getBlockState(pos);
                if (!state.is(Blocks.FIRE) && !state.is(Blocks.SOUL_FIRE)) {
                    toRemove.add(pos);
                    continue;
                }
            }

            if (pos.distSqr(playerPos) <= maxDistSqr) {
                validFires.add(pos);
            }
        }

        if (!toRemove.isEmpty()) {
            fires.removeAll(toRemove);
            FlamingJumpData.get(level).setDirty();
        }

        return validFires;
    }

    public static int getMaxJumpDistance(int sequence) {
        return switch (sequence) {
            case 4 -> 500;
            case 3 -> 750;
            case 2 -> 1500;
            case 1 -> 4000;
            case 0 -> 10000;
            default -> 50;
        };
    }

}
