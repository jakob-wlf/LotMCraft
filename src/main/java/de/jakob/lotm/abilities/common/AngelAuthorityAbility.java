package de.jakob.lotm.abilities.common;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.artifacts.SealedArtifactData;
import de.jakob.lotm.data.ModDataComponents;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.dimension.SpiritWorldHandler;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.potions.BeyonderCharacteristicItemHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
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

public class AngelAuthorityAbility extends SelectableAbility {
    public AngelAuthorityAbility(String id) {
        super(id, 5.0f);
        this.canBeUsedByNPC = false;
    }

    protected float getSpiritualityCost() {
        return 500.0f;
    }

    public Map<String, Integer> getRequirements() {
        Map<String, Integer> reqs = new HashMap();

        for(String pathway : BeyonderData.pathways) {
            reqs.put(pathway, 2);
        }
        return reqs;
    }

    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.angel_authority.spirit_world_passage", "ability.lotmcraft.angel_authority.artifact_shattering"};
    }

    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            switch (abilityIndex) {
                case 0:
                    this.spiritWorldPassage(player);
                    break;

                case 1:
                    this.artifactShattering(player, level, player.getX(), player.getY(), player.getZ());
                    break;
            }
        }
    }

    public void artifactShattering(Player player, Level level, double x, double y, double z){
        SealedArtifactData data = player.getInventory().offhand.getFirst().get(ModDataComponents.SEALED_ARTIFACT_DATA);
        ItemStack handStack = player.getInventory().offhand.getFirst();
        if (data == null) {
            data = player.getItemInHand(InteractionHand.MAIN_HAND).get(ModDataComponents.SEALED_ARTIFACT_DATA);
            handStack = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (data == null) return;
        }

        BeyonderCharacteristicItem selectedCharacteristic = BeyonderCharacteristicItemHandler.
                selectCharacteristicOfPathwayAndSequence(data.pathway(), data.sequence());

        if (selectedCharacteristic == null) return;

        ItemStack characteristicToDrop = new ItemStack(selectedCharacteristic);
        level.addFreshEntity(new ItemEntity(level, x, y, z, characteristicToDrop));

        handStack.shrink(1);

        ParticleUtil.spawnParticles((ServerLevel)player.level(), ModParticles.STAR.get(), player.position(), 200, 2.0, 0.001);
        level.playSound(null, x, y, z, SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    public void spiritWorldPassage(ServerPlayer player) {
        if (player.level().isClientSide) return;
        ServerLevel targetLevel;
        Vec3 targetPos;

        if (!player.level().dimension().equals(ModDimensions.SPIRIT_WORLD_DIMENSION_KEY)) {
            ResourceKey spiritWorld = ResourceKey.create((ResourceKey) Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "spirit_world"));
            targetLevel = player.getServer().getLevel(spiritWorld);
            targetPos = SpiritWorldHandler.getCoordinatesInSpiritWorld(player.position());
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
            targetPos = SpiritWorldHandler.getCoordinatesInOverworld(player.position());
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
}
