package de.jakob.lotm.beyonders.abilities.twilight_giant.handlers;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.error.ParasitationAbility;
import de.jakob.lotm.beyonders.abilities.fool.marionettes.ControllingUtils;
import de.jakob.lotm.beyonders.abilities.twilight_giant.ArsenalOfDawnAbility;
import de.jakob.lotm.beyonders.abilities.twilight_giant.LightOfDawnCoatingAbility;
import de.jakob.lotm.beyonders.abilities.twilight_giant.passives.WeaponMasteryAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public final class DemonHunterOil {

    public enum Type {
        LIGHTNING_STRIKE("lightning_strike", Items.LIGHTNING_ROD, Items.GLOWSTONE_DUST),
        FREEZING("freezing", Items.BLUE_ICE, Items.SNOWBALL),
        PURIFICATION("purification", Items.GOLD_INGOT, Items.SUNFLOWER),
        BURNING("burning", Items.BLAZE_POWDER, Items.MAGMA_CREAM),
        DECAY("decay", Items.ROTTEN_FLESH, Items.FERMENTED_SPIDER_EYE),
        EXORCISM("exorcism", Items.GHAST_TEAR, Items.AMETHYST_SHARD);

        private final String id;
        private final List<Item> ingredients;

        Type(String id, Item first, Item second) {
            this.id = id;
            this.ingredients = List.of(first, second);
        }

        public String id() {
            return id;
        }

        public List<Item> ingredients() {
            return ingredients;
        }

        public static Type byId(String id) {
            return Arrays.stream(values()).filter(type -> type.id.equals(id)).findFirst().orElse(null);
        }
    }

    public static final Item BASE = Items.GLASS_BOTTLE;

    private static final String OIL_TAG = "lotm_oil";
    private static final String COATING_TAG = "lotm_oil_coating";
    private static final String CHARGES_TAG = "lotm_oil_charges";
    private static final String EXPIRES_TAG = "lotm_oil_expires";
    private static final String LIGHTNING_TAG = "lotm_oil_lightning";
    private static final int CHARGES = 6;
    private static final int MIN_DURATION = 20 * 60 * 10;
    private static final int EXTRA_DURATION = 20 * 60 * 5;
    private static final int EXPIRY_CHECK_INTERVAL = 100;
    private static final int FREEZE_TICKS = 100;
    private static final int FREEZE_ROOT_TICKS = 60;
    private static final int BURN_SECONDS = 8;
    private static final int DECAY_TICKS = 20 * 10;
    private static final double FLING_STRENGTH = 2.0D;

    private DemonHunterOil() {
    }

    public static ItemStack create(Type type) {
        ItemStack stack = new ItemStack(Items.DRAGON_BREATH);
        CompoundTag tag = new CompoundTag();
        tag.putString(OIL_TAG, type.id());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.ITEM_NAME, Component.translatable("item.lotmcraft." + type.id() + "_oil"));
        return stack;
    }

    public static Type oilType(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null || !data.contains(OIL_TAG)) return null;
        return Type.byId(data.copyTag().getString(OIL_TAG));
    }

    public static Type coating(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null || !data.contains(COATING_TAG)) return null;
        return Type.byId(data.copyTag().getString(COATING_TAG));
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (event.getHand() != InteractionHand.OFF_HAND) return;
        ItemStack oil = player.getOffhandItem();
        Type type = oilType(oil);
        ItemStack weapon = player.getMainHandItem();
        if (type == null || !WeaponMasteryAbility.isWeapon(weapon)) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (player.level().isClientSide()) return;

        long expires = player.level().getGameTime() + MIN_DURATION + player.getRandom().nextInt(EXTRA_DURATION + 1);
        CustomData.update(DataComponents.CUSTOM_DATA, weapon, tag -> {
            tag.putString(COATING_TAG, type.id());
            tag.putInt(CHARGES_TAG, CHARGES);
            tag.putLong(EXPIRES_TAG, expires);
        });
        weapon.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        if (!player.hasInfiniteMaterials()) oil.shrink(1);
        player.level().playSound(null, player.blockPosition(), SoundEvents.BREWING_STAND_BREW, player.getSoundSource(), 1f, 1.2f);
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!ArsenalOfDawnAbility.isMeleeHit(event) || !(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        if (!(attacker.level() instanceof ServerLevel level)) return;
        ItemStack weapon = attacker.getMainHandItem();
        Type type = coating(weapon);
        if (type == null) return;
        if (isExpired(weapon, level.getGameTime())) {
            clearCoating(weapon);
            return;
        }

        apply(type, event, attacker, event.getEntity(), level);
        int charges = weapon.get(DataComponents.CUSTOM_DATA).copyTag().getInt(CHARGES_TAG) - 1;
        if (charges <= 0) {
            clearCoating(weapon);
        } else {
            CustomData.update(DataComponents.CUSTOM_DATA, weapon, tag -> tag.putInt(CHARGES_TAG, charges));
        }
    }

    @SubscribeEvent
    public static void onStruckByLightning(EntityStruckByLightningEvent event) {
        LightningBolt bolt = event.getLightning();
        if (bolt.getTags().contains(LIGHTNING_TAG) && bolt.getCause() == event.getEntity()) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || player.tickCount % EXPIRY_CHECK_INTERVAL != 0) return;
        long now = player.level().getGameTime();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (coating(stack) != null && isExpired(stack, now)) clearCoating(stack);
        }
    }

    private static void apply(Type type, LivingIncomingDamageEvent event, LivingEntity attacker, LivingEntity target, ServerLevel level) {
        switch (type) {
            case LIGHTNING_STRIKE -> strikeLightning(level, attacker, target);
            case FREEZING -> {
                target.setTicksFrozen(target.getTicksRequiredToFreeze() + FREEZE_TICKS);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, FREEZE_ROOT_TICKS, 9, false, true), attacker);
                target.setDeltaMovement(0, Math.min(0, target.getDeltaMovement().y), 0);
                target.hurtMarked = true;
            }
            case PURIFICATION -> ArsenalOfDawnAbility.purify(event, attacker, level);
            case BURNING -> target.igniteForSeconds(BURN_SECONDS);
            case DECAY -> target.addEffect(new MobEffectInstance(MobEffects.WITHER, DECAY_TICKS, 1), attacker);
            case EXORCISM -> exorcise(level, attacker, target);
        }
    }

    private static void strikeLightning(ServerLevel level, LivingEntity attacker, LivingEntity target) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt == null) return;
        bolt.moveTo(target.position());
        bolt.addTag(LIGHTNING_TAG);
        if (attacker instanceof ServerPlayer player) bolt.setCause(player);
        level.addFreshEntity(bolt);
    }

    private static void exorcise(ServerLevel level, LivingEntity attacker, LivingEntity target) {
        ServerPlayer possessor = null;
        if (target instanceof ServerPlayer player && ControllingUtils.isControlling(player)) {
            possessor = player;
            if (ParasitationAbility.isControlling(player.getUUID())) {
                ParasitationAbility.expel(level, player);
            } else {
                ControllingUtils.cancel(player, 0, true, false);
            }
        } else {
            UUID parasite = target.getData(ModAttachments.PARASITE_COMPONENT).getParasiteUUID();
            if (parasite != null && level.getPlayerByUUID(parasite) instanceof ServerPlayer player) {
                possessor = player;
                ParasitationAbility.expel(level, player);
            }
        }
        if (possessor == null) return;

        level.playSound(null, target.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, target.getSoundSource(), 1f, 0.6f);
        if (BeyonderData.getSequence(possessor) > BeyonderData.getSequence(attacker)) {
            possessor.invulnerableTime = 0;
            possessor.setHealth(0f);
            possessor.die(ModDamageTypes.source(level, ModDamageTypes.PURIFICATION, attacker));
            return;
        }
        Vec3 away = possessor.position().subtract(attacker.position()).multiply(1, 0, 1);
        Vec3 direction = away.lengthSqr() < 1.0E-4 ? attacker.getLookAngle().multiply(1, 0, 1).normalize() : away.normalize();
        possessor.push(direction.x * FLING_STRENGTH, 0.6, direction.z * FLING_STRENGTH);
        possessor.hurtMarked = true;
    }

    private static boolean isExpired(ItemStack stack, long now) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null || now >= data.copyTag().getLong(EXPIRES_TAG);
    }

    private static void clearCoating(ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.remove(COATING_TAG);
            tag.remove(CHARGES_TAG);
            tag.remove(EXPIRES_TAG);
        });
        if (LightOfDawnCoatingAbility.isCoated(stack)) return;
        stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
    }
}
