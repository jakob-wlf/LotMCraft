package de.jakob.lotm.beyonders.abilities.abyss;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.abyss.passives.DevilBloodlines.DevilBloodlineAbilites.Curseling.BloodlineCurselingAbility;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.AbilityUseEvent;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.AbilitySelectionPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ServerChatEvent;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class LanguageOfFoulnessAbility extends SelectableAbility {
    private static final double SLOW_RADIUS = 8.0;
    private static final int CHAT_RANGE = 32;
    private static final int STUN_TICKS = 20 * 2;
    private static final int SLOTH_TICKS = 20 * 8;
    private static final int SLOTH_DELAY_TICKS = 20;
    private static final int BLASPHEMY_INTERVAL = 25;
    // Sloth state
    private static final Map<UUID, Long> slothedUntil = new ConcurrentHashMap<>();
    private static final Set<UUID> slothPending = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> slothReplaying = ConcurrentHashMap.newKeySet();
    private static final Style FOUL_STYLE = Style.EMPTY.withColor(0xFFa3070c).withFont(ResourceLocation.withDefaultNamespace("alt")); // enchanting-table font
    private static final String[][] SPOKEN = {
            {"Slow", "Freeze", "Crawl","Fall"},// 0 slow
            {"Fall to corruption", "Rot from within","Embrace your desires"},// 1 corruption
            {"Die", "Salinger","Perish"},// 2 death
            {"What is the airspeed velocity of an unladen swallow?", "It says gullible on the ceiling", "Confusion"},// 3 confusion
            {"Sloth", "Grow heavy", "Be idle","Eat Cecropia"},// 4 sloth
            {"Pray to your false god", "Blaspheme", "Curse the heavens"}// 5 blasphemy
    };
    // Fallback when nobody online has an honorific name set
    private static final String[] HONORIFICS = {
            "The King of Yellow and Black who wields good luck", "The Tower of Knowledge", "Eternal Blazing Sun", "The Empress of Misfortune and Horror",
            "The Clock-Hand that Tampers with Time", "The Symbol of War and Apocalypse", "Twilight of All Things", "The Mother of All Things",
            "The Lord who Reigns Behind the Curtain of Shadows", "Guardian of Craftsmen", "The Symbol of Destruction and the Apocalypse", "The Clairvoyant Monster",
            "The Divine Son who should never have been born","Great Door of All Doors"
    };
    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(114 / 255f, 59 / 255f, 148 / 255f), 1.5f);
    public LanguageOfFoulnessAbility(String id) {
        super(id, 6); // 3 = cooldown in seconds
    }
    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 6));
    }
    @Override
    protected float getSpiritualityCost() {
        return 100;
    }
    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.language_of_foulness.slow",
                "ability.lotmcraft.language_of_foulness.corruption",
                "ability.lotmcraft.language_of_foulness.death",
                "ability.lotmcraft.language_of_foulness.confusion",
                "ability.lotmcraft.language_of_foulness.sloth",
                "ability.lotmcraft.language_of_foulness.blasphemy"};
    }
    private static int foulness(LivingEntity entity) {return (int) BloodlineCurselingAbility.getFoulnessMultiplier(entity);}
    private int unlockedCount(LivingEntity entity) {
        int seq = AbilityUtil.getSeqWithArt(entity, this);
        if (seq > 6) return 3;
        return 6;
    }
    @Override
    public void nextAbility(LivingEntity entity) {
        if (getAbilityNames().length == 0) return;
        int selected = selectedAbilities.getOrDefault(entity.getUUID(), 0) + 1;
        if (selected >= unlockedCount(entity)) selected = 0;
        selectedAbilities.put(entity.getUUID(), selected);
        PacketHandler.sendToServer(new AbilitySelectionPacket(getId(), selected));
    }
    @Override
    public void previousAbility(LivingEntity entity) {
        if (getAbilityNames().length == 0) return;
        int selected = selectedAbilities.getOrDefault(entity.getUUID(), 0) - 1;
        if (selected < 0) selected = unlockedCount(entity) - 1;
        selectedAbilities.put(entity.getUUID(), selected);
        PacketHandler.sendToServer(new AbilitySelectionPacket(getId(), selected));
    }
    @Override
    public void setSelectedAbility(ServerPlayer player, int selectedAbility) {
        if (selectedAbility >= unlockedCount(player)) return;
        super.setSelectedAbility(player, selectedAbility);
    }
    @Override
    public int getNPCSelectedAbility(LivingEntity entity, Level level) {
        return random.nextInt(unlockedCount(entity));
    }
    //CASTING
    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (abilityIndex >= unlockedCount(entity)) {
            return;
        }
        // Slow is an area effect around the caster
        if (abilityIndex == 0) {
            speak(serverLevel, entity, abilityIndex);
            castSlow(serverLevel, entity);
            return;
        }
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 15 * (int) Math.max(multiplier(entity) / 4, 1), 2);
        if (target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.language_of_foulness.no_target").withColor(0xFFa3070c));
            return;
        }
        speak(serverLevel, entity, abilityIndex);
        ParticleUtil.spawnParticles(serverLevel, dust, target.getEyePosition(), 120, .5, .5, .5, 0.1);
        ParticleUtil.spawnParticles(serverLevel, ParticleTypes.SMOKE, target.getEyePosition(), 120, .5, .5, .5, 0.1);
        switch (abilityIndex) {
            case 1 -> castCorruption(serverLevel, entity, target);
            case 2 -> castDeath(serverLevel, entity, target);
            case 3 -> castConfusion(serverLevel, entity, target);
            case 4 -> castSloth(serverLevel, entity, target);
            case 5 -> castBlasphemy(serverLevel, entity, target);
        }
    }
    // Broadcasts the spoken command in the weird font to the caster and nearby players
    private void speak(ServerLevel level, LivingEntity caster, int index) {
        String[] lines = SPOKEN[index];
        String line = lines[random.nextInt(lines.length)];
        Component msg = Component.literal("<").append(caster.getDisplayName()).append("> ").append(Component.literal(line).withStyle(FOUL_STYLE));
        for (ServerPlayer p : level.players()) {if (p == caster || p.distanceTo(caster) <= CHAT_RANGE) p.sendSystemMessage(msg);}
    }
    private void castSlow(ServerLevel serverLevel, LivingEntity entity) {
        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        int fullDuration = 8 * 20 * (int) Math.max(multiplier(entity) / 2, 1) * foulness(entity);
        ParticleUtil.spawnParticles(serverLevel, dust, entity.position().add(0, 1, 0), 300, SLOW_RADIUS / 2, .5, SLOW_RADIUS / 2, 0.05);
        for (LivingEntity target : AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), SLOW_RADIUS)) {
            if (!target.isAlive()) continue;
            int duration = AbilityUtil.isTargetSignificantlyStronger(entitySeq, BeyonderData.getSequence(target)) ? (int) (10 * multiplier(entity)) : fullDuration;
            holdInPlace(serverLevel, entity, target, duration, true);
        }
    }
    private void castDeath(ServerLevel serverLevel, LivingEntity entity, LivingEntity target) {
        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        if (AbilityUtil.isTargetSignificantlyStronger(entitySeq, BeyonderData.getSequence(target))) return;
        ServerScheduler.scheduleForDuration(0, 1, 20 * 2, () -> {
            if (random.nextInt(8) == 0) {target.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.FILTH, entity), (float) (DamageLookup.lookupDps(6, .8, 8, 8) * multiplier(entity) * foulness(entity)));}
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 8 * 20 * foulness(entity), 3 * foulness(entity), false, false, false));
        }, null, serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), serverLevel)));
    }
    //corruption isn't in the game yet so keeping the old code.
    private void castCorruption(ServerLevel ignoredServerLevel, LivingEntity entity, LivingEntity target) {
        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        if (AbilityUtil.isTargetSignificantlyStronger(entitySeq, BeyonderData.getSequence(target))) return;
        int dur = 20 * 8 * (int) Math.max(multiplier(entity) / 2, 1) * foulness(entity);
        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, dur, 1, false, false, false));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, dur, 1, false, false, false));
        target.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, dur, random.nextInt(3), false, false, false));
        //apply the real Corruption effect once it exists
    }

    // Confusion: short stun (movement + abilities)
    private void castConfusion(ServerLevel serverLevel, LivingEntity entity, LivingEntity target) {
        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        if (AbilityUtil.isTargetSignificantlyStronger(entitySeq, BeyonderData.getSequence(target))) return;
        AbilityUtil.sealAllTargetAbilities(entity, target, "language_of_foulness_confusion", STUN_TICKS*foulness(entity));
        holdInPlace(serverLevel, entity, target, STUN_TICKS, false);
        target.addEffect(new MobEffectInstance(MobEffects.JUMP, STUN_TICKS*foulness(entity), 128, false, false, false));
    }
    // Sloth: mining fatigue + delayed ability starts
    private void castSloth(ServerLevel serverLevel, LivingEntity entity, LivingEntity target) {
        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        if (AbilityUtil.isTargetSignificantlyStronger(entitySeq, BeyonderData.getSequence(target))) return;
        target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, SLOTH_TICKS*foulness(entity), 2, false, false, false));
        slothedUntil.put(target.getUUID(), serverLevel.getGameTime() + (long) SLOTH_TICKS *foulness(entity));
    }
    //delay ability starts
    public static boolean isSlothed(LivingEntity e) {
        Long until = slothedUntil.get(e.getUUID());
        return until != null && e.level().getGameTime() < until;
    }
    @SubscribeEvent
    public static void onSlothedAbilityUse(AbilityUseEvent event) {
        LivingEntity user = event.getEntity();
        if (!(user.level() instanceof ServerLevel serverLevel)) return;
        if (!isSlothed(user)) return;
        UUID id = user.getUUID();
        if (slothReplaying.contains(id)) return;
        event.setCanceled(true);
        if (!slothPending.add(id)) return;
        Ability ability = event.getAbility();
        ServerScheduler.scheduleDelayed(SLOTH_DELAY_TICKS, () -> {
            slothPending.remove(id);
            if (!user.isAlive()) return;
            slothReplaying.add(id);
            try {ability.useAbility(serverLevel, user);}
            finally {slothReplaying.remove(id);}
        });
    }
    private void castBlasphemy(ServerLevel serverLevel, LivingEntity entity, LivingEntity target) {
        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        if (AbilityUtil.isTargetSignificantlyStronger(entitySeq, BeyonderData.getSequence(target))) return;
        UUID id = target.getUUID();
        List<String> lines = pickHonorificLines(serverLevel, id);
        // Ally protection is off only for as long as the recital takes
        boolean added = AbilityUtil.ignoreAllies.putIfAbsent(id, true) == null;
        int totalTicks = lines.size() * BLASPHEMY_INTERVAL;
        ServerScheduler.scheduleDelayed(totalTicks, () -> {if (added) AbilityUtil.ignoreAllies.remove(id);});
        AtomicInteger lineIndex = new AtomicInteger(0);
        // One tick of the schedule per line, so it ends right after the last one
        ServerScheduler.scheduleForDuration(0, BLASPHEMY_INTERVAL, totalTicks, () -> {
            int idx = lineIndex.getAndIncrement();
            if (idx >= lines.size() || !target.isAlive()) return;
            String line = lines.get(idx);
            // Feed the line to your prayer handler as if the player typed it (players only)
            if (target instanceof ServerPlayer player) {NeoForge.EVENT_BUS.post(new ServerChatEvent(player, line, Component.literal(line)));}
        }, null, serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), serverLevel)));
    }
    private static List<String> pickHonorificLines(ServerLevel level, UUID exclude) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        List<List<String>> names = new ArrayList<>();
        for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
            if (p.getUUID().equals(exclude)) continue;
            var honorific = BeyonderData.getHonorificName(p);
            if (honorific.isPresent()) {
                honorific.get();
                if (!honorific.get().isEmpty()) {
                    List<String> lines = honorific.get().lines().stream().filter(l -> l != null && !l.isBlank()).toList();
                    if (!lines.isEmpty()) names.add(lines);
                }
            }
        }
        if (!names.isEmpty()) return names.get(rng.nextInt(names.size()));
        return List.of("Praise the " + HONORIFICS[rng.nextInt(HONORIFICS.length)] + "!");
    }
    private void holdInPlace(ServerLevel serverLevel, LivingEntity caster, LivingEntity target, int duration, boolean slowEffect) {
        ServerScheduler.scheduleForDuration(0, 1, duration, () -> {
            target.setDeltaMovement(0, 0, 0);
            target.hurtMarked = true;
            if (slowEffect) target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 10, false, false, false));
        }, null, serverLevel, () -> AbilityUtil.getTimeInArea(caster, new Location(caster.position(), serverLevel)));
    }
}