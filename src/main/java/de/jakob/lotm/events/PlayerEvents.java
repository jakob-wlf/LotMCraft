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
import de.jakob.lotm.entity.custom.ability_entities.darkness_pathway.ConcealedDomainEntity;
import de.jakob.lotm.gamerule.ModGameRules;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.network.PacketHandler;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3f;

import java.util.Random;

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
        if (event.getEntity() instanceof ServerPlayer player) {
            de.jakob.lotm.abilities.death.InternalUnderworldAbility.despawnSoulsOnDeath(player);
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