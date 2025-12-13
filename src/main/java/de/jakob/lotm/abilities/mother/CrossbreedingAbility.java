package de.jakob.lotm.abilities.mother;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CrossbreedingAbility extends AbilityItem {
    private final HashMap<UUID, LivingEntity> targets = new HashMap<>();

    public CrossbreedingAbility(Properties properties) {
        super(properties, 1);

        canBeUsedByNPC = false;
        canBeCopied = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("mother", 6));
    }

    @Override
    protected float getSpiritualityCost() {
        return 220;
    }

    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(112 / 255f, 212 / 255f, 130 / 255f), 4);

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel))
            return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20, 2);
        if(target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.crossbreeding.not_valid_mob").withColor(0xFF88c276));
            return;
        }

        if(!targets.containsKey(entity.getUUID())) {
            ParticleUtil.spawnParticles(serverLevel, dust, target.position().add(0, target.getBbHeight() / 2, 0), 60, 0.5f, 0.5f, 0.5f, 0.1f);
            targets.put(entity.getUUID(), target);
        } else {
            LivingEntity previousTarget = targets.get(entity.getUUID());
            targets.remove(entity.getUUID());

            if(previousTarget == target) {
                AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.crossbreeding.not_same_mob").withColor(0xFF88c276));
                return;
            }

            if(!previousTarget.isAlive()) {
                ParticleUtil.spawnParticles(serverLevel, dust, target.position().add(0, target.getBbHeight() / 2, 0), 60, 0.5f, 0.5f, 0.5f, 0.1f);
                targets.put(entity.getUUID(), target);
                return;
            }

            ParticleUtil.spawnParticles(serverLevel, dust, target.position().add(0, target.getBbHeight() / 2, 0), 60, 0.5f, 0.5f, 0.5f, 0.1f);

            // Spawn new hybrid mob
            spawnHybridMob(serverLevel, previousTarget, target);

            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.crossbreeding.success").withColor(0xFF88c276));
        }
    }

    private void spawnHybridMob(ServerLevel level, LivingEntity attributeSource, LivingEntity typeSource) {
        System.out.println("=== CROSSBREEDING DEBUG START ===");

        // Get the entity type of the second target
        EntityType<?> entityType = typeSource.getType();
        System.out.println("Entity type: " + entityType);

        // Create new entity at the second target's position
        Entity newEntity = entityType.create(level);
        System.out.println("Created entity: " + newEntity);

        if(!(newEntity instanceof LivingEntity newMob)) {
            System.out.println("ERROR: newEntity is not a LivingEntity!");
            return;
        }

        System.out.println("New mob created successfully: " + newMob.getClass().getSimpleName());

        // Set position and rotation
        newMob.moveTo(typeSource.getX(), typeSource.getY(), typeSource.getZ(),
                typeSource.getYRot(), typeSource.getXRot());
        System.out.println("Position set to: " + typeSource.getX() + ", " + typeSource.getY() + ", " + typeSource.getZ());

        // Transfer attributes from first target to new mob
        transferAttributes(attributeSource, newMob);
        System.out.println("Attributes transferred");

        // Spawn the new mob in the world
        level.addFreshEntity(newMob);
        System.out.println("Entity spawned");

        // Remove the second target
        typeSource.discard();
        System.out.println("Type source discarded");

        System.out.println("=== CROSSBREEDING DEBUG END ===");
    }

    private void transferAttributes(LivingEntity source, LivingEntity target) {
        AttributeMap sourceAttributes = source.getAttributes();
        AttributeMap targetAttributes = target.getAttributes();

        // Iterate through all attributes of the source
        for(AttributeInstance sourceAttr : sourceAttributes.getSyncableAttributes()) {
            Holder<Attribute> attribute = sourceAttr.getAttribute();

            // Check if target also has this attribute
            if(targetAttributes.hasAttribute(attribute)) {
                AttributeInstance targetAttr = targetAttributes.getInstance(attribute);

                if(targetAttr != null) {
                    // Copy base value
                    targetAttr.setBaseValue(sourceAttr.getBaseValue());

                    // Copy all modifiers
                    targetAttr.removeModifiers();
                    for(AttributeModifier modifier : sourceAttr.getModifiers()) {
                        targetAttr.addPermanentModifier(modifier);
                    }
                }
            }
        }

        // Fully heal the new mob to its new max health
        target.setHealth(target.getMaxHealth());
    }
}