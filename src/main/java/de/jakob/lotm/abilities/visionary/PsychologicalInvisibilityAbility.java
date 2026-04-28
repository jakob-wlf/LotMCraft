package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.network.packets.toClient.SyncPsychologicalInvisibilityPacket;
import de.jakob.lotm.rendering.DecryptionRenderLayer;
import de.jakob.lotm.rendering.SpiritVisionOverlayRenderer;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class PsychologicalInvisibilityAbility extends ToggleAbility {
    public static final HashMap<UUID, Integer> invisiblePlayers = new HashMap<>();
    public static HashMap<UUID, Integer> invisiblePlayersClient = new HashMap<>();
    public static final HashMap<UUID, Integer> hits = new HashMap<>();

    public static final HashMap<UUID, Integer> finalInvisiblePlayers = new HashMap<>();

    public PsychologicalInvisibilityAbility(String id) {
        super(id);
        canBeCopied = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 6));
    }

    @Override
    public float getSpiritualityCost() {
        return 13;
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level instanceof ServerLevel serverLevel) {
            if (!invisiblePlayers.containsKey(entity.getUUID()))
                cancel(serverLevel, entity);
        }
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (!invisiblePlayers.containsKey(entity.getUUID())) {
            add(entity, AbilityUtil.getSeqWithArt(entity, this));
        }
    }

    private static void removeOrAddName(LivingEntity entity){
        if(entity instanceof ServerPlayer targetPlayer) {
            AttributeInstance attribute = targetPlayer.getAttribute(NeoForgeMod.NAMETAG_DISTANCE);
            if (attribute != null) {
                if (attribute.getValue() == 0) {
                    attribute.setBaseValue(64);

                    if (targetPlayer.getServer() != null) {
                        targetPlayer.getServer().getPlayerList().broadcastAll(
                                new ClientboundPlayerInfoUpdatePacket(
                                        EnumSet.of(
                                                ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,
                                                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED,
                                                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY,
                                                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE,
                                                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME
                                        ),
                                        List.of(targetPlayer)
                                )
                        );
                    }
                } else {
                    attribute.setBaseValue(0);

                    if (targetPlayer.getServer() != null) {
                        targetPlayer.getServer().getPlayerList().broadcastAll(
                                new ClientboundPlayerInfoRemovePacket(List.of(targetPlayer.getUUID()))
                        );
                    }
                }
            }
        }
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        remove(entity);
        clearArtifactScaling(entity);
    }

    private static void add(LivingEntity entity, int seq) {
        invisiblePlayers.put(entity.getUUID(), seq);
        hits.put(entity.getUUID(), 0);

        finalInvisiblePlayers.putAll(invisiblePlayers);

        removeOrAddName(entity);

        PacketHandler.sendToAllPlayers(new SyncPsychologicalInvisibilityPacket(finalInvisiblePlayers));
        entity.setInvisible(true);
    }

    private static void remove(LivingEntity entity) {
        invisiblePlayers.remove(entity.getUUID());
        hits.remove(entity.getUUID());

        finalInvisiblePlayers.remove(entity.getUUID());

        removeOrAddName(entity);

        PacketHandler.sendToAllPlayers(new SyncPsychologicalInvisibilityPacket(finalInvisiblePlayers));
        entity.setInvisible(false);
    }

    public static void addInvisFromOtherSkills(LivingEntity entity, int seq){
        if(!invisiblePlayers.containsKey(entity.getUUID())) {
            finalInvisiblePlayers.put(entity.getUUID(), seq);

            PacketHandler.sendToAllPlayers(new SyncPsychologicalInvisibilityPacket(finalInvisiblePlayers));
            entity.setInvisible(true);
        }
    }

    public static void removeInvisFromOtherSkills(LivingEntity entity){
        if(!invisiblePlayers.containsKey(entity.getUUID())) {
            finalInvisiblePlayers.remove(entity.getUUID());

            PacketHandler.sendToAllPlayers(new SyncPsychologicalInvisibilityPacket(finalInvisiblePlayers));
            entity.setInvisible(false);
        }
    }

    public static int getHitsBySeq(int seq) {
        return switch (seq) {
            case 6 -> 1;
            case 5 -> 2;
            case 4 -> 10;
            case 3 -> 15;
            case 2 -> 25;
            case 1 -> 40;
            case 0 -> 80;
            default -> 1;
        };
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();

        if (invisiblePlayers.containsKey(player.getUUID())) {
            int seq = invisiblePlayers.get(player.getUUID());

            if (seq > 2) {
                remove(player);
                return;
            }

            BeyonderData.incrementSpirituality(player, -1000f);
        }
    }

    @SubscribeEvent
    public static void onEntityTickPre(EntityTickEvent.Post event) {
        if(event.getEntity() instanceof LivingEntity entity) {
            int tickCount = entity.tickCount;

            if (invisiblePlayers.containsKey(entity.getUUID())){
                if(tickCount % 200 == 0){
                    if(hits.get(entity.getUUID()) != 0)
                        hits.put(entity.getUUID(), hits.get(entity.getUUID()) - 1);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        //was hit
        var entity = event.getEntity();

        if (invisiblePlayers.containsKey(entity.getUUID())) {
            hits.put(entity.getUUID(), hits.get(entity.getUUID()) + 1);

            if (hits.get(entity.getUUID()) >= getHitsBySeq(invisiblePlayers.get(entity.getUUID()))) {
                remove(entity);
            }
        }

        //hit someone
        if (event.getSource().getEntity() instanceof LivingEntity player) {
            var source = event.getSource();

            if (!(source.is(ModDamageTypes.BEYONDER_GENERIC)) || !(source.is(ModDamageTypes.LOOSING_CONTROL))) {
                if (invisiblePlayers.containsKey(player.getUUID())) {
                    remove(player);
                }
            }
        }

    }

    @SubscribeEvent
    public static void onLivingTarget(LivingChangeTargetEvent event) {
        if (event.getNewAboutToBeSetTarget() != null
                && (invisiblePlayers.containsKey(event.getNewAboutToBeSetTarget().getUUID()))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();

        if (invisiblePlayersClient.containsKey(player.getUUID())) {
            var clientPlayer = ClientHandler.getPlayer();

            if (clientPlayer == player) return;

            if (clientPlayer != null) {
                if (ClientBeyonderCache.getPathway(clientPlayer.getUUID()).equals("visionary") &&
                        ClientBeyonderCache.getSequence(clientPlayer.getUUID()) < invisiblePlayersClient.get(player.getUUID()))
                    return;

                if (DecryptionRenderLayer.activeDecryption.contains(clientPlayer.getUUID()) ||
                        SpiritVisionOverlayRenderer.entitiesLookedAt.containsKey(clientPlayer.getUUID())) {
                    if (AbilityUtil.isTargetSignificantlyWeaker(ClientBeyonderCache.getSequence(clientPlayer.getUUID()),
                            invisiblePlayersClient.get(player.getUUID())))
                        return;
                }
            }

            player.setInvisible(true);
            player.setGlowingTag(false);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();

        if (invisiblePlayersClient.containsKey(player.getUUID())) {
            var clientPlayer = ClientHandler.getPlayer();

            if (clientPlayer == player) return;

            player.setInvisible(false);
        }
    }

    @SubscribeEvent
    public static void onPlaySoundAtPos(PlayLevelSoundEvent.AtPosition event) {
        Level level = event.getLevel();

        for (Player player : level.players()) {
            if (invisiblePlayersClient.containsKey(player.getUUID())) {

                double dist = player.distanceToSqr(
                        event.getPosition().x,
                        event.getPosition().y,
                        event.getPosition().z
                );

                if (dist < 5.0) {
                    var clientPlayer = ClientHandler.getPlayer();

                    if (clientPlayer == player) return;

                    if (clientPlayer != null) {
                        if (ClientBeyonderCache.getPathway(clientPlayer.getUUID()).equals("visionary") &&
                                ClientBeyonderCache.getSequence(clientPlayer.getUUID()) < invisiblePlayersClient.get(player.getUUID()))
                            return;

                        if (DecryptionRenderLayer.activeDecryption.contains(clientPlayer.getUUID()) ||
                                SpiritVisionOverlayRenderer.entitiesLookedAt.containsKey(clientPlayer.getUUID())) {
                            if (AbilityUtil.isTargetSignificantlyWeaker(ClientBeyonderCache.getSequence(clientPlayer.getUUID()),
                                    invisiblePlayersClient.get(player.getUUID())))
                                return;
                        }
                    }

                    player.setSilent(true);
                    event.setCanceled(true);
                }
            }
        }
    }

}
