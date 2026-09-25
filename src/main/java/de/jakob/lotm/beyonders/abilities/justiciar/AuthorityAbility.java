package de.jakob.lotm.beyonders.abilities.justiciar;

import de.jakob.lotm.beyonders.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3f;

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

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(2, 3, 3, 4, 4, 5, 6));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(1500f, 800f, 600f, 600f, 550f, 310f, 240f));
    }

    private static final DustParticleOptions GOLD_DUST = new DustParticleOptions(new Vector3f(1.0f, 0.78f, 0.0f), 1.4f);

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("justiciar", 6));
    }

    @Override
    protected float getSpiritualityCost() {
        return 30;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.authority.slow",
                "ability.lotmcraft.authority.armor_remove"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        switch (abilityIndex) {
            case 0 -> slow(level, entity);
            case 1 -> armorRemove(level, entity);
        }
    }

    private void slow(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;

        var target = AbilityUtil.getTargetEntity(entity, baseDistance, 1.0f, true);

        if(target == null){
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.mental_plague.no_target")
                    .withColor(0xf5ca7f));
            return;
        }

        Vec3 center = entity.position();

        spawnAuthorityPulse(serverLevel, center, 10);
        serverLevel.playSound(null, entity.blockPosition(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.PLAYERS, 0.9f, 0.6f);

        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 2, 1));
        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 2, 0));
        applyStunIfEnhanced(entity, target);

        Location targetLoc = new Location(target.position().add(0, 0.1, 0), serverLevel);
        ParticleUtil.createParticleSpirals(
                GOLD_DUST, targetLoc,
                0.3, 0.9, 2.2, 1.0, 1.2,
                35, 2, 3
        );
        ParticleUtil.createParticleSpirals(
                ParticleTypes.END_ROD, targetLoc,
                0.15, 0.9, 2.2, 1.0, 1.4,
                20, 2, 3
        );

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, center, entity, this, interactionFlags, 30, 20 * 2));
    }

    private void armorRemove(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;

        var target = AbilityUtil.getTargetEntity(entity, baseDistance, 1.0f, true);

        if(target == null){
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.mental_plague.no_target")
                    .withColor(0xf5ca7f));
            return;
        }

        Vec3 center = entity.position();

        spawnAuthorityPulse(serverLevel, center, 10);
        serverLevel.playSound(null, entity.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0f, 0.5f);
        serverLevel.playSound(null, entity.blockPosition(), SoundEvents.ARMOR_EQUIP_IRON.value(), SoundSource.PLAYERS, 1.5f, 0.4f);

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

            Vec3 targetPos = target.position().add(0, 1, 0);
            ServerScheduler.scheduleForDuration(0, 2, 15, () -> {
                ParticleUtil.spawnCircleParticles(serverLevel, GOLD_DUST, targetPos, 0.7, 16);
                ParticleUtil.spawnCircleParticles(serverLevel, ParticleTypes.END_ROD, targetPos, 0.4, 8);
                ParticleUtil.spawnSphereParticles(serverLevel, GOLD_DUST, targetPos, 0.3, 5);
            }, serverLevel);
        }

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, center, entity, this, interactionFlags, 30, 20 * 2));
    }


    private void applyStunIfEnhanced(LivingEntity caster, LivingEntity target) {
        if (BeyonderData.getSequence(caster) <= 4) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 2, 127, false, false));
            target.setDeltaMovement(Vec3.ZERO);
        }
    }

    private void spawnAuthorityPulse(ServerLevel level, Vec3 center, int radius) {
        Location loc = new Location(center, level);

        ParticleUtil.createParticleSpirals(
                ParticleTypes.END_ROD, loc,
                0.5, radius, 3.0, 1.8, 1.5,
                30, 3, 4
        );

        ServerScheduler.scheduleForDuration(0, 2, 25, () -> {
            ParticleUtil.spawnCircleParticles(level, ParticleTypes.END_ROD,   center, radius,        64);
            ParticleUtil.spawnCircleParticles(level, GOLD_DUST, center, radius * 0.65, 40);
            ParticleUtil.spawnSphereParticles(level, GOLD_DUST, center, radius * 0.25, 12);
            ParticleUtil.spawnSphereParticles(level, ParticleTypes.END_ROD,   center, radius * 0.1,   6);
        }, level);
    }
}