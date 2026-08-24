package de.jakob.lotm.beyonders.abilities.fool.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.*;
import de.jakob.lotm.beyonders.abilities.core.*;
import de.jakob.lotm.events.AbilityWheelEvents;
import de.jakob.lotm.events.custom.AbilityWheelOpenEvent;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.syncDangerArrowsOverlayPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DivinationUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

@EventBusSubscriber
public class DangerPremonitionAbility extends PassiveAbilityItem {
    public DangerPremonitionAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of("fool", 8);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (!(entity instanceof ServerPlayer serverPlayer)) return;
        if ((serverPlayer.tickCount + entity.getId()) % 2 != 0) {
            return;
        }

        boolean shouldShowOverlay = false;
        LivingEntity chosenThreat = null;

        int radius = 13 + (2 * (10 - BeyonderData.getSequence(serverPlayer)));

        if (!(level instanceof ServerLevel serverLevel)) return;
        List<LivingEntity> possibleThreats = AbilityUtil.getNearbyEntities(serverPlayer, serverLevel, serverPlayer.position(), radius);
        for (LivingEntity threat : possibleThreats) {

            AllyComponent allyComponent = entity.getData(ModAttachments.ALLY_COMPONENT);
            if (allyComponent.isAlly(threat.getUUID())) continue;

            if (ToggleAbility.getActiveAbilitiesForEntity(threat).contains(
                    LOTMCraft.abilityHandler.getById("psychological_invisibility_ability"))) return;

            if (threat instanceof ServerPlayer threatServer) {
                if (DivinationUtil.getConcealmentPower(threatServer) > DivinationUtil.getDivinationPower(serverPlayer) + 4) return;
            }

            if (isThreatFromThreateningPathway(threat) || isThreatAKiller(threat) || isThreatStronger(serverPlayer, threat)) {
                shouldShowOverlay = true;
                chosenThreat = threat;
                break;
            } else if (isThreatLookingAtUser(serverPlayer, threat, radius)) {
                if (doesThreatHaveActiveAbilities(threat) || isThreatChanged(threat) || doesThreatHaveArtifacts(threat)) {
                    shouldShowOverlay = true;
                    chosenThreat = threat;
                    break;
                }
            }
        }

