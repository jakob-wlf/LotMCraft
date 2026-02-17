package de.jakob.lotm.abilities.error;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.AbilityUseEvent;
import de.jakob.lotm.data.ModDataComponents;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.lang.reflect.AccessFlag;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class LoopHoleCreationAbility extends Ability {

    // Track active loopholes: loophole ID -> LoopholeData
    private static final Map<UUID, LoopholeData> activeLoopholes = new ConcurrentHashMap<>();

    // Track which entities are in which loophole (one per entity)
    private static final Map<UUID, UUID> entityToLoophole = new ConcurrentHashMap<>();

    public LoopHoleCreationAbility(String id) {
        super(id, 3.5f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("error", 3));
    }

    @Override
    public float getSpiritualityCost() {
        return 1200;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 40, 2);
        UUID loopholeId = UUID.randomUUID();

        if(entity instanceof ServerPlayer serverPlayer) {
            EffectManager.playEffect(EffectManager.Effect.LOOPHOLE, targetLoc.x, targetLoc.y, targetLoc.z, serverPlayer);
        }

        // Register the loophole
        LoopholeData loopholeData = new LoopholeData(
                loopholeId,
                entity.getUUID(),
                targetLoc,
                3.0, // radius
                serverLevel,
                System.currentTimeMillis() + (20 * 14 * 50) // 14 seconds in milliseconds
        );
        activeLoopholes.put(loopholeId, loopholeData);

        ServerScheduler.scheduleForDuration(0, 2, 20 * 14, () -> {
            // Update entities in loophole
            updateEntitiesInLoophole(loopholeData);

            // Teleport entities to loophole center
            AbilityUtil.getNearbyEntities(entity, serverLevel, targetLoc, 3).forEach(e -> {
                e.teleportTo(targetLoc.x, targetLoc.y, targetLoc.z);
            });
        });

        // Clean up after loophole expires
        ServerScheduler.scheduleDelayed(20 * 14, () -> {
            removeLoophole(loopholeId);
        });
    }

    private static void updateEntitiesInLoophole(LoopholeData loopholeData) {
        if (loopholeData.level == null) return;

        List<LivingEntity> entitiesInRange = AbilityUtil.getNearbyEntities(
                null,
                loopholeData.level,
                loopholeData.center,
                loopholeData.radius
        );

        // Remove entities that left the loophole
        entityToLoophole.entrySet().removeIf(entry -> {
            if (entry.getValue().equals(loopholeData.id)) {
                boolean stillInside = entitiesInRange.stream()
                        .anyMatch(e -> e.getUUID().equals(entry.getKey()));
                return !stillInside;
            }
            return false;
        });

        // Add entities that entered the loophole
        for (LivingEntity entity : entitiesInRange) {
            UUID entityId = entity.getUUID();

            // Only add if not already in another loophole
            if (!entityToLoophole.containsKey(entityId)) {
                entityToLoophole.put(entityId, loopholeData.id);
            }
        }
    }

    private static void removeLoophole(UUID loopholeId) {
        activeLoopholes.remove(loopholeId);
        // Remove all entities that were in this loophole
        entityToLoophole.entrySet().removeIf(entry -> entry.getValue().equals(loopholeId));
    }

    @SubscribeEvent
    public static void onAbilityUse(AbilityUseEvent event) {
        LivingEntity user = event.getEntity();
        if (user == null) return;

        UUID entityId = user.getUUID();

        // Check if entity is in a loophole
        UUID loopholeId = entityToLoophole.get(entityId);
        if (loopholeId == null) return;

        LoopholeData loopholeData = activeLoopholes.get(loopholeId);
        if (loopholeData == null) {
            // Loophole expired, clean up
            entityToLoophole.remove(entityId);
            return;
        }

        // Check if loophole is still active
        if (System.currentTimeMillis() > loopholeData.expirationTime) {
            removeLoophole(loopholeId);
            return;
        }

        // Get the loophole creator
        LivingEntity creator = loopholeData.getCreator();
        if (creator == null || !creator.isAlive()) {
            // Creator is gone, let ability proceed normally
            return;
        }

        // Don't intercept if the user IS the creator
        if (entityId.equals(loopholeData.creatorId)) {
            return;
        }

        // Intercept the ability - cancel it for the original user
        event.setCanceled(true);

        // Have the creator cast the ability instead
        Ability ability = event.getAbility();
        if (ability != null && user.level() instanceof ServerLevel serverLevel && ability.canBeUsedByNPC) {
            // Use the creator as the caster but potentially keep original targeting
            ability.useAbility(serverLevel, creator, false, false, true);
        }
    }

    // Data class to store loophole information
    private static class LoopholeData {
        final UUID id;
        final UUID creatorId;
        final Vec3 center;
        final double radius;
        final ServerLevel level;
        final long expirationTime;

        LoopholeData(UUID id, UUID creatorId, Vec3 center, double radius, ServerLevel level, long expirationTime) {
            this.id = id;
            this.creatorId = creatorId;
            this.center = center;
            this.radius = radius;
            this.level = level;
            this.expirationTime = expirationTime;
        }

        LivingEntity getCreator() {
            if (level == null) return null;

            Entity entity = level.getEntity(creatorId);
            if (entity instanceof LivingEntity livingEntity) {
                return livingEntity;
            }
            return null;
        }
    }

    private void stealAbilities(LivingEntity entity, LivingEntity target) {
        if(!BeyonderData.isBeyonder(target)) {
            return;
        }

//        ArrayList<AbilityItem> stealableAbilities = new ArrayList<>(AbilityItemHandler.ITEMS.getEntries().stream().filter(abilityEntry -> {
//            if(!(abilityEntry.get() instanceof AbilityItem abilityItem) || abilityEntry.get() instanceof ToggleAbilityItem) return false;
//            if(!abilityItem.canBeCopied) return false;
//            if(BeyonderData.isSpecificAbilityDisabled(target, abilityItem.getDescriptionId())) return false;
//
//            return abilityItem.canUse(target, true);
//        }).map(abilityEntry -> (AbilityItem) abilityEntry.get()).toList());
//
//        if(stealableAbilities.isEmpty()) {
//            return;
//        }
//
//        if(AbilityUtil.isTargetSignificantlyStronger(entity, target)) {
//            return;
//        }
//
//        if(doesTheftFail(BeyonderData.getSequence(entity), BeyonderData.getSequence(target))) {
//            return;
//        }
//
//        if(entity instanceof ServerPlayer serverPlayer)  {
//            EffectManager.playEffect(EffectManager.Effect.ABILITY_THEFT, target.position().x, target.position().y + target.getEyeHeight(), target.position().z, serverPlayer);
//        }
//
//        List<AbilityItem> stolenItems = new ArrayList<>();
//        int abilityCount = 3;
//        int abilityUses = 4;
//        for(int i = 0; i < abilityCount; i++) {
//            if(stealableAbilities.isEmpty()) {
//                break;
//            }
//            int index = random.nextInt(stealableAbilities.size());
//            AbilityItem stolenAbility = stealableAbilities.get(index);
//            stealableAbilities.remove(index);
//            stolenItems.add(stolenAbility);
//            BeyonderData.disableSpecificAbilityWithTimeLimit(target, "ability_theft_disable", stolenAbility.getDescriptionId(), 120 * 1000L);
//        }
//
//        for(AbilityItem stolenItem : stolenItems) {
//            ItemStack stolenStack = new ItemStack(stolenItem);
//            stolenStack.set(ModDataComponents.ABILITY_USES, abilityUses);
//            stolenStack.set(ModDataComponents.IS_STOLEN, true);
//
//
//            if(entity instanceof Player player && !player.getInventory().add(stolenStack)) {
//                player.drop(stolenStack, false);
//            }
//
//        }
    }

    private boolean doesTheftFail(int userSeq, int targetSeq) {
        if (targetSeq > userSeq) {
            return false;
        }

        int difference = userSeq - targetSeq;

        double baseFailPerStep = 0.15;

        double failChance = difference * baseFailPerStep;

        failChance = Math.min(Math.max(failChance, 0.0), 0.95);

        return random.nextDouble() < failChance;
    }
}
