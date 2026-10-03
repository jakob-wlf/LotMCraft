package de.jakob.lotm.beyonders.abilities.twilight_giant;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.entity.custom.ability_entities.justiciar_pathway.AncientCourtEntity.CourtProhibitionType;
import de.jakob.lotm.events.AncientCourtHandler;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AllyUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class SilverRapierAbility extends SelectableAbility {

    private static final String RAPIER_TAG = "lotm_silver_rapier";
    private static final int RAPIERS_PER_CAST = 3;
    private static final int MAX_RAPIERS = 12;
    private static final int TARGET_RANGE = 40;
    private static final double NEAREST_RANGE = 30.0D;
    private static final double LEASH_RANGE = 64.0D;
    private static final int STRIKE_INTERVAL = 12;
    private static final double BLINK_RADIUS = 2.5D;
    private static final double FLY_SPEED = 0.8D;
    private static final double ORBIT_RADIUS = 1.6D;
    private static final double DAMAGE_SCALE = 0.25D;
    private static final float DODGE_CHANCE = 0.4f;

    private static final Map<UUID, Swarm> swarms = new HashMap<>();

    public SilverRapierAbility(String id) {
        super(id, 12);
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("twilight_giant", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1400;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.silver_rapier.condense",
                "ability.lotmcraft.silver_rapier.attack",
                "ability.lotmcraft.silver_rapier.defend",
                "ability.lotmcraft.silver_rapier.dilute",
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof ServerPlayer owner)) return;
        Swarm swarm = swarms.computeIfAbsent(owner.getUUID(), uuid -> new Swarm());
        swarm.prune(serverLevel);
        switch (selectedAbility) {
            case 1 -> attack(serverLevel, owner, swarm);
            case 2 -> {
                swarm.defending = true;
                swarm.target = null;
            }
            case 3 -> dilute(serverLevel, owner, swarm);
            default -> condense(serverLevel, owner, swarm);
        }
    }

    public static boolean isRapier(Entity entity) {
        return entity.getTags().contains(RAPIER_TAG);
    }

    private static void condense(ServerLevel level, ServerPlayer owner, Swarm swarm) {
        Vec3 look = owner.getLookAngle();
        for (int i = 0; i < RAPIERS_PER_CAST && swarm.rapiers.size() < MAX_RAPIERS; i++) {
            Vex vex = EntityType.VEX.create(level);
            if (vex == null) return;
            Vec3 pos = owner.getEyePosition().add(look.scale(1.5 + i));
            vex.moveTo(pos.x, pos.y, pos.z, owner.getYRot(), 0);
            vex.setNoAi(true);
            vex.setSilent(true);
            vex.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            vex.setDropChance(EquipmentSlot.MAINHAND, 0);
            vex.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, MobEffectInstance.INFINITE_DURATION, 0, false, false));
            vex.setCustomName(Component.translatable("entity.lotmcraft.silver_rapier"));
            vex.addTag(RAPIER_TAG);
            level.addFreshEntity(vex);
            swarm.rapiers.add(vex.getUUID());
            level.sendParticles(ParticleTypes.WHITE_ASH, pos.x, pos.y, pos.z, 10, 0.2, 0.2, 0.2, 0.02);
        }
        level.playSound(null, owner.blockPosition(), SoundEvents.ANVIL_PLACE, owner.getSoundSource(), 0.6f, 1.8f);
    }

    private static void attack(ServerLevel level, ServerPlayer owner, Swarm swarm) {
        LivingEntity target = AbilityUtil.getTargetEntity(owner, TARGET_RANGE, 1.5f);
        if (target != null && isRapier(target)) target = null;
        if (target == null) target = nearestTarget(level, owner);
        if (target == null) {
            AbilityUtil.sendActionBar(owner, Component.translatable("ability.lotmcraft.silver_rapier.no_target").withColor(0xFFC8CCD9));
            return;
        }
        swarm.defending = false;
        swarm.target = target;
    }

    private static void dilute(ServerLevel level, ServerPlayer owner, Swarm swarm) {
        swarm.discardAll(level);
        swarms.remove(owner.getUUID());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer owner)) return;
        Swarm swarm = swarms.get(owner.getUUID());
        if (swarm == null) return;
        ServerLevel level = owner.serverLevel();
        swarm.prune(level);
        if (swarm.rapiers.isEmpty()) {
            swarms.remove(owner.getUUID());
            return;
        }
        if (swarm.target != null && (!isValidTarget(owner, swarm.target) || swarm.target.level() != level || swarm.target.distanceTo(owner) > LEASH_RANGE)) {
            swarm.target = swarm.defending ? null : nearestTarget(level, owner);
        }

        List<UUID> ids = new ArrayList<>(swarm.rapiers);
        for (int i = 0; i < ids.size(); i++) {
            if (!(level.getEntity(ids.get(i)) instanceof Vex rapier)) continue;
            if (swarm.target == null) {
                orbit(owner, rapier, i, ids.size());
            } else {
                hunt(level, owner, rapier, swarm.target, i);
            }
        }
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;
        Entity direct = event.getSource().getDirectEntity();
        if (direct != null && isRapier(direct) && event.getSource().getEntity() instanceof LivingEntity owner && !isValidTarget(owner, victim)) {
            event.setCanceled(true);
            return;
        }
        if (isRapier(victim)) {
            if (victim.getRandom().nextFloat() < DODGE_CHANCE && victim.level() instanceof ServerLevel level && canBlink(victim, null, ownerOf(level, victim))) {
                event.setCanceled(true);
                Vec3 away = victim.position().add((victim.getRandom().nextDouble() - 0.5) * 4, 0.5, (victim.getRandom().nextDouble() - 0.5) * 4);
                victim.teleportTo(away.x, away.y, away.z);
            }
            return;
        }
        Swarm swarm = swarms.get(victim.getUUID());
        if (swarm != null && swarm.defending && event.getSource().getEntity() instanceof LivingEntity attacker && isValidTarget(victim, attacker)) {
            swarm.target = attacker;
        }
    }

    @SubscribeEvent
    public static void onJoinLevel(EntityJoinLevelEvent event) {
        if (event.loadedFromDisk() && isRapier(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        dismiss(event.getEntity());
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        dismiss(event.getEntity());
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Swarm swarm = swarms.remove(event.getEntity().getUUID());
        if (swarm == null || !(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLevel from = player.server.getLevel(event.getFrom());
        if (from != null) swarm.discardAll(from);
    }

    private static void dismiss(LivingEntity owner) {
        Swarm swarm = swarms.remove(owner.getUUID());
        if (swarm != null && owner.level() instanceof ServerLevel level) swarm.discardAll(level);
    }

    private static boolean isValidTarget(LivingEntity owner, LivingEntity target) {
        return target != owner && target.isAlive() && !isRapier(target) && !AllyUtil.areAllies(owner, target) && AbilityUtil.mayDamage(owner, target);
    }

    private static void orbit(ServerPlayer owner, Vex rapier, int index, int count) {
        double angle = owner.tickCount * 0.08 + index * Math.PI * 2 / count;
        Vec3 pos = owner.position().add(Math.cos(angle) * ORBIT_RADIUS, 1.4, Math.sin(angle) * ORBIT_RADIUS);
        rapier.moveTo(pos.x, pos.y, pos.z, (float) Math.toDegrees(angle), 0);
    }

    private static void hunt(ServerLevel level, ServerPlayer owner, Vex rapier, LivingEntity target, int index) {
        boolean strikeTick = (owner.tickCount + index * 3) % STRIKE_INTERVAL == 0;
        if (canBlink(rapier, target, owner)) {
            if (!strikeTick) return;
            double angle = rapier.getRandom().nextDouble() * Math.PI * 2;
            Vec3 pos = target.position().add(Math.cos(angle) * BLINK_RADIUS, target.getBbHeight() * 0.6, Math.sin(angle) * BLINK_RADIUS);
            level.sendParticles(ParticleTypes.WHITE_ASH, rapier.getX(), rapier.getY(), rapier.getZ(), 6, 0.2, 0.2, 0.2, 0.02);
            rapier.moveTo(pos.x, pos.y, pos.z, yawTowards(pos, target.position()), 0);
        } else {
            Vec3 toTarget = target.position().add(0, target.getBbHeight() * 0.6, 0).subtract(rapier.position());
            Vec3 step = toTarget.length() > FLY_SPEED ? toTarget.normalize().scale(FLY_SPEED) : toTarget;
            Vec3 pos = rapier.position().add(step);
            rapier.moveTo(pos.x, pos.y, pos.z, yawTowards(pos, target.position()), 0);
            if (!strikeTick || rapier.distanceTo(target) > BLINK_RADIUS + 1) return;
        }
        if (!isValidTarget(owner, target)) return;
        target.invulnerableTime = 0;
        target.hurt(level.damageSources().source(ModDamageTypes.BEYONDER_GENERIC, rapier, owner), (float) DamageLookup.lookupDamage(4, DAMAGE_SCALE));
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY(0.6), target.getZ(), 1, 0, 0, 0, 0);
        level.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, owner.getSoundSource(), 0.6f, 1.6f);
    }

    private static boolean canBlink(LivingEntity rapier, LivingEntity target, LivingEntity owner) {
        if (ProtectionAbility.isInForeignDome(rapier, owner)) return false;
        if (target != null && (ProtectionAbility.isInForeignDome(target, owner) || AncientCourtHandler.hasProhibition(target, CourtProhibitionType.TELEPORTING))) return false;
        return !AncientCourtHandler.hasProhibition(rapier, CourtProhibitionType.TELEPORTING);
    }

    private static LivingEntity ownerOf(ServerLevel level, Entity rapier) {
        for (Map.Entry<UUID, Swarm> entry : swarms.entrySet()) {
            if (entry.getValue().rapiers.contains(rapier.getUUID())) return level.getPlayerByUUID(entry.getKey());
        }
        return null;
    }

    private static float yawTowards(Vec3 from, Vec3 to) {
        Vec3 diff = to.subtract(from);
        return (float) (Mth.atan2(diff.z, diff.x) * Mth.RAD_TO_DEG) - 90f;
    }

    private static LivingEntity nearestTarget(ServerLevel level, Player owner) {
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity candidate : AbilityUtil.getNearbyEntities(owner, level, owner.position(), NEAREST_RANGE)) {
            if (isRapier(candidate) || !AbilityUtil.mayDamage(owner, candidate)) continue;
            double distance = candidate.distanceToSqr(owner);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        return best;
    }

    private static final class Swarm {
        private final List<UUID> rapiers = new ArrayList<>();
        private LivingEntity target;
        private boolean defending;

        private void prune(ServerLevel level) {
            rapiers.removeIf(uuid -> !(level.getEntity(uuid) instanceof Vex vex) || !vex.isAlive());
        }

        private void discardAll(ServerLevel level) {
            for (UUID uuid : rapiers) {
                Entity entity = level.getEntity(uuid);
                if (entity != null) entity.discard();
            }
            rapiers.clear();
        }
    }
}
