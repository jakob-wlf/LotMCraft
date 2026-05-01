package de.jakob.lotm.abilities.justiciar;

import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IllusionaryTortureDevicesAbility extends SelectableAbility {

    private static final DustParticleOptions GOLD_DUST        = new DustParticleOptions(new Vector3f(1.0f, 0.75f, 0.0f), 1.2f);
    private static final DustParticleOptions PALE_GOLD_DUST   = new DustParticleOptions(new Vector3f(1.0f, 0.92f, 0.45f), 0.9f);
    private static final DustParticleOptions EMBER_DUST       = new DustParticleOptions(new Vector3f(1.0f, 0.35f, 0.05f), 1.0f);
    private static final DustParticleOptions PSYCHIC_DUST     = new DustParticleOptions(new Vector3f(0.9f, 0.7f, 1.0f), 1.0f);

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
        sanity.decreaseSanityWithSequenceDifference(amount, target, BeyonderData.getSequence(caster), BeyonderData.getSequence(target));
    }

    private int scaledRange(LivingEntity entity, int base) {
        return base * (int) Math.max(multiplier(entity) / 4, 1);
    }

    private void brandingIron(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(entity instanceof Player player)) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        ItemStack brandingIron = new ItemStack(Items.IRON_SWORD);
        brandingIron.set(DataComponents.CUSTOM_NAME, Component.literal("Branding Iron").withColor(0xFFAFA3));
        player.setItemInHand(player.getUsedItemHand(), brandingIron);

        int seq = BeyonderData.getSequence(entity);
        int durationTicks = (seq >= 5 ? (8 - seq) : (5 + (5 - seq) * 2)) * 20;

        serverLevel.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.8f, 0.5f);
        serverLevel.playSound(null, player.blockPosition(), SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.5f, 0.7f);
        AbilityUtil.sendActionBar(entity, Component.literal("§6⚖ §eBranding Iron §fconjured §6⚖"));

        ParticleUtil.spawnSphereParticles(serverLevel, GOLD_DUST, player.position().add(0, 1, 0), 0.8, 20);
        ParticleUtil.spawnSphereParticles(serverLevel, EMBER_DUST, player.position().add(0, 1, 0), 0.6, 14);
        ParticleUtil.spawnCircleParticles(serverLevel, GOLD_DUST, player.position().add(0, 0.05, 0), 0.7, 16);

        Location entityLoc = new Location(entity.position(), serverLevel);

        UUID task = ServerScheduler.scheduleRepeating(
                0, 1, durationTicks,
                () -> {
                    if (!player.getMainHandItem().equals(brandingIron)) return;
                    if (player.getAttackStrengthScale(0) != 1.0f) return;

                    LivingEntity target = AbilityUtil.getTargetEntity(player, scaledRange(entity, 4), 0.8f);
                    if (target == null) return;

                    double damage = DamageLookup.lookupDamage(7, 0.2) * (int )Math.max (multiplier(entity)/4,1);
                    target.hurt(ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, entity), (float) damage);
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0));
                    applySanity(entity, target, 0.12f);

                    serverLevel.playSound(null, target.blockPosition(), SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 1.0f, 0.8f);
                    serverLevel.playSound(null, target.blockPosition(), SoundEvents.BLAZE_HURT, SoundSource.PLAYERS, 0.6f, 1.2f);

                    Vec3 hitPos = target.position().add(0, 1, 0);
                    ParticleUtil.spawnSphereParticles(serverLevel, EMBER_DUST, hitPos, 0.5, 14);
                    ParticleUtil.spawnSphereParticles(serverLevel, GOLD_DUST, hitPos, 0.4, 10);
                    ParticleUtil.spawnParticles(serverLevel, ParticleTypes.FLAME, hitPos, 5, 0.35);
                    ParticleUtil.spawnParticles(serverLevel, ParticleTypes.LAVA, hitPos, 3, 0.25);

                    ParticleUtil.drawParticleLine(serverLevel, GOLD_DUST, player.getEyePosition(), target.getEyePosition(), 0.18, 1);
                    ParticleUtil.drawParticleLine(serverLevel, EMBER_DUST, player.getEyePosition(), target.getEyePosition(), 0.28, 1);

                    brandingIron.shrink(1);
                },
                serverLevel,
                () -> true
        );

        ServerScheduler.scheduleDelayed(durationTicks, () -> {
            if (player.getMainHandItem().equals(brandingIron)) {
                player.getMainHandItem().shrink(1);
            }
            ServerScheduler.cancel(task);
        }, serverLevel);
    }

    private void psychicLashing(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(entity instanceof Player player)) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        ItemStack weapon = player.getMainHandItem();
        if (weapon.isEmpty()) {
            AbilityUtil.sendActionBar(entity, Component.literal("§c✗ §fRequires an item in your main hand"));
            return;
        }

        var enchantmentRegistry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        var sharpnessHolder = enchantmentRegistry.getHolderOrThrow(Enchantments.SHARPNESS);
        int enchLevel = AbilityUtil.getSeqWithArt(entity, this) <= 4 ? 5 : 3;
        weapon.enchant(sharpnessHolder, enchLevel);

        serverLevel.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.9f, 0.7f);
        serverLevel.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5f, 1.8f);

        Vec3 center = player.position().add(0, 1, 0);
        ParticleUtil.spawnSphereParticles(serverLevel, GOLD_DUST, center, 1.0, 28);
        ParticleUtil.spawnSphereParticles(serverLevel, PALE_GOLD_DUST, center, 0.7, 18);
        ParticleUtil.spawnCircleParticles(serverLevel, GOLD_DUST, player.position().add(0, 0.05, 0), 0.9, 20);
        ParticleUtil.spawnCircleParticles(serverLevel, PALE_GOLD_DUST, player.position().add(0, 0.05, 0), 0.6, 14);
        serverLevel.sendParticles(ParticleTypes.ENCHANT, center.x, center.y, center.z, 40, 0.5, 0.5, 0.5, 0.15);

        AbilityUtil.sendActionBar(entity, Component.literal("§6⚖ §eWeapon §fimbued with §ePsychic Lashing §6⚖"));
    }

    private void psychicPiercing(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, scaledRange(entity, 18), 1.3f);
        if (target == null || target == entity) return;

        target.hurt(ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, entity),
                (float) DamageLookup.lookupDamage(7, 0.8) * multiplier(entity));
        float percentDamage = (float) Math.min(multiplier(entity) / 100, 0.1);
        target.setHealth(target.getHealth() - (target.getMaxHealth() * percentDamage));

        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 1));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
        applySanity(entity, target, 0.20f);

        serverLevel.playSound(null, entity.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5f, 0.6f);
        serverLevel.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.4f);
        serverLevel.playSound(null, target.blockPosition(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.PLAYERS, 0.5f, 1.2f);

        Vec3 from = entity.getEyePosition();
        Vec3 to   = target.getEyePosition();

        ParticleUtil.drawParticleLine(serverLevel, GOLD_DUST,    from, to, 0.12, 1);
        ParticleUtil.drawParticleLine(serverLevel, PALE_GOLD_DUST, from, to, 0.20, 1);
        ParticleUtil.drawParticleLine(serverLevel, ParticleTypes.ELECTRIC_SPARK, from, to, 0.18, 2);

        Vec3 impactPos = target.getEyePosition();
        ParticleUtil.spawnSphereParticles(serverLevel, GOLD_DUST,  impactPos, 0.55, 18);
        ParticleUtil.spawnSphereParticles(serverLevel, PSYCHIC_DUST, impactPos, 0.35, 12);
        serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, impactPos.x, impactPos.y, impactPos.z, 12, 0.3, 0.3, 0.3, 0.08);

        Location targetLoc = new Location(target.position(), serverLevel);

        ServerScheduler.scheduleForDuration(0, 3, 24, () -> {
            Vec3 pos = target.getEyePosition();
            ParticleUtil.spawnSphereParticles(serverLevel, GOLD_DUST, pos, 0.4, 6);
            ParticleUtil.spawnSphereParticles(serverLevel, PSYCHIC_DUST, pos, 0.3, 4);
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y, pos.z, 3, 0.2, 0.2, 0.2, 0.04);
        }, null, serverLevel, () -> AbilityUtil.getTimeInArea(entity, targetLoc));
    }

    private void whipOfPain(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, scaledRange(entity, 12), 1.3f);
        if (target == null || target == entity) return;

        double damage = DamageLookup.lookupDamage(7, 0.8) * multiplier(entity);
        target.hurt(ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, entity), (float) damage);
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
        applySanity(entity, target, 0.15f);

        serverLevel.playSound(null, entity.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2f, 0.5f);
        serverLevel.playSound(null, target.blockPosition(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.0f, 0.6f);
        serverLevel.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6f, 1.6f);

        Vec3 from   = entity.getEyePosition();
        Vec3 midPt  = from.add(target.position().add(0, 1, 0)).scale(0.5).add(0, 1.8, 0);
        Vec3 impact = target.position().add(0, 1, 0);

        ParticleUtil.drawParticleLine(serverLevel, GOLD_DUST,      from, midPt, 0.10, 1);
        ParticleUtil.drawParticleLine(serverLevel, PALE_GOLD_DUST, from, midPt, 0.17, 1);
        ParticleUtil.drawParticleLine(serverLevel, GOLD_DUST,      midPt, impact, 0.10, 1);
        ParticleUtil.drawParticleLine(serverLevel, EMBER_DUST,     midPt, impact, 0.17, 1);

        ParticleUtil.spawnSphereParticles(serverLevel, GOLD_DUST,  impact, 0.65, 20);
        ParticleUtil.spawnSphereParticles(serverLevel, EMBER_DUST, impact, 0.45, 12);
        serverLevel.sendParticles(ParticleTypes.CRIT, impact.x, impact.y, impact.z, 14, 0.3, 0.3, 0.3, 0.12);
    }
}