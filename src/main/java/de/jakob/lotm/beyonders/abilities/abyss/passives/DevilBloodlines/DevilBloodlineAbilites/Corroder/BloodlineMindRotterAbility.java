
package de.jakob.lotm.beyonders.abilities.abyss.passives.DevilBloodlines.DevilBloodlineAbilites.Corroder;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.abyss.passives.DevilBloodlines.DevilBloodlineAbility;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.entity.custom.AvatarEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BloodlineMindRotterAbility extends PassiveAbility {
    public static final float POISON_DAMAGE_MULTIPLIER = 1.25F;
    private static final double RETALIATION_DAMAGE_SCALE = 0.5;
    private static final int RETALIATION_NAUSEA_TICKS = 20 * 10;
    private static final long RETALIATION_COOLDOWN_TICKS = 20L;
    private static final Map<UUID, Long> lastRetaliation = new ConcurrentHashMap<>();
    private static final DustParticleOptions ROT_DUST =
            new DustParticleOptions(new Vector3f(0.12F, 0.3F, 0.12F), 1.6F);
    public BloodlineMindRotterAbility(String id) {
        super(id);
    }
    @Override
    public Map<String, Integer> getRequirements() {return new HashMap<>(Map.of("abyss", 5));}
    @Override
    public boolean meetsBloodlineRequirement(LivingEntity entity) {
        return hasCorroderBloodline(entity);
    }
    private static boolean hasCorroderBloodline(LivingEntity entity) {
        if (entity instanceof AvatarEntity) return false;
        return DevilBloodlineAbility.hasBloodline(entity, DevilBloodlineAbility.Bloodline.CORRODER);
    }
    @Override
    public void tick(Level level, LivingEntity entity) {
    }
    public static boolean isMindRotter(LivingEntity entity) {
        if (BeyonderData.getSequence(entity) > 5) return false;
        return hasCorroderBloodline(entity);
    }
    public static boolean protects(LivingEntity victim, int invaderSeq) {return isMindRotter(victim) && invaderSeq >= BeyonderData.getSequence(victim);}
    public static boolean isPlagueImmune(LivingEntity victim, int plagueOwnerSeq) {return protects(victim, plagueOwnerSeq);}
    public static float getPoisonDamageMultiplier(LivingEntity user) {return isMindRotter(user) ? POISON_DAMAGE_MULTIPLIER : 1.0F;}
    //Retaliation
    public static void onMindInvasion(int invaderSeq, LivingEntity invader, LivingEntity victim) {
        if (!(victim.level() instanceof ServerLevel level)) return;
        if (invader == null || invader == victim) return;
        if (!protects(victim, invaderSeq)) return;
        // Never punish your own allies
        if (victim.getData(ModAttachments.ALLY_COMPONENT.get()).isAlly(invader.getUUID())) return;
        long now = level.getGameTime();
        long last = lastRetaliation.getOrDefault(invader.getUUID(), 0L);
        if (now - last < RETALIATION_COOLDOWN_TICKS) return;
        lastRetaliation.put(invader.getUUID(), now);
        float damage = (float) (DamageLookup.lookupDamage(5, RETALIATION_DAMAGE_SCALE) * POISON_DAMAGE_MULTIPLIER);
        // Deal corrosion damage.
        invader.hurt(ModDamageTypes.source(level, ModDamageTypes.CORROSION, victim), damage);
        // Apply Nausea IV for 6 seconds.
        invader.addEffect(new MobEffectInstance(MobEffects.CONFUSION, RETALIATION_NAUSEA_TICKS, 3));
        ParticleUtil.spawnParticles(level, ROT_DUST,invader.getEyePosition(), 60, 0.5, 0.02);
        AbilityUtil.sendActionBar(invader, Component.literal("Their mind is rotting, and it spreads to yours.").withColor(0x2f6b2f));
    }
}