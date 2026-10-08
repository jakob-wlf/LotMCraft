package de.jakob.lotm.beyonders.abilities.twilight_giant;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.common.DivinationAbility;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.AbilityUseEvent;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.death.SpiritWorldTraversalAbility;
import de.jakob.lotm.beyonders.abilities.door.BlinkAbility;
import de.jakob.lotm.beyonders.abilities.door.PlayerTeleportationAbility;
import de.jakob.lotm.beyonders.abilities.door.TeleportationAuthorityAbility;
import de.jakob.lotm.beyonders.abilities.door.TravelersDoorAbility;
import de.jakob.lotm.beyonders.abilities.twilight_giant.handlers.TwilightAging;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.twilight_giant.FightingArenaEntity;
import de.jakob.lotm.entity.custom.ability_entities.twilight_giant.ProtectiveCageEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AllyUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class CombatAuthorityAbility extends SelectableAbility {

    private static final int TARGET_RANGE = 30;
    private static final double AREA_RADIUS = 50.0D;
    private static final double UPGRADED_RADIUS = 100.0D;
    private static final long UPGRADED_LIFE = 20L * 60 * 5;
    private static final long REPAIR_TICKS = 20L * 60 * 2;
    private static final double TARGET_SEAL_RADIUS = 4.0D;
    private static final double ACTIVE_DISTANCE = 10.0D;
    private static final double OUTSIDE_MARGIN = 2.0D;
    private static final double ALLY_RANGE = 20.0D;
    private static final float SEAL_YEARS = 1f;
    private static final int EFFECT_INTERVAL = 20;
    private static final double CAGE_RADIUS = 8.0D;
    private static final double CAGE_SPAWN_OFFSET = 4.0D;
    private static final double CAGE_PUSH = 1.5D;
    private static final int CAGE_BREAK_SECONDS = 5;
    private static final double SEAL_INSET = 0.75D;
    private static final int INSTINCT_TICKS = 20 * 8;

    private static final Map<UUID, Seal> seals = new HashMap<>();
    private static final Map<UUID, Cage> cages = new HashMap<>();
    private static final Map<UUID, Long> instincts = new HashMap<>();

    public CombatAuthorityAbility(String id) {
        super(id, 40);
        canBeUsedByNPC = false;
        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(12000f, 7000f, 4500f));
        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(15, 25, 40));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("twilight_giant", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 4500;
    }

    @Override
    protected String[] getAbilityNames() {
        return namesFor(ClientBeyonderCache.localSequence());
    }

    @Override
    public String getSelectedAbility(LivingEntity entity) {
        String[] names = namesFor(BeyonderData.getSequence(entity));
        int selected = getSelectedAbilityIndex(entity.getUUID());
        if (selected < 0 || selected >= names.length) selected = 0;
        return names[selected];
    }

    @Override
    public void setSelectedAbility(ServerPlayer player, int selectedAbility) {
        if (selectedAbility < 0 || selectedAbility >= namesFor(BeyonderData.getSequence(player)).length) return;
        selectedAbilities.put(player.getUUID(), selectedAbility);
    }

    private static String[] namesFor(int sequence) {
        if (sequence <= 0) return new String[]{
                "ability.lotmcraft.combat_authority.protection_seal",
                "ability.lotmcraft.combat_authority.fighting_cage",
                "ability.lotmcraft.combat_authority.pure_instinct",
        };
        return new String[]{
                "ability.lotmcraft.combat_authority.protection_seal",
                "ability.lotmcraft.combat_authority.fighting_cage",
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof ServerPlayer player)) return;
        if (selectedAbility == 0) {
            castSeal(serverLevel, player);
        } else if (selectedAbility == 1) {
            castCage(serverLevel, player);
        } else {
            castInstinct(player);
        }
    }

    private static void castSeal(ServerLevel level, ServerPlayer player) {
        Seal seal = seals.get(player.getUUID());
        if (player.isShiftKeyDown()) {
            if (seal == null) seal = createSeal(level, player, false);
            gatherAllies(level, player, seal);
            return;
        }
        if (seal != null && seal.lookedAt(player)) {
            removeSeal(player, "ability.lotmcraft.combat_authority.seal_lifted");
            return;
        }
        createSeal(level, player, true);
    }

    private static Seal createSeal(ServerLevel level, ServerPlayer player, boolean pushOut) {
        Seal previous = seals.remove(player.getUUID());
        if (previous != null) previous.discardVisual();
        LivingEntity target = AbilityUtil.getTargetEntity(player, TARGET_RANGE, 1.5f);
        boolean upgraded = target == null && BeyonderData.getSequence(player) <= 1;
        Seal seal = target != null
                ? new Seal(player, level, target.position(), TARGET_SEAL_RADIUS + target.getBbWidth(), true, false, 0L)
                : new Seal(player, level, AbilityUtil.getTargetLocation(player, TARGET_RANGE, 1.5f), upgraded ? UPGRADED_RADIUS : AREA_RADIUS, false, upgraded, upgraded ? level.getGameTime() + UPGRADED_LIFE : 0L);
        seals.put(player.getUUID(), seal);
        level.playSound(null, BlockPos.containing(seal.center), SoundEvents.BEACON_ACTIVATE, player.getSoundSource(), 3f, 0.6f);
        seal.spawnVisual();
        if (pushOut && seal.aroundTarget && seal.contains(player)) {
            Vec3 away = horizontal(player.position().subtract(seal.center));
            Vec3 direction = away.lengthSqr() < 1.0E-4 ? horizontal(player.getLookAngle()).scale(-1) : away.normalize();
            moveTo(player, seal.center.add(direction.scale(seal.radius + OUTSIDE_MARGIN)).with(Direction.Axis.Y, player.getY()));
        }
        bar(player, "ability.lotmcraft.combat_authority.seal_formed");
        return seal;
    }

    private static void gatherAllies(ServerLevel level, ServerPlayer player, Seal seal) {
        List<LivingEntity> members = new ArrayList<>();
        members.add(player);
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(ALLY_RANGE))) {
            if (nearby != player && nearby.isAlive() && AllyUtil.areAllies(player, nearby)) members.add(nearby);
        }
        for (int i = 0; i < members.size(); i++) {
            LivingEntity member = members.get(i);
            double angle = i * Math.PI * 2 / members.size();
            double distance = Math.min(seal.radius * 0.5, 3.0D);
            moveTo(member, seal.center.add(Math.cos(angle) * distance, 0, Math.sin(angle) * distance));
            seal.guarded.add(member.getUUID());
        }
        bar(player, "ability.lotmcraft.combat_authority.allies_sealed");
    }

    private static void castCage(ServerLevel level, ServerPlayer player) {
        if (cages.containsKey(player.getUUID())) {
            removeCage(player, "ability.lotmcraft.combat_authority.cage_lifted");
            return;
        }
        LivingEntity target = AbilityUtil.getTargetEntity(player, TARGET_RANGE, 1.5f);
        if (target == null || !AbilityUtil.mayDamage(player, target)) {
            bar(player, "ability.lotmcraft.combat_authority.no_target");
            return;
        }
        Vec3 center = player.position();
        Vec3 forward = horizontal(player.getLookAngle());
        if (forward.lengthSqr() < 1.0E-4) forward = new Vec3(1, 0, 0);
        forward = forward.normalize();
        Cage cage = new Cage(player, target, level, center);
        cages.put(player.getUUID(), cage);
        moveTo(target, center.add(forward.scale(CAGE_SPAWN_OFFSET)));
        moveTo(player, center.subtract(forward.scale(CAGE_SPAWN_OFFSET)));
        level.playSound(null, BlockPos.containing(center), SoundEvents.IRON_DOOR_CLOSE, player.getSoundSource(), 3f, 0.5f);
        cage.spawnVisual();
        bar(player, "ability.lotmcraft.combat_authority.cage_formed");
    }

    private static void removeSeal(LivingEntity owner, String key) {
        Seal seal = seals.remove(owner.getUUID());
        if (seal == null) return;
        seal.discardVisual();
        seal.level.playSound(null, BlockPos.containing(seal.center), SoundEvents.BEACON_DEACTIVATE, owner.getSoundSource(), 2f, 0.6f);
        bar(owner, key);
    }

    private static void removeCage(LivingEntity owner, String key) {
        Cage cage = cages.remove(owner.getUUID());
        if (cage == null) return;
        cage.discardVisual();
        cage.level.playSound(null, BlockPos.containing(cage.center), SoundEvents.GLASS_BREAK, owner.getSoundSource(), 2f, 0.6f);
        bar(owner, key);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!seals.isEmpty()) {
            for (Seal seal : new ArrayList<>(seals.values())) seal.tick();
        }
        if (!cages.isEmpty()) {
            for (Cage cage : new ArrayList<>(cages.values())) cage.tick();
        }
    }

    private static void castInstinct(ServerPlayer player) {
        if (BeyonderData.getSequence(player) > 0) {
            BeyonderData.incrementSpirituality(player, 4500);
            bar(player, "ability.lotmcraft.combat_authority.instinct_closed");
            return;
        }
        LivingEntity target = AbilityUtil.getTargetEntity(player, TARGET_RANGE, 1.5f);
        if (target == null || !AbilityUtil.mayDamage(player, target)) {
            BeyonderData.incrementSpirituality(player, 4500);
            bar(player, "ability.lotmcraft.combat_authority.instinct_miss");
            return;
        }
        instincts.put(target.getUUID(), player.level().getGameTime() + INSTINCT_TICKS);
        bar(player, "ability.lotmcraft.combat_authority.instinct_struck");
        bar(target, "ability.lotmcraft.combat_authority.instinct_affected");
        player.level().playSound(null, target.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, player.getSoundSource(), 1.2f, 0.5f);
    }

    private static boolean instinct(LivingEntity entity) {
        Long until = instincts.get(entity.getUUID());
        if (until == null) return false;
        if (entity.level().getGameTime() >= until) {
            instincts.remove(entity.getUUID());
            return false;
        }
        return true;
    }

    @SubscribeEvent
    public static void onAbilityUse(AbilityUseEvent event) {
        LivingEntity caster = event.getEntity();
        if (caster == null || caster.level().isClientSide() || event.getAbility() == null) return;
        if (instinct(caster)) {
            event.setCanceled(true);
            return;
        }
        if (event.getAbility() instanceof DivinationAbility) {
            Vec3 point = AbilityUtil.getTargetLocation(caster, 64, 1.5f);
            LivingEntity looked = AbilityUtil.getTargetEntity(caster, 64, 1.5f, true, true);
            if (blocksDivination(caster, point) || (looked != null && blocksDivination(caster, looked))) {
                event.setCanceled(true);
                bar(caster, "ability.lotmcraft.combat_authority.divination_blocked");
            }
            return;
        }
        if (!isTeleport(event.getAbility()) || !isConfined(caster)) return;
        event.setCanceled(true);
        bar(caster, "ability.lotmcraft.combat_authority.teleport_blocked");
    }

    @SubscribeEvent
    public static void onTeleport(EntityTeleportEvent event) {
        if (event instanceof EntityTeleportEvent.TeleportCommand || event instanceof EntityTeleportEvent.SpreadPlayersCommand) return;
        if (event.getEntity().level().isClientSide()) return;
        if (isConfined(event.getEntity()) || arrivesInside(event.getEntity(), event.getTarget())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onSwordRepair(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof ServerPlayer player)) return;
        if (!ArsenalOfDawnAbility.is(event.getItemStack(), ArsenalOfDawnAbility.TWILIGHT_SWORD)) return;
        Vec3 look = AbilityUtil.getTargetLocation(player, 12, 1.5f);
        for (Seal seal : seals.values()) {
            if (!seal.upgraded || seal.level != player.level()) continue;
            double dx = look.x - seal.center.x;
            double dz = look.z - seal.center.z;
            if (Math.abs(Math.sqrt(dx * dx + dz * dz) - seal.radius) > 3) continue;
            seal.expiresAt += REPAIR_TICKS;
            bar(player, "ability.lotmcraft.combat_authority.seal_repaired");
            return;
        }
    }

    public static boolean blocksDivination(LivingEntity caster, LivingEntity target) {
        return target != null && blocksDivination(caster, target.position(), target.level());
    }

    public static boolean blocksDivination(LivingEntity caster, Vec3 point) {
        return caster != null && blocksDivination(caster, point, caster.level());
    }

    private static boolean blocksDivination(LivingEntity caster, Vec3 point, Level level) {
        if (caster == null || point == null || level == null) return false;
        for (Seal seal : seals.values()) {
            if (seal.upgraded && caster != seal.owner && seal.containsPoint(level, point)) return true;
        }
        return false;
    }

    private static boolean arrivesInside(Entity entity, Vec3 destination) {
        if (destination == null) return false;
        for (Seal seal : seals.values()) {
            if (seal.upgraded && entity != seal.owner && seal.containsPoint(entity.level(), destination)) return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onTravelToDimension(EntityTravelToDimensionEvent event) {
        if (!event.getEntity().level().isClientSide() && isConfined(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onInstinctAttack(AttackEntityEvent event) {
        if (event.getEntity().level().isClientSide() || !instinct(event.getEntity())) return;
        if (!event.getEntity().getMainHandItem().isEmpty()) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onInstinctUse(LivingEntityUseItemEvent.Start event) {
        if (!event.getEntity().level().isClientSide() && instinct(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onInstinctShield(LivingShieldBlockEvent event) {
        if (!event.getEntity().level().isClientSide() && instinct(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (seals.isEmpty()) return;
        LivingEntity victim = event.getEntity();
        Entity attacker = event.getSource().getEntity();
        for (Seal seal : seals.values()) {
            boolean victimInside = seal.contains(victim);
            if (attacker != null && seal.contains(attacker) && !victimInside) {
                event.setCanceled(true);
                return;
            }
            if (seal.guarded.contains(victim.getUUID()) && victimInside && (attacker == null || !seal.guarded.contains(attacker.getUUID()))) {
                event.setCanceled(true);
                return;
            }
        }
    }

    private static boolean isConfined(Entity entity) {
        for (Seal seal : seals.values()) {
            if (seal.contains(entity) && !seal.breaks(entity)) return true;
        }
        for (Cage cage : cages.values()) {
            if (cage.owner == entity || cage.target == entity) return true;
        }
        return false;
    }

    private static boolean isTeleport(Ability ability) {
        return ability instanceof SpiritWorldTraversalAbility || ability instanceof TravelersDoorAbility
                || ability instanceof PlayerTeleportationAbility || ability instanceof TeleportationAuthorityAbility
                || ability instanceof BlinkAbility || ability instanceof LightConcealmentAbility;
    }

    private static Vec3 horizontal(Vec3 vector) {
        return new Vec3(vector.x, 0, vector.z);
    }

    private static void moveTo(Entity entity, Vec3 target) {
        if (entity instanceof ServerPlayer player) {
            player.connection.teleport(target.x, target.y, target.z, player.getYRot(), player.getXRot());
        } else {
            entity.teleportTo(target.x, target.y, target.z);
        }
        entity.setDeltaMovement(Vec3.ZERO);
        entity.hurtMarked = true;
        entity.fallDistance = 0;
    }

    private static void bar(LivingEntity entity, String key) {
        AbilityUtil.sendActionBar(entity, Component.translatable(key).withColor(TwilightAging.TWILIGHT_TEXT));
    }

    private static final class Seal {
        private final LivingEntity owner;
        private final ServerLevel level;
        private final Vec3 center;
        private final double radius;
        private final boolean aroundTarget;
        private final boolean upgraded;
        private long expiresAt;
        private final Set<UUID> guarded = new HashSet<>();
        private final Set<LivingEntity> held = new HashSet<>();
        private final Set<LivingEntity> stronger = new HashSet<>();
        private UUID visual;

        private Seal(LivingEntity owner, ServerLevel level, Vec3 center, double radius, boolean aroundTarget, boolean upgraded, long expiresAt) {
            this.owner = owner;
            this.level = level;
            this.center = center;
            this.radius = radius;
            this.aroundTarget = aroundTarget;
            this.upgraded = upgraded;
            this.expiresAt = expiresAt;
        }

        private boolean contains(Entity entity) {
            if (entity.level() != level) return false;
            double dx = entity.getX() - center.x;
            double dz = entity.getZ() - center.z;
            double dy = entity.getY() - center.y;
            return dx * dx + dz * dz <= radius * radius && dy >= -radius && dy <= radius;
        }

        private boolean lookedAt(LivingEntity viewer) {
            if (contains(viewer)) return true;
            Vec3 point = AbilityUtil.getTargetLocation(viewer, (int) (radius + ACTIVE_DISTANCE + TARGET_RANGE), 1.5f);
            double dx = point.x - center.x;
            double dz = point.z - center.z;
            return dx * dx + dz * dz <= (radius + 1) * (radius + 1);
        }

        private boolean containsPoint(Level world, Vec3 point) {
            if (world != level) return false;
            double dx = point.x - center.x;
            double dz = point.z - center.z;
            double dy = point.y - center.y;
            return dx * dx + dz * dz <= radius * radius && dy >= -radius && dy <= radius;
        }

        private void tick() {
            if (expiresAt > 0 && level.getGameTime() >= expiresAt) {
                removeSeal(owner, "ability.lotmcraft.combat_authority.seal_expired");
                return;
            }
            if (!owner.isAlive() || owner.level() != level || horizontal(owner.position().subtract(center)).length() > radius + ACTIVE_DISTANCE) {
                removeSeal(owner, "ability.lotmcraft.combat_authority.seal_faded");
                return;
            }
            if (holdInside()) return;
            if (level.getGameTime() % EFFECT_INTERVAL != 0) return;
            AABB box = new AABB(center.x - radius, center.y - radius, center.z - radius, center.x + radius, center.y + radius, center.z + radius);
            for (LivingEntity inside : level.getEntitiesOfClass(LivingEntity.class, box)) {
                if (!inside.isAlive() || !contains(inside) || guarded.contains(inside.getUUID())) continue;
                TwilightAging.age(level, inside, owner, SEAL_YEARS);
            }
            if (HolinessAuthorityAbility.hasHolyCage(owner)) HolinessAuthorityAbility.purifyEvil(level, owner, center, radius);
        }

        private void spawnVisual() {
            ProtectiveCageEntity cage = ModEntities.PROTECTIVE_CAGE.get().create(level);
            if (cage == null) return;
            cage.setRadius((float) radius);
            if (aroundTarget) {
                cage.moveTo(center.x, center.y - radius, center.z, 0.0F, 0.0F);
            } else {
                cage.moveTo(center.x, AbilityUtil.getTargetBlock(owner, TARGET_RANGE).getY() - 25.0D, center.z, 0.0F, 0.0F);
            }
            level.addFreshEntity(cage);
            visual = cage.getUUID();
        }

        private void discardVisual() {
            if (visual == null) return;
            Entity entity = level.getEntity(visual);
            if (entity != null) entity.discard();
        }

        private boolean breaks(Entity entity) {
            return entity instanceof LivingEntity living && living != owner && BeyonderData.getSequence(living) < BeyonderData.getSequence(owner);
        }

        private boolean holdInside() {
            double reach = radius + 12;
            AABB box = new AABB(center.x - reach, center.y - reach, center.z - reach, center.x + reach, center.y + reach, center.z + reach);
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)) {
                if (entity == owner || !entity.isAlive() || !contains(entity)) continue;
                if (breaks(entity)) {
                    held.remove(entity);
                    stronger.add(entity);
                } else {
                    held.add(entity);
                }
            }
            for (LivingEntity entity : new ArrayList<>(stronger)) {
                if (!entity.isAlive() || entity.isRemoved()) {
                    stronger.remove(entity);
                    continue;
                }
                if (entity.level() != level || !contains(entity)) {
                    shatter();
                    return true;
                }
            }
            for (LivingEntity entity : new ArrayList<>(held)) {
                if (!entity.isAlive() || entity.isRemoved() || entity.level() != level || entity == owner || breaks(entity)) {
                    held.remove(entity);
                    continue;
                }
                if (!contains(entity)) pullInside(entity);
            }
            return false;
        }

        private void shatter() {
            if (seals.get(owner.getUUID()) != this) return;
            seals.remove(owner.getUUID());
            discardVisual();
            level.playSound(null, BlockPos.containing(center), SoundEvents.GLASS_BREAK, owner.getSoundSource(), 2f, 0.6f);
            level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 1, center.z, 80, Math.min(radius / 3, 8), 2, Math.min(radius / 3, 8), 0.1);
            bar(owner, "ability.lotmcraft.combat_authority.seal_shattered");
        }

        private void pullInside(LivingEntity entity) {
            double dx = entity.getX() - center.x;
            double dy = entity.getY() - center.y;
            double dz = entity.getZ() - center.z;
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            double limit = Math.max(0.5, radius - SEAL_INSET);
            double x = center.x;
            double z = center.z;
            if (horizontal > 1.0E-4) {
                double scale = Math.min(1.0, limit / horizontal);
                x = center.x + dx * scale;
                z = center.z + dz * scale;
            }
            double y = center.y + Math.max(-limit, Math.min(limit, dy));
            moveTo(entity, new Vec3(x, y, z));
        }
    }

    private static final class Cage {
        private final LivingEntity owner;
        private final LivingEntity target;
        private final ServerLevel level;
        private final Vec3 center;
        private int struggle;
        private UUID visual;

        private Cage(LivingEntity owner, LivingEntity target, ServerLevel level, Vec3 center) {
            this.owner = owner;
            this.target = target;
            this.level = level;
            this.center = center;
        }

        private void tick() {
            if (!owner.isAlive() || !target.isAlive() || owner.level() != level || target.level() != level) {
                removeCage(owner, "ability.lotmcraft.combat_authority.cage_lifted");
                return;
            }
            contain(owner);
            contain(target);
            if (level.getGameTime() % EFFECT_INTERVAL != 0) return;
            if (HolinessAuthorityAbility.hasHolyCage(owner)) HolinessAuthorityAbility.purifyEvil(level, owner, center, CAGE_RADIUS);
            if (BeyonderData.getSequence(target) >= BeyonderData.getSequence(owner)) return;
            if (++struggle < CAGE_BREAK_SECONDS) return;
            level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 1, center.z, 80, CAGE_RADIUS / 2, 2, CAGE_RADIUS / 2, 0.1);
            removeCage(owner, "ability.lotmcraft.combat_authority.cage_shattered");
        }

        private void contain(LivingEntity member) {
            Vec3 offset = horizontal(member.position().subtract(center));
            if (offset.length() <= CAGE_RADIUS - 0.5) return;
            Vec3 direction = offset.lengthSqr() < 1.0E-4 ? new Vec3(1, 0, 0) : offset.normalize();
            moveTo(member, center.add(direction.scale(CAGE_RADIUS - CAGE_PUSH)).with(Direction.Axis.Y, member.getY()));
        }

        private void spawnVisual() {
            FightingArenaEntity arena = ModEntities.FIGHTING_ARENA.get().create(level);
            if (arena == null) return;
            arena.setRadius((float) CAGE_RADIUS);
            arena.moveTo(center.x, center.y, center.z, owner.getYRot(), 0.0F);
            level.addFreshEntity(arena);
            visual = arena.getUUID();
        }

        private void discardVisual() {
            if (visual == null) return;
            Entity entity = level.getEntity(visual);
            if (entity != null) entity.discard();
        }
    }
}