        if (shouldShowOverlay) {
            double dx = chosenThreat.getX() - serverPlayer.getX();
            double dz = chosenThreat.getZ() - serverPlayer.getZ();
            PacketHandler.sendToPlayer(serverPlayer, new syncDangerArrowsOverlayPacket(
                    getDirection(dx, dz, serverPlayer.getYRot()),
                    40
            ));
        }
    }

    static float[] dodgeChanceForSequence = new float[]{.4f, .35f, .325f, .275f, .25f, .2f, .175f, .15f, .125f, .1f};

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if(!((DangerPremonitionAbility) PassiveAbilityHandler.DANGER_PREMONITION.get()).shouldApplyTo(entity))
            return;

        Entity damager = event.getSource().getEntity();
        if(damager == null ||
                ((damager instanceof LivingEntity damagerLiving) &&
                        BeyonderData.getSequence(damagerLiving) - BeyonderData.getSequence(event.getEntity()) < -2
                ))
            return;

        int sequence = BeyonderData.getSequence(entity);
        if(sequence < 0 || sequence > 9) return;
        if(entity.level().random.nextFloat() <= dodgeChanceForSequence[sequence]) {
            event.setCanceled(true);
            entity.playSound(SoundEvents.ARMOR_STAND_BREAK, 1, 1);
            AbilityUtil.sendActionBar(entity, Component.translatable("lotm.dodged_attack").withColor(getColorForPathway("fool")));
        }

    }

    public static Boolean isThreatLookingAtUser(LivingEntity user, LivingEntity threat, int radius) {
        LivingEntity target = AbilityUtil.getTargetEntity(threat, radius, 2);
        if (target == user) return true;

        return false;
    }

    public static Boolean doesThreatHaveActiveAbilities(LivingEntity threat) {
        if (new HashSet<>(ToggleAbility.getActiveAbilitiesForEntity(threat)).isEmpty()) return false;

        return true;
    }

    public static Boolean isThreatChanged(LivingEntity threat) {
        // check if the threat is using shape shifting, controlling or parasited
        ControllingDataComponent controllingDataComponent = threat.getData(ModAttachments.CONTROLLING_DATA);
        if (controllingDataComponent.isControlling()) return true;

        ParasitationComponent parasitationComponent = threat.getData(ModAttachments.PARASITE_COMPONENT);
        if (parasitationComponent.getParasiteUUID() != null) return true;

        ShapeShiftComponent shapeShiftComponent = threat.getData(ModAttachments.SHAPE_SHIFT);
        if (!(shapeShiftComponent.getShape().isEmpty())) return true;
        return false;
    }

    public static Boolean doesThreatHaveArtifacts(LivingEntity threat) {
        if (threat instanceof Player player) {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = player.getInventory().getItem(i);

                if (!stack.isEmpty() && (stack.getItem() == ModItems.SEALED_ARTIFACT.get()
                        || stack.getItem() == ModItems.SEALED_ARTIFACT_BELL.get()
                        || stack.getItem() == ModItems.SEALED_ARTIFACT_CHAIN.get()
                        || stack.getItem() == ModItems.SEALED_ARTIFACT_GEM.get()
                        || stack.getItem() == ModItems.SEALED_ARTIFACT_STAR.get())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Boolean isThreatFromThreateningPathway(LivingEntity threat) {
        BeyonderComponent beyonderComponent = threat.getData(ModAttachments.BEYONDER_COMPONENT);
        String pathway = beyonderComponent.getPathway();
        return switch (pathway) {
            case "demoness", "abyss", "fool", "error", "door" -> true;
            default -> false;
        };
    }

    public static Boolean isThreatAKiller(LivingEntity threat){
        if (threat instanceof ServerPlayer serverPlayer) {
            int playerKills = serverPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.PLAYER_KILLS));
            if (playerKills >= 20) return true;
        }
        return false;
    }

    public static Boolean isThreatStronger(LivingEntity user, LivingEntity threat) {
        BeyonderComponent threatData = threat.getData(ModAttachments.BEYONDER_COMPONENT);
        int threatSequence = threatData.getSequence();

        BeyonderComponent userData = user.getData(ModAttachments.BEYONDER_COMPONENT);
        int userSequence = userData.getSequence();

        // if the sequence difference is one sequences or more, the target will be considered as a threat
        if (userSequence - threatSequence >= 1) return true;
        return false;
    }

    @SubscribeEvent
    public static void onAbilityWheelOpen(AbilityWheelOpenEvent event) {
        List<LivingEntity> entities = AbilityUtil.getNearbyEntities(event.getEntity(), event.getLevel(), event.getEntity().position(), 33, true);
        for (LivingEntity entity : entities) {

            if (!(entity instanceof ServerPlayer serverPlayer)) continue;

            if (!BeyonderData.isBeyonder(entity)
                    && BeyonderData.getSequence(entity) > 8
                    && (!BeyonderData.getPathway(entity).equals("fool"))
            ) continue;

            int radius = 13 + (2 * (10 - BeyonderData.getSequence(serverPlayer)));
            if (event.getEntity().distanceTo(serverPlayer) > radius) continue;

            if (ToggleAbility.getActiveAbilitiesForEntity(event.getEntity()).contains(
                    LOTMCraft.abilityHandler.getById("psychological_invisibility_ability"))) continue;

            AllyComponent allyComponent = event.getEntity().getData(ModAttachments.ALLY_COMPONENT);
            if (allyComponent.isAlly(entity.getUUID())) continue;

            if (event.getEntity() instanceof ServerPlayer threatServer) {
                if (DivinationUtil.getConcealmentPower(threatServer) > DivinationUtil.getDivinationPower(serverPlayer) + 4) continue;
            }

            double dx = event.getEntity().getX() - serverPlayer.getX();
            double dz = event.getEntity().getZ() - serverPlayer.getZ();
            PacketHandler.sendToPlayer(serverPlayer, new syncDangerArrowsOverlayPacket(
                    getDirection(dx, dz, serverPlayer.getYRot()),
                    80
            ));
        }
    }

    @SubscribeEvent
    public static void onAbilityTrigger(AbilityUsedEvent event) {
        List<LivingEntity> entities = AbilityUtil.getNearbyEntities(event.getEntity(), event.getLevel(), event.getEntity().position(), 33, true);
        for (LivingEntity entity : entities) {

            if (!(entity instanceof ServerPlayer serverPlayer)) continue;

            if (!BeyonderData.isBeyonder(entity)
                    && BeyonderData.getSequence(entity) > 8
                    && (!BeyonderData.getPathway(entity).equals("fool"))
            ) continue;

            int radius = 13 + (2 * (10 - BeyonderData.getSequence(serverPlayer)));
            if (event.getEntity().distanceTo(serverPlayer) > radius) continue;

            if (ToggleAbility.getActiveAbilitiesForEntity(event.getEntity()).contains(
                    LOTMCraft.abilityHandler.getById("psychological_invisibility_ability"))) continue;

            AllyComponent allyComponent = event.getEntity().getData(ModAttachments.ALLY_COMPONENT);
            if (allyComponent.isAlly(entity.getUUID())) continue;

            if (event.getEntity() instanceof ServerPlayer threatServer) {
                if (DivinationUtil.getConcealmentPower(threatServer) > DivinationUtil.getDivinationPower(serverPlayer) + 4) continue;
            }

            double dx = event.getEntity().getX() - serverPlayer.getX();
            double dz = event.getEntity().getZ() - serverPlayer.getZ();
            PacketHandler.sendToPlayer(serverPlayer, new syncDangerArrowsOverlayPacket(
                    getDirection(dx, dz, serverPlayer.getYRot()),
                    80
            ));
        }
    }

    private static String getDirection(double dx, double dz, float playerYaw) {
        double targetYaw = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
        double relativeYaw = targetYaw - playerYaw;

        relativeYaw = (relativeYaw % 360 + 360) % 360;

        String[] directions = {
                "North", "North-East", "East", "South-East",
                "South", "South-West", "West", "North-West", "North"
        };

        int index = (int) Math.floor((relativeYaw + 22.5) / 45);
        return directions[index];
    }
}
