package de.jakob.lotm.abilities.fool;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.AbilityUseEvent;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.entity.custom.LocationGraftingEntity;
import de.jakob.lotm.events.custom.TargetEntityEvent;
import de.jakob.lotm.events.custom.TargetLocationEvent;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.EntityLocation;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import oshi.util.tuples.Pair;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class GraftingAbility extends SelectableAbility {

    private static final HashMap<UUID, Location> graftingLocations = new HashMap<>();
    private static final HashMap<UUID, LivingEntity> graftingDamageEntities = new HashMap<>();
    private static final List<Pair<UUID, UUID>> graftingDamagePairs = new ArrayList<>();

    private static final HashMap<UUID, LivingEntity> graftingAbilitiesEntities = new HashMap<>();
    private static final List<Pair<UUID, UUID>> graftingAbilitiesPairs = new ArrayList<>();

    private static final HashMap<UUID, LivingEntity> graftingTargetsEntities = new HashMap<>();
    private static final List<Pair<UUID, Location>> graftingTargetsPairs = new ArrayList<>();

    // Maps source entity UUID to the caster UUID, for sequence resistance checks
    private static final HashMap<UUID, UUID> graftingDamageCasters = new HashMap<>();
    private static final HashMap<UUID, UUID> graftingAbilitiesCasters = new HashMap<>();


    public GraftingAbility(String id) {
        super(id, 1);

        canBeCopied = false;
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("fool", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1400;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.grafting.locations", "ability.lotmcraft.grafting.damage", "ability.lotmcraft.grafting.abilities", "ability.lotmcraft.grafting.change_target"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        switch (selectedAbility) {
            case 0 -> graftLocations(level, entity);
            case 1 -> graftDamage(level, entity);
            case 2 -> graftAbilities(level, entity);
            case 3 -> graftTarget(level, entity);
        }
    }

    private void graftTarget(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        int color = BeyonderData.pathwayInfos.get("fool").color();

        if(!graftingTargetsEntities.containsKey(entity.getUUID())) {
            LivingEntity targetEntity = AbilityUtil.getTargetEntity(entity, 30, 2);
            if(targetEntity == null) {
                AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.grafting.no_target").withColor(color));
                return;
            }

            UUID targetUUID = targetEntity.getUUID();
            if(graftingTargetsPairs.stream().anyMatch(pair -> pair.getA() == targetUUID)) {
                AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.grafting.already_grafted").withColor(color));
                return;
            }

            ParticleUtil.createParticleSpirals(serverLevel, ParticleTypes.WITCH, targetEntity.position(), 1.2, 1.2, 1.5, 1, 4, 30, 10, 1);

            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.grafting.selected", targetEntity.getName().getString()).withColor(color));

            graftingTargetsEntities.put(entity.getUUID(), targetEntity);
            return;
        }

        LivingEntity targetEntity = AbilityUtil.getTargetEntity(entity, 30, 2);
        LivingEntity graftingStartEntity = graftingTargetsEntities.get(entity.getUUID());

        if(graftingStartEntity == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.grafting.failed", targetEntity.getName().getString()).withColor(color));
            graftingTargetsEntities.remove(entity.getUUID());
            return;
        }

        if(targetEntity == graftingStartEntity) {
            graftingAbilitiesEntities.remove(entity.getUUID());
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.grafting.same_entity", targetEntity.getName().getString()).withColor(color));
            return;
        }

        Location targetLocation = targetEntity != null ? new EntityLocation(targetEntity) : new Location(AbilityUtil.getTargetLocation(entity, 30, 2), entity.level());

        ParticleUtil.createParticleSpirals(serverLevel, ParticleTypes.WITCH, targetLocation.getPosition(), 1.2, 1.2, 1.5, 1, 4, 30, 10, 1);

        graftingTargetsPairs.add(new Pair<>(graftingStartEntity.getUUID(), targetLocation));
        graftingTargetsEntities.remove(entity.getUUID());

        ServerScheduler.scheduleDelayed(20 * 30, () -> graftingTargetsPairs.removeIf(pair -> pair.getA() == graftingStartEntity.getUUID() || pair.getB() == targetLocation));
    }

    private void graftAbilities(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity targetEntity = AbilityUtil.getTargetEntity(entity, 30, 2);
        if(targetEntity == null) {
            targetEntity = entity;
        }

        UUID targetUUID = targetEntity.getUUID();
        int color = BeyonderData.pathwayInfos.get("fool").color();
        if(graftingAbilitiesPairs.stream().anyMatch(pair -> pair.getA() == targetUUID || pair.getB() == targetUUID)) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.grafting.already_grafted").withColor(color));
            return;
        }

        ParticleUtil.createParticleSpirals(serverLevel, ParticleTypes.WITCH, targetEntity.position(), 1.2, 1.2, 1.5, 1, 4, 30, 10, 1);

        AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.grafting.selected", targetEntity.getName().getString()).withColor(color));

        if(!graftingAbilitiesEntities.containsKey(entity.getUUID())) {
            graftingAbilitiesEntities.put(entity.getUUID(), targetEntity);
            return;
        }

        LivingEntity graftingStartEntity = graftingAbilitiesEntities.get(entity.getUUID());

        if(targetUUID == graftingStartEntity.getUUID()) {
            graftingAbilitiesEntities.remove(entity.getUUID());
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.grafting.same_entity", targetEntity.getName().getString()).withColor(color));
            return;
        }

        // Check if the source entity (graftingStartEntity) can resist ability grafting
        double failureChance = AbilityUtil.getSequenceFailureChance(entity, graftingStartEntity);
        if (ThreadLocalRandom.current().nextDouble() < failureChance) {
            graftingAbilitiesEntities.remove(entity.getUUID());
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.grafting.resisted").withColor(color));
            return;
        }

        UUID startUUID = graftingStartEntity.getUUID();
        graftingAbilitiesPairs.add(new Pair<>(startUUID, targetEntity.getUUID()));
        graftingAbilitiesCasters.put(startUUID, entity.getUUID());

        graftingAbilitiesEntities.remove(entity.getUUID());

        ServerScheduler.scheduleDelayed(20 * 30, () -> {
            graftingAbilitiesPairs.removeIf(pair -> startUUID.equals(pair.getA()) && targetUUID.equals(pair.getB()));
            graftingAbilitiesCasters.remove(startUUID);
        });
    }

    private void graftDamage(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity targetEntity = AbilityUtil.getTargetEntity(entity, 30, 2);
        if(targetEntity == null) {
            targetEntity = entity;
        }

        UUID targetUUID = targetEntity.getUUID();
        int color = BeyonderData.pathwayInfos.get("fool").color();
        if(graftingDamagePairs.stream().anyMatch(pair -> pair.getA() == targetUUID || pair.getB() == targetUUID)) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.grafting.already_grafted").withColor(color));
            return;
        }

        ParticleUtil.createParticleSpirals(serverLevel, ParticleTypes.WITCH, targetEntity.position(), 1.2, 1.2, 1.5, 1, 4, 30, 10, 1);

        AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.grafting.selected", targetEntity.getName().getString()).withColor(color));

        if(!graftingDamageEntities.containsKey(entity.getUUID())) {
            graftingDamageEntities.put(entity.getUUID(), targetEntity);
            return;
        }

        LivingEntity graftingStartEntity = graftingDamageEntities.get(entity.getUUID());

        if(targetUUID == graftingStartEntity.getUUID()) {
            graftingDamageEntities.remove(entity.getUUID());
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.grafting.same_entity", targetEntity.getName().getString()).withColor(color));
            return;
        }

        // Check if the source entity (graftingStartEntity) can resist the graft
        double failureChance = AbilityUtil.getSequenceFailureChance(entity, graftingStartEntity);
        if (ThreadLocalRandom.current().nextDouble() < failureChance) {
            graftingDamageEntities.remove(entity.getUUID());
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.grafting.resisted").withColor(color));
            return;
        }

        UUID startUUID = graftingStartEntity.getUUID();
        graftingDamagePairs.add(new Pair<>(startUUID, targetEntity.getUUID()));
        graftingDamageCasters.put(startUUID, entity.getUUID());

        graftingDamageEntities.remove(entity.getUUID());

        ServerScheduler.scheduleDelayed(20 * 30, () -> {
            graftingDamagePairs.removeIf(pair -> startUUID.equals(pair.getA()) && targetUUID.equals(pair.getB()));
            graftingDamageCasters.remove(startUUID);
        });
    }

    private void graftLocations(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if(entity.isShiftKeyDown()) {
            level.getEntitiesOfClass(LocationGraftingEntity.class, entity.getBoundingBox().inflate(20)).stream().min(Comparator.comparing(e -> e.distanceTo(entity))).ifPresent(Entity::discard);
            return;
        }

        Vec3 targetLocation = AbilityUtil.getTargetLocation(entity, 30, 2);
        if(targetLocation == null) {
            graftingLocations.remove(entity.getUUID());
            return;
        }

        ParticleUtil.createParticleSpirals(serverLevel, ParticleTypes.WITCH, targetLocation, 1.2, 1.2, 1.5, 1, 4, 30, 10, 1);

        int x = (int) targetLocation.x;
        int y = (int) targetLocation.y;
        int z = (int) targetLocation.z;

        int color = BeyonderData.pathwayInfos.get("fool").color();

        AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.grafting.selected", x + ", " + y + ", " + z).withColor(color));
        if(!graftingLocations.containsKey(entity.getUUID())) {
            graftingLocations.put(entity.getUUID(), new Location(targetLocation, level));
            return;
        }

        Location graftingStartLocation = graftingLocations.get(entity.getUUID());
        LocationGraftingEntity graftingEntity = new LocationGraftingEntity(level, graftingStartLocation.getPosition(), targetLocation, level.dimension());
        graftingStartLocation.getLevel().addFreshEntity(graftingEntity);

        graftingLocations.remove(entity.getUUID());
    }

    @Override
    public void onHold(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            level.getEntitiesOfClass(LocationGraftingEntity.class, entity.getBoundingBox().inflate(20)).forEach(e -> {
                ParticleUtil.spawnParticles((ClientLevel) level, ParticleTypes.WITCH, e.position(), 30, .6, .1, .6, 0);
                ParticleUtil.spawnParticles((ClientLevel) level, ParticleTypes.END_ROD, e.position(), 5, .6, .1, .6, 0);
            });
        }
    }

    @Override
    public void nextAbility(LivingEntity entity) {
        super.nextAbility(entity);

        graftingLocations.remove(entity.getUUID());
        graftingDamageEntities.remove(entity.getUUID());
    }

    @Override
    public void previousAbility(LivingEntity entity) {
        super.previousAbility(entity);

        graftingLocations.remove(entity.getUUID());
        graftingDamageEntities.remove(entity.getUUID());
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingDamage(LivingIncomingDamageEvent event) {
        LivingEntity hurt = event.getEntity();
        if(!(hurt.level() instanceof ServerLevel serverLevel)) return;
        if(graftingDamagePairs.stream().anyMatch(pair -> pair.getA() == hurt.getUUID())) {
            UUID otherEntityUUID = graftingDamagePairs.stream().filter(pair -> pair.getA() == hurt.getUUID()).findFirst().map(Pair::getB).orElse(null);
            if(otherEntityUUID == null) return;

            LivingEntity otherEntity = (LivingEntity) serverLevel.getEntity(otherEntityUUID);
            if(otherEntity == null) return;

            // Look up the caster to compare sequences with the hurt entity
            UUID casterUUID = graftingDamageCasters.get(hurt.getUUID());
            LivingEntity caster = casterUUID != null ? (LivingEntity) serverLevel.getEntity(casterUUID) : null;

            double resistance = AbilityUtil.getSequenceResistanceFactor(caster, hurt);
            float redirected = (float)(event.getAmount() * (1.0 - resistance));

            event.setCanceled(true);
            if (redirected > 0) {
                otherEntity.hurt(serverLevel.damageSources().generic(), redirected);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onAbilityUse(TargetLocationEvent event) {
        LivingEntity source = event.getSourceEntity();
        if(!(source.level() instanceof ServerLevel serverLevel)) return;
        if(graftingTargetsPairs.stream().anyMatch(pair -> pair.getA() == source.getUUID())) {
            Location loc = graftingTargetsPairs.stream().filter(pair -> pair.getA() == source.getUUID()).findFirst().map(Pair::getB).orElse(null);
            if(loc == null || loc.getLevel() != source.level()) {
                graftingTargetsPairs.removeIf(pair -> pair.getA() == source.getUUID());
                return;
            }

            event.setTargetLocation(loc.getPosition());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onAbilityUse(TargetEntityEvent event) {
        LivingEntity source = event.getSourceEntity();
        if(!(source.level() instanceof ServerLevel serverLevel)) return;
        if(graftingTargetsPairs.stream().anyMatch(pair -> pair.getA() == source.getUUID())) {
            Location loc = graftingTargetsPairs.stream().filter(pair -> pair.getA() == source.getUUID()).findFirst().map(Pair::getB).orElse(null);
            if(loc == null || loc.getLevel() != source.level()) {
                graftingTargetsPairs.removeIf(pair -> pair.getA() == source.getUUID());
                return;
            }

            if(!(loc instanceof EntityLocation entityLocation) || !(entityLocation.getEntity() instanceof LivingEntity newTarget)) {
                return;
            }

            event.setTargetEntity(newTarget);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onAbilityUse(AbilityUseEvent event) {
        LivingEntity used = event.getEntity();
        if(!(used.level() instanceof ServerLevel serverLevel)) return;
        if(graftingAbilitiesPairs.stream().anyMatch(pair -> pair.getA() == used.getUUID())) {
            UUID otherEntityUUID = graftingAbilitiesPairs.stream().filter(pair -> pair.getA() == used.getUUID()).findFirst().map(Pair::getB).orElse(null);
            if(otherEntityUUID == null) return;

            LivingEntity otherEntity = (LivingEntity) serverLevel.getEntity(otherEntityUUID);
            if(otherEntity == null) return;

            // Check if the used entity can resist per-event (resistance = chance graft breaks)
            UUID casterUUID = graftingAbilitiesCasters.get(used.getUUID());
            LivingEntity caster = casterUUID != null ? (LivingEntity) serverLevel.getEntity(casterUUID) : null;
            double resistance = AbilityUtil.getSequenceResistanceFactor(caster, used);
            if (ThreadLocalRandom.current().nextDouble() < resistance) {
                return; // Entity resisted the ability graft this time
            }

            event.setEntity(otherEntity);
        }
    }
}
