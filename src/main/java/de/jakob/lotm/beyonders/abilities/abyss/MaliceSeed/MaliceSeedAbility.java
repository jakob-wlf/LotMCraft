package de.jakob.lotm.beyonders.abilities.abyss.MaliceSeed;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.EntityControllingComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.gui.custom.malice_seed.MaliceSeedMenu;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.beyonders.abilities.fool.marionettes.ControllingUtils;
import de.jakob.lotm.attachments.MarionetteComponent;
import de.jakob.lotm.beyonders.abilities.fool.marionettes.MarionetteUtils;
import de.jakob.lotm.entity.custom.AvatarEntity;
import de.jakob.lotm.util.helper.AbilityWheelHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class MaliceSeedAbility extends SelectableAbility {
    private static final String SEEDS_KEY = "lotm_malice_seeds";
    private static final String MALICE_IDENTITY_KEY = "lotm_malice_identity";
    private static final int SCAN_INTERVAL_TICKS = 29;          // seeds are processed every 1s
    private static final int TICKS_PER_PHASE = 20 * 60 * 10;     // 10 minutes per phase
    public record SeedInfo(UUID hostUUID, String hostName, int phase, int progressPercent, String dimension,boolean isPlayer) {}
    private static final Set<String> PURIFICATION_PATHWAYS = Set.of("tyrant", "sun", "twilight_giant", "abyss");
    private static final Set<String> SEED_SIGHT_PATHWAYS = Set.of("sun", "visionary","twilight_giant");
    private static final Map<UUID, UUID> whisperTarget = new ConcurrentHashMap<>(); // caster -> host
    private static final Map<UUID, UUID> speakAsTarget = new ConcurrentHashMap<>(); // caster -> host
    private static final Map<UUID, UUID> possessingHost = new ConcurrentHashMap<>(); // caster -> host, while taken over
    private static final Set<UUID> detonatingSeed = ConcurrentHashMap.newKeySet();
    private static final DustParticleOptions maliceDust = new DustParticleOptions(new Vector3f(0.4f, 0f, 0.1f), 2f);
    private static final Set<UUID> knownCasters = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Integer> hostPhaseCache = new ConcurrentHashMap<>();
    private static int scanCounter = 0;
    public MaliceSeedAbility(String id) {
        super(id, 100);
        this.canBeCopied = false;
        this.canBeReplicated = false;
        this.canBeUsedInArtifact = true;
        this.cannotBeStolen = true;
        canBeShared = false;
    }
    @Override
    public Map<String, Integer> getRequirements() {return new HashMap<>(Map.of("abyss", 2));}
    @Override
    public float getSpiritualityCost() {return 150;}
    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.malice_seed.plant",
                "ability.lotmcraft.malice_seed.view_seeds",
                "ability.lotmcraft.malice_seed.detonate"};
    }
    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        boolean possessing = entity instanceof ServerPlayer p && possessingHost.containsKey(p.getUUID());
        if (possessing && abilityIndex != 2) return;
        switch (abilityIndex) {
            case 0 -> plantSeed(level, entity);
            case 1 -> openSeedMenu(entity);
            case 2 -> {
                if (entity instanceof ServerPlayer p) detonateSeed(p);
            }
        }
    }
    // Planting
    private void plantSeed(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 25, 2.5f);
        if (target == null || target == entity || getSeeds(target).contains(entity.getUUID().toString())) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.malice_seed.no_target").withColor(0xFFa3070c));
            return;
        }
        if (BeyonderData.isBeyonder(target) && "sun".equals(BeyonderData.getPathway(target)) && BeyonderData.getSequence(target) <= 4) {
            AbilityUtil.sendActionBar(entity, Component.literal("ability.lotmcraft.malice_seed.sun").withColor(0xFFa3070c));
            return;
        }
        if (AbilityUtil.isTargetSignificantlyStronger(entity, target)) {
            entity.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 5, 3));
            entity.hurt(entity.damageSources().generic(), 10);
            return;
        }
        int casterSeq = AbilityUtil.getSeqWithArt(entity, this);
        int targetSeq = BeyonderData.isBeyonder(target) ? BeyonderData.getSequence(target) : 10;
        if (random.nextFloat() >= getInfectionChance(casterSeq, targetSeq, target)) {AbilityUtil.sendActionBar(entity, Component.literal("The seed found no purchase.").withColor(0xFFa3070c));return;}
        UUID identity = getMaliceIdentity(target);
        MinecraftServer server = serverLevel.getServer();
        CompoundTag seeds = findSeedsForIdentity(server, identity);
        String casterKey = entity.getUUID().toString();
        // One seed per caster per target identity.
        if (seeds.contains(casterKey)) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.malice_seed.no_target").withColor(0xFFa3070c));
            return;
        }
        CompoundTag seed = new CompoundTag();
        seed.putInt("phase", 1);
        seed.putInt("progress", 0);
        seed.putInt("casterSeq", casterSeq);
        seeds.put(casterKey, seed);
        synchronizeIdentitySeeds(server, identity, seeds);
        knownCasters.add(entity.getUUID());
        ParticleUtil.spawnParticles(serverLevel, maliceDust, target.getEyePosition(), 40, .75, 0);
        level.playSound(null, BlockPos.containing(entity.position()), SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 1, 1);
    }
    private static float getInfectionChance(int casterSeq, int targetSeq, LivingEntity target) {
        float chance = Math.clamp(0.25f + (targetSeq - casterSeq) * 0.25f, 0f, 1f);
        String pathway = BeyonderData.isBeyonder(target) ? BeyonderData.getPathway(target) : null;
        if (pathway != null && PURIFICATION_PATHWAYS.contains(pathway)) chance *= 0.5f;
        return chance;
    }
    private static CompoundTag getSeeds(LivingEntity host) {
        if (host == null) return new CompoundTag();
        MinecraftServer server = host.getServer();
        if (server != null) {
            UUID identity = getMaliceIdentity(host);
            return findSeedsForIdentity(server, identity);
        }
        return host.getPersistentData().getCompound(SEEDS_KEY).copy();
    }
    private void openSeedMenu(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) return;
        List<SeedInfo> seeds = collectSeeds(player);
        player.openMenu(new SimpleMenuProvider((id, inv, p) -> new MaliceSeedMenu(id, inv, seeds), Component.translatable("gui.lotm.malice_seed.title")), buf -> MaliceSeedMenu.writeSeeds(buf, seeds));
    }
    //Phase progression
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (++scanCounter < SCAN_INTERVAL_TICKS) return;
        scanCounter = 0;
        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (!(entity instanceof LivingEntity caster)) continue;
                if (!knownCasters.contains(caster.getUUID())) continue;
                if (BeyonderData.isBeyonder(caster) && BeyonderData.getSequence(caster) > 2) removeAllSeedsFromCaster(caster.getUUID(), event.getServer());
            }
        }
        Set<UUID> processedIdentities = new HashSet<>();
        MinecraftServer server = event.getServer();
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (!(entity instanceof LivingEntity host) || !host.isAlive()) continue;
                if (!host.getPersistentData().contains(SEEDS_KEY)) continue;
                UUID identity = getMaliceIdentity(host);
                // A shared infection must advance only once per scan.
                if (!processedIdentities.add(identity)) continue;
                // Prefer the original body for sanity and phase calculations.
                LivingEntity canonicalHost = findLivingEntity(server, identity);
                if (canonicalHost == null || !canonicalHost.getPersistentData().contains(SEEDS_KEY)) canonicalHost = host;
                tickSeeds((ServerLevel) canonicalHost.level(), canonicalHost);
                CompoundTag updatedSeeds = getSeeds(canonicalHost);
                synchronizeIdentitySeeds(server, identity, updatedSeeds.copy());
            }
        }
    }
    private static void tickSeeds(ServerLevel level, LivingEntity host) {
        CompoundTag data = host.getPersistentData();
        CompoundTag seeds = data.getCompound(SEEDS_KEY);
        // Sanity loss on the host speeds the timer up: 1x at full sanity, up to 3x at none
        float sanity = host.getData(ModAttachments.SANITY_COMPONENT).getSanity();
        float speed = 1f + 2f * (1f - Math.clamp(sanity, 0f, 1f));
        int gained = Math.round(SCAN_INTERVAL_TICKS * speed);
        int highestPhase = 0;
        for (String casterKey : new ArrayList<>(seeds.getAllKeys())) {
            CompoundTag seed = seeds.getCompound(casterKey);
            int casterSeq = seed.getInt("casterSeq");
            if (InteractionHandler.isInteractionPossible(new Location(host.position(), level), "purification", casterSeq)) {
                seeds.remove(casterKey); // cleansed
                continue;
            }
            int phase = seed.getInt("phase");
            int maxPhase = casterSeq <= 0 ? 4 : 3; // phase 4 is a seq 0 caster only
            if (phase < maxPhase) {
                int progress = seed.getInt("progress") + gained;
                if (progress >= TICKS_PER_PHASE) {
                    phase++;
                    progress = 0;
                    seed.putInt("phase", phase);
                }
                seed.putInt("progress", progress);
            }
            seeds.put(casterKey, seed);
            knownCasters.add(UUID.fromString(casterKey));
            highestPhase = Math.max(highestPhase, phase);
        }
        if (seeds.isEmpty()) {
            data.remove(SEEDS_KEY);
            hostPhaseCache.remove(host.getUUID());
        } else {
            data.put(SEEDS_KEY, seeds);
            hostPhaseCache.put(host.getUUID(), highestPhase);
        }
    }
    //Resurrection
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onCasterDeath(LivingDeathEvent event) {
        LivingEntity caster = event.getEntity();
        // Purification damage permanently bypasses Malice Seed resurrection.
        String damageId = event.getSource().getMsgId().toLowerCase(Locale.ROOT);
        if (damageId.contains("purification") || damageId.contains("purify")) {
            removeAllSeedsFromCaster(caster.getUUID(), caster.level() instanceof ServerLevel level ? level.getServer() : null);
            knownCasters.remove(caster.getUUID());
            return;
        }
        if (!(caster.level() instanceof ServerLevel serverLevel)) return;
        MinecraftServer server = serverLevel.getServer();
        if (!knownCasters.contains(caster.getUUID())) return;
        String casterKey = caster.getUUID().toString();
        LivingEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (ServerLevel level : serverLevel.getServer().getAllLevels()) {
            for (Entity e : level.getAllEntities()) {
                if (!(e instanceof LivingEntity host) || host == caster || !host.isAlive()) {continue;}
                UUID hostIdentity = getMaliceIdentity(host);
                CompoundTag hostSeeds = findSeedsForIdentity(server, hostIdentity);
                if (!hostSeeds.contains(casterKey)) continue;
                double dist = level == serverLevel ? host.distanceToSqr(caster) : Double.MAX_VALUE / 2;
                if (dist < best) {
                    best = dist;
                    nearest = host;
                }
            }
        }
        if (nearest == null) {
            knownCasters.remove(caster.getUUID());
            return;
        }
        UUID identity = getMaliceIdentity(nearest);
        CompoundTag seeds = findSeedsForIdentity(server, identity);
        CompoundTag seed = seeds.getCompound(casterKey).copy();
        float damage = calculateSeedDamage(caster, nearest, seed);
        event.setCanceled(true);
        caster.setHealth(caster.getMaxHealth() * 0.5f);
        if (nearest.level() instanceof ServerLevel hostLevel) {
            if (caster instanceof ServerPlayer player) player.teleportTo(hostLevel, nearest.getX(), nearest.getY(), nearest.getZ(), Set.of(), player.getYRot(), player.getXRot());
            else if (hostLevel == serverLevel) {caster.teleportTo(nearest.getX(), nearest.getY(), nearest.getZ());}
            ModDamageTypes.trueDamage(nearest, damage, serverLevel, caster);
            ParticleUtil.spawnParticles(hostLevel, new DustParticleOptions(new Vector3f(0.4f, 0f, 0.1f), 2.5f), nearest.getEyePosition(), 60, 2.0, 0);
        }
        seeds.remove(casterKey);
        synchronizeIdentitySeeds(server, identity, seeds);
        if (seeds.isEmpty()) {knownCasters.remove(caster.getUUID());}
    }
    private static float calculateSeedDamage(LivingEntity ignoredCaster, LivingEntity host, CompoundTag seed) {
        int phase = seed.getInt("phase");
        int casterSeq = seed.getInt("casterSeq");
        int maxPhase = casterSeq <= 1 ? 4 : 3;
        int hostSeq = BeyonderData.isBeyonder(host) ? BeyonderData.getSequence(host) : 10;
        // 50% at equal sequence
        // +25% per sequence the host is weaker
        // -25% per sequence the host is stronger
        float cap = Math.clamp(0.5f + 0.25f * (hostSeq - casterSeq), 0f, 1f);
        int additionalPhases = 0;
        // Optional future rule:
        // additionalPhases = Math.max(0, (hostSeq - casterSeq) - 2);
        int effectivePhase = Math.min(phase + additionalPhases, maxPhase);
        return host.getMaxHealth() * cap * ((float) effectivePhase / maxPhase);
    }
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onHostDeath(LivingDeathEvent event) {
        if (event.isCanceled()) return;
        LivingEntity host = event.getEntity();
        if (!(host.level() instanceof ServerLevel level)) return;
        UUID identity = getMaliceIdentity(host);
        // Remove only this body's local copy.
        host.getPersistentData().remove(SEEDS_KEY);
        hostPhaseCache.remove(host.getUUID());
        CompoundTag remaining = findSeedsForIdentity(level.getServer(), identity);
        if (!remaining.isEmpty()) synchronizeIdentitySeeds(level.getServer(), identity, remaining);
    }
    public static boolean hasSeed(UUID targetUUID) {return hostPhaseCache.containsKey(targetUUID);}
    public static int getGrowth(UUID targetUUID) {return hostPhaseCache.getOrDefault(targetUUID, 0);}
    public static List<SeedInfo> collectSeeds(ServerPlayer caster) {
        String casterKey = caster.getUUID().toString();
        List<SeedInfo> result = new ArrayList<>();
        for (ServerLevel level : caster.serverLevel().getServer().getAllLevels()) {
            for (Entity e : level.getAllEntities()) {
                if (!(e instanceof LivingEntity host) || !host.isAlive()) continue;
                if (host.hasEffect(ModEffects.CONCEALMENT)) continue;
                CompoundTag seed = getSeeds(host).getCompound(casterKey);
                if (seed.isEmpty()) continue;
                result.add(new SeedInfo(host.getUUID(), host.getName().getString(), seed.getInt("phase"), seed.getInt("progress") * 100 / TICKS_PER_PHASE, level.dimension().location().toString(), host instanceof ServerPlayer));
            }
        }
        return result;
    }
    public static void setChannel(ServerPlayer caster, UUID hostUUID, int mode) {
        UUID casterUUID = caster.getUUID();
        boolean wasSpeakingAs = speakAsTarget.containsKey(casterUUID);
        UUID previousSpeakAsHost = speakAsTarget.get(casterUUID);
        boolean wasWhispering = whisperTarget.containsKey(casterUUID);
        whisperTarget.remove(casterUUID);
        speakAsTarget.remove(casterUUID);
        if (wasWhispering && mode != 1) AbilityUtil.sendActionBar(caster, Component.literal("Whispering: OFF").withColor(0xFFa3070c));
        boolean switchingHost = mode == 2 && wasSpeakingAs && !hostUUID.equals(previousSpeakAsHost);
        if (mode == 0) return;
        LivingEntity host = findHostEntity(caster, hostUUID);
        if (host == null) return;
        if (mode == 1 && !(host instanceof ServerPlayer)) return;
        CompoundTag seed = getSeeds(host).getCompound(casterUUID.toString());
        int requiredPhase = mode == 1 ? 2 : 3;
        if (seed.isEmpty() || seed.getInt("phase") < requiredPhase) return;
        if (mode == 1) {
            if (whisperTarget.containsKey(casterUUID)) {
                whisperTarget.put(casterUUID, hostUUID);
                AbilityUtil.sendActionBar(caster, Component.literal("Whispering: OFF").withColor(0xFFa3070c));}
            else {
                whisperTarget.put(casterUUID, hostUUID);
                AbilityUtil.sendActionBar(caster, Component.literal("Whispering: ON").withColor(0xFFa3070c));
            }
        } else if (mode == 2) {
            if (!wasSpeakingAs || switchingHost) {
                startSpeakAs(caster, host);
            }
        }
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true) // let CorruptingVoiceAbility's NORMAL-priority handler process damage first
    public static void onChatMessage(ServerChatEvent event) {
        ServerPlayer caster = event.getPlayer();
        UUID whisperHost = whisperTarget.get(caster.getUUID());
        if (whisperHost != null) {
            handleWhisper(event, caster, whisperHost);
            return;
        }
        UUID speakAsHost = speakAsTarget.get(caster.getUUID());
        if (speakAsHost != null) {
            handleSpeakAs(event, caster, speakAsHost);
        }
    }
    private static void handleWhisper(ServerChatEvent event, ServerPlayer caster, UUID hostUUID) {
        LivingEntity hostEntity = findHostEntity(caster, hostUUID);
        if (!(hostEntity instanceof ServerPlayer hostPlayer)) {
            whisperTarget.remove(caster.getUUID());
            return;
        }
        CompoundTag seed = getSeeds(hostPlayer).getCompound(caster.getUUID().toString());
        if (seed.isEmpty() || seed.getInt("phase") < 2) {
            whisperTarget.remove(caster.getUUID());
            return;
        }
        event.setCanceled(true);
        String message = event.getRawText();
        ServerPlayer candidate = findRandomPlayerInBand(hostPlayer);
        String speakerName = candidate != null ? candidate.getName().getString() : "";
        Component displayed = Component.literal("<" + speakerName + "> " + message);
        hostPlayer.sendSystemMessage(displayed);
        advancePhaseFromChat(hostPlayer, caster, seed, message.length());
    }
    private static void handleSpeakAs(ServerChatEvent event, ServerPlayer caster, UUID hostUUID) {
        event.setCanceled(true);
        LivingEntity host = findHostEntity(caster, hostUUID);
        if (host == null || !host.isAlive()) {
            endSpeakAs(caster);
            return;
        }
        CompoundTag seed = getSeeds(host).getCompound(caster.getUUID().toString());
        if (seed.isEmpty() || seed.getInt("phase") < 3) {
            endSpeakAs(caster);
            return;
        }
        String message = event.getRawText();
        Component spoofed = Component.literal("<" + host.getName().getString() + "> " + message);
        for (ServerPlayer online : caster.server.getPlayerList().getPlayers()) {
            if (host instanceof ServerPlayer hostPlayer && online.getUUID().equals(hostPlayer.getUUID())) {continue;}
            online.sendSystemMessage(spoofed);
        }
        advancePhaseFromChat(host, caster, seed, message.length());
    }
    private static ServerPlayer findRandomPlayerInBand(ServerPlayer host) {
        List<ServerPlayer> candidates = new ArrayList<>();
        for (ServerPlayer p : host.server.getPlayerList().getPlayers()) {
            if (p == host) continue;
            if (p.level() != host.level()) continue;
            double d = p.distanceTo(host);
            if (d >= (double) 20 && d <= (double) 90) candidates.add(p);
        }
        if (candidates.isEmpty()) return null;
        return candidates.get(new Random().nextInt(candidates.size()));
    }
    private static void advancePhaseFromChat(LivingEntity host, ServerPlayer caster, CompoundTag seed, int charCount) {
        int phase = seed.getInt("phase");
        int casterSeq = seed.getInt("casterSeq");
        int maxPhase = casterSeq <= 1 ? 4 : 3;
        if (phase >= maxPhase) return;
        double reductionFraction = 0.0001 * phase * charCount;
        int reductionTicks = (int) Math.round(TICKS_PER_PHASE * reductionFraction);
        int progress = seed.getInt("progress") + reductionTicks;
        if (progress >= TICKS_PER_PHASE) {phase++;progress = 0;seed.putInt("phase", phase);}
        seed.putInt("progress", progress);
        CompoundTag seeds = getSeeds(host);
        seeds.put(caster.getUUID().toString(), seed);
        if (host.level() instanceof ServerLevel level) {synchronizeIdentitySeeds(level.getServer(), getMaliceIdentity(host), seeds.copy());}
    }
    private static LivingEntity findHostEntity(ServerPlayer caster, UUID hostUUID) {
        for (ServerLevel level : caster.server.getAllLevels()) {
            Entity e = level.getEntity(hostUUID);
            if (e instanceof LivingEntity living) return living;
        }
        return null;
    }
    @SubscribeEvent
    public static void onCasterViewTick(PlayerTickEvent.Post event) {
        Player p = event.getEntity();
        if (!(p instanceof ServerPlayer caster)) return;
        UUID casterUUID = caster.getUUID();
        UUID hostUUID = speakAsTarget.get(casterUUID);
        if (hostUUID == null) return;
        LivingEntity host = findHostEntity(caster, hostUUID);
        boolean stillValid = host != null && host.isAlive();
        if (stillValid) {
            CompoundTag seed = getSeeds(host).getCompound(casterUUID.toString());
            stillValid = !seed.isEmpty() && seed.getInt("phase") >= 3;
        }
        if (!stillValid) {
            endSpeakAs(caster);
            return;
        }
        if (caster.getCamera() != host) {caster.setCamera(host);}
    }
    public static void cleanupOnLogout(ServerPlayer player) {
        UUID playerUUID = player.getUUID();
        whisperTarget.remove(playerUUID);
        if (speakAsTarget.remove(playerUUID) != null) ControllingUtils.endSpiritProjection(player);
        possessingHost.remove(playerUUID);
        detonatingSeed.remove(playerUUID);
    }
    public static void takeOverHost(ServerPlayer caster, UUID hostUUID) {
        if (ControllingUtils.isControlling(caster)) return;
        LivingEntity host = findHostEntity(caster, hostUUID);
        if (host == null) return;
        CompoundTag seed = getSeeds(host).getCompound(caster.getUUID().toString());
        if (seed.isEmpty() || seed.getInt("phase") < 4) return;
        if (BeyonderData.getSequence(caster) != 0) return;
        boolean success = ControllingUtils.startControlling(caster, host, false, true);
        if (!success) return;
        possessingHost.put(caster.getUUID(), hostUUID);
        MaliceSeedAbility instance = (MaliceSeedAbility) LOTMCraft.abilityHandler.getById("malice_seed_ability");
        ControllingUtils.setAllowedAbilitiesWhileControlling(caster, List.of(instance.getId()));
        AbilityWheelHelper.setAbilities(caster, new ArrayList<>(List.of(instance.getId())));
        instance.selectedAbilities.put(caster.getUUID(), 2);
        AbilityWheelHelper.setSelectedAbility(caster, 0);
    }
    private static void detonateSeed(ServerPlayer caster) {//doesn't work on non player mobs
        UUID hostUUID = possessingHost.get(caster.getUUID());
        if (hostUUID == null) return;
        if (!ControllingUtils.isControlling(caster)) {
            possessingHost.remove(caster.getUUID());
            return;
        }
        EntityControllingComponent component = caster.getData(ModAttachments.ENTITY_CONTROLLING_COMPONENT);
        LivingEntity host = component.getControlledEntity();
        if (host == null) {return;}
        CompoundTag seed = getSeeds(host).getCompound(caster.getUUID().toString());
        if (seed.isEmpty()) {return;}
        float damage = calculateSeedDamage(caster, host, seed);
        if (!(caster.level() instanceof ServerLevel hostLevel)) return;
        Vec3 explosionPos = caster.position();
        detonatingSeed.add(caster.getUUID());
        ControllingUtils.cancel(caster, 0, true, false); // triggers cleanupTakeover(...) internally now
        ModDamageTypes.trueDamage(host, damage, hostLevel, caster);
        caster.teleportTo(hostLevel, explosionPos.x, explosionPos.y, explosionPos.z, Set.of(), caster.getYRot(), caster.getXRot());
        ParticleUtil.spawnParticles(hostLevel, ParticleTypes.SOUL, explosionPos, 80, 1.0, 1.0);
        hostLevel.playSound(null, BlockPos.containing(explosionPos), SoundEvents.WARDEN_TENDRIL_CLICKS, SoundSource.PLAYERS, 2f, 0.6f);
        AbilityUtil.getNearbyEntities(caster, hostLevel, explosionPos, 6).forEach(e -> {SanityComponent sanity = e.getData(ModAttachments.SANITY_COMPONENT);sanity.increaseSanityAndSync(-0.05f, e);});
    }
    public static boolean isSpeakAsHost(UUID casterUUID, UUID entityUUID) {
        UUID host = speakAsTarget.get(casterUUID);
        return host != null && host.equals(entityUUID);
    }
    public static void cleanupTakeover(ServerPlayer player) {
        UUID playerUUID = player.getUUID();
        UUID hostUUID = possessingHost.remove(playerUUID);
        if (hostUUID == null) return;
        if (!detonatingSeed.contains(playerUUID)) return;
        LivingEntity host = findHostEntity(player, hostUUID);
        if (host != null && host.isAlive()) removeSpecificSeed(host, playerUUID);
    }
    private static void removeSpecificSeed(LivingEntity host, UUID casterUUID) {
        MinecraftServer server = host.getServer();
        if (server == null) return;
        UUID identity = getMaliceIdentity(host);
        CompoundTag seeds = findSeedsForIdentity(server, identity);
        seeds.remove(casterUUID.toString());
        synchronizeIdentitySeeds(server, identity, seeds);
    }
    @SubscribeEvent
    public static void onPurificationUsed(AbilityUsedEvent event) {
        ArrayList<String> flags = event.getInteractionFlags();
        if (flags == null) return;
        boolean isPurification = false;
        for (String flag : flags) {if ("purification".equals(flag)) { isPurification = true; break; }}
        if (!isPurification) return;
        LivingEntity purifier = event.getEntity();
        if (!(purifier.level() instanceof ServerLevel level)) return;
        int purifierSeq = BeyonderData.isBeyonder(purifier) ? BeyonderData.getSequence(purifier) : 10;
        boolean purifierIsPurificationPathway = BeyonderData.isBeyonder(purifier) && PURIFICATION_PATHWAYS.contains(BeyonderData.getPathway(purifier));
        int leniency = purifierIsPurificationPathway ? 1 : 0;
        for (LivingEntity host : level.getEntitiesOfClass(LivingEntity.class, purifier.getBoundingBox().inflate(event.getInteractionRadius()))) {
            if (!host.getPersistentData().contains(SEEDS_KEY)) continue;
            CompoundTag seeds = getSeeds(host);
            boolean anyFailedAttempt = false;
            for (String casterKey : new ArrayList<>(seeds.getAllKeys())) {
                CompoundTag seed = seeds.getCompound(casterKey);
                int casterSeq = seed.getInt("casterSeq");
                int diff = purifierSeq - casterSeq; // >0 = purifier weaker, <=0 = purifier equal or stronger
                if (diff <= leniency) {
                    seeds.remove(casterKey);
                    notifyCaster(casterKey, "cleansed");
                } else if (diff == leniency + 1) {
                    int phase = seed.getInt("phase");
                    if (phase <= 1) {
                        seeds.remove(casterKey);
                        notifyCaster(casterKey, "cleansed");
                    } else {
                        seed.putInt("phase", phase - 1);
                        seed.putInt("progress", 0);
                        seeds.put(casterKey, seed);
                        notifyCaster(casterKey, "lowered");
                    }
                } else {anyFailedAttempt = true;}
            }
            if (anyFailedAttempt) {
                SanityComponent sanity = host.getData(ModAttachments.SANITY_COMPONENT);
                sanity.increaseSanityAndSync(-0.10f, host);
            }
            synchronizeIdentitySeeds(level.getServer(), getMaliceIdentity(host), seeds.copy());
        }
    }
    private static void notifyCaster(String casterKey, String outcome) {
        UUID casterUUID;
        try { casterUUID = UUID.fromString(casterKey); } catch (Exception e) { return; }
        var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        ServerPlayer caster = server.getPlayerList().getPlayer(casterUUID);
        if (caster == null) return;
        String msg = "cleansed".equals(outcome) ? "One of your malice seeds has been purified away." : "One of your malice seeds has been weakened by purification.";
        AbilityUtil.sendActionBar(caster, Component.literal(msg).withColor(0xFFff124d));
    }
    static void startSpeakAs(ServerPlayer caster, LivingEntity host) {
        if (host == null || !host.isAlive()) return;
        if (speakAsTarget.containsKey(caster.getUUID())) return;
        if (!ControllingUtils.startSpiritProjection(caster, host)) return;
        speakAsTarget.put(caster.getUUID(), host.getUUID());
    }
    public static void endSpeakAs(ServerPlayer caster) {
        speakAsTarget.remove(caster.getUUID());
        ControllingUtils.endSpiritProjection(caster);
    }
    private static void removeAllSeedsFromCaster(UUID casterUUID, net.minecraft.server.MinecraftServer server) {
        if (server == null) return;
        String casterKey = casterUUID.toString();
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (!(entity instanceof LivingEntity host)) continue;
                CompoundTag seeds = getSeeds(host);
                if (!seeds.contains(casterKey)) continue;
                seeds.remove(casterKey);
                if (seeds.isEmpty()) {
                    host.getPersistentData().remove(SEEDS_KEY);
                    hostPhaseCache.remove(host.getUUID());
                } else {
                    host.getPersistentData().put(SEEDS_KEY, seeds);
                    int highest = 0;
                    for (String key : seeds.getAllKeys()) {highest = Math.max(highest, seeds.getCompound(key).getInt("phase"));}
                    hostPhaseCache.put(host.getUUID(), highest);
                }
            }
        }
        knownCasters.remove(casterUUID);
    }
    public static LivingEntity getSpeakAsHost(UUID casterUUID) {
        UUID hostUUID = speakAsTarget.get(casterUUID);
        if (hostUUID == null) {return null;}
        var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) {return null;}
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(hostUUID);
            if (entity instanceof LivingEntity living && living.isAlive()) {return living;}
        }
        return null;
    }
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {cleanupOnLogout(player);}
    }
    public static boolean canObserverSeeSeed(LivingEntity observer, LivingEntity host) {
        if (observer == host) return false;
        if (host.hasEffect(ModEffects.CONCEALMENT)) return false;
        CompoundTag seeds = getSeeds(host);
        if (seeds.isEmpty()) return false;
        boolean isBeyonder = BeyonderData.isBeyonder(observer);
        int observerSeq = isBeyonder ? BeyonderData.getSequence(observer) : 10;
        int leniency = (isBeyonder && SEED_SIGHT_PATHWAYS.contains(BeyonderData.getPathway(observer))) ? 1 : 0;
        for (String casterKey : seeds.getAllKeys()) {
            int casterSeq = seeds.getCompound(casterKey).getInt("casterSeq");
            if (observerSeq <= casterSeq + leniency) return true;
        }
        return false;
    }
    public static UUID getMaliceIdentity(LivingEntity entity) {
        if (entity instanceof AvatarEntity avatar) {
            UUID owner = avatar.getOriginalOwner();
            if (owner != null) return owner;
        }
        if (MarionetteUtils.isMarionette(entity)) {
            MarionetteComponent component = entity.getData(ModAttachments.MARIONETTE_COMPONENT);
            try {
                UUID owner = UUID.fromString(component.getControllerUUID());
                MinecraftServer server = entity.getServer();
                if (server != null) {
                    LivingEntity controller = findLivingEntity(server, owner);
                    if (controller != null && controller != entity) return getMaliceIdentity(controller);
                }
                return owner;
            } catch (IllegalArgumentException ignored) {
            }
        }
        CompoundTag data = entity.getPersistentData();
        if (data.hasUUID(MALICE_IDENTITY_KEY)) return data.getUUID(MALICE_IDENTITY_KEY);
        UUID identity = entity.getUUID();
        data.putUUID(MALICE_IDENTITY_KEY, identity);
        return identity;
    }
    public static void registerManifestation(UUID identity, LivingEntity manifestation) {
        if (identity == null || manifestation == null) return;
        manifestation.getPersistentData().putUUID(MALICE_IDENTITY_KEY, identity);
        MinecraftServer server = manifestation.getServer();
        if (server == null) {return;}
        CompoundTag seeds = findSeedsForIdentity(server, identity);
        if (seeds.isEmpty()) {
            manifestation.getPersistentData().remove(SEEDS_KEY);
            hostPhaseCache.remove(manifestation.getUUID());
            return;
        }
        manifestation.getPersistentData().put(SEEDS_KEY, seeds.copy());
        int highest = 0;
        for (String key : seeds.getAllKeys()) {highest = Math.max(highest, seeds.getCompound(key).getInt("phase"));}
        hostPhaseCache.put(manifestation.getUUID(), highest);
    }
    private static LivingEntity findLivingEntity(MinecraftServer server, UUID uuid) {
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(uuid);
            if (entity instanceof LivingEntity living) {return living;}
        }
        return null;
    }
    private static CompoundTag findSeedsForIdentity(MinecraftServer server, UUID identity) {
        if (server == null || identity == null) {return new CompoundTag();}
        return MaliceSeedData.get(server).getSeeds(identity);
    }
    private static void synchronizeIdentitySeeds(MinecraftServer server, UUID identity, CompoundTag seeds) {
        if (server == null || identity == null) {return;}
        CompoundTag authoritativeSeeds = seeds.copy();
        MaliceSeedData.get(server).setSeeds(identity, authoritativeSeeds);
        int highest = 0;
        for (String key : authoritativeSeeds.getAllKeys()) {highest = Math.max(highest, authoritativeSeeds.getCompound(key).getInt("phase"));}
        // Synchronize every loaded puppet, avatar, and connected person.
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (!(entity instanceof LivingEntity living)) {continue;}
                if (!getMaliceIdentity(living).equals(identity)) {continue;}
                CompoundTag persistentData = living.getPersistentData();
                if (authoritativeSeeds.isEmpty()) {
                    persistentData.remove(SEEDS_KEY);
                    hostPhaseCache.remove(living.getUUID());
                } else {
                    persistentData.put(SEEDS_KEY, authoritativeSeeds.copy());
                    hostPhaseCache.put(living.getUUID(), highest);
                }
            }
        }
    }
    public static void copyInfectionsToPersonaRecipient(LivingEntity owner, LivingEntity recipient) {
        if (owner == null || recipient == null || owner == recipient) {return;}
        MinecraftServer server = owner.getServer();
        if (server == null || recipient.getServer() != server) {return;}
        UUID ownerIdentity = getMaliceIdentity(owner);
        UUID recipientIdentity = getMaliceIdentity(recipient);
        if (ownerIdentity.equals(recipientIdentity)) {return;}
        CompoundTag ownerSeeds = findSeedsForIdentity(server, ownerIdentity);
        if (ownerSeeds.isEmpty()) {return;}
        CompoundTag recipientSeeds = findSeedsForIdentity(server, recipientIdentity);
        // Copy each infection independently
        for (String casterKey : ownerSeeds.getAllKeys()) {if (!recipientSeeds.contains(casterKey)) {recipientSeeds.put(casterKey, ownerSeeds.getCompound(casterKey).copy());}}
        synchronizeIdentitySeeds(server, recipientIdentity, recipientSeeds);
    }
}