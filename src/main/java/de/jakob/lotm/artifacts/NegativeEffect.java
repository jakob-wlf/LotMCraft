package de.jakob.lotm.artifacts;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.DamageLookup;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

import java.util.Random;

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

    public static NegativeEffect createDefault() {
        // Return a safe default negative effect
        return new NegativeEffect(NegativeEffectType.DRAIN_HEALTH, 9, null, 1);
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
            case POTION_EFFECT:
                if (mobEffect != null) {
                    player.addEffect(new MobEffectInstance(mobEffect, 100, effectAmplifier, false, false));
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
            case ATTRACT_MOBS:
                break;
            case RANDOM_TELEPORT:
                if (player.tickCount % getTeleportIntervalForSequence(sequence) == 0) {
                    double range = 5 + (10 - sequence) * 2;
                    double x = player.getX() + (player.getRandom().nextDouble() - 0.5) * range * 2;
                    double z = player.getZ() + (player.getRandom().nextDouble() - 0.5) * range * 2;
                    player.teleportTo(x, player.getY(), z);
                }
                break;
            case HEARING_WHISPERS:
                if (player.tickCount % getWhisperIntervalForSequence(sequence) == 0) {
                    player.displayClientMessage(Component.translatable("lotm.whisper." + player.getRandom().nextInt(5)), true);
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
    public static NegativeEffect createRandom(String pathway, int sequence, Random random) {
        // Lower sequence = stronger negative effect

        // Pathway-specific effects
        NegativeEffectType[] pathwayEffects = getPathwayEffects(pathway);
        
        // Mix pathway-specific with generic effects
        NegativeEffectType chosenType;
        if (pathwayEffects.length > 0) {
            chosenType = pathwayEffects[random.nextInt(pathwayEffects.length)];
        } else {
            chosenType = NegativeEffectType.getGenericEffect(random);
        }

        return new NegativeEffect(chosenType, sequence, null, sequence);
    }

    private static NegativeEffectType[] getPathwayEffects(String pathway) {
        return switch (pathway) {
            case "sun", "abyss" -> new NegativeEffectType[]{
                    NegativeEffectType.DRAIN_HEALTH,
            };
            case "darkness", "demoness" -> new NegativeEffectType[]{
                    NegativeEffectType.HEARING_WHISPERS,
            };
            case "tyrant" -> new NegativeEffectType[]{
                    NegativeEffectType.RANDOM_TELEPORT,
                    NegativeEffectType.DRAIN_HEALTH
            };
            case "door" -> new NegativeEffectType[]{
                    NegativeEffectType.RANDOM_TELEPORT
            };
            default -> new NegativeEffectType[0];
        };
    }

    public enum NegativeEffectType {
        POTION_EFFECT,
        DRAIN_HEALTH,
        DRAIN_HUNGER,
        ATTRACT_MOBS,
        RANDOM_TELEPORT,
        HEARING_WHISPERS;

        public static NegativeEffectType getGenericEffect(Random random) {
            NegativeEffectType[] generic = {DRAIN_HEALTH, DRAIN_HUNGER, HEARING_WHISPERS};
            return generic[random.nextInt(generic.length)];
        }
    }
}