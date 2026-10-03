package de.jakob.lotm.beyonders.abilities.death;

import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.beyonders.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.shapeShifting.ShapeShiftingUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class ZombieDisguiseAbility extends ToggleAbility {

    private static final ResourceLocation HEALTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("lotmcraft", "zombie_disguise_health");
    private static final ResourceLocation STRENGTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("lotmcraft", "zombie_disguise_strength");

    // Sequence 6 equivalent values from PhysicalEnhancementsDeathAbility:
    // HEALTH 7 -> 7 * 4.0 = 28 extra max HP
    // STRENGTH 2 -> 2 * 3.0 = 6 extra attack damage
    // RESISTANCE 6 -> amplifier 5
    private static final double HEALTH_BONUS = 28.0;
    private static final double STRENGTH_BONUS = 6.0;
    private static final int RESISTANCE_AMPLIFIER = 0;

    public ZombieDisguiseAbility(String id) {
        super(id);
        canBeCopied = false;
        canBeReplicated = false;
        cannotBeStolen = false;
        canBeUsedInArtifact = false;
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("death", 7));
    }

    @Override
    protected float getSpiritualityCost() {
        return 7f;
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (level.isClientSide) {
            entity.playSound(SoundEvents.ZOMBIE_AMBIENT, 1.0f, 0.8f);
            return;
        }

        if (!(entity instanceof ServerPlayer player)) return;

        // Change appearance to zombie
        ShapeShiftingUtil.shapeShift(player, "minecraft:zombie", false);

        // Add max health bonus (sequence 6 equivalent)
        AttributeInstance healthAttr = entity.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.removeModifier(HEALTH_MODIFIER_ID);
            healthAttr.addPermanentModifier(new AttributeModifier(HEALTH_MODIFIER_ID, HEALTH_BONUS, AttributeModifier.Operation.ADD_VALUE));
            // Heal up to new max so the bar isn't immediately depleted
            entity.setHealth(Math.min(entity.getHealth() + (float) HEALTH_BONUS, entity.getMaxHealth()));
        }

        // Add strength bonus
        AttributeInstance strengthAttr = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (strengthAttr != null) {
            strengthAttr.removeModifier(STRENGTH_MODIFIER_ID);
            strengthAttr.addPermanentModifier(new AttributeModifier(STRENGTH_MODIFIER_ID, STRENGTH_BONUS, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        if (InteractionHandler.isInteractionPossibleStrictlyHigher(new Location(entity.position(), (net.minecraft.server.level.ServerLevel) level), "purification", BeyonderData.getSequence(entity), -1)) {
            stop(level, entity);
            return;
        }

        // Continuously refresh resistance so it never falls off while active
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, RESISTANCE_AMPLIFIER, false, false, false));
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if (level.isClientSide) {
            entity.playSound(SoundEvents.ZOMBIE_DEATH, 1.0f, 1.0f);
            return;
        }

        if (!(entity instanceof ServerPlayer player)) return;

        // Restore appearance
        ShapeShiftingUtil.resetShape(player);

        // Remove health bonus — clamp HP to new max so they don't have phantom HP
        AttributeInstance healthAttr = entity.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.removeModifier(HEALTH_MODIFIER_ID);
            if (entity.getHealth() > entity.getMaxHealth()) {
                entity.setHealth(entity.getMaxHealth());
            }
        }

        // Remove strength bonus
        AttributeInstance strengthAttr = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (strengthAttr != null) {
            strengthAttr.removeModifier(STRENGTH_MODIFIER_ID);
        }

        // Remove resistance
        entity.removeEffect(MobEffects.DAMAGE_RESISTANCE);
    }
}
