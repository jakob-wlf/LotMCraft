package de.jakob.lotm.beyonders.abilities.black_emperor;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.beyonders.abilities.black_emperor.handlers.CorrosionUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber
public class CorrosionAbility extends Ability {

    private static final double RADIUS = 10.0;
    private static final int DURATION_TICKS = 400;
    private static final int FIRST_ACTION_DELAY = 20;
    private static final int ACTION_INTERVAL = 40;
    private static final float  IMPACT_DAMAGE      = 8.0f;
    private static final double IMPACT_KNOCKBACK   = 0.6;


    public static final RandomSource RANDOM = RandomSource.create();
    public CorrosionAbility(String id) {
        super(id, 0);
        hasDynamicCooldown = true;
        hasDynamicSpirituality = true;
        dynamicCooldown = new LinkedList<>(List.of(2, 3, 3, 4, 4, 5, 5));
        dynamicSpirituality = new LinkedList<>(List.of(4000f, 2000f, 1500f, 700f, 500f, 300f, 200f));
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        long until = serverLevel.getGameTime() + DURATION_TICKS;
        int damaged   = 0;
        int corrupted = 0;

        for (LivingEntity target : AbilityUtil.getNearbyEntities(entity, serverLevel,
                entity.position(), RADIUS)) {

            if (target == entity) continue;
            if (target.isAlliedTo(entity)) continue;

            boolean hit = target.hurt(
                    ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, entity),
                    IMPACT_DAMAGE
            );
            if (hit) damaged++;

            double dx = entity.getX() - target.getX();
            double dz = entity.getZ() - target.getZ();
            if (dx * dx + dz * dz > 1.0E-4) {
                target.knockback(IMPACT_KNOCKBACK, dx, dz);
                target.hurtMarked = true;
            }
            serverLevel.sendParticles(ParticleTypes.SQUID_INK,
                    target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                    12, 0.4, 0.4, 0.4, 0.03);

            if (!canCorrode(entity, target)) continue;

            CorrosionUtil.corrupt(target, entity, until, FIRST_ACTION_DELAY, ACTION_INTERVAL);
            corrupted++;

            if (target instanceof ServerPlayer sp) {
                sp.sendSystemMessage(Component.literal("§8Dark, greedy thoughts cloud your mind…"));
            }
        }
        serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0f, 0.6f);
        serverLevel.sendParticles(ParticleTypes.SOUL,
                entity.getX(), entity.getY() + 1.0, entity.getZ(),
                30, RADIUS * 0.4, 0.5, RADIUS * 0.4, 0.01);

        if (entity instanceof ServerPlayer caster && (damaged > 0 || corrupted > 0)) {
            caster.sendSystemMessage(Component.literal(
                    "§5Corrosion: §f" + damaged + " hit, §5" + corrupted + " corrupted."));
        }
    }

    private boolean canCorrode(LivingEntity player, LivingEntity target) {
        if(!BeyonderData.isBeyonder(target)) return true;
        return (RANDOM.nextDouble() < EloquenceAbility.calculateMultiplier(BeyonderData.getSequence(player), BeyonderData.getSequence(target)));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of("black_emperor", 6);
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            CorrosionUtil.tick(level);
        }
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        Entity attackerEntity = event.getSource().getEntity();
        if (attackerEntity == null) return;
        if (!(attackerEntity instanceof LivingEntity attacker)) return;
        if (!CorrosionUtil.isCorroded(attacker.getUUID())) return;

        event.setCanceled(true);
        if (!(attacker.level() instanceof ServerLevel sl)) return;
        if (attacker instanceof ServerPlayer sp) {
            CorrosionUtil.forcePlayerAction(sp, sl);
            sp.sendSystemMessage(Component.literal(
                    "§8You can't focus, the thought slips away…"));

            LivingEntity victim = event.getEntity();
            sl.sendParticles(ParticleTypes.SMOKE,
                    victim.getX(), victim.getY() + victim.getBbHeight() * 0.5, victim.getZ(),
                    5, 0.2, 0.2, 0.2, 0.01);
        }
    }


    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;

        UUID source = CorrosionUtil.getSourceId(mob.getUUID());
        if (source == null) return;

        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        if (newTarget == null) return;
        if (newTarget instanceof ServerPlayer) {
            event.setNewAboutToBeSetTarget(null);
            return;
        }

        if (newTarget.getUUID().equals(source)) {
            event.setNewAboutToBeSetTarget(null);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        CorrosionUtil.remove(event.getEntity().getUUID());
    }
}