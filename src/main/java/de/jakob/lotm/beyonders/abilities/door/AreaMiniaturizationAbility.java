package de.jakob.lotm.beyonders.abilities.door;

import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.data.ModDataComponents;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AreaMiniaturizationAbility extends SelectableAbility {
    private static final double RADIUS = 20.0;
    private static final double Y_RADIUS = 9.0;
    private static final double MINIATURE_SCALE = 0.25;
    private static final int BREAK_FREE_TICKS = 20;
    private static final String BREAKING = "lotm_miniature_break";

    public AreaMiniaturizationAbility(String id) {
        super(id, 2);

        canBeUsedByNPC = false;
        canBeCopied = false;
        canBeShared = false;

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(18000f, 8500f, 6000f));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 2));
    }

    @Override
    public float getSpiritualityCost() {
        return 1200f;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.area_miniaturization.area",
                "ability.lotmcraft.area_miniaturization.creature"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if (level.isClientSide) return;
        if (selectedAbility == 1) {
            miniaturizeCreature((ServerLevel) level, entity);
            return;
        }
        miniaturizeArea((ServerLevel) level, entity);
    }

    private void miniaturizeArea(ServerLevel serverLevel, LivingEntity entity) {
        if (!BeyonderData.isGriefingEnabled(entity)) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.petrification.griefing_disabled").withColor(0xa0e2fa));
            refund(entity);
            return;
        }

        Vec3 targetLocation = AbilityUtil.getTargetLocation(entity, 50, 0.1f);

        List<BlockPos> blocks = AbilityUtil.getBlocksInEllipsoid(
                serverLevel,
                targetLocation,
                RADIUS,
                Y_RADIUS,
                true,
                true,
                false
        );

        // Store block data
        Map<String, String> blockData = new HashMap<>();
        int blockCount = 0;

        for (BlockPos pos : blocks) {
            BlockState state = serverLevel.getBlockState(pos);

            // Skip air and bedrock
            if (state.isAir() || state.is(Blocks.BEDROCK)) {
                continue;
            }

            // Store block state using registry name and properties
            String posKey = pos.getX() + "," + pos.getY() + "," + pos.getZ();

            // Get the block's registry name
            String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();

            // Get properties as string
            StringBuilder stateStr = new StringBuilder(blockId);
            if (!state.getProperties().isEmpty()) {
                stateStr.append("[");
                boolean first = true;
                for (var property : state.getProperties()) {
                    if (!first) stateStr.append(",");
                    first = false;
                    stateStr.append(property.getName()).append("=").append(state.getValue(property));
                }
                stateStr.append("]");
            }

            blockData.put(posKey, stateStr.toString());

            // Remove the block
            serverLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            blockCount++;

            // Limit to prevent performance issues
            if (blockCount >= 10000) break;
        }

        // Create the excavated area item
        ItemStack excavatedArea = new ItemStack(ModItems.EXCAVATED_AREA_ITEM.get());
        excavatedArea.set(ModDataComponents.EXCAVATED_BLOCKS, blockData);
        excavatedArea.set(ModDataComponents.EXCAVATION_CENTER,
                targetLocation.x + "," + targetLocation.y + "," + targetLocation.z);

        // Give item to player
        if (entity instanceof Player player) {
            if (!player.getInventory().add(excavatedArea)) {
                player.drop(excavatedArea, false);
            }
        }
    }

    private void miniaturizeCreature(ServerLevel level, LivingEntity entity) {
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20, 1.5f, true);
        if (!(entity instanceof Player player) || target == null || target instanceof Player || !AbilityUtil.mayDamage(entity, target) || target.getPersistentData().getBoolean(BREAKING)) {
            refund(entity);
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.area_miniaturization.no_target").withColor(0xa0e2fa));
            return;
        }

        if (tooStrong(entity, target)) {
            double original = scaleOf(target);
            setScale(target, original * MINIATURE_SCALE);
            target.getPersistentData().putBoolean(BREAKING, true);
            ServerScheduler.scheduleDelayed(BREAK_FREE_TICKS, () -> {
                if (!target.isAlive()) return;
                setScale(target, original);
                target.getPersistentData().remove(BREAKING);
            }, level);
            level.playSound(null, target.blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL, entity.getSoundSource(), 1f, 1.6f);
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.area_miniaturization.broke_free", target.getName()).withColor(0xa0e2fa));
            return;
        }

        CompoundTag saved = new CompoundTag();
        target.saveWithoutId(saved);
        CompoundTag stored = new CompoundTag();
        stored.putString("EntityType", EntityType.getKey(target.getType()).toString());
        stored.putString("Name", target.getName().getString());
        stored.put("EntityNBT", saved);
        level.playSound(null, target.blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL, entity.getSoundSource(), 1f, 1.6f);
        target.discard();

        ItemStack miniature = new ItemStack(ModItems.MINIATURE_CREATURE.get());
        miniature.set(DataComponents.CUSTOM_DATA, CustomData.of(stored));
        miniature.set(DataComponents.ITEM_NAME, Component.translatable("item.lotmcraft.miniature_creature.named", stored.getString("Name")));
        if (!player.getInventory().add(miniature)) {
            player.drop(miniature, false);
        }
        AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.area_miniaturization.stored", stored.getString("Name")).withColor(0xa0e2fa));
    }

    private static boolean tooStrong(LivingEntity caster, LivingEntity target) {
        return BeyonderData.isBeyonder(target) && BeyonderData.getSequence(target) < BeyonderData.getSequence(caster);
    }

    private static double scaleOf(LivingEntity entity) {
        AttributeInstance scale = entity.getAttribute(Attributes.SCALE);
        return scale == null ? 1.0 : scale.getBaseValue();
    }

    private static void setScale(LivingEntity entity, double value) {
        AttributeInstance scale = entity.getAttribute(Attributes.SCALE);
        if (scale != null) scale.setBaseValue(value);
    }

    private void refund(LivingEntity entity) {
        BeyonderData.incrementSpirituality(entity, spiritualityCost(BeyonderData.getSequence(entity)));
    }
}
