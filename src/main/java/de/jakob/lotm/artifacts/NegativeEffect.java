package de.jakob.lotm.artifacts;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Random;

/**
 * Represents a negative effect that sealed artifacts inflict on their holders
 */
public class NegativeEffect {
    
    private final NegativeEffectType type;
    private final int strength; // 0-9, where higher is more severe
    private final Holder<MobEffect> mobEffect; // Can be null for non-potion effects
    private final int effectAmplifier;

    public static final Codec<NegativeEffect> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.xmap(NegativeEffectType::valueOf, NegativeEffectType::name)
                            .fieldOf("type").forGetter(e -> e.type),
                    Codec.INT.fieldOf("strength").forGetter(e -> e.strength),
                    Codec.STRING.optionalFieldOf("mob_effect", "").forGetter(e -> 
                            e.mobEffect != null ? e.mobEffect.toString() : ""),
                    Codec.INT.fieldOf("effect_amplifier").forGetter(e -> e.effectAmplifier)
            ).apply(instance, (type, strength, effectStr, amplifier) -> 
                    new NegativeEffect(type, strength, null, amplifier))
    );

    public NegativeEffect(NegativeEffectType type, int strength, Holder<MobEffect> mobEffect, int effectAmplifier) {
        this.type = type;
        this.strength = Math.max(0, Math.min(9, strength));
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
                if (player.tickCount % (60 - strength * 5) == 0) {
                    player.hurt(player.damageSources().magic(), 0.5f + strength * 2f);
                }
                break;
            case DRAIN_HUNGER:
                if (player.tickCount % (80 - strength * 6) == 0) {
                    player.getFoodData().addExhaustion(0.2f + strength * 5f);
                }
                break;
            case ATTRACT_MOBS:
                // This would need custom implementation in a tick handler
                // to spawn mobs or make nearby mobs aggressive
                break;
            case RANDOM_TELEPORT:
                if (player.tickCount % Math.max((1200 - strength * 200), 1) == 0 && player.getRandom().nextFloat() < 0.6f) {
                    // Teleport logic would go here
                    double range = 5 + strength * 2;
                    double x = player.getX() + (player.getRandom().nextDouble() - 0.5) * range * 2;
                    double z = player.getZ() + (player.getRandom().nextDouble() - 0.5) * range * 2;
                    player.teleportTo(x, player.getY(), z);
                }
                break;
            case HEARING_WHISPERS:
                if (player.tickCount % (200 - strength * 15) == 0) {
                    player.displayClientMessage(Component.translatable("lotm.whisper." + player.getRandom().nextInt(5)), true);
                }
                break;
        }
    }

    public Component getDisplayName() {
        return Component.translatable("lotm.negative_effect." + type.name().toLowerCase());
    }

    public NegativeEffectType getType() {
        return type;
    }

    public int getStrength() {
        return strength;
    }

    /**
     * Creates a random negative effect appropriate for a pathway and sequence
     */
    public static NegativeEffect createRandom(String pathway, int sequence, Random random) {
        // Lower sequence = stronger negative effect
        int strength = 9 - sequence;
        
        // Pathway-specific effects
        NegativeEffectType[] pathwayEffects = getPathwayEffects(pathway);
        
        // Mix pathway-specific with generic effects
        NegativeEffectType chosenType;
        if (pathwayEffects.length > 0) {
            chosenType = pathwayEffects[random.nextInt(pathwayEffects.length)];
        } else {
            chosenType = NegativeEffectType.getGenericEffect(random);
        }

        return new NegativeEffect(chosenType, strength, null, strength / 3);
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
            NegativeEffectType[] generic = {DRAIN_HEALTH, DRAIN_HUNGER, POTION_EFFECT};
            return generic[random.nextInt(generic.length)];
        }
    }
}