package de.jakob.lotm.beyonders.abilities.abyss.passives.DevilBloodlines.DevilBloodlineAbilites.Shapeshifter;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.abyss.passives.DevilBloodlines.DevilBloodlineAbility;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility; // ASSUMPTION: adjust to your toggle base class
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.shapeShifting.ShapeShiftingUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class BloodlineSkinshifterAbility extends ToggleAbility {
    public static final String ACTIVE_TAG = "lotm_shapeshifter_active";
    public static final String MASK_FORM_TAG = "lotm_shapeshifter_mask_form";
    public static final String MASK_DAMAGE_TAG = "lotm_shapeshifter_mask_damage";
    public static final String MASK_UPGRADED_TAG = "lotm_shapeshifter_mask_upgraded";
    // Humanoids that can drop a Skin Mask. Add more types here
    private static final Set<EntityType<?>> MASK_VICTIMS = Set.of(
            EntityType.PLAYER,
            EntityType.ZOMBIE,
            EntityType.HUSK,
            EntityType.DROWNED,
            EntityType.PIGLIN,
            EntityType.PILLAGER,
            EntityType.VILLAGER
    );

    /** Total damage taken while masked, as a fraction of max health, that breaks the mask form. */
    private static final float BREAK_DAMAGE_TOTAL = 20.0f;

    public BloodlineSkinshifterAbility(String id) {
        super(id);
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 6));
    }
    @Override
    protected float getSpiritualityCost() {
        return 0;
    }
    @Override
    public boolean meetsBloodlineRequirement(LivingEntity entity) {
        return hasShapeshifterBloodline(entity);
    }
    private static boolean hasShapeshifterBloodline(LivingEntity entity) {return DevilBloodlineAbility.hasBloodline(entity, DevilBloodlineAbility.Bloodline.SHAPESHIFTER);}
    public static boolean isValidShapeshifter(LivingEntity entity) {
        if (!BeyonderData.isBeyonder(entity)) return false;
        if (!"abyss".equals(BeyonderData.getPathway(entity))) return false;
        if (BeyonderData.getSequence(entity) > 6) return false;
        return hasShapeshifterBloodline(entity);
    }
    @Override
    public void start(Level level, LivingEntity entity) {
        if (level.isClientSide()) return;
        entity.getPersistentData().putBoolean(ACTIVE_TAG, true);
        if (entity instanceof ServerPlayer player) {player.displayClientMessage(Component.literal("Skin harvesting: ON"), true);}
    }
    @Override
    public void tick(Level level, LivingEntity entity) {
    }
    @Override
    public void stop(Level level, LivingEntity entity) {
        if (level.isClientSide()) return;
        entity.getPersistentData().remove(ACTIVE_TAG);
        if (entity instanceof ServerPlayer player) {player.displayClientMessage(Component.literal("Skin harvesting: OFF"), true);}
    }

    //Skin mask drops

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel)) return;
        boolean isNpc = victim instanceof BeyonderNPCEntity;
        if (!isNpc && !MASK_VICTIMS.contains(victim.getType())) return;
        if (!(event.getSource().getEntity() instanceof LivingEntity killer)) return;
        if (!killer.getPersistentData().getBoolean(ACTIVE_TAG)) return;
        if (!isValidShapeshifter(killer)) return;
        ItemStack mask = SkinMaskItem.create(ModItems.SKIN_MASK.get(), victim, isUpgradedShapeshifter(killer));
        event.getDrops().add(new ItemEntity(victim.level(), victim.getX(), victim.getY(), victim.getZ(), mask));
    }

    //Mask form breaks after enough total damage
    @SubscribeEvent
    public static void onDamaged(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CompoundTag data = player.getPersistentData();
        if (!data.getBoolean(MASK_FORM_TAG)) return;
        float total = data.getFloat(MASK_DAMAGE_TAG) + event.getNewDamage();
        if (total < BREAK_DAMAGE_TOTAL) {
            data.putFloat(MASK_DAMAGE_TAG, total);
            return;
        }
        breakMask(player);
    }
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % 5 != 0) return;
        if (!player.getPersistentData().getBoolean(MASK_FORM_TAG)) return;
        if (player.getData(ModAttachments.TRANSFORMATION_COMPONENT).isTransformed()) {breakMask(player);}
    }
    private static void breakMask(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        data.remove(MASK_FORM_TAG);
        data.remove(MASK_DAMAGE_TAG);
        data.remove(MASK_UPGRADED_TAG);
        ShapeShiftingUtil.resetShape(player);
        player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);
        player.displayClientMessage(Component.literal("Your skin mask shatters!"), true);
    }
    public static boolean isUpgradedShapeshifter(LivingEntity entity) {
        return isValidShapeshifter(entity) && BeyonderData.getSequence(entity) <= SpiritDisguiseUtil.UPGRADE_SEQUENCE;
    }
}