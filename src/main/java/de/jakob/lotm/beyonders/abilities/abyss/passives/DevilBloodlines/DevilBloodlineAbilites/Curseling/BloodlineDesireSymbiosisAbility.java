package de.jakob.lotm.beyonders.abilities.abyss.passives.DevilBloodlines.DevilBloodlineAbilites.Curseling;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class BloodlineDesireSymbiosisAbility extends Ability {
    private static final double MIN_RANGE = 5.0D;
    private static final double MAX_RANGE = 10.0D;
    private static final long HOLD_TICKS = 20L * 5L;
    private static final long HOLD_TIMEOUT_TICKS = 15L;
    //lock out time
    private static final long FAIL_LOCKOUT_TICKS = 20L * 60L * 10L;
    //Safty cap for link
    private static final int LINK_MAX_TICKS = 20 * 60 * 20;
    private static final float MIN_HEALTH = 1.0F;
    private static final double APPROACH_DISTANCE = 2.5D;
    private static final int MESSAGE_COLOR = 0x6d32a8;
    private enum DesireType { KILL_MOB, GAIN_RESOURCE, APPROACH_PLAYER }
    private static final List<EntityType<?>> KILL_TARGETS = List.of(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER, EntityType.CREEPER, EntityType.ENDERMAN);
    private static final List<Item> RESOURCES = List.of(Items.DIAMOND, Items.GOLD_INGOT, Items.EMERALD, Items.NETHERITE_SCRAP, Items.LAPIS_LAZULI);
    private static final class Desire {
        final DesireType type;
        final EntityType<?> mob;   // KILL_MOB
        final Item item;           // GAIN_RESOURCE
        int baseline;              // GAIN_RESOURCE: item count when linked
        final String text;
        Desire(DesireType type, EntityType<?> mob, Item item, int baseline, String text) {
            this.type = type;
            this.mob = mob;
            this.item = item;
            this.baseline = baseline;
            this.text = text;
        }
    }
    private static final class Link {
        final UUID target;
        final Desire desire;
        UUID taskId;

        Link(UUID target, Desire desire) {
            this.target = target;
            this.desire = desire;
        }
    }
    private static final class Channel {
        final UUID target;
        final long start;
        long lastHold;
        Channel(UUID target, long start) {
            this.target = target;
            this.start = start;
            this.lastHold = start;
        }
    }
    private static final Map<UUID, Link> links = new ConcurrentHashMap<>();
    private static final Map<UUID, Channel> channels = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<UUID, Long>> lockouts = new ConcurrentHashMap<>();

    public BloodlineDesireSymbiosisAbility(String id) {
        super(id, 100f);
        canBeUsedByNPC = false;
        canBeCopied = false;
        canBeReplicated = false;
        cannotBeStolen = true;
        canBeShared = false;
        canBeUsedInArtifact = true;
    }
    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 5));
    }
    @Override
    public float getSpiritualityCost() {
        return 200;
    }
    @Override
    public boolean meetsBloodlineRequirement(LivingEntity entity) {
        return BloodlineCurselingAbility.isValidCurseling(entity);
    }
    //Linking: hold for 5 seconds
    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!(entity instanceof ServerPlayer player)) return;
        UUID linkerId = player.getUUID();
        if (channels.containsKey(linkerId)) return;
        Link existing = links.get(linkerId);
        if (existing != null) {
            AbilityUtil.sendActionBar(player, Component.literal("You are already linked to a desire.").withColor(MESSAGE_COLOR));
            return;
        }
        LivingEntity found = AbilityUtil.getTargetEntity(player, (int) MAX_RANGE, 2f);
        if (!(found instanceof ServerPlayer target) || target == player) {
            AbilityUtil.sendActionBar(player, Component.literal("Desire Symbiosis only works on other players.").withColor(MESSAGE_COLOR));
            return;
        }
        double distance = player.distanceTo(target);
        if (distance < MIN_RANGE || distance > MAX_RANGE) {
            AbilityUtil.sendActionBar(player, Component.literal("Stand 5 to 10 blocks away from them.").withColor(MESSAGE_COLOR));
            return;
        }
        if (BeyonderData.getSequence(target) < AbilityUtil.getSeqWithArt(player, this)) {
            AbilityUtil.sendActionBar(player, Component.literal("Their spirit is too strong to link with.").withColor(MESSAGE_COLOR));
            return;
        }
        long now = serverLevel.getGameTime();
        long lockedUntil = lockouts.getOrDefault(linkerId, Map.of()).getOrDefault(target.getUUID(), 0L);
        if (now < lockedUntil) {
            long seconds = (lockedUntil - now) / 20L;
            AbilityUtil.sendActionBar(player, Component.literal("Their desire rejected you. Try again in " + seconds + "s.").withColor(MESSAGE_COLOR));
            return;
        }
        channels.put(linkerId, new Channel(target.getUUID(), now));
        AbilityUtil.sendActionBar(player, Component.literal("Hold to link their desire...").withColor(MESSAGE_COLOR));

        AtomicReference<UUID> taskRef = new AtomicReference<>();
        UUID taskId = ServerScheduler.scheduleForDuration(0, 5, (int) (HOLD_TICKS + 40), () -> channelTick(serverLevel, linkerId, taskRef), null, serverLevel);
        taskRef.set(taskId);
    }
    @Override
    public void onHold(Level level, LivingEntity entity) {
        if (level.isClientSide()) return;

        Channel channel = channels.get(entity.getUUID());
        if (channel != null) {
            channel.lastHold = level.getGameTime();
        }
    }
    private void channelTick(ServerLevel level, UUID linkerId, AtomicReference<UUID> taskRef) {
        Channel channel = channels.get(linkerId);
        MinecraftServer server = level.getServer();
        ServerPlayer linker = server.getPlayerList().getPlayer(linkerId);
        if (channel == null || linker == null) {
            stopChannel(linkerId, taskRef);
            return;
        }
        ServerPlayer target = server.getPlayerList().getPlayer(channel.target);
        long now = level.getGameTime();
        // The target died or left: nothing to blame the linker for
        if (target == null || !target.isAlive()) {
            AbilityUtil.sendActionBar(linker, Component.literal("The link was lost.").withColor(MESSAGE_COLOR));
            stopChannel(linkerId, taskRef);
            return;
        }
        if (now - channel.lastHold > HOLD_TIMEOUT_TICKS) {
            failLink(level, linker, target, "You let go too early.");
            stopChannel(linkerId, taskRef);
            return;
        }
        double distance = linker.distanceTo(target);
        if (distance < MIN_RANGE || distance > MAX_RANGE) {
            failLink(level, linker, target, "They slipped out of range.");
            stopChannel(linkerId, taskRef);
            return;
        }
        long held = now - channel.start;
        if (held >= HOLD_TICKS) {
            stopChannel(linkerId, taskRef);
            startLink(level, linker, target);
            return;
        }
        if (held % 20L < 5L) {
            AbilityUtil.sendActionBar(linker, Component.literal("Linking desire... " + (held * 100L / HOLD_TICKS) + "%").withColor(MESSAGE_COLOR));
        }
    }
    private static void stopChannel(UUID linkerId, AtomicReference<UUID> taskRef) {
        channels.remove(linkerId);
        if (taskRef.get() != null) {
            ServerScheduler.cancel(taskRef.get());
        }
    }
    private static void failLink(ServerLevel level, ServerPlayer linker, ServerPlayer target, String reason) {
        lockouts.computeIfAbsent(linker.getUUID(), id -> new ConcurrentHashMap<>())
                .put(target.getUUID(), level.getGameTime() + FAIL_LOCKOUT_TICKS);
        AbilityUtil.sendActionBar(linker, Component.literal(reason + " The link failed.").withColor(0xbf3232));
    }
    private void startLink(ServerLevel level, ServerPlayer linker, ServerPlayer target) {
        Desire desire = rollDesire(target);
        Link link = new Link(target.getUUID(), desire);
        links.put(linker.getUUID(), link);
        AbilityUtil.sendActionBar(linker, Component.literal("Linked to " + target.getName().getString() + "'s desire: " + desire.text).withColor(MESSAGE_COLOR));
        ParticleUtil.spawnParticles(level, ParticleTypes.SOUL, linker.position().add(0, 1, 0), 40, .5, .02);
        // The target is told what their desire is, but not who linked it
        target.sendSystemMessage(Component.literal("Something has latched onto your desire " + desire.text + ".").withColor(MESSAGE_COLOR));
        UUID linkerId = linker.getUUID();
        AtomicReference<UUID> taskRef = new AtomicReference<>();
        UUID taskId = ServerScheduler.scheduleForDuration(0, 20, LINK_MAX_TICKS,
                () -> linkTick(level, linkerId, taskRef),
                () -> endLink(level.getServer(), linkerId, link, "The desire faded."),
                level);
        taskRef.set(taskId);
        link.taskId = taskId;
    }
    private Desire rollDesire(ServerPlayer target) {
        return switch (DesireType.values()[random.nextInt(DesireType.values().length)]) {
            case KILL_MOB -> {
                EntityType<?> mob = KILL_TARGETS.get(random.nextInt(KILL_TARGETS.size()));
                yield new Desire(DesireType.KILL_MOB, mob, null, 0, "to kill a " + mob.getDescription().getString());
            }
            case GAIN_RESOURCE -> {
                Item item = RESOURCES.get(random.nextInt(RESOURCES.size()));
                int baseline = target.getInventory().countItem(item);
                yield new Desire(DesireType.GAIN_RESOURCE, null, item, baseline, "to gain " + item.getDescription().getString());
            }
            case APPROACH_PLAYER -> new Desire(DesireType.APPROACH_PLAYER, null, null, 0, "to get close to another player");
        };
    }
    private static void linkTick(ServerLevel level, UUID linkerId, AtomicReference<UUID> taskRef) {
        Link link = links.get(linkerId);
        if (link == null) {
            // Ended somewhere else, e.g. the kill desire was fulfilled
            if (taskRef.get() != null) ServerScheduler.cancel(taskRef.get());
            return;
        }
        MinecraftServer server = level.getServer();
        ServerPlayer linker = server.getPlayerList().getPlayer(linkerId);
        ServerPlayer target = server.getPlayerList().getPlayer(link.target);
        if (linker == null) {
            endLink(server, linkerId, link, null);
            return;
        }
        if (target == null || !target.isAlive()) {
            endLink(server, linkerId, link, "Their desire is gone.");
            return;
        }
        // Cleansing/Purifaction destroys the desire
        Location loc = new Location(linker.position(), level);
        if (InteractionHandler.isInteractionPossible(loc, "cleansing", BeyonderData.getSequence(linker)) || InteractionHandler.isInteractionPossible(loc, "purification", BeyonderData.getSequence(linker))) {
            endLink(server, linkerId, link, "The desire was lifted away.");
            return;
        }
        if (isFulfilled(link.desire, target)) {
            endLink(server, linkerId, link, "Their desire was fulfilled. The link is gone.");
        }
    }
    private static boolean isFulfilled(Desire desire, ServerPlayer target) {
        return switch (desire.type) {
            case GAIN_RESOURCE -> {
                int count = target.getInventory().countItem(desire.item);
                if (count < desire.baseline) desire.baseline = count;
                yield count > desire.baseline;
            }
            case APPROACH_PLAYER -> target.level().players().stream().anyMatch(p -> p != target && !p.isSpectator() && p.distanceTo(target) <= APPROACH_DISTANCE);
            case KILL_MOB -> false;
        };
    }
    private static void endLink(MinecraftServer server, UUID linkerId, Link link, String reason) {
        if (!links.remove(linkerId, link)) return;
        if (link.taskId != null) ServerScheduler.cancel(link.taskId);

        if (reason != null) {
            ServerPlayer linker = server.getPlayerList().getPlayer(linkerId);
            if (linker != null) {
                AbilityUtil.sendActionBar(linker, Component.literal(reason).withColor(MESSAGE_COLOR));
            }
        }
    }
    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Pre event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel level)) return;
        Link link = links.get(victim.getUUID());
        if (link == null) return;
        Entity attacker = event.getSource().getEntity();
        if (attacker == null || !attacker.getUUID().equals(link.target)) return;
        // Absorption soaks damage first, then health. Never let the health go below MIN_HEALTH.
        float allowed = victim.getHealth() + victim.getAbsorptionAmount() - MIN_HEALTH;
        if (event.getNewDamage() > allowed) {
            event.setNewDamage(Math.max(0.0F, allowed));
            ParticleUtil.spawnParticles(level, ParticleTypes.SOUL, victim.position().add(0, 1, 0), 15, .4, .02);
        }
    }
    //Deaths: kill desires, and links that lose a side
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (!(dead.level() instanceof ServerLevel level)) return;
        MinecraftServer server = level.getServer();
        Entity killer = event.getSource().getEntity();
        for (Map.Entry<UUID, Link> entry : new ArrayList<>(links.entrySet())) {
            UUID linkerId = entry.getKey();
            Link link = entry.getValue();
            if (linkerId.equals(dead.getUUID())) {
                endLink(server, linkerId, link, null);
            } else if (link.target.equals(dead.getUUID())) {
                endLink(server, linkerId, link, "Their desire died with them.");
            } else if (link.desire.type == DesireType.KILL_MOB
                    && killer != null && killer.getUUID().equals(link.target)
                    && dead.getType() == link.desire.mob) {
                endLink(server, linkerId, link, "Their desire was fulfilled. The link is gone.");
            }
        }
    }
}