package de.jakob.lotm.beyonders.abilities.twilight_giant;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.twilight_giant.handlers.DemonHunterOil;
import de.jakob.lotm.beyonders.abilities.twilight_giant.passives.WeaponMasteryAbility;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class LightOfDawnCoatingAbility extends Ability {


    private static final String COATING_TAG = "lotm_dawn_coating";
    private static final String ADDED_UNBREAKABLE_TAG = "lotm_dawn_coating_unbreakable";
    private static final int DURATION = 20 * 60 * 2;
    private static final float DAMAGE_MULTIPLIER = 1.5f;
    private static final int EXPIRY_CHECK_INTERVAL = 20;

    public LightOfDawnCoatingAbility(String id) {
        super(id, 8, "purification", "light_source");
        canBeUsedByNPC = false;
        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(2000f, 1400f, 1000f, 700f, 500f));
        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(2, 4, 5, 6, 8));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("twilight_giant", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 500;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        ItemStack weapon = entity.getMainHandItem();
        if (!WeaponMasteryAbility.isWeapon(weapon)) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.light_of_dawn_coating.no_weapon").withColor(0xFFFFF3D6));
            return;
        }

        long expires = level.getGameTime() + DURATION;
        boolean addUnbreakable = !weapon.has(DataComponents.UNBREAKABLE);
        CustomData.update(DataComponents.CUSTOM_DATA, weapon, tag -> {
            tag.putLong(COATING_TAG, expires);
            if (addUnbreakable || tag.getBoolean(ADDED_UNBREAKABLE_TAG)) tag.putBoolean(ADDED_UNBREAKABLE_TAG, true);
        });
        if (addUnbreakable) weapon.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        weapon.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        ParticleUtil.spawnParticles(serverLevel, ParticleTypes.END_ROD, entity.position().add(0, 1.2, 0), 30, 0.4, 0.6, 0.4, 0.04);
        level.playSound(null, entity.blockPosition(), SoundEvents.BEACON_POWER_SELECT, entity.getSoundSource(), 0.8f, 1.6f);
    }

    public static boolean isCoated(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.contains(COATING_TAG);
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!ArsenalOfDawnAbility.isMeleeHit(event) || !(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        if (!(attacker.level() instanceof ServerLevel level)) return;
        ItemStack weapon = attacker.getMainHandItem();
        if (!isCoated(weapon)) return;
        if (isExpired(weapon, level.getGameTime())) {
            clear(weapon);
            return;
        }
        event.setAmount(event.getAmount() * DAMAGE_MULTIPLIER);
        if (!ArsenalOfDawnAbility.is(weapon, ArsenalOfDawnAbility.WEAPONS)) ArsenalOfDawnAbility.purify(event, attacker, level);
        level.sendParticles(ParticleTypes.END_ROD, event.getEntity().getX(), event.getEntity().getY(0.5), event.getEntity().getZ(), 6, 0.3, 0.4, 0.3, 0.03);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || player.tickCount % EXPIRY_CHECK_INTERVAL != 0) return;
        long now = player.level().getGameTime();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (isCoated(stack) && isExpired(stack, now)) clear(stack);
        }
    }

    private static boolean isExpired(ItemStack stack, long now) {
        return now >= stack.get(DataComponents.CUSTOM_DATA).copyTag().getLong(COATING_TAG);
    }

    private static void clear(ItemStack stack) {
        boolean addedUnbreakable = stack.get(DataComponents.CUSTOM_DATA).copyTag().getBoolean(ADDED_UNBREAKABLE_TAG);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.remove(COATING_TAG);
            tag.remove(ADDED_UNBREAKABLE_TAG);
        });
        if (addedUnbreakable) stack.remove(DataComponents.UNBREAKABLE);
        if (DemonHunterOil.coating(stack) == null) stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
    }
}
