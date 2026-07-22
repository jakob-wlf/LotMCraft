package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.*;
import de.jakob.lotm.beyonders.abilities.visionary.DreamTraversalAbility;
import de.jakob.lotm.beyonders.abilities.visionary.PsychologicalInvisibilityAbility;
import de.jakob.lotm.beyonders.artifacts.SealedArtifactData;
import de.jakob.lotm.beyonders.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.beyonders.potions.BeyonderCharacteristicItemHandler;
import de.jakob.lotm.beyonders.potions.BeyonderPotion;
import de.jakob.lotm.beyonders.sefirah.SefirahHandler;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.data.ModDataComponents;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.gamerule.ModGameRules;
import de.jakob.lotm.item.PotionIngredient;
import de.jakob.lotm.item.custom.BlasphemyCardItem;
import de.jakob.lotm.item.custom.BlasphemySlateHalfItem;
import de.jakob.lotm.item.custom.BlasphemySlateItem;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncKillCountPacket;
import de.jakob.lotm.network.packets.toClient.SyncPsychologicalInvisibilityPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.PureIdealismUtil;
import de.jakob.lotm.util.helper.TeamUtils;
import de.jakob.lotm.util.playerMap.Characteristic;
import de.jakob.lotm.util.playerMap.StoredData;
import de.jakob.lotm.util.playerMap.StoredDataBuilder;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;


import java.util.*;

