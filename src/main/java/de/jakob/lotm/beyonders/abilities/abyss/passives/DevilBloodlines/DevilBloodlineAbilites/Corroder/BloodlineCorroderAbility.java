package de.jakob.lotm.beyonders.abilities.abyss.passives.DevilBloodlines.DevilBloodlineAbilites.Corroder;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.abyss.passives.DevilBloodlines.DevilBloodlineAbility;
import de.jakob.lotm.beyonders.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static de.jakob.lotm.beyonders.abilities.abyss.passives.DevilBloodlines.DevilBloodlineAbilites.Corroder.BloodlineMindRotterAbility.getPoisonDamageMultiplier;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class BloodlineCorroderAbility extends SelectableAbility {
    // Corroding Flames
    private static final int WITHER_II = 1; // amplifier 1 = Wither II
    // Armor durability lost per hit, as a fraction of each piece's max durability (15%)
    private static final float FLAME_ARMOR_DAMAGE = 0.15F;
    // Dissolving Mist
    private static final double MIST_RADIUS = 15.0D;
    private static final int MIST_DURATION_TICKS = 20 * 15;
    private static final int POISON_II = 1; // amplifier 1 = Poison II
    // Armor durability lost per second inside the mist, as a fraction of max durability (1.5%)
    private static final float MIST_ARMOR_DAMAGE = 0.015F;
    // Extra damage taken by anything inside the mist (10%).
    private static final float MIST_DAMAGE_BONUS = 0.10F;
    private static final long MIST_LINGER_TICKS = 20L * 3L;
    private static final Map<UUID, Long> dissolvedUntil = new ConcurrentHashMap<>();
    private final DustParticleOptions corrodingDust = new DustParticleOptions(new Vector3f(25 / 255f, 110 / 255f, 70 / 255f), 1.2f);
    private final DustParticleOptions mistDust = new DustParticleOptions(new Vector3f(0.12F, 0.35F, 0.15F), 2.4F);

    public BloodlineCorroderAbility(String id) {
        super(id, 5.5f); // flame cooldown, like Poisonous Flame. The mist has its own cooldown below.
        canBeUsedByNPC = false;
    }
    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 6));
    }
    @Override
    public float getSpiritualityCost() {
        return 60;
    }
    @Override
    public boolean meetsBloodlineRequirement(LivingEntity entity) {
        return DevilBloodlineAbility.hasBloodline(entity, DevilBloodlineAbility.Bloodline.CORRODER);
    }
    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.corroder_ability.corroding_flames",
                "ability.lotmcraft.corroder_ability.dissolving_mist"
        };
    }
    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        switch (abilityIndex) {
            case 0 -> castCorrodingFlames(serverLevel, entity);
            case 1 -> castDissolvingMist(serverLevel, entity);
        }
    }
    //Corroding Flames (Poisonous Flame, but Wither II)
    private void castCorrodingFlames(ServerLevel level, LivingEntity entity) {
        Vec3 look = entity.getLookAngle().normalize();
        Vec3 startPos = entity.getEyePosition().subtract(0, .2, 0).add(look);
        level.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 2.0f, .5f);
        int seq = AbilityUtil.getSeqWithArt(entity, this);
        ParticleUtil.drawParticleLine(level, ParticleTypes.SMOKE, startPos, look, 2, .5, 23, .4);
        ParticleUtil.drawParticleLine(level, ModParticles.GREEN_FLAME.get(), startPos, look, 2, .5, 35, .4);
        ParticleUtil.drawParticleLine(level, corrodingDust, startPos, look, 2, .5, 23, .4);
        // Purification neutralizes the wither effect
        boolean purified = InteractionHandler.isInteractionPossible(new Location(startPos, level), "purification", seq);
        AbilityUtil.damageNearbyEntities(level, entity, 2.75 * multiplier(entity), DamageLookup.lookupDamage(6, .9) * multiplier(entity), startPos, true, false, true, 0, 20 * 3, ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, entity));
        // Heavy durability damage to the armor of everything hit
        AbilityUtil.getNearbyEntities(entity, level, startPos, 2.75 * multiplier(entity)).forEach(hit -> {
            if (hit == entity) return;
            if (entity.getData(ModAttachments.ALLY_COMPONENT.get()).isAlly(hit.getUUID())) return;
            corrodeArmor(hit, FLAME_ARMOR_DAMAGE);
        });
        if (!purified) {AbilityUtil.addPotionEffectToNearbyEntities(level, entity, 3, startPos, new MobEffectInstance(MobEffects.WITHER, 20 * 8 * (int) Math.max(multiplier(entity) / 2, 1), WITHER_II, false, true));}
        BlockPos lightPos = BlockPos.containing(startPos);
        if (level.getBlockState(lightPos).isAir()) {level.setBlockAndUpdate(lightPos, Blocks.LIGHT.defaultBlockState());}
        ServerScheduler.scheduleDelayed(25, () -> {
            // Only remove our own light block
            if (level.getBlockState(lightPos).is(Blocks.LIGHT)) {level.setBlockAndUpdate(lightPos, Blocks.AIR.defaultBlockState());}});
        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(level, startPos, entity, this, interactionFlags, 2.4f, interactionCacheTicks));
    }
    //Dissolving Mist
    private void castDissolvingMist(ServerLevel level, LivingEntity entity) {
        ServerScheduler.scheduleForDuration(0, 20, (int) (MIST_DURATION_TICKS * multiplier(entity)), () -> {
            if (entity.level().isClientSide) return;
            Location currentLoc = new Location(entity.position(), entity.level());
            int seq = AbilityUtil.getSeqWithArt(entity, this);
            if (InteractionHandler.isInteractionPossible(currentLoc, "purification", seq) || InteractionHandler.isInteractionPossible(currentLoc, "cleansing", seq)) {return;}
            ServerLevel serverLevel = (ServerLevel) entity.level();
            long gameTime = serverLevel.getGameTime();
            ParticleUtil.spawnParticles(serverLevel, mistDust, entity.position().add(0, 1, 0), 200, MIST_RADIUS * 0.6, 0.02);
            AbilityUtil.addPotionEffectToNearbyEntities(serverLevel, entity, MIST_RADIUS, entity.position(), new MobEffectInstance(MobEffects.POISON, 40, POISON_II, false, false, false));
            // Mark everything in the mist: it takes extra damage while inside and shortly after
            AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), MIST_RADIUS).forEach(target -> {
                if (target == entity) return;
                if (entity.getData(ModAttachments.ALLY_COMPONENT.get()).isAlly(target.getUUID())) return;
                dissolvedUntil.put(target.getUUID(), gameTime + MIST_LINGER_TICKS);
                corrodeArmor(target, MIST_ARMOR_DAMAGE);
            });
            AbilityUtil.damageNearbyEntities(serverLevel, entity, MIST_RADIUS, (float) DamageLookup.lookupDps(6, .2, 35, 20)*getPoisonDamageMultiplier(entity), entity.position(), true, false, true, 0, ModDamageTypes.source(serverLevel, ModDamageTypes.DEMONESS_GENERIC, entity));}, () -> clearArtifactScaling(entity), level, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));
    }
    private static void corrodeArmor(LivingEntity target, float fractionOfMaxDurability) {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = target.getItemBySlot(slot);
            if (stack.isEmpty() || !stack.isDamageableItem()) continue;
            int amount = Math.max(1, (int) (stack.getMaxDamage() * fractionOfMaxDurability));
            stack.hurtAndBreak(amount, target, slot);
        }
    }
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) return;
        Long until = dissolvedUntil.get(target.getUUID());
        if (until == null) return;
        if (target.level().getGameTime() > until) {
            dissolvedUntil.remove(target.getUUID());
            return;
        }
        event.setAmount(event.getAmount() * (1.0F + MIST_DAMAGE_BONUS));
    }
}