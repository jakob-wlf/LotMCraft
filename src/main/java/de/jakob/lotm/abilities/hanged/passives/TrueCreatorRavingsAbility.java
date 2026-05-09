package de.jakob.lotm.abilities.hanged.passives;

import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.abilities.hanged.HangedPathwayConstants;
import de.jakob.lotm.abilities.hanged.ListeningAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TrueCreatorRavingsAbility extends PassiveAbilityItem {
    private static final Map<UUID, Long> NEXT_RAVING_TICK = new HashMap<>();
    private static final Map<UUID, WhisperEpisode> ACTIVE_EPISODES = new HashMap<>();

    private static final String PULSE_MESSAGE_KEY = "ability.lotmcraft.true_creator_ravings.pulse";

    public TrueCreatorRavingsAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of(HangedPathwayConstants.PATHWAY_ID, HangedPathwayConstants.SEQUENCE_LISTENER);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof ServerPlayer player)) {
            return;
        }

        if (!shouldHearRavings(player)) {
            NEXT_RAVING_TICK.remove(player.getUUID());
            ACTIVE_EPISODES.remove(player.getUUID());
            return;
        }

        long gameTime = serverLevel.getGameTime();
        NEXT_RAVING_TICK.putIfAbsent(player.getUUID(), gameTime + HangedPathwayConstants.TRUE_CREATOR_RAVING_CYCLE_TICKS);

        WhisperEpisode activeEpisode = ACTIVE_EPISODES.get(player.getUUID());
        if (activeEpisode != null) {
            if (gameTime >= activeEpisode.nextPulseTick()) {
                player.getData(ModAttachments.SANITY_COMPONENT).decreaseSanityAndSync(
                        activeEpisode.intensity().nextSanityLoss(random), player);
                AbilityUtil.sendActionBar(player, createGlitchedMessage(Component.translatable(PULSE_MESSAGE_KEY)));

                if (activeEpisode.remainingPulses() <= 1) {
                    ACTIVE_EPISODES.remove(player.getUUID());
                } else {
                    ACTIVE_EPISODES.put(player.getUUID(), activeEpisode.advance(gameTime, random));
                }
            }
            return;
        }

        long nextRavingTick = NEXT_RAVING_TICK.get(player.getUUID());
        if (gameTime < nextRavingTick) {
            return;
        }

        WhisperIntensity intensity = WhisperIntensity.random(random);
        WhisperEpisode episode = WhisperEpisode.create(gameTime, intensity, random);
        ACTIVE_EPISODES.put(player.getUUID(), episode);
        NEXT_RAVING_TICK.put(player.getUUID(), gameTime + HangedPathwayConstants.TRUE_CREATOR_RAVING_CYCLE_TICKS);

        player.sendSystemMessage(createGlitchedMessage(Component.translatable(intensity.messageKey())));
    }

    private static boolean shouldHearRavings(ServerPlayer player) {
        int sequence = BeyonderData.getSequence(player);
        if (sequence == HangedPathwayConstants.SEQUENCE_LISTENER) {
            return true;
        }

        return sequence <= HangedPathwayConstants.SEQUENCE_SHADOW_ASCETIC && ListeningAbility.isListening(player);
    }

    private static Component createGlitchedMessage(Component body) {
        int pathwayColor = HangedPathwayConstants.pathwayColor();
        return Component.empty()
                .append(Component.literal("████").withStyle(ChatFormatting.OBFUSCATED).withColor(pathwayColor))
                .append(Component.literal(" "))
                .append(body.copy().withStyle(ChatFormatting.DARK_RED).withColor(pathwayColor))
                .append(Component.literal(" "))
                .append(Component.literal("████").withStyle(ChatFormatting.OBFUSCATED).withColor(pathwayColor));
    }

    private record WhisperEpisode(WhisperIntensity intensity, int remainingPulses, long nextPulseTick) {
        private static WhisperEpisode create(long gameTime, WhisperIntensity intensity, java.util.Random random) {
            return new WhisperEpisode(
                    intensity,
                    intensity.pulseCount(random),
                    gameTime + intensity.nextPulseInterval(random)
            );
        }

        private WhisperEpisode advance(long gameTime, java.util.Random random) {
            return new WhisperEpisode(
                    intensity,
                    remainingPulses - 1,
                    gameTime + intensity.nextPulseInterval(random)
            );
        }
    }

    private enum WhisperIntensity {
        LIGHT("ability.lotmcraft.true_creator_ravings.light", 3, 5, 80, 180, 0.0025f, 0.0045f),
        MILD("ability.lotmcraft.true_creator_ravings.mild", 4, 6, 60, 140, 0.0045f, 0.0085f),
        INTENSE("ability.lotmcraft.true_creator_ravings.intense", 5, 7, 40, 100, 0.0085f, 0.0145f);

        private final String messageKey;
        private final int minPulses;
        private final int maxPulses;
        private final int minIntervalTicks;
        private final int maxIntervalTicks;
        private final float minSanityLoss;
        private final float maxSanityLoss;

        WhisperIntensity(String messageKey, int minPulses, int maxPulses, int minIntervalTicks, int maxIntervalTicks,
                         float minSanityLoss, float maxSanityLoss) {
            this.messageKey = messageKey;
            this.minPulses = minPulses;
            this.maxPulses = maxPulses;
            this.minIntervalTicks = minIntervalTicks;
            this.maxIntervalTicks = maxIntervalTicks;
            this.minSanityLoss = minSanityLoss;
            this.maxSanityLoss = maxSanityLoss;
        }

        private static WhisperIntensity random(java.util.Random random) {
            WhisperIntensity[] values = values();
            return values[random.nextInt(values.length)];
        }

        private String messageKey() {
            return messageKey;
        }

        private int pulseCount(java.util.Random random) {
            return minPulses + random.nextInt(maxPulses - minPulses + 1);
        }

        private long nextPulseInterval(java.util.Random random) {
            return minIntervalTicks + random.nextInt(maxIntervalTicks - minIntervalTicks + 1);
        }

        private float nextSanityLoss(java.util.Random random) {
            return minSanityLoss + random.nextFloat() * (maxSanityLoss - minSanityLoss);
        }
    }
}
