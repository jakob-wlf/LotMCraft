package de.jakob.lotm.beyonders.abilities.death;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.beyonders.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.gamerule.ModGameRules;
import de.jakob.lotm.util.helper.AllyUtil;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class HandOfDeathAbility extends ToggleAbility {

    private static final float SAME_SEQUENCE_DAMAGE_FRACTION = 0.30f;
    private static final float DAMAGE_FRACTION_STEP_PER_SEQUENCE = 0.10f;

    public HandOfDeathAbility(String id) {
        super(id);
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("death", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 128;
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        Vec3 center = entity.position().add(0, 1, 0);
        ParticleUtil.spawnSphereParticles((ServerLevel) level, ParticleTypes.SOUL, center, 1.2, 40, 0.25);
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.WITHER_AMBIENT, SoundSource.PLAYERS, 1.2f, 0.4f);

        AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.hand_of_death.activated").withColor(0xFF334f23));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        if (InteractionHandler.isInteractionPossibleStrictlyHigher(new Location(entity.position(), (ServerLevel) level), "purification", BeyonderData.getSequence(entity), -1)) {
            cancel((ServerLevel) level, entity);
        }
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.SOUL_SAND_BREAK, SoundSource.PLAYERS, 1.0f, 0.6f);

        AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.hand_of_death.deactivated").withColor(0xFF334f23));
    }

    @SubscribeEvent
    public static void onHitEntity(LivingDamageEvent.Pre event) {
        DamageSource source = event.getSource();
        Entity attackerEntity = source.getEntity();
        if (!(attackerEntity instanceof LivingEntity attacker)) return;

        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) return;

        HandOfDeathAbility ability = (HandOfDeathAbility) LOTMCraft.abilityHandler.getById("hand_of_death_ability");
        if (ability == null || !ability.isActiveForEntity(attacker)) return;

        String damageType = source.typeHolder().getRegisteredName();
        if (!isMeleeHit(source)) {
            LOTMCraft.LOGGER.info("[Hand of Death] Skipped non-melee hit: {} -> {} (type={}, direct={})",
                    attacker.getName().getString(), victim.getName().getString(), damageType,
                    source.getDirectEntity() == null ? "none" : source.getDirectEntity().getName().getString());
            return;
        }

        // Lower sequence number = stronger. seqDiff > 0 means the victim is weaker (higher sequence).
        int attackerSeq = BeyonderData.getSequence(attacker);
        int victimSeq = BeyonderData.getSequence(victim);
        int seqDiff = victimSeq - attackerSeq;
        float damageFraction = SAME_SEQUENCE_DAMAGE_FRACTION + (seqDiff * DAMAGE_FRACTION_STEP_PER_SEQUENCE);
        if (damageFraction <= 0f) {
            LOTMCraft.LOGGER.info("[Hand of Death] No bonus: {} (seq {}) -> {} (seq {}), victim too strong",
                    attacker.getName().getString(), attackerSeq, victim.getName().getString(), victimSeq);
            return;
        }

        float baseDamage = event.getNewDamage();
        float bonusDamage = victim.getMaxHealth() * damageFraction;
        event.setNewDamage(baseDamage + bonusDamage);

        LOTMCraft.LOGGER.info("[Hand of Death] Hit: {} (seq {}) -> {} (seq {}) | type={} | base={} + bonus={} ({}% of {} max HP) = {}",
                attacker.getName().getString(), attackerSeq, victim.getName().getString(), victimSeq, damageType,
                String.format("%.2f", baseDamage), String.format("%.2f", bonusDamage), Math.round(damageFraction * 100),
                String.format("%.1f", victim.getMaxHealth()), String.format("%.2f", baseDamage + bonusDamage));

        ServerLevel level = (ServerLevel) victim.level();
        if (victim instanceof Player) {
            playLeftHandMarkEffects(level, victim);
        } else {
            Vec3 center = victim.position().add(0, 1, 0);
            ParticleUtil.spawnSphereParticles(level, ParticleTypes.SOUL, center, 1.0, 30, 0.3f);
            level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 1.0f, 0.7f);
        }
    }

    // TEMP DEBUG: traces Hand of Death hits through the damage pipeline. Remove once the hit issue is solved.
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void debugTraceAttack(AttackEntityEvent event) {
        Player attacker = event.getEntity();
        if (attacker.level().isClientSide || !"death".equals(BeyonderData.getPathway(attacker))) return;
        if (!(event.getTarget() instanceof LivingEntity victim)) return;

        LOTMCraft.LOGGER.info("[Hand of Death][trace 1/3 swing] {} -> {} | handOfDeathActive={} spirituality={} | swingCanceled={} angelImmunityRule={}",
                describe(attacker), describe(victim), isHandOfDeathActive(attacker),
                String.format("%.0f", BeyonderData.getSpirituality(attacker)), event.isCanceled(),
                victim.level().getGameRules().getBoolean(ModGameRules.ANGEL_DAMAGE_IMMUNITY));
    }

    // TEMP DEBUG
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void debugTraceIncoming(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide || !(attacker instanceof Player) || !"death".equals(BeyonderData.getPathway(attacker))) return;

        LOTMCraft.LOGGER.info("[Hand of Death][trace 2/3 incoming] {} -> {} | type={} | original={} final={} canceled={} | mayDamage={} allies={} canAttack={} | modDamage={} angelImmunityRule={} victimInvulTime={}",
                describe(attacker), describe(victim), event.getSource().typeHolder().getRegisteredName(),
                String.format("%.2f", event.getContainer().getOriginalDamage()), String.format("%.2f", event.getAmount()),
                event.isCanceled(), AbilityUtil.mayDamage(attacker, victim), AllyUtil.areAllies(attacker, victim),
                attacker.canAttack(victim), ModDamageTypes.isModDamage(event.getSource()),
                victim.level().getGameRules().getBoolean(ModGameRules.ANGEL_DAMAGE_IMMUNITY), victim.invulnerableTime);
    }

    private static boolean isHandOfDeathActive(LivingEntity entity) {
        HandOfDeathAbility ability = (HandOfDeathAbility) LOTMCraft.abilityHandler.getById("hand_of_death_ability");
        return ability != null && ability.isActiveForEntity(entity);
    }

    private static String describe(LivingEntity entity) {
        return entity.getName().getString() + " (" + BeyonderData.getPathway(entity) + " seq " + BeyonderData.getSequence(entity) + ")";
    }

    private static boolean isMeleeHit(DamageSource source) {
        return source.getDirectEntity() == source.getEntity()
                && (source.is(DamageTypes.PLAYER_ATTACK)
                 || source.is(DamageTypes.MOB_ATTACK)
                 || source.is(DamageTypes.MOB_ATTACK_NO_AGGRO));
    }

    private static void playLeftHandMarkEffects(ServerLevel level, LivingEntity target) {
        Vec3 center = target.position().add(0, 1, 0);
        Vec3 feet   = target.position();

        ParticleUtil.spawnSphereParticles(level, ParticleTypes.REVERSE_PORTAL, center, 4.5, 140);
        ParticleUtil.spawnSphereParticles(level, ParticleTypes.REVERSE_PORTAL, center, 3.0, 100);
        ParticleUtil.spawnSphereParticles(level, ParticleTypes.SOUL, center, 1.2, 50, 0.35);
        ParticleUtil.spawnSphereParticles(level, ParticleTypes.SOUL, center, 2.5, 80, 0.20);

        ParticleUtil.spawnCircleParticles(level, ParticleTypes.SOUL, feet,               3.5, 56);
        ParticleUtil.spawnCircleParticles(level, ParticleTypes.SOUL, feet.add(0, 1,  0), 3.0, 48);
        ParticleUtil.spawnCircleParticles(level, ParticleTypes.SOUL, feet.add(0, 2,  0), 2.5, 40);
        ParticleUtil.spawnCircleParticles(level, ParticleTypes.SOUL, feet.add(0, 3,  0), 1.5, 28);

        ParticleUtil.createParticleSpirals(level, ParticleTypes.SOUL, center, 0.4, 2.8, 5.0, 1.5, 2.0, 60, 3, 7);

        ParticleUtil.spawnParticles(level, ParticleTypes.LARGE_SMOKE, center, 25, 1.2, 0.05);
        ParticleUtil.spawnParticles(level, ParticleTypes.SMOKE,        center, 40, 2.0, 0.02);

        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.WITHER_AMBIENT,  SoundSource.PLAYERS, 1.8f, 0.45f);
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.SOUL_SAND_BREAK, SoundSource.PLAYERS, 1.2f, 0.55f);
    }
}
