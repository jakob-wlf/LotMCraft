package de.jakob.lotm.beyonders.abilities.abyss;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.AllyComponent;
import de.jakob.lotm.attachments.HistoricalVoidComponent;
import de.jakob.lotm.attachments.MarionetteComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.fool.marionettes.goals.MarionetteMaxDistanceGoal;
import de.jakob.lotm.beyonders.abilities.fool.marionettes.goals.MarionetteMovementGoal;
import de.jakob.lotm.beyonders.abilities.fool.marionettes.goals.MarionetteTargetGoal;
import de.jakob.lotm.beyonders.abilities.fool.marionettes.goals.NonBeyonderMarionetteUseAbilityGoal;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.AvatarEntity;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.entity.goals.EntityLoadChunksGoal;
import de.jakob.lotm.entity.goals.SubordinateFollowGoal;
import de.jakob.lotm.entity.goals.SubordinateTargetGoal;
import de.jakob.lotm.entity.goals.SuordinateStayGoal;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AllyUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.subordinates.SubordinateComponent;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class DegenerationAbility extends SelectableAbility {

    private static final int PERSON_CLONE_TICKS = 20 * 60 * 5;   // 5 minutes
    private static final int AREA_DURATION_TICKS = 20 * 60;      // 60 seconds
    private static final double AREA_RADIUS = 25;
    private static final double TARGET_SEARCH_RADIUS = 20;
    private static final int AI_INTERVAL_TICKS = 10;
    private static final String CLONE_TAG = "lotm_degen_clone";
    private static final String FREED_TAG = "lotm_degen_freed";
    private static final String MASTER_TAG = "lotm_degen_master";
    private static final String MASTER_ONLY_TAG = "lotm_degen_master_only";
    // Same keys MaliceSeedAbility uses, so freed things and clones stop sharing the master's seeds
    private static final String MALICE_IDENTITY_KEY = "lotm_malice_identity";
    private static final String MALICE_SEEDS_KEY = "lotm_malice_seeds";
    private static final DustParticleOptions darkDust = new DustParticleOptions(new Vector3f(0.05f, 0f, 0.1f), 2f);
    public record CloneInfo(UUID originalUUID, long expireTick, boolean areaClone) {}
    public record FreedInfo(UUID formerMasterUUID, boolean hostileToMasterOnly) {}
    public static class Zone {
        final UUID casterUUID;
        final ServerLevel level;
        final Vec3 center;
        final long endTick;
        Zone(UUID casterUUID, ServerLevel level, Vec3 center, long endTick) {this.casterUUID = casterUUID;this.level = level;this.center = center;this.endTick = endTick;}
        boolean contains(Vec3 pos) {return pos.distanceToSqr(center) <= AREA_RADIUS * AREA_RADIUS;}
        AABB box() {return new AABB(center, center).inflate(AREA_RADIUS);}
    }
    private static final Map<UUID, CloneInfo> clones = new ConcurrentHashMap<>();
    private static final Map<UUID, FreedInfo> freed = new ConcurrentHashMap<>();
    private static final List<Zone> zones = Collections.synchronizedList(new ArrayList<>());
    private static int aiCounter = 0;
    public DegenerationAbility(String id) {
        super(id, 600);
        this.canBeCopied = false;
        this.canBeReplicated = false;
        this.canBeUsedInArtifact = false;
        this.cannotBeStolen = true;
        canBeShared = false;
    }
    @Override
    public Map<String, Integer> getRequirements() {return new HashMap<>(Map.of("abyss", 0));}
    @Override
    public float getSpiritualityCost() {return 800;}
    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.degeneration.person",
                "ability.lotmcraft.degeneration.area"};
    }
    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        switch (abilityIndex) {
            case 0 -> degeneratePerson(serverLevel, entity);
            case 1 -> degenerateArea(serverLevel, entity);
        }
    }
    private static boolean shouldClone(LivingEntity target) {return target instanceof ServerPlayer || BeyonderData.isBeyonder(target);}
    private static boolean isClone(Entity e) {return e.getPersistentData().getBoolean(CLONE_TAG);}
    // Degenerate Person
    private void degeneratePerson(ServerLevel level, LivingEntity caster) {
        LivingEntity target = AbilityUtil.getTargetEntity(caster, 25, 2.5f);
        if (target == null || target == caster || isClone(target)) {
            AbilityUtil.sendActionBar(caster, Component.literal("No target.").withColor(0xFFa3070c));
            return;
        }
        if (resistsDegeneration(target)) {
            AbilityUtil.sendActionBar(caster, Component.literal("The target resists degeneration.").withColor(0xFFa3070c));
            return;
        }
        MinecraftServer server = level.getServer();
        // Existing constructs break free and hunt their former master. Permanent.
        for (LivingEntity construct : getConstructs(server, target.getUUID())) {freeConstruct(server, construct, target.getUUID(), true);}
        spawnClone(level, target, level.getGameTime() + PERSON_CLONE_TICKS, false);
        ParticleUtil.spawnParticles(level, darkDust, target.position().add(0, 1, 0), 80, 1.5, 0);
        ParticleUtil.spawnParticles(level, ParticleTypes.SOUL, target.position().add(0, 1, 0), 40, 1.0, 1.0);
        level.playSound(null, BlockPos.containing(target.position()), SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.PLAYERS, 1.5f, 0.5f);
    }
    // Degenerate Area
    private void degenerateArea(ServerLevel level, LivingEntity caster) {
        MinecraftServer server = level.getServer();
        Vec3 center = caster.position();
        long end = level.getGameTime() + AREA_DURATION_TICKS;
        Zone zone = new Zone(caster.getUUID(), level, center, end);
        zones.add(zone);
        // Snapshot the masters before anything is freed. Constructs never get clones.
        List<LivingEntity> masters = new ArrayList<>();
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, zone.box(), x -> x.isAlive() && zone.contains(x.position()))) {
            if (e == caster || isClone(e) || getOwnerUUID(e) != null || resistsDegeneration(e)) continue;
            masters.add(e);
        }
        for (LivingEntity master : masters) {
            for (LivingEntity construct : getConstructs(server, master.getUUID())) {freeConstruct(server, construct, master.getUUID(), false);}
            if (shouldClone(master)) spawnClone(level, master, end, true);
        }
        for (int i = 0; i < 6; i++) {ParticleUtil.spawnParticles(level, darkDust, center.add(0, 0.5 + i * 0.5, 0), 120, AREA_RADIUS * 0.6, 0);}
        level.playSound(null, BlockPos.containing(center), SoundEvents.WARDEN_EMERGE, SoundSource.PLAYERS, 3f, 0.4f);
    }
    // Clones
    private static void spawnClone(ServerLevel level, LivingEntity original, long expireTick, boolean areaClone) {
        LivingEntity clone = createCloneEntity(level, original);
        resetMaliceIdentity(clone);
        clone.getPersistentData().putBoolean(CLONE_TAG, true);
        Vec3 pos = original.position().add((level.random.nextDouble() - 0.5) * 4, 0, (level.random.nextDouble() - 0.5) * 4);
        clone.moveTo(pos.x, pos.y, pos.z, original.getYRot(), original.getXRot());
        clone.setHealth(clone.getMaxHealth());
        if (clone instanceof Mob m) m.setPersistenceRequired();
        clones.put(clone.getUUID(), new CloneInfo(original.getUUID(), expireTick, areaClone));
        boolean added = level.addFreshEntity(clone);
        if (added) {
            healToFull(clone);
            ServerScheduler.scheduleDelayed(2, () -> healToFull(clone), level);
            ServerScheduler.scheduleDelayed(20, () -> healToFull(clone), level);
        }
        setHostileTarget(clone, original);
        ParticleUtil.spawnParticles(level, darkDust, clone.position().add(0, 1, 0), 40, 0.6, 0);
    }
    private static LivingEntity createCloneEntity(ServerLevel level, LivingEntity original) {
        if (original instanceof ServerPlayer player) {
            if (!BeyonderData.isBeyonder(player)) return null;
            BeyonderNPCEntity clone = new BeyonderNPCEntity(ModEntities.BEYONDER_NPC.get(), level, true, "", BeyonderData.getPathway(player), BeyonderData.getSequence(player), false, false);
            clone.setTargetPlayerUUID(player.getUUID()); // skin
            clone.setQuestId("");
            clone.getPersistentData().putBoolean("Initialized", true);
            return clone;
        }
        if (original instanceof BeyonderNPCEntity npc) {
            BeyonderNPCEntity clone = new BeyonderNPCEntity((EntityType<? extends BeyonderNPCEntity>) npc.getType(), level, true, npc.getSkinName(), BeyonderData.getPathway(npc), BeyonderData.getSequence(npc), false, false);
            CompoundTag tag = npc.saveWithoutId(new CompoundTag());
            tag.remove("UUID");
            tag.remove("skin");
            tag.remove("hostile");
            clone.load(tag);
            clone.setUUID(UUID.randomUUID());
            clone.setHostile(true);
            clone.setQuestId("");
            clone.getPersistentData().putBoolean("Initialized", true);
            clone.getPersistentData().remove(FREED_TAG);
            return clone;
        }
        Entity created = original.getType().create(level);
        if (!(created instanceof LivingEntity clone)) return null;
        CompoundTag tag = original.saveWithoutId(new CompoundTag());
        tag.remove("UUID");
        clone.load(tag);
        clone.setUUID(UUID.randomUUID());
        clone.getPersistentData().remove(FREED_TAG);
        return clone;
    }
    private static void dissolve(MinecraftServer server, Entity e) {
        if (e.level() instanceof ServerLevel sl) ParticleUtil.spawnParticles(sl, darkDust, e.position().add(0, 1, 0), 40, 0.6, 0);
        if (e instanceof LivingEntity clone) {for (LivingEntity c : getConstructs(server, clone.getUUID())) c.discard();}
        e.discard();
    }
    // Construct detection
    private static UUID getOwnerUUID(LivingEntity e) {
        CompoundTag data = e.getPersistentData();
        if (data.getBoolean(FREED_TAG) || data.getBoolean(CLONE_TAG)) return null;
        if (e instanceof AvatarEntity avatar) {
            UUID owner = avatar.getOriginalOwner();
            if (owner != null) return owner;
        }
        MarionetteComponent marionette = e.getData(ModAttachments.MARIONETTE_COMPONENT);
        if (marionette.isMarionette()) {try {return UUID.fromString(marionette.getControllerUUID());} catch (IllegalArgumentException ignored) {}}
        if (data.getBoolean("VoidSummoned") && data.hasUUID("VoidSummonOwner")) {return data.getUUID("VoidSummonOwner");}
        return getSubordinateOwner(e);
    }
    private static UUID getSubordinateOwner(LivingEntity e) {
        SubordinateComponent sc = e.getData(ModAttachments.SUBORDINATE_COMPONENT.get());
        if (!sc.isSubordinate()) return null;
        try {return UUID.fromString(sc.getControllerUUID());} catch (IllegalArgumentException ignored) {return null;}
    }
    private static List<LivingEntity> getConstructs(MinecraftServer server, UUID masterUUID) {
        List<LivingEntity> result = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity e : level.getAllEntities()) {if (e instanceof LivingEntity le && le.isAlive() && masterUUID.equals(getOwnerUUID(le))) result.add(le);}
        }
        return result;
    }
    // Freeing constructs
    private static void freeConstruct(MinecraftServer server, LivingEntity construct, UUID masterUUID, boolean masterOnly) {
        severControl(server, construct, masterUUID);
        resetMaliceIdentity(construct);
        clearAllies(server, construct, masterUUID);
        CompoundTag data = construct.getPersistentData();
        data.putBoolean(FREED_TAG, true);
        data.putUUID(MASTER_TAG, masterUUID);
        data.putBoolean(MASTER_ONLY_TAG, masterOnly);
        freed.put(construct.getUUID(), new FreedInfo(masterUUID, masterOnly));
        if (construct.level() instanceof ServerLevel sl) ParticleUtil.spawnParticles(sl, darkDust, construct.position().add(0, 1, 0), 25, 0.5, 0);
    }
    /** Permanently removes the construct from every registry that tracks it. */
    private static void severControl(MinecraftServer server, LivingEntity construct, UUID masterUUID) {
        Entity masterEntity = findEntity(server, masterUUID);
        // Marionette
        MarionetteComponent mc = construct.getData(ModAttachments.MARIONETTE_COMPONENT);
        if (mc.isMarionette()) {
            mc.setMarionette(false);
            mc.setControllerUUID("");
            mc.setShouldAttack(true);
            if (masterEntity != null) masterEntity.getData(ModAttachments.MARIONETTE_OWNER_COMPONENT).removeMarionette(construct.getUUID());
            if (construct instanceof Mob mob) {
                mob.goalSelector.removeAllGoals(g -> g instanceof MarionetteMovementGoal || g instanceof NonBeyonderMarionetteUseAbilityGoal || g instanceof MarionetteMaxDistanceGoal || g instanceof EntityLoadChunksGoal);
                mob.targetSelector.removeAllGoals(g -> g instanceof MarionetteTargetGoal);
            }
        }
        // Avatar
        if (construct instanceof AvatarEntity avatar) {
            if (masterEntity != null) {
                masterEntity.getData(ModAttachments.VIRTUAL_PERSONAS.get()).removeAvatar(avatar.getUUID());
                var split = masterEntity.getData(ModAttachments.ENVISION_SPLIT.get());
                if (split.contains(avatar)) split.removeAsAvatar(avatar.getUUID());
            }
            avatar.setOriginalOwner(null);
        }
        // Void summoned (HistoricalVoidSummoning)
        CompoundTag data = construct.getPersistentData();
        if (data.getBoolean("VoidSummoned") && data.hasUUID("VoidSummonOwner")) {
            long summonTime = data.getLong("VoidSummonTime");
            int cost = Math.max(1, data.getInt("VoidSlotCost"));
            ServerPlayer owner = server.getPlayerList().getPlayer(data.getUUID("VoidSummonOwner"));
            if (owner != null) {
                HistoricalVoidComponent hv = owner.getData(ModAttachments.HISTORICAL_VOID_COMPONENT.get());
                hv.summonedCount = Math.max(0, hv.summonedCount - cost);
                hv.activeSummonTimes.remove(summonTime);
            }
            data.putBoolean("VoidSummoned", false); // stops the expiry and spirituality-drain handlers
            data.remove("VoidSummonTime");
            data.remove("VoidSummonOwner");
            data.remove("VoidSlotCost");
            data.remove("VoidDrainPerSecond");
        }
        SubordinateComponent sc = construct.getData(ModAttachments.SUBORDINATE_COMPONENT.get());
        if (sc.isSubordinate()) {
            sc.setSubordinate(false);
            sc.setControllerUUID("");
            sc.setShouldAttack(true);
            if (construct instanceof Mob mob) {
                mob.goalSelector.removeAllGoals(g -> g instanceof SubordinateFollowGoal || g instanceof SuordinateStayGoal || g instanceof EntityLoadChunksGoal);
                mob.targetSelector.removeAllGoals(g -> g instanceof SubordinateTargetGoal);
            }
        }
    }
    private static void resetMaliceIdentity(LivingEntity entity) {
        CompoundTag data = entity.getPersistentData();
        data.putUUID(MALICE_IDENTITY_KEY, entity.getUUID());
        data.remove(MALICE_SEEDS_KEY);
    }
    private static void setHostileTarget(LivingEntity construct, LivingEntity victim) {
        if (construct instanceof BeyonderNPCEntity npc) {
            if (!npc.isHostile()) npc.setHostile(true);
            npc.setTarget(victim);
        } else if (construct instanceof Mob mob) {mob.setTarget(victim);}
    }
    // Tick
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        long now = server.overworld().getGameTime();
        synchronized (zones) {zones.removeIf(z -> now >= z.endTick);}
        if (++aiCounter < AI_INTERVAL_TICKS) return;
        aiCounter = 0;
        tickZones(server);
        tickClones(server, now);
        tickFreed(server);
    }
    private static void tickZones(MinecraftServer server) {
        List<Zone> snapshot;
        synchronized (zones) {snapshot = new ArrayList<>(zones);}
        for (Zone z : snapshot) {
            for (LivingEntity e : z.level.getEntitiesOfClass(LivingEntity.class, z.box(), x -> x.isAlive() && z.contains(x.position()))) {
                UUID owner = getOwnerUUID(e);
                if (owner == null || owner.equals(z.casterUUID)) continue;
                if (findEntity(server, owner) instanceof LivingEntity master && resistsDegeneration(master)) continue;
                freeConstruct(server, e, owner, false);
            }
        }
    }
    private static void tickClones(MinecraftServer server, long now) {
        for (Iterator<Map.Entry<UUID, CloneInfo>> it = clones.entrySet().iterator(); it.hasNext(); ) {
            var entry = it.next();
            CloneInfo info = entry.getValue();
            Entity clone = findEntity(server, entry.getKey());
            if (clone == null || !clone.isAlive()) {it.remove();continue;}
            if (now >= info.expireTick()) {it.remove();dissolve(server, clone);continue;}
            if (!(clone instanceof LivingEntity cl)) continue;
            Entity original = findEntity(server, info.originalUUID());
            if (original instanceof LivingEntity le && le.isAlive()) {
                if (!(cl instanceof Mob m) || m.getTarget() != le) setHostileTarget(cl, le);
            } else if (!info.areaClone()) {it.remove();dissolve(server, clone);}
        }
    }
    private static void tickFreed(MinecraftServer server) {
        for (Iterator<Map.Entry<UUID, FreedInfo>> it = freed.entrySet().iterator(); it.hasNext(); ) {
            var entry = it.next();
            Entity e = findEntity(server, entry.getKey());
            if (!(e instanceof LivingEntity construct) || !construct.isAlive()) {it.remove();continue;}
            FreedInfo info = entry.getValue();
            LivingEntity victim = info.hostileToMasterOnly() ? (findEntity(server, info.formerMasterUUID()) instanceof LivingEntity l ? l : null) : nearestLiving(construct);
            if (victim != null && victim.isAlive()) setHostileTarget(construct, victim);
        }
    }
    private static LivingEntity nearestLiving(LivingEntity self) {
        if (!(self.level() instanceof ServerLevel level)) return null;
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, self.getBoundingBox().inflate(TARGET_SEARCH_RADIUS), x -> x != self && x.isAlive())) {
            double d = e.distanceToSqr(self);
            if (d < bestDist) {bestDist = d;best = e;}
        }
        return best;
    }
    private static Entity findEntity(MinecraftServer server, UUID uuid) {
        for (ServerLevel level : server.getAllLevels()) {
            Entity e = level.getEntity(uuid);
            if (e != null) return e;
        }
        return null;
    }
    // Events
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel)) return;
        Entity entity = event.getEntity();
        if (isClone(entity) && !clones.containsKey(entity.getUUID())) {
            event.setCanceled(true);
            return;
        }
        CompoundTag data = entity.getPersistentData();
        if (data.getBoolean(FREED_TAG) && !freed.containsKey(entity.getUUID()) && data.hasUUID(MASTER_TAG)) {
            freed.put(entity.getUUID(), new FreedInfo(data.getUUID(MASTER_TAG), data.getBoolean(MASTER_ONLY_TAG)));
        }
    }
    private static void clearAllies(MinecraftServer server, LivingEntity construct, UUID masterUUID) {
        AllyComponent comp = construct.getData(ModAttachments.ALLY_COMPONENT.get());
        for (AllyComponent.AllyInfo info : new ArrayList<>(comp.allies())) {
            UUID allyUUID = info.uuid();
            if (findEntity(server, allyUUID) instanceof LivingEntity ally) AllyUtil.removeAllies(construct, ally, false);
            else AllyUtil.removeAllyOneWay(construct, allyUUID); // offline or unloaded ally
        }
        if (findEntity(server, masterUUID) instanceof LivingEntity master) AllyUtil.removeAllyOneWay(master, construct.getUUID());
    }
    private static boolean resistsDegeneration(LivingEntity target) {
        return BeyonderData.isBeyonder(target) && BeyonderData.getSequence(target) <= 4 && Objects.equals(BeyonderData.getPathway(target), "sun");
    }
    private static void healToFull(LivingEntity clone) {
        if (clone.isRemoved() || !clone.isAlive()) return;
        clone.setHealth(clone.getMaxHealth());
    }
    @SubscribeEvent
    public static void onCloneDrops(LivingDropsEvent event) {if (isClone(event.getEntity())) event.setCanceled(true);}
    @SubscribeEvent
    public static void onCloneXp(LivingExperienceDropEvent event) {if (isClone(event.getEntity())) event.setCanceled(true);}
}