package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.common.CogitationAbility;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.attachments.*;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.PathwayInfos;
import de.jakob.lotm.util.playerMap.Characteristic;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@EventBusSubscriber
public class CorruptionEventHandler {

    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if (entity.level().isClientSide || entity.tickCount % 20 != 0) {
            return;
        }

        handleCorruptionGain(entity);
        handleCorruptionEffects(entity);
    }

    private static void handleCorruptionGain(LivingEntity entity) {
        CorruptionComponent corruptionComp = entity.getData(ModAttachments.CORRUPTION_COMPONENT);
        BeyonderComponent beyonderComp = entity.getData(ModAttachments.BEYONDER_COMPONENT);
        CorruptedPlayerComponent corruptedComp = entity.getData(ModAttachments.CORRUPTED_PLAYER_COMPONENT);

        if(entity.level() instanceof ServerLevel serverLevel) {
            Entity npc = serverLevel.getEntity(corruptedComp.getNpcUUID());
            if(npc != null) {
                if (corruptedComp.isFullyCorrupted() && corruptedComp.getNpcUUID() != null && entity.distanceTo(npc) > 30) {
                    entity.teleportTo(npc.getX(), npc.getY(), npc.getZ());
                }
            }
        }

        // Players exempted from corruption leakage gain nothing
        //if (corruptionComp.isLeakageExempt()) return;

        // Global leakage override (set by River Authority owner)
        //if (entity instanceof ServerPlayer sp && sp.server != null) {
        //    if (de.jakob.lotm.attachments.DeathImprintData.get(sp.server).isGlobalLeakageOff()) return;
        //}

        // Original sefirot owner inside their own dimension is shielded from all corruption gain
        if (entity instanceof ServerPlayer sp && sp.server != null) {
            SefirotData sd = SefirotData.get(sp.server);
            String ownedSefirot = sd.getClaimedSefirot(sp.getUUID());
            if (ownedSefirot != null && !ownedSefirot.isEmpty()) {
                java.util.UUID firstOwner = sd.getFirstOwner(ownedSefirot);
                if (firstOwner != null && firstOwner.equals(sp.getUUID())) {
                    net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dim =
                            de.jakob.lotm.beyonders.sefirah.SefirahHandler.getSefirotDimensionKey(ownedSefirot);
                    if (dim != null && sp.level().dimension().equals(dim)) {
                        return;
                    }
                }
            }
        }

        String currentPathway = beyonderComp.getPathway();
        int currentSequence = beyonderComp.getSequence();

        // Check for Cogitation
        Ability cogitation = LOTMCraft.abilityHandler.getById("cogitation_ability");
        boolean isCogitating = false;
        if (cogitation instanceof CogitationAbility toggleCogitation) {
            if (toggleCogitation.isActiveForEntity(entity)) {
                isCogitating = true;
            }
        }

        if (isCogitating) {
            // Cogitation decreases corruption very slightly
            corruptionComp.increaseCorruptionAndSync(-0.0001f, entity);
            return;
        }

        float totalGain = 0;
        float digestionProgress = beyonderComp.getDigestionProgress(); // 0 to 1

        PathwayInfos currentPathwayInfo = BeyonderData.pathwayInfos.get(currentPathway);
        List<String> neighboring = currentPathwayInfo != null ? Arrays.asList(currentPathwayInfo.neighboringPathways()) : List.of();

        for (Characteristic characteristic : beyonderComp.getCharacteristicList()) {
            String charPathway = characteristic.pathway();
            int charSeq = characteristic.sequence();
            int charStack = characteristic.stack();

            // We consider the "main" characteristics as the one belonging to the current pathway and current sequence.
            // Everything else is "extra".
            // Actually, if you are a sequence 5 fool, you should have characteristics for fool 9, 8, 7, 6, 5.
            // If you have more than 1 of fool 5, it's extra.
            // If you have fool 4, it's extra (and likely dangerous).
            
            int expectedStack = (charPathway.equals(currentPathway)) ? 1 : 0;
            int extraStack = Math.max(0, charStack - expectedStack);

            if (extraStack > 0) {
                float baseGain = 0.00005f * extraStack * ((float) (10 - charSeq) /10); // Base gain per extra characteristic

                if(currentSequence <= 0){
                    baseGain *= 0.5F;
                }


                if (charPathway.equals(currentPathway)) {
                    // Same pathway, extra characteristic
                    totalGain += baseGain;
                } else if (neighboring.contains(charPathway)) {
                    // Neighboring pathway
                    totalGain += baseGain * 2.0f;
                } else {
                    // Non-neighboring pathway
                    totalGain += baseGain * 10.0f;
                }
            }
        }

        if (totalGain > 0) {
            // Scales with digestion but never fully goes away.
            // At 100% digestion (1.0), gain is reduced but still present.
            // Let's say at 100% digestion it's reduced by 50%.
            float digestionMultiplier = 1.0f - (digestionProgress * 0.6f);
            corruptionComp.increaseCorruptionAndSync(totalGain * digestionMultiplier, entity);
        }
    }

    private static void handleCorruptionEffects(LivingEntity entity) {
        CorruptionComponent corruptionComp = entity.getData(ModAttachments.CORRUPTION_COMPONENT);
        CorruptedPlayerComponent corruptedComp = entity.getData(ModAttachments.CORRUPTED_PLAYER_COMPONENT);
        float corruption = corruptionComp.getCorruption();
        int corruptionValue = (int) (corruption * 100);

        if (corruptionValue <= 20) {
            return;
        }

        Random random = new Random();

        // Passive sanity loss
        if (entity.tickCount % 20 == 0 && !corruptedComp.isFullyCorrupted()) {
            float sanityLoss = corruption * 0.01f; // Up to 0.01% per second at 100% corruption
            SanityComponent sanityComp = entity.getData(ModAttachments.SANITY_COMPONENT);
            sanityComp.decreaseSanityAndSync(sanityLoss, entity);
        }

        // Status effects
        if (corruptionValue >= 20) {
            if (random.nextInt(1000) < corruptionValue) {
                //entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, corruptionValue / 20));
            }
        }
        if (corruptionValue >= 40) {
            if (random.nextInt(1000) < corruptionValue) {
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 10, 1));
            }
            if (random.nextInt(1000) < corruptionValue) {
                //entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
            }
        }
        if (corruptionValue >= 60) {
            if (random.nextInt(1000) < corruptionValue) {
                entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 1, 1));
            }
        }
        if (corruptionValue >= 80) {
            if (random.nextInt(1000) < corruptionValue) {
                entity.addEffect(new MobEffectInstance(ModEffects.MUTATED, 10, 2));
            }
        }

        

        if (corruptionValue < 50) {
            if (entity instanceof ServerPlayer player) {
                revertFullCorruption(player);
            }
        }

        if (corruptionValue >= 100) {
            // If this player is a non-original sefirot owner, reclaim the sefirot for the original owner
            if (entity instanceof ServerPlayer sp && sp.server != null) {
                SefirotData sd = SefirotData.get(sp.server);
                String ownedSef = sd.getClaimedSefirot(sp.getUUID());
                if (ownedSef != null && !ownedSef.isEmpty()) {
                    java.util.UUID firstOwner = sd.getFirstOwner(ownedSef);
                    if (firstOwner != null && !firstOwner.equals(sp.getUUID()) && sd.getMentalImprint(ownedSef) > 0) {
                        de.jakob.lotm.beyonders.sefirah.SefirotImprintEventHandler.tryReclaimForOriginalOwner(
                                ownedSef, firstOwner, sp, sp.server);
                    }
                }
            }
            handleFullCorruption(entity);
        }
    }

    private static void revertFullCorruption(ServerPlayer player) {
        CorruptedPlayerComponent corruptedComp = player.getData(ModAttachments.CORRUPTED_PLAYER_COMPONENT);
        if (corruptedComp.isFullyCorrupted()) {
            corruptedComp.setFullyCorrupted(false);

            if (player.level() instanceof ServerLevel serverLevel) {
                UUID npcUUID = corruptedComp.getNpcUUID();
                if (npcUUID != null) {
                    net.minecraft.world.entity.Entity npc = serverLevel.getEntity(npcUUID);
                    if (npc instanceof BeyonderNPCEntity) {
                        npc.discard();
                    }
                }
            }

            corruptedComp.setNpcUUID(null);
            player.setGameMode(GameType.SURVIVAL);

            player.sendSystemMessage(Component.literal("Your corruption has decreased, and you have regained control!"));
        }
    }

    private static void handleFullCorruption(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        if (entity instanceof ServerPlayer player) {
            CorruptedPlayerComponent corruptedComp = player.getData(ModAttachments.CORRUPTED_PLAYER_COMPONENT);
            if (!corruptedComp.isFullyCorrupted()) {
                corruptedComp.setFullyCorrupted(true);
                
                BeyonderComponent beyonderComp = player.getData(ModAttachments.BEYONDER_COMPONENT);
                String pathway = beyonderComp.getPathway();
                int sequence = beyonderComp.getSequence();
                
                // Spawn the Beyonder NPC
                BeyonderNPCEntity rogueBeyonder = new BeyonderNPCEntity(ModEntities.BEYONDER_NPC.get(), serverLevel, true, "amon", pathway, sequence, true, false, false);
                rogueBeyonder.setPos(player.getX(), player.getY(), player.getZ());
                rogueBeyonder.setCustomName(Component.literal(player.getName().getString()));

                rogueBeyonder.setCustomNameVisible(true);
                
                serverLevel.addFreshEntity(rogueBeyonder);
                rogueBeyonder.setPersistentNPC(true);
                corruptedComp.setNpcUUID(rogueBeyonder.getUUID());
                
                // Set sanity to full
                SanityComponent sanityComp = player.getData(ModAttachments.SANITY_COMPONENT);
                sanityComp.setSanityAndSync(1.0f, player);
                
                // Set player to spectator and notify

                player.setGameMode(GameType.SPECTATOR);

                player.setCamera(rogueBeyonder);
                player.sendSystemMessage(Component.literal("You have lost control and become a rogue beyonder!"));
            }
            return;
        }

        // For non-players, just damage them for now or some other effect
        entity.hurt(ModDamageTypes.source(entity.level(), ModDamageTypes.LOOSING_CONTROL), 2.0f);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) return;

        // If the entity that died is a BeyonderNPCEntity, check if it was a corrupted player's NPC
        if (event.getEntity() instanceof BeyonderNPCEntity npc) {
            UUID npcUUID = npc.getUUID();
            ServerLevel level = (ServerLevel) npc.level();
            for (ServerPlayer player : level.players()) {
                CorruptedPlayerComponent corruptedComp = player.getData(ModAttachments.CORRUPTED_PLAYER_COMPONENT);
                if (corruptedComp.isFullyCorrupted() && npcUUID.equals(corruptedComp.getNpcUUID())) {
                    // Kill the player
                    player.hurt(ModDamageTypes.source(player.level(), ModDamageTypes.LOOSING_CONTROL), Float.MAX_VALUE);
                    corruptedComp.setFullyCorrupted(false);
                    corruptedComp.setNpcUUID(null);
                    player.setGameMode(GameType.SURVIVAL); // Reset game mode so they can respawn normally
                }
            }
        }
    }
}
