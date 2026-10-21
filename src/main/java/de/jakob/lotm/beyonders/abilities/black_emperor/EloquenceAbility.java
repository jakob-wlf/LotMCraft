package de.jakob.lotm.beyonders.abilities.black_emperor;

import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber
public class EloquenceAbility extends ToggleAbility {

    private static final RandomSource RANDOM = RandomSource.create();

    private static final Set<UUID> ACTIVE_USERS = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Long> CHAT_COOLDOWN = new ConcurrentHashMap<>();

    private static final long CHAT_COOLDOWN_MS = 2500L;
    private static final double RANGE = 16.0;
    private static final int EFFECT_DURATION = 200;

    private static final List<String> PERSUASIVE_WORDS = List.of(
            "please", "trust", "believe", "friend", "ally", "help",
            "listen", "understand", "reasonable", "sensible", "surely",
            "obviously", "of course", "come on", "think", "suppose",
            "agree", "together", "share", "surely you"
    );

    private static final List<String> COMMANDING_WORDS = List.of(
            "drop", "drop it", "give", "hand over", "release",
            "yield", "surrender", "obey", "let go"
    );

    public EloquenceAbility(String id) {
        super(id);
        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(200f,100f,75f,35f,25f,11.5f,8f,4.5f,2f, 0.9f));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of("black_emperor", 9);
    }

    @Override
    public float getSpiritualityCost() {

        return 0;
    }

    @Override
    public void tick(Level level, LivingEntity entity) {}

    @Override
    public void start(Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            ACTIVE_USERS.remove(player.getUUID());
            ACTIVE_USERS.add(player.getUUID());
            level.playSound(null, player.getOnPos(), SoundEvents.WITCH_CELEBRATE, SoundSource.NEUTRAL, 1f, 1f);
        }
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            ACTIVE_USERS.remove(player.getUUID());
            CHAT_COOLDOWN.remove(player.getUUID());
        }
    }

    public static boolean isEloquenceActive(ServerPlayer player) {
        return ACTIVE_USERS.contains(player.getUUID());
    }

    //   HELPERS
    private static double analyzeMessage(String raw) {
        String msg = raw.toLowerCase(Locale.ROOT).trim();
        double score = 0.0;
        if (msg.length() < 4) return score;

        // Length contribution (up to 0.3), effort counts for something
        score += Math.min(0.3, msg.length() / 100.0);

        // Keyword contribution
        int keywordHits = 0;
        for (String word : PERSUASIVE_WORDS) {
            if (msg.contains(word)) keywordHits++;
        }
        score += Math.min(0.6, keywordHits * 0.2);

        // Punctuation signals: exclamation or question marks
        if (msg.contains("!")) score += 0.05;
        if (msg.contains("?")) score += 0.05;

        return Math.min(1.0, score);
    }

    private static boolean containsAny(String msg) {
        String lower = msg.toLowerCase(Locale.ROOT);
        for (String w : COMMANDING_WORDS) {
            if (lower.contains(w)) return true;
        }
        return false;
    }
    
    private static void forcefullyAlly(LivingEntity target, LivingEntity caster, double persuasiveness) {
        if (target instanceof Mob mob) {
            mob.setTarget(null);
        }
        int duration = (int) (EFFECT_DURATION * (0.5 + persuasiveness));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 1));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 1));
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, duration, 1));
        target.sendSystemMessage(Component.literal("You are experiencing hesitation with the magic of " + caster.getDisplayName().getString() + "'s words").withStyle(ChatFormatting.DARK_GRAY));
        caster.sendSystemMessage(Component.literal("Your words caused "+target.getDisplayName().getString() + " to tremble").withStyle(ChatFormatting.DARK_GRAY));
    }

    private static void applyVillagerDiscount(Villager villager, LivingEntity player, int amplifier) {
        GossipContainer gossip = villager.getGossips();
        gossip.add(player.getUUID(), GossipType.MAJOR_POSITIVE, 20*(amplifier+1));
        ServerScheduler.scheduleDelayed(10*20, () -> gossip.remove(player.getUUID(), GossipType.MAJOR_POSITIVE));
        player.sendSystemMessage(Component.literal("You successfully persuaded the villager to give you a discount").withStyle(ChatFormatting.DARK_GRAY));
    }

    private static void attemptDisarm(LivingEntity caster, LivingEntity target) {
        ItemStack mainHand = target.getMainHandItem();
        if (mainHand.isEmpty()) return;
        ItemStack toDrop = mainHand.copy();
        target.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        target.spawnAtLocation(toDrop, 1.0f);
        target.sendSystemMessage(Component.literal("You were influenced by "+caster.getDisplayName().getString() + " and dropped your stuff").withStyle(ChatFormatting.DARK_GRAY));
        caster.sendSystemMessage(Component.literal("You successfully persuaded " + target.getDisplayName().getString() + " to drop his stuff").withStyle(ChatFormatting.DARK_GRAY));
    }

    @SubscribeEvent
    public static void chat(ServerChatEvent event) {
        ServerPlayer speaker = event.getPlayer();
        String message = event.getRawText();

        if (!isEloquenceActive(speaker)) return;
        if (message.isBlank()) return;

        long now = System.currentTimeMillis();
        Long last = CHAT_COOLDOWN.get(speaker.getUUID());
        if (last != null && now - last < CHAT_COOLDOWN_MS) return;

        Level level = speaker.level();
        if (level.isClientSide()) return;

        LivingEntity target = AbilityUtil.getTargetEntity(speaker, (int) RANGE, (float) RANGE/2);
        if (target == null || !target.isAlive()) return;

        double persuasiveness = analyzeMessage(message);
        if (persuasiveness <= 0.0) return;

        // Consume the cooldown only on an actual attempt
        CHAT_COOLDOWN.put(speaker.getUUID(), now);

        // 1. Villager discount
        if (target instanceof Villager villager) {
            double discountChance = 0.30 + persuasiveness * 0.50;
            if (RANDOM.nextDouble() < discountChance) {

                applyVillagerDiscount(villager, speaker, Math.min(3, (int) Math.round(persuasiveness * 3)));
            }
            return;
        }

        int casterSeq = BeyonderData.getSequence(speaker);
        int targetSeq = BeyonderData.getSequence(target);

        double multiplier = calculateMultiplier(casterSeq, targetSeq);

        // 2. Force-ally
        double allyChance = (0.15 + persuasiveness * 0.35) * multiplier;
        if (RANDOM.nextDouble() < allyChance) {
            forcefullyAlly(target, speaker, persuasiveness);
        }


        // 3. Disarm
        double disarmChance = (0.01 + persuasiveness * 0.06) * multiplier;
        if (containsAny(message)) {
            disarmChance += 0.18;
        }

        if (RANDOM.nextDouble() < disarmChance) {
            attemptDisarm(speaker, target);
        }
    }

    public static double calculateMultiplier(int casterSeq, int targetSeq) {
        int diff = AbilityUtil.getSequenceDifference(
                AbilityUtil.getSequenceCategory(casterSeq),
                AbilityUtil.getSequenceCategory(targetSeq)
        );

        double tierMultiplier;
        if (diff >= 4) {
            tierMultiplier = 0.0;      // seq 9-5 vs seq 0 no chance
        } else if (diff == 3) {
            tierMultiplier = 0.03;     // seq 9-5 vs seq 1 or seq 3-4 vs seq 0 near immune
        } else if (diff == 2) {
            tierMultiplier = 0.09;     // 9-5 vs 2 or 3-4 vs 1 or 2 vs 0 strong resistance
        } else if (diff == 1) {
            tierMultiplier = 0.30;     // 9-5 vs 3-4 or 3-4 vs 2 or 2 vs 1 or 1 vs 0 mid resistance
        } else if (diff == 0) {
            if((targetSeq <= 7 && targetSeq >= 5) && (casterSeq >= 8)) tierMultiplier = 0.3; // 9-8 vs 7-5
            else tierMultiplier = 1.0;      // full effect
        } else {
            tierMultiplier = 1.0 + Math.min(0.5, -diff * 0.15); // Bonus effect
        }
        if(targetSeq == 0 && casterSeq != 0) tierMultiplier = 0.0;
        return tierMultiplier;
    }
}