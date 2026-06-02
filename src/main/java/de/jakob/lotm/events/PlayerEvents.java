package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.common.DivinationAbility;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.abilities.darkness.NightmareAbility;
import de.jakob.lotm.attachments.AbilityCooldownComponent;
import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.NewPlayerComponent;
import de.jakob.lotm.attachments.SacrificeRevertComponent;
import de.jakob.lotm.attachments.UniquenessComponent;
import de.jakob.lotm.entity.custom.ability_entities.darkness_pathway.ConcealedDomainEntity;
import de.jakob.lotm.entity.custom.uniqueness.UniquenessEntity;
import de.jakob.lotm.gamerule.ModGameRules;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.ResetClientEffectsPacket;
import de.jakob.lotm.network.packets.toClient.SyncGriefingGamerulePacket;
import de.jakob.lotm.potions.BeyonderCharacteristicItemHandler;
import de.jakob.lotm.potions.PotionRecipeItemHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.attachments.AllyComponent;
import de.jakob.lotm.util.helper.AllyUtil;
import de.jakob.lotm.util.helper.ExplodingFallingBlockHelper;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3f;


import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.LOTMCraft;




import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class PlayerEvents {

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DisabledAbilitiesComponent disabledAbilitiesComponent = player.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
            disabledAbilitiesComponent.enableAllAbilities();

            AbilityCooldownComponent abilityCooldownComponent = player.getData(ModAttachments.COOLDOWN_COMPONENT);
            abilityCooldownComponent.removeAllCooldowns();

            ToggleAbility.cleanUp(player.serverLevel(), player);
            DivinationAbility.cleanupOnLogout(player);

            // Clean up concealed domain entities
            ConcealedDomainEntity concealedDomainEntity = ConcealedDomainEntity.getActiveForOwner(player.getUUID());
            if(concealedDomainEntity != null) {
                concealedDomainEntity.discard();
            }

            if(BeyonderData.isBeyonder(player))
                BeyonderData.playerMap.addLastPosition(player);

            // Return any active souls to storage when the owner logs out.
            de.jakob.lotm.abilities.death.InternalUnderworldAbility.recallSoulsOnLogout(player);

            // Revert sacrifice upgrade if active when logging out
            SacrificeRevertComponent revert = player.getData(ModAttachments.SACRIFICE_REVERT_COMPONENT);
            if (revert.isActive()) {
                if (BeyonderData.isBeyonder(player)
                        && BeyonderData.getPathway(player).equals(revert.getPathway())
                        && BeyonderData.getSequence(player) == revert.getRevertToSequence() - 1) {
                    float digestion = revert.getSavedDigestion();
                    BeyonderData.setBeyonder(player, revert.getPathway(), revert.getRevertToSequence(), true, false, true, false);
                    BeyonderData.setDigestionProgress(player, digestion);
                    // No sync needed on logout — player reads NBT fresh on next login
                }
                revert.clear();
            }

            PacketHandler.sendToPlayer(player, new ResetClientEffectsPacket());
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Prune stale ally entries: remove any ally UUID whose owner no longer lists
            // this player back (e.g. the player was removed while they were offline).
            AllyComponent comp = player.getData(ModAttachments.ALLY_COMPONENT.get());
            for (String uuidStr : new java.util.HashSet<>(comp.allies())) {
                try {
                    java.util.UUID allyUUID = java.util.UUID.fromString(uuidStr);
                    ServerPlayer ally = player.getServer().getPlayerList().getPlayer(allyUUID);
                    if (ally != null && !AllyUtil.isAlly(ally, player.getUUID())) {
                        // Ally is online but doesn't have us listed — remove the stale entry
                        player.setData(ModAttachments.ALLY_COMPONENT.get(),
                                player.getData(ModAttachments.ALLY_COMPONENT.get()).removeAlly(allyUUID));
                    }
                    // If ally is offline we can't check their data, so leave it for the
                    // next time they log in and this same check runs on their side.
                } catch (IllegalArgumentException ignored) {}
            }

            PacketHandler.sendToPlayer(player, new SyncGriefingGamerulePacket(player.level().getGameRules().getBoolean(ModGameRules.ALLOW_GRIEFING)));

            // Fallback migration: if component migration produced only a minimal char list, prefer stored PlayerMap chars
            try {
                var component = player.getData(ModAttachments.BEYONDER_COMPONENT);
                Optional<de.jakob.lotm.util.playerMap.StoredData> stored = BeyonderData.playerMap != null ? BeyonderData.playerMap.get(player) : Optional.empty();
                if (stored.isPresent() && !stored.get().chars().isEmpty()) {
                    // Decide whether to prefer stored PlayerMap chars over migrated component chars.
                    int compSum = component.getCharacteristicList().stream().mapToInt(c -> c.stack()).sum();
                    long compNonDefault = component.getCharacteristicList().stream().filter(c -> c.stack() > 1).count();
                    int storedSum = stored.get().chars().stream().mapToInt(c -> c.stack()).sum();

                    boolean shouldApply = false;
                    // If component has no non-default stacks (all stacks == 1), prefer stored data
                    if (compNonDefault == 0 && storedSum > 0) shouldApply = true;
                    // Or if totals differ significantly (migration probably wrong)
                    if (!shouldApply && storedSum != compSum) shouldApply = true;

                    if (shouldApply) {
                        component.setCharacteristicList(new ArrayList<>(stored.get().chars()));
                        component.syncHighest();
                        PacketHandler.syncBeyonderDataToPlayer(player);
                        de.jakob.lotm.LOTMCraft.LOGGER.info("Applied PlayerMap fallback migration for player {} (compSum={}, storedSum={}, compNonDefault={})", player.getGameProfile().getName(), compSum, storedSum, compNonDefault);
                    }
                }
            } catch (Exception e) {
                de.jakob.lotm.LOTMCraft.LOGGER.warn("Error during BeyonderComponent fallback migration for {}", player.getGameProfile().getName(), e);
            }

            NewPlayerComponent component = player.getData(ModAttachments.BOOK_COMPONENT);
            if(!component.isHasReceivedNewPlayerPerks() && player.serverLevel().getGameRules().getBoolean(ModGameRules.SPAWN_WITH_STARTING_CHARACTERISTIC)) {
                player.addItem(new ItemStack(ModItems.GUIDING_BOOK.get()));

                String pathway = BeyonderData.implementedPathways.get(random.nextInt(BeyonderData.implementedPathways.size()));
                Item characteristic = BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence(pathway, 9);
                Item recipe = PotionRecipeItemHandler.selectRecipeOfPathwayAndSequence(pathway, 9);

                if(characteristic != null && recipe != null) {
                    player.addItem(new ItemStack(characteristic));
                    player.addItem(new ItemStack(recipe));
                }

                component.setHasReceivedNewPlayerPerks(true);
            }
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.isCanceled()) {
            return;
        }
        if (event.getEntity() instanceof ServerPlayer player) {
            de.jakob.lotm.abilities.death.InternalUnderworldAbility.despawnSoulsOnDeath(player);
            // Clear stored souls and announce freed seq0/seq1 slots.
            de.jakob.lotm.abilities.death.InternalUnderworldAbility.FreedSoulSlots freed =
                    de.jakob.lotm.abilities.death.InternalUnderworldAbility.clearStoredSoulsAndCollectFreedPaths(player);
            spawnFreedUniquenesses(player, freed.seq0Paths());
            if (!freed.seq0Paths().isEmpty() || !freed.seq1Paths().isEmpty()) {
                if (!freed.seq0Paths().isEmpty()) {
                    String message = "A True God slot has been opened: " + formatPathList(freed.seq0Paths());
                    player.getServer().getPlayerList().broadcastSystemMessage(
                        Component.literal(message).withStyle(ChatFormatting.GOLD), false);
                }
                if (!freed.seq1Paths().isEmpty()) {
                    int count = freed.seq1Paths().size();
                    String prefix;
                    if (count == 1) {
                        prefix = "An angel slot has been opened: ";
                    } else if (count == 2) {
                        prefix = "Multiple angel slots have been opened: ";
                    } else {
                        prefix = "All angel slots have opened: ";
                    }
                    String message = prefix + formatPathList(freed.seq1Paths());
                    player.getServer().getPlayerList().broadcastSystemMessage(
                        Component.literal(message).withStyle(ChatFormatting.GOLD), false);
                }
            }
        }
        if(!(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }

        ToggleAbility.cleanUp(level, event.getEntity());

    }

    private static final Random random = new Random();

    private static final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(.05f, 0, 0),
            1.5f
    );

    private static String formatPathList(Set<String> paths) {
        List<String> list = new ArrayList<>(paths);
        Collections.sort(list);
        return String.join(", ", list);
    }

    private static void spawnFreedUniquenesses(ServerPlayer player, Set<String> seq0Paths) {
        if (seq0Paths == null) return;
        ServerLevel level = player.serverLevel();
        Vec3 deathPos = player.position();

        Set<String> spawnPaths = new HashSet<>(seq0Paths);
        if (BeyonderData.isBeyonder(player) && BeyonderData.getSequence(player) == 0) {
            UniquenessComponent comp = player.getData(ModAttachments.UNIQUENESS_COMPONENT);
            if (!comp.hasUniqueness()) {
                String playerPathway = BeyonderData.getPathway(player);
                if (playerPathway != null && !playerPathway.isEmpty() && !"none".equals(playerPathway)) {
                    spawnPaths.add(playerPathway);
                }
            }
        }

        for (String pathway : spawnPaths) {
            if (pathway == null || pathway.isEmpty() || "none".equals(pathway)) continue;
            if (UniquenessEntity.existsInWorld(level, pathway)) continue;
            if (UniquenessEntity.anyPlayerHoldsUniqueness(level, pathway)) continue;

            Vec3 spawnPos = randomSpawnAround(deathPos, 20);
            UniquenessEntity.trySpawn(level, spawnPos, pathway);
        }
    }

    private static Vec3 randomSpawnAround(Vec3 origin, int radius) {
        double minDistance = Math.min(2.0, radius);
        double maxDistance = Math.max(minDistance, radius);
        double distance = minDistance + random.nextDouble() * (maxDistance - minDistance);
        double angle = random.nextDouble() * Math.PI * 2.0;
        double dx = Math.cos(angle) * distance;
        double dz = Math.sin(angle) * distance;
        return new Vec3(origin.x + dx, origin.y, origin.z + dz);
    }

    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        if(event.getEntity().level().isClientSide)
            return;

        if(DivinationAbility.dangerPremonitionActive.contains(event.getEntity().getUUID()) && random.nextFloat() < .1) {
            event.setCanceled(true);
            if(event.getEntity() instanceof ServerPlayer player) {
                Component actionBarText = Component.literal("Dodged Attack").withStyle(ChatFormatting.DARK_PURPLE);
                sendActionBar(player, actionBarText);
            }
        }
        if(NightmareAbility.hasActiveNightmare(event.getEntity())) {
            if(event.getAmount() >= event.getEntity().getHealth()) {
                event.setCanceled(true);
                event.getEntity().setHealth(event.getEntity().getMaxHealth());
                NightmareAbility.stopNightmare(event.getEntity().getUUID());
            }
        }
        Entity damager = event.getSource().getEntity();
        if(damager instanceof LivingEntity source && ((ToggleAbility) LOTMCraft.abilityHandler.getById("cull_ability")).isActiveForEntity(source)) {
            Level level = event.getEntity().level();
            if(!level.isClientSide) {
                ParticleUtil.spawnParticles((ServerLevel) level, dust, event.getEntity().getEyePosition().subtract(0, .4, 0), 40, .4, .8, .4, 0);
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            ExplodingFallingBlockHelper.tickExplodingBlocks(level);
        }
    }

    private static void sendActionBar(ServerPlayer player, Component message) {
        ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(message);
        player.connection.send(packet);





    }
}