import static de.jakob.lotm.util.BeyonderData.getSequence;
import static de.jakob.lotm.util.BeyonderData.playerMap;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class BeyonderEventHandler {

    @SubscribeEvent
    public static void onPlayerJoinWorld(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // Convert legacy nbt tags to component data to preserve data through code changes, then remove legacy tags
            convertLegacyNBT(serverPlayer);

            playerMap.onPlayerUUIDChange(serverPlayer);

            if (!playerMap.contains(serverPlayer)) {
                playerMap.put(serverPlayer);
            } else {
                StoredData data = playerMap.get(serverPlayer).get();

                // Only restore from map if player has NO beyonder data (data loss scenario)
                // Or when marked to do so by server admin
                if (!BeyonderData.isBeyonder(serverPlayer) || data.modified()) {
                    BeyonderData.setBeyonder(serverPlayer, data.pathway(), data.sequence());
                    SefirahHandler.claimSefirot(serverPlayer, data.claimedSefirot());
                    playerMap.markModified(serverPlayer, false);

                } else if (playerMap.isDiffPathSeq(serverPlayer)) {
                    // If they have data but it differs, update the map to match
                    playerMap.put(serverPlayer);
                }
            }

            ParasitationComponent parasitationComponent = serverPlayer.getData(ModAttachments.PARASITE_COMPONENT);
            parasitationComponent.setParasited(false);
            parasitationComponent.setParasiteUUID(null);

            VirtualPersonaComponent personaComponent = serverPlayer.getData(ModAttachments.VIRTUAL_PERSONAS);
            personaComponent.onJoin((ServerLevel) serverPlayer.level(), serverPlayer.getName().getString());

            var splitComponent = serverPlayer.getData(ModAttachments.ENVISION_SPLIT.get());
            splitComponent.onJoin((ServerLevel) serverPlayer.level());

            serverPlayer.addEffect(new MobEffectInstance(ModEffects.CONCEALMENT, 20 * 5, 99));
            BeyonderData.recalculateCharStackModifiers(serverPlayer);
            serverPlayer.getData(ModAttachments.LUCK_COMPONENT.get()).setLuck(0);
            PacketHandler.sendToPlayer(serverPlayer, new SyncPsychologicalInvisibilityPacket(PsychologicalInvisibilityAbility.invisiblePlayers));

            PacketHandler.syncBeyonderDataToPlayer(serverPlayer);
        }
    }

    private static void convertLegacyNBT(ServerPlayer serverPlayer) {
        if (!serverPlayer.getPersistentData().contains("beyonder_pathway") || !serverPlayer.getPersistentData().contains("beyonder_sequence"))
            return;
        String oldPathway = serverPlayer.getPersistentData().getString("beyonder_pathway");
        int oldSequence = serverPlayer.getPersistentData().getInt("beyonder_sequence");
        float digestionsProgress = serverPlayer.getPersistentData().contains("beyonder_digestion_progress") ? serverPlayer.getPersistentData().getFloat("beyonder_digestion_progress") : 0f;
        boolean griefingEnabled = serverPlayer.getPersistentData().contains("beyonder_griefing_enabled") || !serverPlayer.getPersistentData().getBoolean("beyonder_griefing_enabled");

        // Migrating existing data, not becoming a beyonder — must not trigger the acting cap
        de.jakob.lotm.beyonders.acting.ActingCapHelper.skipNextCapApplication = true;
        try {
            BeyonderData.setBeyonder(serverPlayer, oldPathway, oldSequence, true, true, true, true);
        } finally {
            de.jakob.lotm.beyonders.acting.ActingCapHelper.skipNextCapApplication = false;
        }
        BeyonderData.setDigestionProgress(serverPlayer, digestionsProgress);
        BeyonderData.setGriefingEnabled(serverPlayer, griefingEnabled);

        serverPlayer.getPersistentData().remove("beyonder_pathway");
        serverPlayer.getPersistentData().remove("beyonder_sequence");
        serverPlayer.getPersistentData().remove("beyonder_digestion_progress");
        serverPlayer.getPersistentData().remove("beyonder_griefing_enabled");
    }

    @SubscribeEvent
    public static void onTotemUsed(LivingUseTotemEvent event) {
        LivingEntity entity = event.getEntity();

        if (BeyonderData.isBeyonder(entity) && !event.getEntity().level().getGameRules().getBoolean(ModGameRules.ALLOW_TOTEMS)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // Re-sync data when changing dimensions
            PacketHandler.syncBeyonderDataToPlayer(serverPlayer);
            BeyonderData.recalculateCharStackModifiers(serverPlayer);
            // Re-sync corruption so client-side HUD/shaders don't reset to zero
            float corruption = serverPlayer.getData(ModAttachments.CORRUPTION_COMPONENT).getCorruption();
            PacketHandler.sendToPlayer(serverPlayer, new de.jakob.lotm.network.packets.toClient.SyncCorruptionPacket(corruption, serverPlayer.getId()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity source) {
            if (!AbilityUtil.mayDamage(source, event.getEntity())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamageLiving(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity source)) return;
        LivingEntity target = event.getEntity();

        if (!BeyonderData.isBeyonder(target)) return;

        int targetSeq = BeyonderData.getSequence(target);
        int sourceSeq = BeyonderData.getSequence(source);

        if (targetSeq >= sourceSeq) return;

        float baseMultiplier = 1f / (1f + (sourceSeq - targetSeq) * 0.175f);
        if (AbilityUtil.isTargetSignificantlyStronger(sourceSeq, targetSeq)) {
            baseMultiplier *= 0.35f;
        }

        event.setAmount(event.getAmount() * baseMultiplier);
    }

    // Disable Flight while in combat
    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Post event) {
        if (event.getSource().getEntity() instanceof LivingEntity source) {
            if (BeyonderData.isBeyonder(source) && event.getEntity().level().getGameRules().getBoolean(ModGameRules.DISABLE_FLIGHT_IN_COMBAT)) {
                DisabledFlightComponent flightData = event.getEntity().getData(ModAttachments.FLIGHT_DISABLE_COMPONENT);
                flightData.setCooldownTicks(20 * 20);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // Re-sync data on respawn
            PacketHandler.syncBeyonderDataToPlayer(serverPlayer);
            BeyonderData.recalculateCharStackModifiers(serverPlayer);
            serverPlayer.getData(ModAttachments.LUCK_COMPONENT.get()).setLuck(0);
            // Re-sync corruption so client-side HUD/shaders don't reset to zero
            float corruption = serverPlayer.getData(ModAttachments.CORRUPTION_COMPONENT).getCorruption();
            PacketHandler.sendToPlayer(serverPlayer, new de.jakob.lotm.network.packets.toClient.SyncCorruptionPacket(corruption, serverPlayer.getId()));
            // Clear sacrifice bar if it was active when the player died
            if (serverPlayer.getPersistentData().getBoolean("sacrifice_bar_clear")) {
                serverPlayer.getPersistentData().remove("sacrifice_bar_clear");
                PacketHandler.sendToPlayer(serverPlayer, new de.jakob.lotm.network.packets.toClient.SyncSacrificeDurationPacket(0));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            // Client side — clear cache
            ClientBeyonderCache.removePlayer(event.getEntity().getUUID());
            return;
        }
        if (player.getServer() == null) return;

        DreamTraversalAbility.cancelHide((ServerLevel) player.level(), player);

        TeamComponent team = player.getData(ModAttachments.TEAM_COMPONENT.get());

        if (!team.isInTeam() && team.memberCount() > 0) {
            // Player is the leader logging out — schedule a clear to online members after disconnect completes
            java.util.List<String> memberUUIDs = new java.util.ArrayList<>(team.memberUUIDs());
            ServerScheduler.scheduleDelayed(1, () -> {
                for (String memberUUID : memberUUIDs) {
                    ServerPlayer member = player.getServer().getPlayerList().getPlayer(
                            java.util.UUID.fromString(memberUUID));
                    if (member != null) {
                        PacketHandler.sendToPlayer(member, new de.jakob.lotm.network.packets.toClient.SyncSharedAbilitiesDataPacket(
                                "", new java.util.ArrayList<>(), new java.util.ArrayList<>(), new java.util.HashMap<>(), 0, 0));
                    }
                }
            });
        } else if (team.isInTeam()) {
            // Player is a member logging out — schedule a re-sync from leader after disconnect completes
            String leaderUUID = team.leaderUUID();
            ServerScheduler.scheduleDelayed(1, () -> {
                ServerPlayer leader = player.getServer().getPlayerList().getPlayer(
                        java.util.UUID.fromString(leaderUUID));
                if (leader != null) {
                    TeamUtils.syncToTeam(leader);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerDrops(LivingDropsEvent event) {
        // sorry nihil i have to mess with your method :)
        // cancel the drop of items completely for summoned entities
        if (event.getEntity().getPersistentData().contains("VoidSummoned")) {
            event.setCanceled(true);
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        //if (!BeyonderData.isBeyonder(player)) return;
        if (!player.serverLevel().getGameRules().getBoolean(ModGameRules.REGRESS_SEQUENCE_ON_DEATH)) return;

        // onDeath (LivingDeathEvent) already handled regression and cleared the revert component.
        // Here we only need to drop the correct characteristic item.
        // The sequence to drop is the one onDeath regressed FROM, stored temporarily in NBT.
        int dropSequence = player.getPersistentData().contains("sacrifice_drop_sequence")
                ? player.getPersistentData().getInt("sacrifice_drop_sequence")
                : BeyonderData.getSequence(player);
        player.getPersistentData().remove("sacrifice_drop_sequence");

        if (playerMap.get(player).isEmpty()) return;

        var envisionedSplitComponent = player.getData(ModAttachments.ENVISION_SPLIT.get());

        var data = playerMap.get(player).get();

        BeyonderCharacteristicItem charItem = BeyonderCharacteristicItemHandler
                .selectCharacteristicOfPathwayAndSequence(BeyonderData.getPathway(player), dropSequence);

        BeyonderData.setBeyonder(player, data.pathway(), data.sequence(), true, false, false, false);

        if (charItem == null) return;

        int current = BeyonderData.getCharList(player).stream()
                .filter(c -> c.sequence() == dropSequence && c.pathway().equals(BeyonderData.getPathway(player))).mapToInt(Characteristic::stack).findFirst().orElse(0);

        BeyonderData.setCharacteristic(player,
                current != 0 ?  current-1 : 0,
                dropSequence,
                true,
                BeyonderData.getPathway(player)
        );


        if(PureIdealismUtil.died.containsKey(player.getUUID())){
            return;
        }
        else if(envisionedSplitComponent.isEnvisioned()){
            envisionedSplitComponent.setEnvisioned(false);
            return;
        }
        else {
            ItemEntity itemEntity = new ItemEntity(
                    player.level(),
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    new ItemStack(charItem.asItem())
            );

            event.getDrops().add(itemEntity);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {

            VirtualPersonaComponent personaComponent = player.getData(ModAttachments.VIRTUAL_PERSONAS);
            personaComponent.onDeath((ServerLevel) player.level(), player.getName().getString());

            var source = event.getSource().getEntity();
            if (source != null) {
                LOTMCraft.LOGGER.info("{} was killed by {} with {}", player.getGameProfile().getName(), event.getSource().getEntity().getName(), event.getSource());
            } else {
                LOTMCraft.LOGGER.info("{} was killed with {}", player.getGameProfile().getName(), event.getSource());
            }

            if (!BeyonderData.isBeyonder(player)) return;
            if (playerMap.get(player).isEmpty()) return;
            if (!player.level().getGameRules().getBoolean(ModGameRules.REGRESS_SEQUENCE_ON_DEATH)
                    && !player.getData(ModAttachments.ENVISION_SPLIT.get()).isEnvisioned()) {
                BeyonderData.recalculateCharStackModifiers(player);
                return;
            }
            if (!BeyonderData.isBeyonder(player)) return;
            if (playerMap.get(player).isEmpty()) return;

            // Great Old One: only a seq-0 beyonder can end their transcendence
            if (de.jakob.lotm.beyonders.sefirah.GreatOldOneManager.isGreatOldOne(player)) {
                Entity killer = event.getSource().getEntity();
                if (killer instanceof ServerPlayer killerPlayer
                        && BeyonderData.getSequence(killerPlayer) == 0) {
                    de.jakob.lotm.beyonders.sefirah.GreatOldOneManager.revert(player);
                    BeyonderData.setDigestionProgress(player, 1.0f);
                }
                // All other deaths are ignored for GOO — no regression, no sefirot unclaim
                return;
            }

            StoredData data = playerMap.get(player).get();
            StoredData regressed;

            boolean shouldRegress = player.level().getGameRules().getBoolean(ModGameRules.REGRESS_SEQUENCE_ON_DEATH);

            if (shouldRegress) {
                SacrificeRevertComponent revert = player.getData(ModAttachments.SACRIFICE_REVERT_COMPONENT);
                if (revert.isActive()) {
                    int originalSeq = revert.getRevertToSequence();
                    // Store the original sequence so onPlayerDrops drops the right characteristic
                    player.getPersistentData().putInt("sacrifice_drop_sequence", originalSeq);
                    player.getPersistentData().putBoolean("sacrifice_bar_clear", true);
                    revert.clear();
                    // Regress from the original sequence, not the temporary sacrificed one
                    StoredData dataAtOriginalSeq = StoredData.builder.copyFrom(data).sequence(originalSeq).build();
                    regressed = dataAtOriginalSeq.regressSeq(false);
                } else {
                    boolean respectStack = player.level().getGameRules().getBoolean(ModGameRules.LOOSE_CHAR_ON_REGRESSION);
                    regressed = data.regressSeq(respectStack);
                }
            } else {

                StoredDataBuilder b = StoredData.builder.copyFrom(data).clearCharList();
                for (Characteristic c : data.chars()) {
                    if (c.pathway().equals(data.pathway()) && c.sequence() >= data.sequence() && c.sequence() < 10) {
                        b.characteristic(1, c.sequence(), c.pathway());
                    }
                }
                regressed = b.build();
            }

            playerMap.put(player, regressed);
            // We DO NOT update BeyonderComponent here, as onPlayerDrops needs to compare component (old) with playerMap (new)

            BeyonderData.setDigestionProgress(player, 1.0f);
            player.getData(ModAttachments.LUCK_COMPONENT.get()).setLuck(0);
            SefirahHandler.unclaimSefirot(player);

            if (Objects.equals(regressed.sequence(), LOTMCraft.NON_BEYONDER_SEQ)) {
                ClientBeyonderCache.removePlayer(player.getUUID());
            } else {
                ClientBeyonderCache.updateData(player.getUUID(), regressed.pathway(), regressed.sequence(),
                        0.0f, false, true, 1.0f, 0);
            }
        }
    }

    @SubscribeEvent
    public static void onMobKilled(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer) return; // only track mob kills, not player kills
        Entity causeEntity = event.getSource().getEntity();
        if (causeEntity == null) causeEntity = event.getSource().getDirectEntity();
        if (!(causeEntity instanceof ServerPlayer player)) return;
        if (!BeyonderData.isBeyonder(player)) return;
        if (!BeyonderData.getPathway(player).equals("red_priest")) return;
        if (BeyonderData.getSequence(player) > 3) return;

        KillCountComponent killCount = player.getData(ModAttachments.KILL_COUNT_COMPONENT);
        killCount.increment();
        PacketHandler.sendToPlayer(player, new SyncKillCountPacket(killCount.getKillCount()));
    }

    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (event.getEntity().level().isClientSide()) return;

        var player = event.getEntity();

        if (player.isCreative())
            return;

        Objects.requireNonNull(player.getServer()).execute(() -> {
            var container = event.getContainer();

            for (Slot slot : container.slots) {
                ItemStack stack = slot.getItem();
                if (stack.isEmpty()) continue;

                Item item = stack.getItem();

                // Blasphemy cards and slates are crafting ingredients — never lock them out of crafting tables
                if (container instanceof CraftingMenu &&
                        (item instanceof BlasphemyCardItem
                                || item instanceof BlasphemySlateHalfItem
                                || item instanceof BlasphemySlateItem)) {
                    continue;
                }

                if (item instanceof PotionIngredient obj) {
                    for (var path : obj.getPathways()) {
                        if (!BeyonderData.playerMap.check(path, obj.getSequence())) {
                            slot.set(ItemStack.EMPTY);
                            break;
                        }
                    }
                } else if (item instanceof BeyonderPotion potion) {
                    if (!BeyonderData.playerMap.check(
                            potion.getPathway(), potion.getSequence())) {
                        slot.set(ItemStack.EMPTY);
                    }
                } else if (item instanceof BeyonderCharacteristicItem cha) {
                    if (!BeyonderData.playerMap.check(
                            cha.getPathway(), cha.getSequence())) {
                        slot.set(ItemStack.EMPTY);
                    }
                } else {
                    SealedArtifactData data = stack.get(ModDataComponents.SEALED_ARTIFACT_DATA.get());

                    boolean valid = true, allowed = true, noNegativesAllowed = true;
                    if (data != null) {
                        valid = playerMap.check(data.pathway(), data.sequence());
                        allowed = player.level().getGameRules().getBoolean(ModGameRules.ALLOW_ARTIFACTS);

                        noNegativesAllowed = !player.level().getGameRules().getBoolean(ModGameRules.
                                ALLOW_ARTIFACTS_WITH_NO_NEGATIVES) && data.negativeEffect().isEmpty();
                    }

                    if (data != null && (!valid || !allowed || noNegativesAllowed)) {
                        slot.set(ItemStack.EMPTY);
                    }
                }
            }

            container.broadcastChanges();
        });
    }

    /**
     * Sun Pathway seq ≤ 3: hits reduce the victim's digestion.
     * Direct hit (attacker == direct entity): 3% base, ±1% per sequence difference.
     * Indirect hit (projectile / AoE): 0.5% flat.
     * If digestion hits 0, each hit has a 10% chance to regress the victim by 1 sequence
     * and give the attacker the corresponding characteristic item.
     */
    @SubscribeEvent
    public static void onSunHitDigestion(LivingDamageEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;

        // Attacker must be a Sun Pathway Beyonder at seq 3 or stronger
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        if (!BeyonderData.isBeyonder(attacker)) return;

        int attackerSeq;
        if (BeyonderData.getPathway(attacker).equals("sun")) {
            attackerSeq = BeyonderData.getSequence(attacker);
            if (attackerSeq > 3) return;
        } else {
            // Allow shared Sun abilities: attacker must have a Sun team member (seq <= 3) who contributed an ability.
            if (!(attacker instanceof ServerPlayer attackerPlayer)) return;
            attackerSeq = getSunContributorSeq(attackerPlayer);
            if (attackerSeq < 0) return;
        }

        // Victim must be a Beyonder Player with digestion
        LivingEntity victim = event.getEntity();
        if (!(victim instanceof Player victimPlayer)) return;
        if (!BeyonderData.isBeyonder(victim)) return;
        if (victim.level().isClientSide()) return;

        // Sacrifice ability protects the victim from digestion drain and regression while active
        if (victim instanceof ServerPlayer victimSp
                && victimSp.getData(ModAttachments.SACRIFICE_REVERT_COMPONENT).isActive()) return;

        int victimSeq = BeyonderData.getSequence(victim);

        // Indirect = ticking AoEs (PURIFICATION_INDIRECT). Everything else — melee, projectiles,
        // spawned entities — counts as direct (PURIFICATION or any other damage type).
        boolean isDirect = !event.getSource().is(ModDamageTypes.PURIFICATION_INDIRECT);

        // seqDiff > 0 means attacker is stronger (lower seq number), < 0 means weaker

        if (victimSeq <= 2) return;

        int seqDiff = victimSeq - attackerSeq;

        float digestionDrain;
        if (isDirect) {
            // Base 0.3%, +0.1% per level attacker is stronger, -0.1% per level attacker is weaker, floor 0.1%
            digestionDrain = Math.max(0.001f, 0.003f + seqDiff * 0.001f);
        } else {
            // Base 0.05%, +0.01% per level attacker is stronger, -0.001% per level attacker is weaker, floor 0.01%
            digestionDrain = Math.max(0.0001f, 0.0005f + seqDiff * 0.0001f);
        }

        float currentDigestion = BeyonderData.getDigestionProgress(victimPlayer);
        float newDigestion = Math.max(0f, currentDigestion - digestionDrain);
        BeyonderData.setDigestionProgress(victimPlayer, newDigestion);
        if (victim instanceof ServerPlayer sp) {
            PacketHandler.syncBeyonderDataToPlayer(sp);
        }

        // If digestion is fully drained, 10% chance to regress victim and reward attacker
        if (newDigestion <= 0f && new Random().nextFloat() < 0.01f) {
            // Capture pathway before regression changes it — the dropped characteristic belongs to the old pathway/seq
            String pathwayBeforeRegress = BeyonderData.getPathway(victim);
            // Check if victim has an extra characteristic stack at their current sequence
            boolean hasExtraStack = BeyonderData.getCurrentCharacteristicCount(victim) > 1;

            if (hasExtraStack) {
                // Consume one extra stack instead of desequencing
                BeyonderData.setCharacteristic(victim, BeyonderData.getCurrentCharacteristicCount(victim) - 1, getSequence(victim), true, BeyonderData.getPathway(victim));
            } else {
                // No stack — desequence the victim, using regressSeq so domain-switched players restore to their previous pathway
                if (victim instanceof ServerPlayer sp && BeyonderData.playerMap.get(sp).isPresent()) {
                    StoredData regressed = BeyonderData.playerMap.get(sp).get().regressSeq();
                    BeyonderData.playerMap.put(sp, regressed);
                    BeyonderData.setBeyonder(victim, regressed.pathway(), regressed.sequence());
                } else {
                    BeyonderData.setBeyonder(victim, BeyonderData.getPathway(victim), victimSeq + 1);
                }
            }

            // Always give the attacker the corresponding characteristic item (not for void-summoned puppets or players possessing one)
            if (!victim.getPersistentData().getBoolean("VoidSummoned")) {
                BeyonderCharacteristicItem charItem = BeyonderCharacteristicItemHandler
                        .selectCharacteristicOfPathwayAndSequence(pathwayBeforeRegress, victimSeq);
                if (charItem != null && attacker instanceof Player attackerPlayer) {
                    attackerPlayer.getInventory().add(new ItemStack(charItem.asItem()));
                }
            }

            // Either way, reset digestion to full so the victim isn't immediately vulnerable again
            victimPlayer.getData(ModAttachments.BEYONDER_COMPONENT).setSpirituality(1);
            if (victim instanceof ServerPlayer sp) PacketHandler.syncBeyonderDataToPlayer(sp);
        }
    }

    private static float getDigestionDrain(int victimSeq, int attackerSeq, boolean isDirect) {
        int seqDiff = victimSeq - attackerSeq;

        float digestionDrain;
        if (isDirect) {
            // Base 3%, +1% per level attacker is stronger, -1% per level attacker is weaker, floor 1%
            digestionDrain = Math.max(0.01f, 0.03f + seqDiff * 0.01f);
        } else {
            // Base 0.5%, +0.1% per level attacker is stronger, -0.1% per level attacker is weaker, floor 0.1%
            digestionDrain = Math.max(0.001f, 0.005f + seqDiff * 0.001f);
        }
        return digestionDrain;
    }

    /**
     * Returns the sequence of the strongest Sun contributor (seq <= 3) sharing abilities with the given player,
     * or -1 if no such contributor exists.
     */
    private static int getSunContributorSeq(ServerPlayer player) {
        if (player.getServer() == null) return -1;
        TeamComponent team = player.getData(ModAttachments.TEAM_COMPONENT.get());

        // Collect all member UUIDs to check — if player is a team member, check other members + leader;
        // if player is the leader, check their members.
        java.util.List<String> toCheck = new java.util.ArrayList<>();
        if (team.isInTeam()) {
            // player is a member — the leader UUID and other members contributed abilities accessible to the leader
            toCheck.add(team.leaderUUID());
            ServerPlayer leader = player.getServer().getPlayerList().getPlayer(
                    java.util.UUID.fromString(team.leaderUUID()));
            if (leader != null) {
                toCheck.addAll(leader.getData(ModAttachments.TEAM_COMPONENT.get()).memberUUIDs());
            }
        } else {
            toCheck.addAll(team.memberUUIDs());
        }

        int best = -1;
        for (String uuid : toCheck) {
            ServerPlayer member = player.getServer().getPlayerList().getPlayer(java.util.UUID.fromString(uuid));
            if (member == null) continue;
            if (!BeyonderData.isBeyonder(member)) continue;
            if (!BeyonderData.getPathway(member).equals("sun")) continue;
            int seq = BeyonderData.getSequence(member);
            if (seq > 3) continue;
            // Check that this member has actually contributed at least one ability to the team
            SharedAbilitiesComponent shared = member.getData(ModAttachments.SHARED_ABILITIES_COMPONENT.get());
            String leaderUUID = team.isInTeam() ? team.leaderUUID() : player.getStringUUID();
            if (shared.getContributions(leaderUUID).isEmpty()) continue;
            if (best < 0 || seq < best) best = seq;
        }
        return best;
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();

        if (!(victim instanceof Player)) return;

        if (source.getEntity() instanceof ServerPlayer player) {
            if (player.level().isClientSide) return;

            if (!BeyonderData.isBeyonder(player) || !BeyonderData.isBeyonder(victim)) return;

            BeyonderData.recalculateCharStackModifiers(player);

            float diff = BeyonderData.getSequence(player) - BeyonderData.getSequence(victim);

            if (diff >= 0) {
                BeyonderData.digest(player, (0.01f + (diff * 0.1f)), false);
            }

            victim.addEffect(new MobEffectInstance(ModEffects.CONCEALMENT, 20 * 5, 99, false, false));

            victim.getData(ModAttachments.LUCK_COMPONENT).setLuck(0);
        }
    }

    @SubscribeEvent
    public static void onTravel(EntityTravelToDimensionEvent event) {
        if(!(event.getEntity() instanceof LivingEntity entity)) return;

        Level level = entity.level();
        if(level.isClientSide()) return;
        if(!level.getGameRules().getBoolean(ModGameRules.SEQUENCE_DIMENSION_LOCK)) return;

        ResourceKey<Level> target = event.getDimension();

        int seq = BeyonderData.getSequence(entity);
        String path =  BeyonderData.getPathway(entity);


        if (target == Level.NETHER && seq > 7){
            event.setCanceled(true);
        }
        else if (target == Level.END){
            if(path.equals("door") && seq > 3)
                event.setCanceled(true);
            else if(seq > 2)
                event.setCanceled(true);
        }
    }
}