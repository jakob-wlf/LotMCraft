package de.jakob.lotm.abilities.death;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.common.AngelFlightAbility;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.artifacts.SealedArtifactData;
import de.jakob.lotm.data.ModDataComponents;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.dimension.SpiritWorldHandler;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.potions.BeyonderCharacteristicItemHandler;
import de.jakob.lotm.potions.BeyonderPotion;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class SpiritWorldTraversalAbility extends Ability {

    public SpiritWorldTraversalAbility(String id) {
        super(id, 2.0f);
        this.canBeUsedByNPC = false;
        this.canBeCopied = false;
        this.cannotBeStolen = true;
        this.canBeReplicated = false;
        this.canBeUsedInArtifact = false;
        this.doesNotIncreaseDigestion = true;
    }

    protected float getSpiritualityCost() {
        return 500.0f;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(entity instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;
        ServerLevel targetLevel;
        Vec3 targetPos;

        if (!player.level().dimension().equals(ModDimensions.SPIRIT_WORLD_DIMENSION_KEY)) {
            ResourceKey spiritWorld = ResourceKey.create((ResourceKey) Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "spirit_world"));
            targetLevel = player.getServer().getLevel(spiritWorld);
            targetPos = SpiritWorldHandler.getCoordinatesInSpiritWorld(player.position(), targetLevel);
            BlockPos pos = BlockPos.containing(targetPos);

            while (!targetLevel.getBlockState(pos).isAir()) {
                pos = pos.above();
            }
            BlockPos below = pos.below();
            if (targetLevel.getBlockState(below).isAir()) {
                targetLevel.setBlockAndUpdate(below, Blocks.END_STONE.defaultBlockState());
            }
            player.teleportTo(targetLevel, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYRot(), player.getXRot());

        } else {
            targetLevel = player.server.getLevel(Level.OVERWORLD);
            if(targetLevel == null) return;

            targetPos = SpiritWorldHandler.getCoordinatesInOverworld(player.position(), targetLevel);
            BlockPos pos = BlockPos.containing(targetPos);

            while (!targetLevel.getBlockState(pos).isAir()) {
                pos = pos.above();
            }
            BlockPos below = pos.below();
            if (targetLevel.getBlockState(below).isAir()) {
                targetLevel.setBlockAndUpdate(below, Blocks.STONE.defaultBlockState());
            }
            player.teleportTo(targetLevel, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYRot(), player.getXRot());
        }
        ParticleUtil.spawnParticles((ServerLevel)player.level(), ParticleTypes.END_ROD, player.position(), 200, 2.0, 0.001);
        targetLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("death", 4));
    }

    @Override
    public void onHold(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        ResourceKey<Level> spiritWorld = ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "spirit_world"));
        ServerLevel spiritWorldLevel = serverLevel.getServer().getLevel(spiritWorld);
        if (spiritWorldLevel == null) {
            return;
        }

        if (entity.level().dimension().equals(ModDimensions.SPIRIT_WORLD_DIMENSION_KEY)) {
            Vec3 posInSpiritWorld = entity.position();
            Vec3 overworldCoords = SpiritWorldHandler.getCoordinatesInOverworld(posInSpiritWorld, serverLevel);
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.spirit_world_awareness.overworld_coordinates").append(": ").append(
                    Component.literal(
                            "(" +
                                    (int) overworldCoords.x + ", " +
                                    (int) overworldCoords.y + ", " +
                                    (int) overworldCoords.z +
                                    ")"
                    )
            ).withColor(0x80a37e));
        }
    }
}
