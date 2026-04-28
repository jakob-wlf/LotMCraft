package de.jakob.lotm.abilities.justiciar;

import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;
import java.util.stream.Collectors;

public class AuthorityAbility extends SelectableAbility {

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public AuthorityAbility(String id) {
        super(id, 3f, "authority");
        interactionRadius = 15;
        hasOptimalDistance = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("justiciar", 9));
    }

    @Override
    protected float getSpiritualityCost() {
        return 30;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.authority.strip_defense",
                "ability.lotmcraft.authority.slow",
                "ability.lotmcraft.authority.armor_remove"
        };
    }
    // Comment out to test GitHub.
    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        switch (abilityIndex) {
            case 0 -> stripDefense(level, entity);
            case 1 -> slow(level, entity);
            case 2 -> armorRemove(level, entity);
        }
    }

    // ── Seq 4 Enhancement: Stun ───────────────────────────────────────────────

    private void applyStunIfEnhanced(LivingEntity caster, LivingEntity target) {
        if (BeyonderData.getSequence(caster) <= 4) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40*(int) Math.max(multiplier(caster)/4,1), 127, false, false));
            target.setDeltaMovement(Vec3.ZERO);
        }
    }

    // ── Spell 1: Strip Defense ────────────────────────────────────────────────

    private void stripDefense(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;

        AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), 15*(int) Math.max(multiplier(entity)/4,1)).forEach(target -> {
            List<Holder<MobEffect>> toRemove = target.getActiveEffects().stream()
                    .filter(e -> e.getEffect().value().getCategory() == MobEffectCategory.BENEFICIAL)
                    .map(MobEffectInstance::getEffect)
                    .collect(Collectors.toList());
            toRemove.forEach(target::removeEffect);
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 8*(int) Math.max(multiplier(entity)/4,1), 0));
            applyStunIfEnhanced(entity, target);
        });

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, entity.position(), entity, this, interactionFlags, 15, 20 * 2));
    }

    // ── Spell 2: Slow ─────────────────────────────────────────────────────────

    private void slow(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;

        AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), 15*(int) Math.max(multiplier(entity)/4,1)).forEach(target -> {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 20*(int) Math.max(multiplier(entity)/4,1), 1)); // Slowness II, 20 seconds
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 8*(int) Math.max(multiplier(entity)/4,1), 0));
            applyStunIfEnhanced(entity, target);
        });

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, entity.position(), entity, this, interactionFlags, 15, 20 * 2));
    }

    // ── Spell 3: Armor Remove ─────────────────────────────────────────────────

    private void armorRemove(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;

        AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), 15*(int) Math.max(multiplier(entity)/4,1)).forEach(target -> {
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 8*(int) Math.max(multiplier(entity)/4,1), 0));
            applyStunIfEnhanced(entity, target);
            if (random.nextDouble() < 0.4) {
                List<EquipmentSlot> equippedSlots = new ArrayList<>();
                for (EquipmentSlot slot : ARMOR_SLOTS) {
                    if (!target.getItemBySlot(slot).isEmpty()) {
                        equippedSlots.add(slot);
                    }
                }

                Collections.shuffle(equippedSlots, random);
                int toStrip = Math.min(2, equippedSlots.size());
                for (int i = 0; i < toStrip; i++) {
                    ItemStack stripped = target.getItemBySlot(equippedSlots.get(i)).copy();
                    target.setItemSlot(equippedSlots.get(i), ItemStack.EMPTY);
                    target.spawnAtLocation(stripped);
                }
            }
        });

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, entity.position(), entity, this, interactionFlags, 15, 20 * 2));
    }
}