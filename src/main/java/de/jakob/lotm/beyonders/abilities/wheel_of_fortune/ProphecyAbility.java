package de.jakob.lotm.beyonders.abilities.wheel_of_fortune;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.AbilityUseEvent;
import de.jakob.lotm.beyonders.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.beyonders.abilities.door.SpaceConcealmentAbility;
import de.jakob.lotm.beyonders.abilities.error.ParasitationAbility;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.calamities.Calamity;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.calamities.Earthquake;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.calamities.Meteor;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.calamities.Tornado;
import de.jakob.lotm.beyonders.sefirah.SefirahHandler;
import de.jakob.lotm.beyonders.sefirah.SefirotAuthorityManager;
import de.jakob.lotm.beyonders.sefirah.SefrotConvergenceHandler;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.events.custom.TargetEntityEvent;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.LuckManager;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ProphecyAbility extends ToggleAbility {
    private static final int nearbyRange = 64;
    private static final int revealRange = 128;
    private static final float effectTicksPerLuck = 20f * 60f / 1000f;
    private static final float statuePunishmentStrength = 0.85f;
    private static final int maxDelayMinutes = 60;
    private static final String targetCooldownsKey = "lotm_prophecy_target_cooldowns";
    private static final Pattern prophecyPattern = Pattern.compile(
            "^(.+?)\\s+will\\s+be\\s+affected\\s+by\\s+"
                    + "(fortune|misfortune|disaster|convergence|convergance)\\s+"
                    + "at\\s+the\\s+price\\s+of\\s+(\\d+)\\s+luck"
                    + "(?:\\s+in\\s+(\\d+)\\s+(?:min|mins|minute|minutes))?$",
            Pattern.CASE_INSENSITIVE);
    private static final Calamity[] calamities = {new Tornado(), new Earthquake(), new Meteor()};
    private static final Map<UUID, ProphecyEffect> activeEffects = new HashMap<>();
    private static final Map<UUID, PendingProphecy> pendingProphecies = new HashMap<>();
    private static final Map<UUID, UUID> writingPlayers = new HashMap<>();
    private static final Map<UUID, CastOpportunity> activeCastOpportunities = new HashMap<>();
    private static final Map<UUID, CalamitousDebuff> calamitousDebuffs = new HashMap<>();
    private static final java.util.Set<UUID> redirectedDamageTargets = new java.util.HashSet<>();

    public ProphecyAbility(String id) {
        super(id);
        canBeShared = false;
        canBeCopied = false;
        canBeReplicated = false;
        cannotBeStolen = true;
        canBeUsedInArtifact = false;
        tickRate = 20;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("wheel_of_fortune", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (level.isClientSide()) {
            if (entity.isShiftKeyDown()) {
                ClientHandler.openProphecyExplanation();
            }
            return;
        }
        if (!(entity instanceof ServerPlayer player)) return;
        if (entity.isShiftKeyDown()) {
            cancel((ServerLevel) level, entity);
            return;
        }
        writingPlayers.put(player.getUUID(), player.getUUID());
        player.sendSystemMessage(Component.literal(
            "\u00A76Write: <target> will be affected by <modifier> at the price of <amount> luck [in <minutes> mins]"));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        writingPlayers.remove(entity.getUUID());
        clearArtifactScaling(entity);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onChat(ServerChatEvent event) {
        ServerPlayer author = event.getPlayer();
        if (!writingPlayers.containsKey(author.getUUID())) return;

        event.setCanceled(true);
        Matcher matcher = prophecyPattern.matcher(event.getRawText().trim());
        if (!matcher.matches()) {
            fail(author, "Invalid prophecy. Use: <target> will be affected by <modifier> at the price of <amount> luck [in <minutes> mins]");
            return;
        }

        int amount;
        int delayMinutes;
        try {
            amount = Integer.parseInt(matcher.group(3));
            delayMinutes = matcher.group(4) == null ? 0 : Integer.parseInt(matcher.group(4));
        } catch (NumberFormatException ignored) {
            fail(author, "The luck price or timer is too large.");
            return;
        }
        if (delayMinutes < 0 || delayMinutes > maxDelayMinutes) {
            fail(author, "The prophecy timer must be between 0 and " + maxDelayMinutes + " minutes.");
            return;
        }
        if (amount <= 0 || LuckManager.getLuck(author) < LuckManager.getLuckCost(author, amount)) {
            fail(author, "You do not have enough positive luck for that prophecy.");
            return;
        }

        List<ServerPlayer> resolvedTargets = resolveTargets(author, matcher.group(1).trim());
        if (resolvedTargets.isEmpty()) return;

        List<ServerPlayer> reachableTargets = resolvedTargets.stream()
            .filter(target -> !isProphecyProtected(author, target))
            .toList();
        int protectedTargets = resolvedTargets.size() - reachableTargets.size();
        if (protectedTargets > 0) {
            author.sendSystemMessage(Component.literal("\u00A7e" + protectedTargets
                + " target(s) could not be reached by prophecy."));
        }
        if (reachableTargets.isEmpty()) {
            fail(author, "No selected target can currently be reached by prophecy. No luck was spent.");
            return;
        }

        long gameTime = author.serverLevel().getGameTime();
        List<ServerPlayer> targets = reachableTargets.stream()
            .filter(target -> !isTargetOnCooldown(author, target, gameTime))
            .filter(target -> !hasPendingOrActiveProphecy(target))
            .toList();
        if (targets.isEmpty()) {
            fail(author, "Every selected target is on cooldown or already has a prophecy.");
            sendShortestCooldown(author, resolvedTargets, gameTime);
            return;
        }
        if (targets.size() < resolvedTargets.size()) {
            author.sendSystemMessage(Component.literal("\u00A7eSkipped "
                + (resolvedTargets.size() - targets.size())
                + " target(s) on cooldown or already holding a prophecy."));
        }

        Modifier modifier = Modifier.fromInput(matcher.group(2));
        int paidLuck = LuckManager.getLuckCost(author, amount);
        if (!LuckManager.consumeLuck(author, amount)) {
            fail(author, "The prophecy could not consume its luck price.");
            return;
        }

        List<TargetProphecy> assessedTargets = targets.stream()
            .map(target -> assessProphecy(author, target, modifier, paidLuck))
            .toList();
        List<TargetProphecy> fundedTargets = assessedTargets.stream()
                .filter(TargetProphecy::funded)
                .toList();
        int resistedTargets = (int) assessedTargets.stream()
            .filter(prophecy -> !prophecy.funded() && !prophecy.chanceFailed())
            .count();
        int chanceFailures = (int) assessedTargets.stream()
            .filter(TargetProphecy::chanceFailed)
            .count();
        if (resistedTargets > 0) {
            int lowestRequiredLuck = assessedTargets.stream()
                .filter(prophecy -> !prophecy.funded())
                .mapToInt(TargetProphecy::requiredLuck)
                .min()
                .orElse(amount + 1);
            author.sendSystemMessage(Component.literal("\u00A7e" + resistedTargets
                + " target(s) resisted the underfunded prophecy. At least "
                + lowestRequiredLuck + " luck was required."));
        }
        if (chanceFailures > 0) {
            author.sendSystemMessage(Component.literal("\u00A7c" + chanceFailures
                    + " high-authority target(s) resisted the prophecy despite its funding."));
        }
        if (fundedTargets.isEmpty()) {
            fail(author, "The paid luck was insufficient to affect any selected target.");
            return;
        }

        long activationDelay = delayMinutes * 20L * 60;
        fundedTargets.forEach(prophecy -> {
            ServerPlayer target = prophecy.target();
            if (activationDelay == 0) {
                activateProphecy(author, target, modifier, paidLuck, prophecy.strength(), prophecy.tier());
            } else {
                pendingProphecies.put(target.getUUID(), new PendingProphecy(
                        author.getUUID(), modifier, paidLuck, prophecy.strength(), prophecy.tier(),
                        gameTime + activationDelay));
            }
        });
        if (activationDelay > 0) {
            author.sendSystemMessage(Component.literal("\u00A76" + fundedTargets.size()
                    + " prophecy(s) will take hold in " + delayMinutes + " minute(s)."));
        }
    }

    private static boolean hasPendingOrActiveProphecy(ServerPlayer target) {
        return pendingProphecies.containsKey(target.getUUID()) || activeEffect(target) != null;
    }

    private static boolean isProphecyProtected(ServerPlayer author, ServerPlayer target) {
        var dimension = target.level().dimension();
        return SefirotAuthorityManager.isSefirotDimension(dimension)
                || dimension.equals(ModDimensions.CONCEALMENT_WORLD_DIMENSION_KEY)
                || dimension.equals(ModDimensions.DREAM_MAZE_DIMENSION_KEY)
                || dimension.equals(ModDimensions.SPACE_DIMENSION_KEY)
                || dimension.equals(ModDimensions.MAUSOLEUM_DIMENSION_KEY)
                || dimension.equals(ModDimensions.SPACE_TIME_LABYRINTH_DIMENSION_KEY)
                || dimension.equals(ModDimensions.WORLD_CREATION_DIMENSION_KEY)
                || dimension.equals(ModDimensions.SPIRIT_WORLD_DIMENSION_KEY)
                || ParasitationAbility.isConcealed(target.getUUID())
                || SpaceConcealmentAbility.isInsideConcealedSpace(target.serverLevel(), target.position())
                || SefirotAuthorityManager.riverConcealmentActive.contains(target.getUUID())
                || SefirotAuthorityManager.blocksConcealment(target.getUUID(), author);
    }

    private static void activateProphecy(ServerPlayer author, ServerPlayer target, Modifier modifier,
                                         int paidLuck, float strength, EffectTier tier) {
        applyProphecy(author, target, modifier, paidLuck, strength, tier);
        long cooldown = getTargetCooldownTicks(author);
        if (target == author) cooldown = Math.max(1, cooldown / 2);
        setTargetCooldown(author, target, target.serverLevel().getGameTime() + cooldown);
    }

    private static boolean isTargetOnCooldown(ServerPlayer author, ServerPlayer target, long gameTime) {
        CompoundTag cooldowns = author.getPersistentData().getCompound(targetCooldownsKey);
        String targetKey = target.getUUID().toString();
        long expiresAt = cooldowns.getLong(targetKey);
        if (expiresAt <= gameTime) {
            cooldowns.remove(targetKey);
            author.getPersistentData().put(targetCooldownsKey, cooldowns);
            return false;
        }
        return true;
    }

    private static void setTargetCooldown(ServerPlayer author, ServerPlayer target, long expiresAt) {
        CompoundTag cooldowns = author.getPersistentData().getCompound(targetCooldownsKey);
        cooldowns.putLong(target.getUUID().toString(), expiresAt);
        author.getPersistentData().put(targetCooldownsKey, cooldowns);
    }

    private static long getTargetCooldownTicks(ServerPlayer author) {
        int sequence = BeyonderData.getSequence(author);
        if (sequence <= 0 && "key_of_light".equals(SefirahHandler.getClaimedSefirot(author))) {
            return 20L * 60 * 5;
        }
        return switch (sequence) {
            case 0 -> 20L * 60 * 10;
            case 1 -> 20L * 60 * 20;
            default -> 20L * 60 * 30;
        };
    }

    private static void sendShortestCooldown(ServerPlayer author, List<ServerPlayer> targets, long gameTime) {
        CompoundTag cooldowns = author.getPersistentData().getCompound(targetCooldownsKey);

        long remainingTicks = targets.stream()
            .mapToLong(target -> Math.max(0, cooldowns.getLong(target.getUUID().toString()) - gameTime))
                .filter(remaining -> remaining > 0)
                .min()
                .orElse(0);
        if (remainingTicks > 0) {
            long remainingSeconds = (remainingTicks + 19) / 20;
            author.sendSystemMessage(Component.literal("\u00A7eThe next selected target becomes available in "
                    + (remainingSeconds / 60) + "m " + (remainingSeconds % 60) + "s."));
        }
    }

    private static List<ServerPlayer> resolveTargets(ServerPlayer author, String targetInput) {
        if (targetInput.equalsIgnoreCase("all")) {
            boolean allowed = BeyonderData.getSequence(author) == 0
                    && "key_of_light".equals(SefirahHandler.getClaimedSefirot(author));
            if (!allowed) {
                fail(author, "Targeting All requires Sequence 0 and ownership of the Key of Light.");
                return List.of();
            }
            return new ArrayList<>(author.getServer().getPlayerList().getPlayers());
        }

        if (targetInput.equalsIgnoreCase("nearby")) {
            List<ServerPlayer> targets = author.serverLevel().getPlayers(player ->
                    player != author && player.distanceToSqr(author) <= nearbyRange * nearbyRange);
            if (targets.isEmpty()) fail(author, "No nearby players were found.");
            return targets;
        }

        ServerPlayer target = author.getServer().getPlayerList().getPlayerByName(targetInput);
        if (target == null) {
            fail(author, "No online player named " + targetInput + " was found.");
            return List.of();
        }
        return List.of(target);
    }

    private static TargetProphecy assessProphecy(ServerPlayer author, ServerPlayer target,
                                                  Modifier modifier, int amount) {
        int maximumLuck = Math.max(1, LuckManager.getMaximumLuck(author));
        String targetSefirot = SefirahHandler.getClaimedSefirot(target);
        boolean sefirotOwner = !targetSefirot.isEmpty();
        boolean higherSequence = BeyonderData.isBeyonder(target)
                && BeyonderData.getSequence(target) < BeyonderData.getSequence(author);
        if (sefirotOwner || higherSequence) {
            float requiredRatio = sefirotOwner ? 1f : 0.8f;
            int requiredLuck = Math.max(1, (int) Math.ceil(maximumLuck * requiredRatio));
            float investment = amount / (float) maximumLuck;
            if (amount < requiredLuck) {
                return new TargetProphecy(target, 0, EffectTier.HIGH, requiredLuck, false, false);
            }

            float successChance;
            if (sefirotOwner && higherSequence) {
                successChance = 0.05f;
            } else if (sefirotOwner) {
                successChance = 0.1f;
            } else {
                float extraFunding = Math.clamp((investment - 0.8f) / 0.2f, 0f, 1f);
                successChance = 0.15f + 0.15f * extraFunding;
            }
            if (author.getRandom().nextFloat() >= successChance) {
                return new TargetProphecy(target, 0, EffectTier.HIGH, requiredLuck, false, true);
            }
            return new TargetProphecy(target, Math.clamp(investment, 0.8f, 1f),
                    EffectTier.HIGH, requiredLuck, true, false);
        }

        float priceMultiplier = 1f;
        double sequenceResistance = AbilityUtil.getSequenceResistanceFactor(author, target);
        if (sequenceResistance > 0) {
            priceMultiplier /= (float) (1 - sequenceResistance);
        }

        float fullStrengthPrice = maximumLuck * priceMultiplier;
        float investment = amount / fullStrengthPrice;
        int requiredLuck = Math.max(1, (int) Math.ceil(fullStrengthPrice * modifier.minimumInvestment));
        if (investment < modifier.minimumInvestment) {
            return new TargetProphecy(target, 0, EffectTier.LOW, requiredLuck, false, false);
        }

        EffectTier tier = investment >= EffectTier.HIGH.minimumInvestment
                ? EffectTier.HIGH
                : investment >= EffectTier.MEDIUM.minimumInvestment ? EffectTier.MEDIUM : EffectTier.LOW;
        return new TargetProphecy(target, Math.clamp(investment, 0.01f, 1f), tier, requiredLuck, true, false);
    }

    private static void applyProphecy(ServerPlayer author, ServerPlayer target, Modifier modifier,
                                      int paidLuck, float strength, EffectTier tier) {
        long duration = Math.max(1, Math.round(paidLuck * effectTicksPerLuck));

        if (modifier == Modifier.DISASTER) {
            Calamity calamity = calamities[author.getRandom().nextInt(calamities.length)];
            float multiplier = (float) BeyonderData.getMultiplier(author) * (0.5f + 2.5f * strength);
            calamity.spawnCalamity(target.serverLevel(), target.position(), multiplier,
                    BeyonderData.isGriefingEnabled(author), BeyonderData.getSequence(author));
                target.sendSystemMessage(Component.literal("\u00A76A prophecy has taken hold."));
                author.sendSystemMessage(Component.literal("\u00A76" + target.getName().getString()
                    + " receives an immediate " + calamity.getClass().getSimpleName() + " disaster."));
            return;
        }

            if (modifier == Modifier.CONVERGENCE) {
                SefrotConvergenceHandler.ConvergenceResult result =
                    SefrotConvergenceHandler.triggerManualConvergence(target);
                switch (result) {
                case SUCCESS -> {
                    target.sendSystemMessage(Component.literal("\u00A76A prophecy has taken hold."));
                    if (author != target) {
                    author.sendSystemMessage(Component.literal("\u00A76" + target.getName().getString()
                        + " experienced a guaranteed Sefirot convergence."));
                    }
                }
                case NO_PIECES -> author.sendSystemMessage(Component.literal("\u00A7e"
                    + target.getName().getString()
                    + " carries no eligible Sefirot piece associated with their pathway."));
                case NO_TARGET -> author.sendSystemMessage(Component.literal("\u00A7eNo online player in "
                    + target.getName().getString()
                    + "'s dimension carries a missing piece of their eligible Sefirot."));
                case CHANCE_FAILED -> throw new IllegalStateException(
                    "Guaranteed Sefirot convergence unexpectedly failed its chance roll");
                }
                return;
            }

        Outcome outcome = switch (modifier) {
            case FORTUNE -> randomOutcome(author, tier.fortuneOutcomes);
            case MISFORTUNE -> randomOutcome(author, tier.misfortuneOutcomes);
                case CONVERGENCE, DISASTER -> throw new IllegalStateException("Immediate prophecy reached timed outcome selection");
        };
        LuckManager.clearLuckDrain(target, LuckManager.prophecySource);
        LuckManager.clearLuckGain(target, LuckManager.borrowedTomorrowSource);
        LuckManager.clearLuckDrain(target, LuckManager.borrowedTomorrowSource);
        LuckManager.clearLuckGain(target, LuckManager.debtOfYesterdaySource);
        LuckManager.clearLuckDrain(target, LuckManager.debtOfYesterdaySource);
        long gameTime = target.serverLevel().getGameTime();
        ProphecyEffect effect = new ProphecyEffect(modifier, outcome, strength, gameTime + duration);
        activeEffects.put(target.getUUID(), effect);
        if (outcome == Outcome.LUCK_DRAIN) {
            float drainRate = paidLuck * 20f * 60f / duration;
            LuckManager.applyLuckDrain(author, target, LuckManager.prophecySource, drainRate, duration);
        } else if (outcome == Outcome.BORROWED_TOMORROW) {
            long bonusDuration = Math.max(20, duration / 2);
            effect.borrowedTransitionAt = gameTime + bonusDuration;
            effect.borrowedAmount = Math.max(1, Math.round(
                    LuckManager.getMaximumLuck(target) * (0.25f + 0.5f * strength)));
            float gainRate = effect.borrowedAmount * 20f * 60f / bonusDuration;
            LuckManager.applyLuckGain(target, LuckManager.borrowedTomorrowSource, gainRate, bonusDuration);
            } else if (outcome == Outcome.DEBT_OF_YESTERDAY) {
                long debtDuration = Math.max(20, duration / 2);
                effect.debtTransitionAt = gameTime + debtDuration;
                effect.debtAmount = Math.max(1, Math.round(
                    LuckManager.getMaximumLuck(target) * (0.75f + 0.75f * highTierScale(strength))));
                float drainRate = effect.debtAmount * 20f * 60f / debtDuration;
                LuckManager.applyLuckDrain(author, target, LuckManager.debtOfYesterdaySource, drainRate, debtDuration);
        }

        long durationSeconds = duration / 20;
        String durationText = (durationSeconds / 60) + "m " + (durationSeconds % 60) + "s";
        target.sendSystemMessage(Component.literal("\u00A76A prophecy has taken hold."));
        author.sendSystemMessage(Component.literal("\u00A76" + target.getName().getString()
            + " receives " + outcome.description + " for " + durationText + "."));
        if (outcome == Outcome.EXPOSURE) revealCoordinates(target);
    }

    private static Outcome randomOutcome(ServerPlayer author, Outcome[] outcomes) {
        return outcomes[author.getRandom().nextInt(outcomes.length)];
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) return;
        if (player.tickCount % 20 == 0) {
            PendingProphecy pending = pendingProphecies.get(player.getUUID());
            if (pending != null && player.serverLevel().getGameTime() >= pending.activatesAt) {
                ServerPlayer author = player.server.getPlayerList().getPlayer(pending.authorUUID);
                if (author != null) {
                    pendingProphecies.remove(player.getUUID());
                    if (isProphecyProtected(author, player)) {
                        author.sendSystemMessage(Component.literal("\u00A7eThe delayed prophecy on "
                                + player.getName().getString() + " dissolved before it could take hold."));
                    } else {
                        activateProphecy(author, player, pending.modifier, pending.paidLuck,
                                pending.strength, pending.tier);
                    }
                }
            }
        }
        ProphecyEffect effect = activeEffect(player);
        if (effect == null || player.tickCount % 20 != 0) return;

        if (effect.hasOutcome(Outcome.EXPOSURE) && player.tickCount % 200 == 0) {
            revealCoordinates(player);
        } else if (effect.outcome == Outcome.FORTUNATE_RECOVERY) {
            player.heal(Math.max(1f, player.getMaxHealth() * 0.02f));
            float maximumSpirituality = BeyonderData.getMaxSpirituality(
                    BeyonderData.getPathway(player), BeyonderData.getSequence(player), player);
            BeyonderData.incrementSpirituality(player, maximumSpirituality * 0.01f);
        } else if (effect.outcome == Outcome.BORROWED_TOMORROW
                && !effect.borrowedDebtStarted
                && player.serverLevel().getGameTime() >= effect.borrowedTransitionAt) {
            effect.borrowedDebtStarted = true;
            LuckManager.clearLuckGain(player, LuckManager.borrowedTomorrowSource);
            long debtDuration = Math.max(20, effect.expiresAt - player.serverLevel().getGameTime());
            float drainRate = effect.borrowedAmount * 20f * 60f / debtDuration;
            LuckManager.applyLuckDrain(player, LuckManager.borrowedTomorrowSource, drainRate, debtDuration);
            player.sendSystemMessage(Component.literal("\u00A76Borrowed Tomorrow has come due."));
            } else if (effect.hasOutcome(Outcome.DEBT_OF_YESTERDAY)
                && !effect.debtRecoveryStarted
                && player.serverLevel().getGameTime() >= effect.debtTransitionAt) {
                effect.debtRecoveryStarted = true;
                LuckManager.clearLuckDrain(player, LuckManager.debtOfYesterdaySource);
                long recoveryDuration = Math.max(20, effect.expiresAt - player.serverLevel().getGameTime());
                float gainRate = effect.debtAmount * 20f * 60f / recoveryDuration;
                LuckManager.applyLuckGain(player, LuckManager.debtOfYesterdaySource, gainRate, recoveryDuration);
                player.sendSystemMessage(Component.literal("\u00A76Debt of Yesterday begins returning what it took."));
            } else if (effect.hasOutcome(Outcome.SPIRITUAL_LEAKAGE)) {
                float maximum = BeyonderData.getMaxSpirituality(
                    BeyonderData.getPathway(player), BeyonderData.getSequence(player), player);
                BeyonderData.reduceSpirituality(player, maximum * (0.001f + 0.002f * effect.strength));
            } else if (effect.hasOutcome(Outcome.FALTERING_STEP) && player.tickCount % 100 == 0
                && player.getRandom().nextFloat() < 0.2f + 0.4f * effect.strength) {
                int duration = 30 + Math.round(50 * effect.strength);
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration,
                    effect.strength >= 0.7f ? 1 : 0, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration,
                    effect.strength >= 0.7f ? 1 : 0, false, true));
                } else if (effect.hasOutcome(Outcome.HOSTILE_ATTENTION) && player.tickCount % 100 == 0) {
                player.serverLevel().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(24),
                    mob -> mob instanceof Enemy && mob.isAlive()).forEach(mob -> mob.setTarget(player));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onAbilityUse(AbilityUseEvent event) {
        ProphecyEffect effect = activeEffect(event.getEntity());
        if (effect == null) return;
        if (effect.hasOutcome(Outcome.ABILITY_FAILURE)
                && event.getEntity().getRandom().nextFloat() < 0.15f + 0.65f * effect.strength) {
            event.setCanceled(true);
            AbilityUtil.sendActionBar(event.getEntity(), Component.literal("\u00A7cThe misfortune prophecy made your ability fail."));
        } else if (effect.hasOutcome(Outcome.CHOSEN_MISFORTUNE)) {
            event.setCanceled(true);
            activeEffects.remove(event.getEntity().getUUID());
            AbilityUtil.sendActionBar(event.getEntity(), Component.literal(
                "\u00A7cChosen Misfortune forced your ability to fail."));
        } else if (effect.outcome == Outcome.PERFECT_OPPORTUNITY
                || effect.outcome == Outcome.CHOSEN_OUTCOME) {
            activateCastOpportunity(event.getEntity(), effect);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onAbilityUsed(AbilityUsedEvent event) {
        LivingEntity entity = event.getEntity();
        ProphecyEffect effect = activeEffect(entity);
        if (effect == null) return;
        if (effect.hasOutcome(Outcome.BROKEN_THREAD)) {
            consumeCorrection(entity, effect, 1);
            AbilityUtil.sendActionBar(entity, Component.literal("\u00A7cBroken Thread weakened this ability. "
                + effect.charges + " charge(s) remain."));
        } else if (effect.hasOutcome(Outcome.TWISTED_OPPORTUNITY)
            || effect.hasOutcome(Outcome.CHOSEN_MISFORTUNE)) {
            activeEffects.remove(entity.getUUID());
            AbilityUtil.sendActionBar(entity, Component.literal("\u00A7cMisfortune twisted this opportunity."));
        } else if (effect.hasOutcome(Outcome.CALAMITOUS_OPENING)) {
            activeEffects.remove(entity.getUUID());
            int debuffDuration = 20 * 10;
            long expiresAt = entity.level().getGameTime() + debuffDuration;
            calamitousDebuffs.put(entity.getUUID(), new CalamitousDebuff(
                expiresAt, 2f + highTierScale(effect.strength)));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                debuffDuration, 2 + Math.round(highTierScale(effect.strength)), false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,
                debuffDuration, 1 + Math.round(highTierScale(effect.strength)), false, true));
            AbilityUtil.sendActionBar(entity, Component.literal("\u00A7cYour successful ability left a calamitous opening."));
        }
    }

    private static void activateCastOpportunity(LivingEntity entity, ProphecyEffect effect) {
        activeCastOpportunities.put(entity.getUUID(), new CastOpportunity(
                entity.level().getGameTime(), 1.35f + 0.65f * effect.strength));
        activeEffects.remove(entity.getUUID());
        AbilityUtil.sendActionBar(entity, Component.literal("\u00A7ePerfect Opportunity: this ability costs no spirituality and is strengthened."));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onFortunateDamage(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        CalamitousDebuff debuff = calamitousDebuffs.get(target.getUUID());
        if (debuff != null) {
            if (target.level().getGameTime() < debuff.expiresAt) {
                event.setNewDamage(event.getNewDamage() * debuff.damageMultiplier);
            } else {
                calamitousDebuffs.remove(target.getUUID());
            }
        }

        ProphecyEffect burden = activeEffect(target);
        boolean heavyHit = event.getNewDamage() >= target.getMaxHealth() * 0.2f
                && event.getNewDamage() < target.getHealth();
        if (heavyHit && burden != null && (burden.hasOutcome(Outcome.FATES_BURDEN)
            || burden.hasOutcome(Outcome.CHOSEN_MISFORTUNE))) {
            event.setNewDamage(event.getNewDamage() * (1.75f + 0.75f * highTierScale(burden.strength)));
            consumeCorrection(target, burden,
                    burden.hasOutcome(Outcome.CHOSEN_MISFORTUNE) ? burden.charges : 1);
            AbilityUtil.sendActionBar(target, Component.literal("\u00A7cFate's Burden magnified a heavy blow."));
        }

        if (redirectedDamageTargets.contains(target.getUUID())
                || event.getNewDamage() < target.getHealth()) return;

        ProphecyEffect effect = activeEffect(target);
        if (effect == null) return;
        if (effect.outcome == Outcome.GOLDEN_THREAD) {
            redirectFatalDamage(event, effect);
        } else if ((effect.outcome == Outcome.FATES_CORRECTION && effect.charges > 0)
                || effect.outcome == Outcome.CHOSEN_OUTCOME) {
            event.setNewDamage(event.getNewDamage() * (0.6f - 0.3f * effect.strength));
            consumeCorrection(target, effect, effect.outcome == Outcome.CHOSEN_OUTCOME ? effect.charges : 1);
            AbilityUtil.sendActionBar(target, Component.literal("\u00A7eFate softened a potentially lethal blow."));
        }
    }

    private static void redirectFatalDamage(LivingDamageEvent.Pre event, ProphecyEffect effect) {
        LivingEntity target = event.getEntity();
        if (!(target.level() instanceof ServerLevel level)) return;
        List<LivingEntity> alternatives = AbilityUtil.getNearbyEntities(
            target, level, target.position(), 16, true).stream()
                .filter(candidate -> candidate != event.getSource().getEntity())
                .toList();
        if (alternatives.isEmpty()) return;

        LivingEntity redirectedTarget = alternatives.get(target.getRandom().nextInt(alternatives.size()));
        float resistance = (float) AbilityUtil.getSequenceResistanceFactor(target, redirectedTarget);
        float redirectedDamage = event.getNewDamage() * (1f - resistance);
        event.setNewDamage(event.getNewDamage() * resistance);
        activeEffects.remove(target.getUUID());
        redirectedDamageTargets.add(redirectedTarget.getUUID());
        try {
            redirectedTarget.hurt(event.getSource(), redirectedDamage);
        } finally {
            redirectedDamageTargets.remove(redirectedTarget.getUUID());
        }
        AbilityUtil.sendActionBar(target, Component.literal("\u00A7eThe Golden Thread carried disaster to another fate."));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onTargetSelected(TargetEntityEvent event) {
        LivingEntity selectedTarget = event.getTargetEntity();
        ProphecyEffect targetEffect = selectedTarget == null ? null : activeEffect(selectedTarget);
        if (targetEffect != null && (targetEffect.outcome == Outcome.TARGET_WARD
            || (targetEffect.outcome == Outcome.CHOSEN_OUTCOME && !event.isAllowAllies()))) {
            redirectAwayFromProtectedTarget(event, selectedTarget);
            if (targetEffect.outcome == Outcome.CHOSEN_OUTCOME) {
            activeEffects.remove(selectedTarget.getUUID());
            AbilityUtil.sendActionBar(selectedTarget, Component.literal(
                "\u00A7eChosen Outcome diverted a hostile ability."));
            }
            return;
        }

        ProphecyEffect sourceEffect = activeEffect(event.getSourceEntity());
        if (sourceEffect == null) return;
        boolean uncertainAim = sourceEffect.hasOutcome(Outcome.UNCERTAIN_AIM);
        boolean chosenMisfortune = sourceEffect.hasOutcome(Outcome.CHOSEN_MISFORTUNE);
        boolean targetRedirection = sourceEffect.outcome == Outcome.TARGET_REDIRECTION;
        if (!uncertainAim && !chosenMisfortune && !targetRedirection) return;
        float chance = targetRedirection ? 0.15f + 0.65f * sourceEffect.strength
            : chosenMisfortune ? 1f : 0.2f + 0.3f * sourceEffect.strength;
        if (event.getSourceEntity().getRandom().nextFloat() >= chance) return;

        if (targetRedirection && event.getSourceEntity().getRandom().nextBoolean()) {
            event.setTargetEntity(event.getSourceEntity());
            return;
        }
        if (!(event.getSourceEntity().level() instanceof ServerLevel level)) return;
        List<LivingEntity> alternatives = AbilityUtil.getNearbyEntities(
            event.getSourceEntity(), level, event.getSourceEntity().position(),
            Math.min(32, event.getRadius()), true, event.isAllowAllies());
        if (!alternatives.isEmpty()) {
            event.setTargetEntity(alternatives.get(event.getSourceEntity().getRandom().nextInt(alternatives.size())));
            if (chosenMisfortune) {
                activeEffects.remove(event.getSourceEntity().getUUID());
                AbilityUtil.sendActionBar(event.getSourceEntity(), Component.literal("\u00A7cChosen Misfortune twisted your aim."));
            }
        }
    }

    private static void redirectAwayFromProtectedTarget(TargetEntityEvent event, LivingEntity protectedTarget) {
        LivingEntity source = event.getSourceEntity();
        if (!(source.level() instanceof ServerLevel level)) {
            event.setTargetEntity(null);
            return;
        }

        double radius = Math.min(32, Math.max(8, event.getRadius()));
        List<LivingEntity> alternatives = AbilityUtil.getNearbyEntities(
                protectedTarget, level, protectedTarget.position(), radius, true, true).stream()
                .filter(candidate -> candidate != protectedTarget && candidate != source)
                .filter(candidate -> AbilityUtil.mayTarget(source, candidate, event.isAllowAllies(), false))
                .toList();
        event.setTargetEntity(alternatives.isEmpty() ? null
                : alternatives.get(source.getRandom().nextInt(alternatives.size())));
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof ServerPlayer player)) return;
        ProphecyEffect effect = activeEffect(player);
        if (effect != null && effect.hasOutcome(Outcome.MEAGER_HARVEST)
                && player.getRandom().nextFloat() < 0.25f + 0.25f * effect.strength) {
            event.getDrops().forEach(drop -> halveRenewableStack(drop.getItem()));
            return;
        }
        if (effect == null || effect.outcome != Outcome.BLOCK_LOOT
                || player.getRandom().nextFloat() >= 0.25f + 0.7f * effect.strength) return;

        List<ItemEntity> originalDrops = new ArrayList<>(event.getDrops());
        originalDrops.forEach(drop -> {
            ItemStack copy = new ItemStack(drop.getItem().getItem(), drop.getItem().getCount());
            Block.popResource(player.level(), event.getPos(), copy);
        });
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        ProphecyEffect effect = activeEffect(player);
        if (effect != null && effect.hasOutcome(Outcome.MEAGER_HARVEST)
                && player.getRandom().nextFloat() < 0.25f + 0.25f * effect.strength) {
            event.getDrops().forEach(drop -> halveRenewableStack(drop.getItem()));
            return;
        }
        if (effect == null || effect.outcome != Outcome.MOB_LOOT
                || player.getRandom().nextFloat() >= 0.2f + 0.65f * effect.strength) return;

        List<ItemEntity> copies = event.getDrops().stream().map(drop -> {
            ItemEntity copy = new ItemEntity(drop.level(), drop.getX(), drop.getY(), drop.getZ(), drop.getItem().copy());
            copy.setDefaultPickUpDelay();
            return copy;
        }).toList();
        event.getDrops().addAll(copies);
    }

    private static void halveRenewableStack(ItemStack stack) {
        if (stack.getCount() > 1) {
            stack.setCount(Math.max(1, (stack.getCount() + 1) / 2));
        }
    }

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        ProphecyEffect effect = activeEffect(event.getEntity());
        if (effect != null && effect.hasOutcome(Outcome.MISFORTUNATE_RECOVERY)) {
            event.setAmount(event.getAmount() * (0.75f - 0.25f * effect.strength));
        }
    }

    public static float getLuckRegenerationMultiplier(LivingEntity entity) {
        ProphecyEffect effect = activeEffect(entity);
        if (effect == null) return 1;
        if (effect.outcome == Outcome.LUCK_REGENERATION) return 1.5f + 2.5f * effect.strength;
        if (effect.outcome == Outcome.BORROWED_TOMORROW && !effect.borrowedDebtStarted) {
            return 2.5f + 3.5f * effect.strength;
        }
        return 1;
    }

    public static float getCostMultiplier(LivingEntity entity) {
        ProphecyEffect effect = activeEffect(entity);
        if (effect != null && (effect.outcome == Outcome.PERFECT_OPPORTUNITY
            || effect.outcome == Outcome.CHOSEN_OUTCOME)) return 0;
        if (hasCurrentCastOpportunity(entity)) return 0;
        if (effect != null && effect.hasOutcome(Outcome.CHOSEN_MISFORTUNE)) {
            return 2f + highTierScale(effect.strength);
        }
        if (effect != null && effect.hasOutcome(Outcome.TWISTED_OPPORTUNITY)) {
            return 1.25f + 0.25f * effect.strength;
        }
        return effect != null && effect.outcome == Outcome.HALVED_COSTS ? 0.5f : 1f;
    }

    public static float getCooldownMultiplier(LivingEntity entity) {
        ProphecyEffect effect = activeEffect(entity);
        if (effect != null && effect.hasOutcome(Outcome.CHOSEN_MISFORTUNE)) {
            return 2f + highTierScale(effect.strength);
        }
        if (effect != null && effect.hasOutcome(Outcome.TWISTED_OPPORTUNITY)) {
            return 1.25f + 0.25f * effect.strength;
        }
        return effect != null && effect.outcome == Outcome.HALVED_COOLDOWNS ? 0.5f : 1f;
    }

    public static int modifyPositiveLuckGain(LivingEntity entity, int amount) {
        ProphecyEffect effect = activeEffect(entity);
        if (amount > 0 && effect != null && effect.hasOutcome(Outcome.FRACTURED_FORTUNE)) {
            return Math.max(1, Math.round(amount * (0.7f - 0.3f * effect.strength)));
        }
        if (amount <= 0 || effect == null || effect.outcome != Outcome.FORTUNES_INTEREST) return amount;
        return Math.max(amount, Math.round(amount * (1.25f + 0.75f * effect.strength)));
    }

    public static float modifySpiritualityGain(LivingEntity entity, float amount) {
        ProphecyEffect effect = activeEffect(entity);
        return amount > 0 && effect != null && effect.hasOutcome(Outcome.MISFORTUNATE_RECOVERY)
                ? amount * (0.75f - 0.25f * effect.strength) : amount;
    }

    public static boolean interceptPassiveLuckEvent(LivingEntity entity, int cost) {
        ProphecyEffect effect = activeEffect(entity);
        if (effect == null || !effect.hasOutcome(Outcome.FATE_REVERSAL)) return false;
        int reversedLuck = Math.max(cost, Math.round(LuckManager.getMaximumLuck(entity)
            * (0.1f + 0.15f * highTierScale(effect.strength))));
        LuckManager.addLuck(entity, -reversedLuck);
        activeEffects.remove(entity.getUUID());
        AbilityUtil.sendActionBar(entity, Component.literal(
            "\u00A7cFate Reversal denied a fortunate event and inverted its luck."));
        return true;
    }

    public static void onLuckSpent(LivingEntity entity, int amount) {
        ProphecyEffect effect = activeEffect(entity);
        if (amount <= 0 || effect == null || effect.outcome != Outcome.FORTUNES_INTEREST) return;
        long durationCost = Math.max(1, Math.round(amount * effectTicksPerLuck));
        effect.expiresAt = Math.max(entity.level().getGameTime() + 20, effect.expiresAt - durationCost);
    }

    public static boolean applyMaximumMisfortuneProphecy(LivingEntity entity) {
        if (activeEffect(entity) != null || pendingProphecies.containsKey(entity.getUUID())) {
            return false;
        }
        int paidLuck = LuckManager.getMaximumLuck(entity);
        long duration = Math.max(20 * 60 * 10, Math.round(paidLuck * effectTicksPerLuck));
        long gameTime = entity.level().getGameTime();
        Outcome outcome = EffectTier.HIGH.misfortuneOutcomes[
            entity.getRandom().nextInt(EffectTier.HIGH.misfortuneOutcomes.length)];
        ProphecyEffect effect = new ProphecyEffect(Modifier.MISFORTUNE,
            outcome, statuePunishmentStrength, gameTime + duration);
        activeEffects.put(entity.getUUID(), effect);
        if (outcome == Outcome.DEBT_OF_YESTERDAY) {
            long debtDuration = Math.max(20, duration / 2);
            effect.debtTransitionAt = gameTime + debtDuration;
            effect.debtAmount = Math.max(1, Math.round(
                paidLuck * (0.75f + 0.75f * highTierScale(statuePunishmentStrength))));
            float drainRate = effect.debtAmount * 20f * 60f / debtDuration;
            LuckManager.applyLuckDrain(entity, LuckManager.debtOfYesterdaySource, drainRate, debtDuration);
        }
        long durationSeconds = duration / 20;
        entity.sendSystemMessage(Component.literal("\u00A75The statue inflicted " + outcome.description
            + " for " + (durationSeconds / 60) + "m " + (durationSeconds % 60) + "s."));
        return true;
    }

    public static void clearActiveProphecy(LivingEntity entity) {
        activeEffects.remove(entity.getUUID());
        pendingProphecies.remove(entity.getUUID());
        calamitousDebuffs.remove(entity.getUUID());
        LuckManager.clearLuckDrain(entity, LuckManager.prophecySource);
        LuckManager.clearLuckGain(entity, LuckManager.borrowedTomorrowSource);
        LuckManager.clearLuckDrain(entity, LuckManager.borrowedTomorrowSource);
        LuckManager.clearLuckGain(entity, LuckManager.debtOfYesterdaySource);
        LuckManager.clearLuckDrain(entity, LuckManager.debtOfYesterdaySource);
    }

    public static float getAbilityStrengthMultiplier(LivingEntity entity) {
        CastOpportunity opportunity = activeCastOpportunities.get(entity.getUUID());
        if (opportunity != null && opportunity.gameTime == entity.level().getGameTime()) {
            return opportunity.strengthMultiplier;
        }
        ProphecyEffect effect = activeEffect(entity);
        return effect != null && effect.hasOutcome(Outcome.BROKEN_THREAD)
            ? 0.25f - 0.15f * highTierScale(effect.strength) : 1f;
    }

    public static int getEffectiveLuckBonus(LivingEntity entity) {
        ProphecyEffect effect = activeEffect(entity);
        return effect != null && effect.outcome == Outcome.BORROWED_TOMORROW && !effect.borrowedDebtStarted
                ? Math.round(500 + 2500 * effect.strength) : 0;
    }

    private static boolean hasCurrentCastOpportunity(LivingEntity entity) {
        CastOpportunity opportunity = activeCastOpportunities.get(entity.getUUID());
        if (opportunity == null) return false;
        if (opportunity.gameTime == entity.level().getGameTime()) return true;
        activeCastOpportunities.remove(entity.getUUID());
        return false;
    }

    private static float highTierScale(float strength) {
        return Math.clamp((strength - EffectTier.HIGH.minimumInvestment)
                / (1f - EffectTier.HIGH.minimumInvestment), 0f, 1f);
    }

    private static void consumeCorrection(LivingEntity entity, ProphecyEffect effect, int amount) {
        effect.charges = Math.max(0, effect.charges - amount);
        if (effect.charges == 0) activeEffects.remove(entity.getUUID());
    }

    private static ProphecyEffect activeEffect(LivingEntity entity) {
        ProphecyEffect effect = activeEffects.get(entity.getUUID());
        if (effect == null) return null;
        if (!(entity.level() instanceof ServerLevel level) || level.getGameTime() < effect.expiresAt) return effect;
        activeEffects.remove(entity.getUUID());
        return null;
    }

    private static void revealCoordinates(ServerPlayer target) {
        Component message = Component.literal("\u00A7c[Misfortune Prophecy] " + target.getName().getString()
                + " is at " + target.getBlockX() + ", " + target.getBlockY() + ", " + target.getBlockZ()
                + " in " + target.level().dimension().location());
        target.serverLevel().getPlayers(player -> player != target
                && BeyonderData.isBeyonder(player)
                && player.distanceToSqr(target) <= revealRange * revealRange).forEach(player -> player.sendSystemMessage(message));
    }

    private static void fail(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal("\u00A7c" + message));
    }

    private enum Modifier {
        FORTUNE("fortune", 0.05f),
        MISFORTUNE("misfortune", 0.1f),
        DISASTER("disaster", 0.5f),
        CONVERGENCE("convergence", 0.35f);

        private final String displayName;
        private final float minimumInvestment;

        Modifier(String displayName, float minimumInvestment) {
            this.displayName = displayName;
            this.minimumInvestment = minimumInvestment;
        }

        private static Modifier fromInput(String input) {
            return input.toLowerCase(Locale.ROOT).startsWith("converg")
                    ? CONVERGENCE
                    : valueOf(input.toUpperCase(Locale.ROOT));
        }
    }

    private enum Outcome {
        LUCK_REGENERATION("increased luck regeneration"),
        MOB_LOOT("improved mob drops"),
        BLOCK_LOOT("multiplied block drops"),
        HALVED_COSTS("spirituality and luck costs are halved"),
        HALVED_COOLDOWNS("ability cooldowns are halved"),
        FORTUNATE_RECOVERY("health and spirituality continuously recover"),
        TARGET_WARD("targeted abilities are redirected to nearby entities"),
        FATES_CORRECTION("fate mitigates up to three potentially lethal blows"),
        BORROWED_TOMORROW("tomorrow's fortune is borrowed now and repaid as misfortune"),
        GOLDEN_THREAD("one fatal disaster may be redirected to a nearby hostile fate"),
        PERFECT_OPPORTUNITY("the next ability costs no spirituality and is strengthened"),
        FORTUNES_INTEREST("positive luck gains multiply, but spending luck shortens the prophecy"),
        CHOSEN_OUTCOME("lethal damage, an ability use, or hostile targeting realizes one foretold future"),
        LUCK_DRAIN("luck drains over time"),
        ABILITY_FAILURE("abilities may fail"),
        TARGET_REDIRECTION("targeted abilities may change targets"),
        EXPOSURE("nearby Beyonders learn your exact coordinates"),
        FALTERING_STEP("movement and mining periodically falter"),
        MEAGER_HARVEST("renewable block and mob harvests may be halved"),
        SPIRITUAL_LEAKAGE("spirituality steadily leaks away"),
        TWISTED_OPPORTUNITY("the next ability costs more and recovers more slowly"),
        FRACTURED_FORTUNE("positive luck generation is reduced"),
        HOSTILE_ATTENTION("nearby hostile creatures are drawn to attack"),
        UNCERTAIN_AIM("targeted abilities may select another valid target"),
        MISFORTUNATE_RECOVERY("health and spirituality recovery are reduced"),
        FATES_BURDEN("two to four heavy nonlethal hits suffer greatly amplified damage"),
        DEBT_OF_YESTERDAY("a massive luck debt is collected now and returned later"),
        BROKEN_THREAD("the next two to four successful abilities lose most of their strength"),
        CALAMITOUS_OPENING("the next ability leaves a severe defensive opening"),
        CHOSEN_MISFORTUNE("a heavy hit, ability use, or uncertain aim realizes one bad future"),
        FATE_REVERSAL("the next passive luck event fails and becomes a major luck loss");

        private final String description;

        Outcome(String description) {
            this.description = description;
        }
    }

    private enum EffectTier {
        LOW(0, new Outcome[]{Outcome.MOB_LOOT, Outcome.BLOCK_LOOT},
                new Outcome[]{Outcome.LUCK_DRAIN, Outcome.EXPOSURE, Outcome.FALTERING_STEP,
                    Outcome.MEAGER_HARVEST, Outcome.SPIRITUAL_LEAKAGE}),
        MEDIUM(0.35f, new Outcome[]{Outcome.LUCK_REGENERATION},
            new Outcome[]{Outcome.ABILITY_FAILURE, Outcome.TWISTED_OPPORTUNITY, Outcome.FRACTURED_FORTUNE,
                    Outcome.HOSTILE_ATTENTION, Outcome.UNCERTAIN_AIM,
                    Outcome.MISFORTUNATE_RECOVERY}),
        HIGH(0.7f, new Outcome[]{Outcome.HALVED_COSTS, Outcome.HALVED_COOLDOWNS,
            Outcome.FORTUNATE_RECOVERY, Outcome.TARGET_WARD, Outcome.FATES_CORRECTION,
            Outcome.BORROWED_TOMORROW, Outcome.GOLDEN_THREAD, Outcome.PERFECT_OPPORTUNITY,
            Outcome.FORTUNES_INTEREST, Outcome.CHOSEN_OUTCOME},
                new Outcome[]{Outcome.FATES_BURDEN, Outcome.DEBT_OF_YESTERDAY,
                    Outcome.BROKEN_THREAD, Outcome.CALAMITOUS_OPENING,
                    Outcome.CHOSEN_MISFORTUNE, Outcome.FATE_REVERSAL});

        private final float minimumInvestment;
        private final Outcome[] fortuneOutcomes;
        private final Outcome[] misfortuneOutcomes;

        EffectTier(float minimumInvestment, Outcome[] fortuneOutcomes, Outcome[] misfortuneOutcomes) {
            this.minimumInvestment = minimumInvestment;
            this.fortuneOutcomes = fortuneOutcomes;
            this.misfortuneOutcomes = misfortuneOutcomes;
        }
    }

        private record TargetProphecy(
            ServerPlayer target, float strength, EffectTier tier, int requiredLuck,
            boolean funded, boolean chanceFailed) {
    }

    private static final class ProphecyEffect {
        private final Modifier modifier;
        private final Outcome outcome;
        private final float strength;
        private long expiresAt;
        private int charges;
        private long borrowedTransitionAt;
        private int borrowedAmount;
        private boolean borrowedDebtStarted;
        private long debtTransitionAt;
        private int debtAmount;
        private boolean debtRecoveryStarted;

        private ProphecyEffect(Modifier modifier, Outcome outcome, float strength, long expiresAt) {
            this.modifier = modifier;
            this.outcome = outcome;
            this.strength = strength;
            this.expiresAt = expiresAt;
                this.charges = outcome == Outcome.FATES_CORRECTION
                    ? 1 + Math.round(2 * strength)
                    : outcome == Outcome.FATES_BURDEN || outcome == Outcome.BROKEN_THREAD
                    ? 2 + Math.round(2 * highTierScale(strength))
                    : outcome == Outcome.CHOSEN_OUTCOME || outcome == Outcome.CHOSEN_MISFORTUNE ? 1 : 0;
        }

        private boolean hasOutcome(Outcome expected) {
            return outcome == expected;
        }
    }

    private record CastOpportunity(long gameTime, float strengthMultiplier) {
    }

    private record CalamitousDebuff(long expiresAt, float damageMultiplier) {
    }

    private record PendingProphecy(UUID authorUUID, Modifier modifier, int paidLuck,
                                   float strength, EffectTier tier, long activatesAt) {
    }
}