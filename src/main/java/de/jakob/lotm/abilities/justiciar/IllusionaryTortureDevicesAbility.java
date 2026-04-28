package de.jakob.lotm.abilities.justiciar;

import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.Registries;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IllusionaryTortureDevicesAbility extends SelectableAbility {

    public IllusionaryTortureDevicesAbility(String id) {
        super(id, 1.5f);

        canBeCopied = true;
        canBeUsedByNPC = true;
        cannotBeStolen = false;
        canBeReplicated = true;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("justiciar", 7));
    }

    @Override
    protected float getSpiritualityCost() {
        return 100;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.interrogator.branding_iron",
                "ability.lotmcraft.interrogator.psychic_lashing",
                "ability.lotmcraft.interrogator.psychic_piercing",
                "ability.lotmcraft.interrogator.whip_of_pain"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int index) {
        switch (index) {
            case 0 -> brandingIron(level, entity);
            case 1 -> psychicLashing(level, entity);
            case 2 -> psychicPiercing(level, entity);
            case 3 -> whipOfPain(level, entity);
        }
    }

    private void applySanity(LivingEntity caster, LivingEntity target, float amount) {
        SanityComponent sanity = target.getData(ModAttachments.SANITY_COMPONENT);
        int casterSeq = BeyonderData.getSequence(caster);
        int targetSeq = BeyonderData.getSequence(target);
        sanity.decreaseSanityWithSequenceDifference(amount, target, casterSeq, targetSeq);
    }

    // 1. Branding Iron
    private void brandingIron(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(entity instanceof Player player)) return;

        ItemStack brandingIron = new ItemStack(Items.IRON_SWORD);
        // Should work?
        brandingIron.set(DataComponents.CUSTOM_NAME, Component.literal("Branding Iron").withColor(0xFFAFA3));
        player.setItemInHand(player.getUsedItemHand(), brandingIron);

        int seq = BeyonderData.getSequence(entity);
        int durationSeconds =
                seq >= 5 ? (8 - seq) : (5 + (5 - seq) * 2); // 7→3, 6→4, 5→5, 4→7, 3→9, 2→11, 1→13, 0→15
        int durationTicks = durationSeconds * 20;

        UUID task = ServerScheduler.scheduleRepeating(
                0, 1, durationTicks,
                () -> {
                    if (!player.getMainHandItem().equals(brandingIron)) return;

                    if (player.getAttackStrengthScale(0) == 1.0f) {
                        LivingEntity target = AbilityUtil.getTargetEntity(player, 4* (int) Math.max(multiplier(entity)/4,1), 0.8f);
                        if (target != null) {
                            double damage = DamageLookup.lookupDamage(7, 0.6) * (int) Math.max(multiplier(entity)/4,1);
                            target.hurt(ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, entity), (float) damage);

                            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0));
                            applySanity(entity, target, 0.12f);

                            brandingIron.shrink(1);
                        }
                    }
                }
        );

        ServerScheduler.scheduleDelayed(durationTicks, () -> {
            if (player.getMainHandItem().equals(brandingIron)) {
                player.getMainHandItem().shrink(1);
            }
            ServerScheduler.cancel(task);
        });
    }

    // 2. Psychic Lashing
    private void psychicLashing(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(entity instanceof Player player)) return;

        ItemStack weapon = player.getMainHandItem();
        if (weapon.isEmpty()) {
            entity.sendSystemMessage(Component.literal(" You need an item in your main hand."));
            return;
        };
        var enchantmentRegistry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        var sharpnessHolder = enchantmentRegistry.getHolderOrThrow(Enchantments.SHARPNESS);
        if (BeyonderData.getSequence(entity) <=4) {
            weapon.enchant(sharpnessHolder, 5);
        }else {weapon.enchant(sharpnessHolder, 3);};

    }

    // 3. Psychic Piercing
    private void psychicPiercing(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 18* (int) Math.max(multiplier(entity)/4,1), 1.3f);
        if (target == null || target == entity) return;

        target.hurt(ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, entity), (float) DamageLookup.lookupDamage(7, 0.8));
        float damage = (float) Math.min(multiplier(entity)/100,0.1);
        target.setHealth(target.getHealth() - (target.getMaxHealth() * damage));

        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 1));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));

        applySanity(entity, target, 0.20f);
        level.playSound(null,
                entity.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.BLOCKS,
                10.0f,
                1.0f);
        ParticleUtil.drawParticleLine(
                serverLevel,
                ParticleTypes.ELECTRIC_SPARK,
                entity.getEyePosition(),
                target.getEyePosition(),
                0.2,
                4
        );
    }

    // 4. Whip of Pain
    private void whipOfPain(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 12* (int) Math.max(multiplier(entity)/4,1), 1.3f);
        if (target == null || target == entity) return;

        double damage = DamageLookup.lookupDamage(7, 0.6) * (int) Math.max(multiplier(entity)/4,1);
        target.hurt(ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, entity), (float) damage);

        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
        applySanity(entity, target, 0.15f);
        level.playSound(null,
                entity.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.BLOCKS,
                10.0f,
                1.0f);
        ParticleUtil.drawParticleLine(
                serverLevel,
                ParticleTypes.CRIT,
                entity.getEyePosition(),
                target.position(),
                0.15,
                3
        );
    }
}