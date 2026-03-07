package de.jakob.lotm.util;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.common.DivinationAbility;
import de.jakob.lotm.abilities.core.AbilityUseEvent;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.abilities.darkness.NightDomainAbility;
import de.jakob.lotm.abilities.demoness.InvisibilityAbility;
import de.jakob.lotm.abilities.demoness.ShadowConcealmentAbility;
import de.jakob.lotm.abilities.door.SpaceConcealmentAbility;
import de.jakob.lotm.abilities.fool.HistoricalVoidHidingAbility;
import de.jakob.lotm.abilities.red_priest.FogOfWarAbility;
import de.jakob.lotm.abilities.tyrant.LightningStormAbility;
import de.jakob.lotm.attachments.MirrorWorldTraversalComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.*;


@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class DivinationUtil {
    public static final Map<UUID, Map<String, AbilityPower>> PLAYER_ABILITY_MAP = new HashMap<>();

    public static int getDivinationPower(ServerPlayer serverPlayer) {
        int divinationPower = 0;
        String pathway = BeyonderData.getPathway(serverPlayer);
        int sequence = BeyonderData.getSequence(serverPlayer);

        if (sequence <= 9) divinationPower += getLowerSequenceDivinationPower(pathway);

        if (sequence <= 7) divinationPower += getMidSequenceDivinationPower(pathway);

        if (sequence <= 4) divinationPower += getSaintSequenceDivinationPower(pathway);

        if (sequence <= 2) divinationPower += getSequenceTwoDivinationPower(pathway);

        if (sequence <= 1) divinationPower += getSequenceOneDivinationPower(pathway);

        divinationPower += getDivinationItemInInventory(serverPlayer);
        divinationPower += getDivinationItemInHand(serverPlayer);

        return divinationPower;
    }


    private static int getLowerSequenceDivinationPower(String pathway) {
        return switch (pathway) {
            case "fool" -> 2;
            case "wheel_of_fortune" -> 2;
            case "darkness" -> 1;
            default -> 0;
        };
    }

    private static int getMidSequenceDivinationPower(String pathway) {
        return switch (pathway) {
            case "fool" -> 3;
            case "wheel_of_fortune" -> 2;
            case "darkness" -> 2;
            case "door" -> 3;
            case "demoness" -> 2;
            default -> 1;
        };
    }

    private static int getSaintSequenceDivinationPower(String pathway) {
        return switch (pathway) {
            case "fool" -> 4;
            case "wheel_of_fortune" -> 4;
            case "darkness" -> 3;
            default -> 2;
        };
    }

    private static int getSequenceTwoDivinationPower(String pathway) {
        return switch (pathway) {
            case "fool" -> 5;
            case "wheel_of_fortune" -> 5;
            default -> 3;
        };
    }

    private static int getSequenceOneDivinationPower(String pathway) {
        return switch (pathway) {
            case "fool" -> 5;
            case "wheel_of_fortune" -> 5;
            default -> 4;
        };
    }


    public static int getConcealmentPower(ServerPlayer serverPlayer) {
        int concealmentPower = 0;
        String pathway = BeyonderData.getPathway(serverPlayer);
        int sequence = BeyonderData.getSequence(serverPlayer);

        if (sequence <= 9) concealmentPower += getLowerSequenceConcealmentPower(pathway);

        if (sequence <= 7) concealmentPower += getMidSequenceConcealmentPower(pathway);

        if (sequence <= 4) concealmentPower += getSaintSequenceConcealmentPower(pathway);

        if (sequence <= 2) concealmentPower += getSequenceTwoConcealmentPower(pathway);

        if (sequence <= 1) concealmentPower += getSequenceOneConcealmentPower(pathway);

        concealmentPower += getConcealmentItemInInventory(serverPlayer);
        concealmentPower += getConcealmentAbilities(serverPlayer);
        concealmentPower += getConcealmentDimension(serverPlayer);
        concealmentPower += getConcealmentEffect(serverPlayer);

        return concealmentPower;
    }


    private static int getLowerSequenceConcealmentPower(String pathway) {
        return switch (pathway) {
            case "demoness" -> 1;
            default -> 0;
        };
    }

    private static int getMidSequenceConcealmentPower(String pathway) {
        return switch (pathway) {
            case "fool" -> 2;
            case "door" -> 2;
            case "error" -> 2;
            case "darkness" -> 2;
            case "demoness" -> 2;
            case "wheel_of_fortune" -> 3;
            case "abyss" -> 3;
            default -> 1;
        };
    }

    private static int getSaintSequenceConcealmentPower(String pathway) {
        return switch (pathway) {
            case "fool" -> 3;
            case "door" -> 3;
            case "error" -> 3;
            case "visionary" -> 3;
            case "darkness" -> 3;
            case "wheel_of_fortune" -> 3;
            default -> 2;
        };
    }

    private static int getSequenceTwoConcealmentPower(String pathway) {
        return switch (pathway) {
            case "fool" -> 5;
            case "visionary" -> 5;
            case "darkness" -> 5;
            default -> 4;
        };
    }

    private static int getSequenceOneConcealmentPower(String pathway) {
//        return switch (pathway) {
//            default -> 6;
//        };
        return 6;
    }

    
    // get divination items in inventory
    private static int getDivinationItemInInventory(ServerPlayer serverPlayer) {
        int addedValue = 0;
        if (serverPlayer.getInventory().contains(ModItems.FOOL_Card.toStack())) {
            addedValue += 99;
        }
        return addedValue;
    }

    // get items for concealment in inventory
    private static int getConcealmentItemInInventory(ServerPlayer serverPlayer) {
        int addedValue = 0;
        if (serverPlayer.getInventory().contains(ModItems.FOOL_Card.toStack())) {
            addedValue += 99;
        }
        return addedValue;
    }

    private static int getDivinationItemInHand(ServerPlayer serverPlayer){
        int addedValue = 0;
        if (serverPlayer.getItemInHand(InteractionHand.MAIN_HAND).is(ModItems.CANE) ||serverPlayer.getItemInHand(InteractionHand.OFF_HAND).is(ModItems.CANE)) {
            addedValue += 2;
        }
        if (serverPlayer.getItemInHand(InteractionHand.MAIN_HAND).is(ModItems.CRYSTAL_BALL) ||serverPlayer.getItemInHand(InteractionHand.OFF_HAND).is(ModItems.CRYSTAL_BALL)) {
            addedValue += 3;
        }
        return addedValue;
    }

    // get active abilities
    private static int getConcealmentAbilities(ServerPlayer serverPlayer) {
        int addedValue = 0;
        Set<ToggleAbility> activeAbilities = ToggleAbility.getActiveAbilitiesForEntity(serverPlayer);

        if (activeAbilities.stream().anyMatch(ability -> ability instanceof HistoricalVoidHidingAbility)) {
            addedValue += 99;
        }
        if (activeAbilities.stream().anyMatch(ability -> ability instanceof FogOfWarAbility)) {
            addedValue += 6;
        }
        if ((InvisibilityAbility.invisiblePlayers.contains(serverPlayer.getUUID())) || (ShadowConcealmentAbility.invisiblePlayers.contains(serverPlayer.getUUID()))) {
            addedValue += 2;
        }
        MirrorWorldTraversalComponent component = serverPlayer.getData(ModAttachments.MIRROR_WORLD_COMPONENT.get());
        if(component.isInMirrorWorld()) {
            addedValue += 8;
        }
        return addedValue;
    }

    // get effects
    private static int getConcealmentEffect(ServerPlayer serverPlayer){
        int addedValue = 0;
        if (serverPlayer.getEffect(ModEffects.CONCEALMENT) != null){
            addedValue += serverPlayer.getEffect(ModEffects.CONCEALMENT).getAmplifier();
        }
        return addedValue;
    }

    // get dimension
    private static int getConcealmentDimension(ServerPlayer serverPlayer){
        int addedValue = 0;
        if (serverPlayer.level().dimension().location().equals(ModDimensions.SPACE_TYPE_KEY.location())) {
            addedValue += 99;
        } else if (serverPlayer.level().dimension().equals(ModDimensions.CONCEALMENT_WORLD_DIMENSION_KEY)) {
            addedValue += 99;
        } else if (serverPlayer.level().dimension().location().equals(ModDimensions.WORLD_CREATION_LEVEL_KEY.location())) {
            addedValue += 99;
        }
        return addedValue;
    }


    @SubscribeEvent
    private static void onAbilityTriggered(AbilityUseEvent event) {
        LivingEntity user = event.getEntity();
        if (user instanceof ServerPlayer serverPlayer){
            if(serverPlayer.level() instanceof ServerLevel serverLevel){

                int sequence = BeyonderData.getSequence(serverPlayer);
                int durationMultiplier = 0;
                int powerMultiplier = 0;

                String abilityKey = event.getAbility().getClass().getSimpleName();

                // seq3 ability - Lightning Storm - Tyrant
                if (event.getAbility() instanceof LightningStormAbility) {
                    durationMultiplier = 3;
                    powerMultiplier = (sequence < 4) ?  2 + (4 - sequence) : 2;
                }

                // seq 4 ability - Space Concealment - Door
                else if (event.getAbility() instanceof SpaceConcealmentAbility spaceAbility) {
                    if (spaceAbility.getSelectedAbility(user).equals("ability.lotmcraft.space_concealment.self")) {
                        durationMultiplier = 4;
                        powerMultiplier = (sequence < 5) ? 2 + (5 - sequence) : 2;
                    }
                }

                // seq 9 ability - Anti Divination - Common
                else if (event.getAbility() instanceof DivinationAbility divinationAbility) {
                    if (divinationAbility.getSelectedAbility(user).equals("ability.lotmcraft.divination.anti_divination")) {
                        durationMultiplier = 4;
                        powerMultiplier = (10 - sequence);
                    }
                }

                // seq4 ability - Night Domain - Darkness
                else if (event.getAbility() instanceof NightDomainAbility) {
                    Vec3 startPos = user.position();
                    final int durationMultiplierFinal = 8;
                    final int powerMultiplierFinal = (sequence < 5) ? 3 + (5 - sequence) : 3;

                    ServerScheduler.scheduleForDuration(0, 2, 20 * 25, () -> {
                        // Using your existing Utility method to find targets
                        List<LivingEntity> targets = AbilityUtil.getNearbyEntities(null, serverLevel, startPos, 35, false, true);

                        // Also include the caster manually since 'exclude' might hit them depending on your flags
                        updateEffect(serverPlayer, abilityKey, powerMultiplierFinal, durationMultiplierFinal);

                        for (LivingEntity target : targets) {
                            updateEffect(target, abilityKey, powerMultiplierFinal, durationMultiplierFinal);
                        }
                    });
                    return;
                }
                if (durationMultiplier > 0) {
                    updateEffect(serverPlayer, abilityKey, powerMultiplier, durationMultiplier);
                }
            }
        }
    }

    private static void updateEffect(LivingEntity entity, String key, int power, int durationMultiplier) {
        Map<String, AbilityPower> activeAbilities = PLAYER_ABILITY_MAP.computeIfAbsent(entity.getUUID(), k -> new HashMap<>());

        // if the abilities already exits with the same power lvl, return
        AbilityPower existing = activeAbilities.get(key);
        if (existing != null && existing.power == power) {
            return;
        }

        int durationTicks = 300 * durationMultiplier;
        long expiryTime = System.currentTimeMillis() + (durationTicks * 50L);
        activeAbilities.put(key, new AbilityPower(power, expiryTime));

        activeAbilities.entrySet().removeIf(entry -> System.currentTimeMillis() > entry.getValue().expiryTime);
        int totalPower = activeAbilities.values().stream().mapToInt(a -> a.power).sum();

        if (totalPower > 0) {
            entity.addEffect(new MobEffectInstance(ModEffects.CONCEALMENT, durationTicks, totalPower, false, false));
        } else {
            entity.removeEffect(ModEffects.CONCEALMENT);
        }
    }

    public static class AbilityPower {
        int power;
        long expiryTime;

        AbilityPower(int power, long expiryTime) {
            this.power = power;
            this.expiryTime = expiryTime;
        }
    }

}
