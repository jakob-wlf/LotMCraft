package de.jakob.lotm.artifacts;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.error.MundaneConceptualTheft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.sound.ModSounds;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.DamageLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.*;
import java.util.stream.Stream;

/**
 * Represents a negative effect that sealed artifacts inflict on their holders
 */
public class NegativeEffect {

    private final NegativeEffectType type;
    private final int sequence; // 0-9, where higher is more severe
    private final Holder<MobEffect> mobEffect; // Can be null for non-potion effects
    private final int effectAmplifier;

    public static final Codec<NegativeEffect> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.xmap(NegativeEffectType::valueOf, NegativeEffectType::name)
                            .fieldOf("type").forGetter(e -> e.type),
                    Codec.INT.fieldOf("strength").forGetter(e -> e.sequence),
                    Codec.STRING.optionalFieldOf("mob_effect", "").forGetter(e ->
                            e.mobEffect != null ? e.mobEffect.toString() : ""),
                    Codec.INT.fieldOf("effect_amplifier").forGetter(e -> e.effectAmplifier)
            ).apply(instance, (type, strength, effectStr, amplifier) ->
                    new NegativeEffect(type, strength, null, amplifier))
    );

    public static List<NegativeEffect> createDefault() {
        // Return a safe default negative effect
        return List.of(new NegativeEffect(NegativeEffectType.DRAIN_HEALTH, 9, null, 1));
    }

    public NegativeEffect(NegativeEffectType type, int sequence, Holder<MobEffect> mobEffect, int effectAmplifier) {
        this.type = type;
        this.sequence = Math.max(0, Math.min(9, sequence));
        this.mobEffect = mobEffect;
        this.effectAmplifier = effectAmplifier;
    }

    /**
     * Applies the negative effect to a player
     * @param player The player holding the sealed artifact
     * @param inMainHand Whether the artifact is in the main hand
     */
    public void apply(Player player, boolean inMainHand) {
        switch (type) {
                // seer
            case SLOWER_IN_HOT_PLACES:
                if (player.tickCount % 20 == 0) {
                    if ((player.level().getBiome(player.blockPosition()).value().shouldMeltFrozenOceanIcebergSlightly(player.blockPosition()))
                            || (getBlockInRadius(player, player.blockPosition(), 4, Blocks.FIRE))) {
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2, false, false));
                    }
                }
                break;

                // error
            case GOLD_ITEM_DEBUFF:
                if (player.tickCount % 100 == 0) {
                    for (ItemStack stack : player.getInventory().items) {
                        if (stack.is(ItemTags.PIGLIN_LOVED)) {
                            return;
                        }
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2, false, false));
                        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 2, false, false));
                        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 2, false, false));
                        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 40, 2, false, false));

                    }
                }
                break;
            case LOSE_CONCEPTS:
                if (player.tickCount % 100 == 0) {
                    MundaneConceptualTheft mundaneConceptualTheft = new MundaneConceptualTheft("mundane_conceptual_theft_ability");
//                    mundaneConceptualTheft.stealWalk(player, 40);
//                    mundaneConceptualTheft.stealSight(player, 40);
                }
                break;
            case LOSE_ABILITIES:
                if (player.tickCount % 400 == 0) {
                    if (!BeyonderData.isBeyonder(player)) {
                        return;
                    }
                    HashSet<Ability> targetAbilities = LOTMCraft.abilityHandler.getByPathwayAndSequence(
                            BeyonderData.getPathway(player), BeyonderData.getSequence(player));

                    ArrayList<Ability> stealableAbilities = new ArrayList<>(targetAbilities.stream()
                            .filter(ability -> ability.canBeCopied
                                    && ability.canUse(player, true, false)
                                    && !BeyonderData.isSpecificAbilityDisabled(player, ability.getId()))
                            .toList());
                    if (stealableAbilities.isEmpty()) {
                        return;
                    }

                    for (int i = 0; i < 1; i++) {
                        Random random = new Random();
                        int index = random.nextInt(stealableAbilities.size());
                        Ability stolenAbility = stealableAbilities.get(index);
                        stealableAbilities.remove(index);

                        // Disable the ability on the target for the duration
                        BeyonderData.disableSpecificAbilityWithTimeLimit(player, "ability_theft_disable",
                                stolenAbility.getId(), 5000L);

                    }
                }
                break;

                // door
            case RANDOM_TELEPORT:
                if (player.tickCount % getTeleportIntervalForSequence(sequence) == 0) {
                    double range = 5 + (10 - sequence) * 2;
                    double x = player.getX() + (player.getRandom().nextDouble() - 0.5) * range * 2;
                    double z = player.getZ() + (player.getRandom().nextDouble() - 0.5) * range * 2;
                    player.teleportTo(x, player.getY(), z);
                }
                break;

                // sun
            case SLOWER_IN_COLD_PLACES:
                if (player.tickCount % 20 == 0) {
                    if ((player.level().getBiome(player.blockPosition()).value().coldEnoughToSnow(player.blockPosition()))
                            || (getBlockInRadius(player, player.blockPosition(), 4, Blocks.FIRE))) {
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2, false, false));
                    }
                }
                break;
            case BRUN:
                if (player.tickCount % 200 == 0) {
                    player.setRemainingFireTicks(80);
                }
                break;

                // tyrant
            case BREATH_DEPLETION:
                if (player.tickCount % 100 == 0) {
                    player.setAirSupply(-200);
                }
                break;
            case STRUCK_BY_LIGHTNING:
                if (player.tickCount % 200 == 0) {
                    if (player instanceof ServerPlayer serverPlayer) {
                        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(serverPlayer.serverLevel());

                        if (lightning != null) {
                            lightning.moveTo(player.position());
                            serverPlayer.serverLevel().addFreshEntity(lightning);
                        }
                    }
                }
                break;
            case WEAKNESS_WHEN_ALONE:
                if (player.tickCount % 20 == 0) {
                    boolean entitiesNearby = !player.level().getEntitiesOfClass(
                            LivingEntity.class,
                            player.getBoundingBox().inflate(8.0),
                            e -> e != player
                    ).isEmpty();
                    if (!entitiesNearby) {
                        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 2, false, false));
                    }
                }
                break;

                // visionary
            case MENTAL_PLAGUE:
                if (player.tickCount % 20 == 0) {
                    player.addEffect(new MobEffectInstance(ModEffects.MENTAL_PLAGUE, 40, 2, false, false));
                }
                break;
            case SPIRIT_HAUNTING:
                if (player.tickCount % getSpiritHauntIntervalForSequence(sequence) == 0) {
                    if (player instanceof ServerPlayer serverPlayer) {
                        Vex vex = EntityType.VEX.create(serverPlayer.serverLevel());
                        if (vex != null) {
                            vex.moveTo(
                                    player.getX() + (player.getRandom().nextDouble() - 0.5) * 4,
                                    player.getY() + 1,
                                    player.getZ() + (player.getRandom().nextDouble() - 0.5) * 4
                            );
                            serverPlayer.serverLevel().addFreshEntity(vex);
                        }
                    }
                }
                break;

                // demoness
            case PETRIFICATION:
                if (player.tickCount % 20 == 0) {
                    player.addEffect(new MobEffectInstance(ModEffects.PETRIFICATION, 40, 2, false, false));
                }
                break;
            case CHARM_BACKLASH:
                if (player.tickCount % 140 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 0, false, false));
                }
                break;

                // hunter
            case TARGETED_BY_ENTITIES:
                if (player.tickCount % 20 == 0) {
                    for (Mob mob : player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(10.0))) {
                        mob.setTarget(player);
                    }}
                break;
            case WITHER:
                if (player.tickCount % 20 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 2, false, false));
                }
                break;
            case CRIMSON_CHAIN:
                if (player.tickCount % 20 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 40, 1, false, false));
                }
                break;

                // darkness
            case BLINDNESS:
                if (player.tickCount % 20 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 2, false, false));
                }
                break;
            case ASLEEP:
                if (player.tickCount % 20 == 0) {
                    player.addEffect(new MobEffectInstance(ModEffects.ASLEEP, 40, 2, false, false));
                }
                break;

                // mother
            case MUTATED:
                if (player.tickCount % 20 == 0) {
                    player.addEffect(new MobEffectInstance(ModEffects.MUTATED, 40, 2, false, false));
                }
                break;
            case POISON:
                if (player.tickCount % 20 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.POISON, 40, 2, false, false));
                }
                break;
            case SILK_TRAP:
                if (player.tickCount % 300 == 0) {
                    BlockPos feet = player.blockPosition();
                    BlockPos head = feet.above();
                    if (player.level().getBlockState(feet).isAir()) {
                        player.level().setBlock(feet, Blocks.COBWEB.defaultBlockState(), 3);
                    }
                    if (player.level().getBlockState(head).isAir()) {
                        player.level().setBlock(head, Blocks.COBWEB.defaultBlockState(), 3);
                    }
                }
                break;

                // monster
            case BAD_LUCK:
                if (player.tickCount % 20 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 40, 2, false, false));
                }
                break;
            case FATE_SPIN:
                if (player.tickCount % 300 == 0) {
                    int roll = player.getRandom().nextInt(5);
                    switch (roll) {
                        case 0 -> player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0, false, false));
                        case 1 -> player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1, false, false));
                        case 2 -> player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1, false, false));
                        case 3 -> player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 200, 1, false, false));
                        case 4 -> player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0, false, false));
                    }
                }
                break;

                // abyss
            case NAUSEA:
                if (player.tickCount % 20 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 40, 2, false, false));
                }
                break;

                // general
            case SLOWNESS:
                if (player.tickCount % 20 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2, false, false));
                }
                break;
            case HEAR_SOUNDS:
                if (player.tickCount % 100 == 0) {
                    if (player instanceof ServerPlayer serverPlayer) {
                        Holder<SoundEvent> holder = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModSounds.LOUD_SOUND_1.get());

                        serverPlayer.connection.send(new ClientboundSoundPacket(
                                holder,
                                SoundSource.AMBIENT,
                                player.getX(),
                                player.getY(),
                                player.getZ(),
                                1.0f,
                                1.0f,
                                player.level().getRandom().nextLong()
                        ));
                    }
                }
                break;
            case MINING_FATIGUE:
                if (player.tickCount % 20 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 40, 2, false, false));
                }
                break;
            case DRAIN_HEALTH:
                if (player.tickCount % 20 == 0) {
                    player.hurt(player.damageSources().magic(), (float) DamageLookup.lookupDamage(sequence >= 0 ? Math.clamp(sequence - 1, 0, 9) : -1, 1f) * (float) BeyonderData.getMultiplierForSequence(sequence));
                }
                break;
            case DRAIN_HUNGER:
                if (player.tickCount % 20 == 0) {
                    player.getFoodData().setFoodLevel(player.getFoodData().getFoodLevel() - 1);
                }
                break;
            case HEARING_WHISPERS:
                if (player.tickCount % getWhisperIntervalForSequence(sequence) == 0) {
                    player.displayClientMessage(Component.translatable("lotm.whisper." + player.getRandom().nextInt(5)), true);
                    SanityComponent sanityComponent = player.getData(ModAttachments.SANITY_COMPONENT);
                    sanityComponent.increaseSanityAndSync(-0.01f * (10 - sequence), player);
                }
                break;
        }
    }

    private int getTeleportIntervalForSequence(int sequence) {
        return switch (sequence) {
            case 8 -> 20 * 25;
            case 7 -> 20 * 22;
            case 6, 5 -> 20 * 20;
            case 4, 3 -> 20 * 9;
            case 2 -> 20 * 4;
            case 1 -> 50;
            default -> 20 * 30;
        };
    }

    private int getWhisperIntervalForSequence(int sequence) {
        return switch (sequence) {
            case 8 -> 20 * 8;
            case 7 -> 20 * 7;
            case 6, 5 -> 20 * 5;
            case 4, 3 -> 20;
            case 2 -> 10;
            case 1 -> 5;
            default -> 20 * 10;
        };
    }

    private int getSpiritHauntIntervalForSequence(int sequence) {
        return switch (sequence) {
            case 8, 9 -> 20 * 120;
            case 6, 7 -> 20 * 90;
            case 4, 5 -> 20 * 60;
            case 2, 3 -> 20 * 40;
            case 1 -> 20 * 12;
            default -> 20 * 120;
        };
    }

    public Component getDisplayName() {
        return Component.translatable("lotm.negative_effect." + type.name().toLowerCase());
    }

    public NegativeEffectType getType() {
        return type;
    }

    public int getSequence() {
        return sequence;
    }

    /**
     * Creates a random negative effect appropriate for a pathway and sequence
     */
    public static List<NegativeEffect> createRandom(String pathway, int sequence, Random random, String baseItem) {
        List<NegativeEffectType> pathwayEffects = new ArrayList<>(getPathwayEffects(pathway, sequence));
        List<NegativeEffectType> defaultEffects = new ArrayList<>(getPathwayEffects("default", sequence));

        Collections.shuffle(pathwayEffects, random);
        Collections.shuffle(defaultEffects, random);

        int totalEffects = (sequence <= 1) ? 5 :
                    (sequence <= 2) ? 4 :
                    (sequence <= 4) ? 3 :
                    (sequence <= 7) ? 2 : 1;

        if (baseItem.equals("star")) {
            totalEffects -= 2;
        } else if (baseItem.equals("gem")) {
            totalEffects -= 1;
        }

        totalEffects = Math.max(0, totalEffects);

        List<NegativeEffect> finalEffects = new ArrayList<>();

        int fromPathway = Math.min(totalEffects, pathwayEffects.size());
        for (int i = 0; i < fromPathway; i++) {
            finalEffects.add(new NegativeEffect(pathwayEffects.get(i), sequence, null, sequence));
        }

        int remainingSlots = totalEffects - fromPathway;
        if (remainingSlots > 0) {
            for (int i = 0; i < Math.min(remainingSlots, defaultEffects.size()); i++) {
                finalEffects.add(new NegativeEffect(defaultEffects.get(i), sequence, null, sequence));
            }
        }

        return finalEffects;
    }

    private static List<NegativeEffectType> getPathwayEffects(String pathway, int sequence) {
        return switch (pathway) {
            case "fool" -> Stream.of(
                    NegativeEffectType.BRUN,
                    NegativeEffectType.BREATH_DEPLETION,
                    NegativeEffectType.SLOWER_IN_HOT_PLACES,
                    (sequence <= 4) ? NegativeEffectType.TURN_TO_MARIONETTE : null,
                    (sequence <= 2) ? NegativeEffectType.WISH_CALAMITY : null
            ).filter(Objects::nonNull).toList();

            case "error" -> Stream.of(
                    NegativeEffectType.GOLD_ITEM_DEBUFF,
                    (sequence <= 6) ? NegativeEffectType.LOSE_ABILITIES : null,
                    (sequence <= 4) ? NegativeEffectType.LOSE_CONCEPTS : null,
                    (sequence <= 2) ? NegativeEffectType.STOP_YOUR_TIME : null
            ).filter(Objects::nonNull).toList();

            case "door" -> Stream.of(
                    NegativeEffectType.FULL_MOON_WHISPERS,
                    (sequence <= 5) ? NegativeEffectType.RANDOM_TELEPORT : null,
                    (sequence <= 3) ? NegativeEffectType.ENTER_SPIRIT_WORLD : null
            ).filter(Objects::nonNull).toList();

            case "sun" -> Stream.of(
                    NegativeEffectType.BRUN,
                    NegativeEffectType.SLOWER_IN_COLD_PLACES,
                    (sequence <= 4) ? NegativeEffectType.CONFLICT_WITH_ARTIFACTS : null
            ).filter(Objects::nonNull).toList();

            case "tyrant" -> Stream.of(
                    NegativeEffectType.BREATH_DEPLETION,
                    NegativeEffectType.WEAKNESS_WHEN_ALONE,
                    NegativeEffectType.DRAIN_HUNGER,
                    (sequence <= 6) ? NegativeEffectType.TARGETED_BY_ENTITIES : null,
                    (sequence <= 4) ? NegativeEffectType.STRUCK_BY_LIGHTNING : null
            ).filter(Objects::nonNull).toList();

            case "visionary" -> Stream.of(
                    NegativeEffectType.MENTAL_PLAGUE,
                    NegativeEffectType.SPIRIT_HAUNTING,
                    NegativeEffectType.HEAR_SOUNDS,
                    NegativeEffectType.HEARING_WHISPERS
            ).filter(Objects::nonNull).toList();

            case "demoness" -> Stream.of(
                    NegativeEffectType.PETRIFICATION,
                    NegativeEffectType.CURSED,
                    NegativeEffectType.CHARM_BACKLASH
            ).filter(Objects::nonNull).toList();

            case "red_priest" -> Stream.of(
                    NegativeEffectType.TARGETED_BY_ENTITIES,
                    NegativeEffectType.WITHER,
                    NegativeEffectType.CRIMSON_CHAIN
            ).filter(Objects::nonNull).toList();

            case "darkness" -> Stream.of(
                    NegativeEffectType.BLINDNESS,
                    NegativeEffectType.ASLEEP,
                    NegativeEffectType.CURSED
            ).filter(Objects::nonNull).toList();

            case "mother" -> Stream.of(
                    NegativeEffectType.POISON,
                    NegativeEffectType.MUTATED,
                    NegativeEffectType.SILK_TRAP
            ).filter(Objects::nonNull).toList();

            case "wheel_of_fortune" -> Stream.of(
                    NegativeEffectType.BAD_LUCK,
                    NegativeEffectType.CALAMITY_ATTRACTION,
                    NegativeEffectType.FATE_SPIN
            ).filter(Objects::nonNull).toList();

            case "abyss" -> Stream.of(
                    NegativeEffectType.NAUSEA
            ).filter(Objects::nonNull).toList();

            default -> List.of(
                    NegativeEffectType.DRAIN_HEALTH,
                    NegativeEffectType.DRAIN_HUNGER,
                    NegativeEffectType.HEARING_WHISPERS,
                    NegativeEffectType.SLOWNESS,
                    NegativeEffectType.MINING_FATIGUE,
                    NegativeEffectType.HEAR_SOUNDS
            );
        };
    }

    public enum NegativeEffectType {
        // seer
        SLOWER_IN_HOT_PLACES,
        TURN_TO_MARIONETTE,
        WISH_CALAMITY,

        // error
        GOLD_ITEM_DEBUFF,
        LOSE_CONCEPTS,
        LOSE_ABILITIES,
        STOP_YOUR_TIME,

        // door
        FULL_MOON_WHISPERS,
        RANDOM_TELEPORT,
        ENTER_SPIRIT_WORLD,

        // sun
        SLOWER_IN_COLD_PLACES,
        BRUN,
        CONFLICT_WITH_ARTIFACTS,

        // tyrant
        BREATH_DEPLETION,
        STRUCK_BY_LIGHTNING,
        WEAKNESS_WHEN_ALONE,

        // spectator
        MENTAL_PLAGUE,
        SPIRIT_HAUNTING,

        // demoness
        PETRIFICATION,
        CHARM_BACKLASH,

        // hunter
        WITHER,
        TARGETED_BY_ENTITIES,
        CRIMSON_CHAIN,

        // darkness
        BLINDNESS,
        ASLEEP,
        CURSED,

        // mother
        POISON,
        MUTATED,
        SILK_TRAP,

        // monster
        BAD_LUCK,
        CALAMITY_ATTRACTION,
        FATE_SPIN,

        // abyss
        NAUSEA,

        // general
        DRAIN_HEALTH,
        DRAIN_HUNGER,
        HEARING_WHISPERS,
        SLOWNESS,
        MINING_FATIGUE,
        HEAR_SOUNDS;
    }

    private static boolean getBlockInRadius (Player player, BlockPos center, int radius, Block block){
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius))) {

            if (player.level().getBlockState(pos).is(block)) {
                return true;
            }
        }
        return false;
    }
}
