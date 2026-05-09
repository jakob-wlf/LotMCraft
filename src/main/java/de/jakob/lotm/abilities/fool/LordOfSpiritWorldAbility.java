package de.jakob.lotm.abilities.fool;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

// ╔══════════════════════════════════════════════════════════════╗
// ║  LORD OF THE SPIRIT WORLD — Sequence 0                      ║
// ║  Tears open a rift and summons 3-5 angel-tier spirit servants║
// ╚══════════════════════════════════════════════════════════════╝
public class LordOfSpiritWorldAbility extends Ability {

    // Rift / ritual colours
    private static final DustParticleOptions VOID_BLACK =
            new DustParticleOptions(new Vector3f(0.08f, 0.0f, 0.18f), 2.5f);
    private static final DustParticleOptions SPIRIT_PURPLE =
            new DustParticleOptions(new Vector3f(0.55f, 0.0f, 0.88f), 2.5f);
    private static final DustParticleOptions SPIRIT_LIGHT =
            new DustParticleOptions(new Vector3f(0.75f, 0.35f, 1.0f), 2.0f);
    private static final DustParticleOptions GOLD_SIGIL =
            new DustParticleOptions(new Vector3f(1.0f, 0.85f, 0.0f), 2.0f);

    /** Ticks summoned servants persist before FoolingAbility removes them. */
    private static final int SERVANT_DURATION_TICKS = 1200; // 60 s

    public LordOfSpiritWorldAbility() {
        super("lord_of_spirit_world", 60);
        this.canBeCopied = false;
        this.canBeReplicated = false;
        this.canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() { return Map.of("fool", 0); }

    @Override
    protected float getSpiritualityCost() { return 600; }

    @Override
    @SuppressWarnings("unchecked")
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!(entity instanceof Player player)) return;

        final int color   = BeyonderData.pathwayInfos.get("fool").color();
        final Vec3 center = player.position();

        player.displayClientMessage(
                Component.translatable("ability.lotmcraft.fooling.spirit_world_servant").withColor(color), true);

        // Choose 3–5 random spirit types
        List<EntityType<?>> pool = new ArrayList<>();
        pool.add(ModEntities.SPIRIT_TRANSLUCENT_WIZARD.get());
        pool.add(ModEntities.SPIRIT_BANE.get());
        pool.add(ModEntities.SPIRIT_BIZARRO_BANE.get());
        pool.add(ModEntities.SPIRIT_GHOST.get());
        pool.add(ModEntities.SPIRIT_DERVISH_ENTITY.get());
        pool.add(ModEntities.SPIRIT_MALMOUTH.get());
        pool.add(ModEntities.SPIRIT_BLUE_WIZARD.get());
        pool.add(ModEntities.SPIRIT_BUBBLES_ENTITY.get());
        Collections.shuffle(pool, new java.util.Random(serverLevel.random.nextLong()));
        final int spawnCount = Math.min(3 + serverLevel.random.nextInt(3), pool.size());
        final List<EntityType<?>> chosen = new ArrayList<>(pool.subList(0, spawnCount));

        // ── Phase 1: Ritual tear (0–60 ticks) ──────────────────────────────────
        // A dark rift opens in the ground — three concentric rings contract inward
        // while golden sigil runes circle the perimeter.
        serverLevel.playSound(null, center.x, center.y, center.z,
                SoundEvents.EVOKER_CAST_SPELL, SoundSource.AMBIENT, 3.0f, 0.35f);

        AtomicInteger phase1Tick = new AtomicInteger(0);
        ServerScheduler.scheduleForDuration(0, 1, 60, () -> {
            int t = phase1Tick.getAndIncrement();
            double prog = t / 60.0;                    // 0 → 1

            // Three contracting void rings (r=14→1, r=10→1, r=6→1)
            double[] initR = {14.0, 10.0, 6.0};
            for (int ring = 0; ring < 3; ring++) {
                double r = initR[ring] * (1.0 - prog) + 1.0;
                int pts = 72 - ring * 16;
                for (int i = 0; i < pts; i++) {
                    double a = i * Math.PI * 2.0 / pts + t * (0.07 + ring * 0.04);
                    double px = center.x + Math.cos(a) * r;
                    double pz = center.z + Math.sin(a) * r;
                    DustParticleOptions col = ring == 0 ? VOID_BLACK : (ring == 1 ? SPIRIT_PURPLE : SPIRIT_LIGHT);
                    serverLevel.sendParticles(col, px, center.y + 0.2 + ring * 0.3, pz, 1, 0, 0, 0, 0);
                }
            }

            // Golden sigil runes orbiting at r=16 and r=8
            for (int rune = 0; rune < 2; rune++) {
                double runeR = rune == 0 ? 16.0 : 8.0;
                int runePts = rune == 0 ? 12 : 8;
                for (int i = 0; i < runePts; i++) {
                    double a = i * Math.PI * 2.0 / runePts + t * (0.05 - rune * 0.02);
                    serverLevel.sendParticles(GOLD_SIGIL,
                            center.x + Math.cos(a) * runeR, center.y + 1.2 + rune * 0.5,
                            center.z + Math.sin(a) * runeR, 1, 0, 0, 0, 0);
                }
            }

            // WITCH + SOUL particles rising from the rift core
            for (int i = 0; i < 8; i++) {
                double ox = (serverLevel.random.nextDouble() - 0.5) * 3.0;
                double oz = (serverLevel.random.nextDouble() - 0.5) * 3.0;
                serverLevel.sendParticles(ParticleTypes.WITCH, center.x + ox, center.y + 0.5, center.z + oz,
                        1, 0, 0.2, 0, 0.05);
            }
            for (int i = 0; i < 5; i++) {
                double ox = (serverLevel.random.nextDouble() - 0.5) * 2.0;
                double oz = (serverLevel.random.nextDouble() - 0.5) * 2.0;
                serverLevel.sendParticles(ParticleTypes.SOUL, center.x + ox, center.y + 0.5, center.z + oz,
                        1, 0, 0.18, 0, 0.04);
            }

            // PORTAL particles bleed out of the rift
            for (int i = 0; i < 12; i++) {
                double a = serverLevel.random.nextDouble() * Math.PI * 2;
                double r = serverLevel.random.nextDouble() * 5.0;
                serverLevel.sendParticles(ParticleTypes.PORTAL,
                        center.x + Math.cos(a) * r, center.y + 0.3, center.z + Math.sin(a) * r,
                        1, 0, 0.5, 0, 0.12);
            }

            // Growing vertical rift tear: column of void dust rising from center
            for (double hy = 0; hy < (prog * 10.0); hy += 0.7) {
                serverLevel.sendParticles(VOID_BLACK, center.x, center.y + hy, center.z,
                        1, 0.4, 0, 0.4, 0);
                serverLevel.sendParticles(SPIRIT_PURPLE, center.x, center.y + hy + 0.3, center.z,
                        1, 0.6, 0, 0.6, 0);
            }

            // Mid-point roar
            if (t == 30)
                serverLevel.playSound(null, center.x, center.y, center.z,
                        SoundEvents.WITHER_AMBIENT, SoundSource.HOSTILE, 2.5f, 0.5f);
        }, serverLevel);

        // ── Phase 2: Servants materialize (tick 60) ────────────────────────────
        // Grand flash + portal vortex → spirits coalesce from particle cocoons
        ServerScheduler.scheduleDelayed(60, () -> {
            // Grand flash + rift-collapse ring
            serverLevel.sendParticles(ParticleTypes.FLASH, center.x, center.y + 1.0, center.z, 1, 0, 0, 0, 0);
            for (int i = 0; i < 80; i++) {
                double a = i * Math.PI * 2.0 / 80;
                double r = 3.0 + serverLevel.random.nextDouble() * 5.0;
                serverLevel.sendParticles(VOID_BLACK,
                        center.x + Math.cos(a) * r, center.y + 0.5, center.z + Math.sin(a) * r, 1, 0, 0, 0, 0);
                serverLevel.sendParticles(ParticleTypes.PORTAL,
                        center.x + Math.cos(a) * r, center.y + 0.5, center.z + Math.sin(a) * r,
                        1, 0, 0.4, 0, 0.15);
            }
            // Void sphere burst
            ParticleUtil.spawnSphereParticles(serverLevel, SPIRIT_PURPLE, center.add(0, 2, 0), 8.0, 200, 0.0);

            serverLevel.playSound(null, center.x, center.y, center.z,
                    SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 3.5f, 0.55f);

            // Spawn each spirit with its own materialization vortex
            for (int s = 0; s < spawnCount; s++) {
                Entity spawned = ((EntityType<Entity>) chosen.get(s)).create(serverLevel);
                if (!(spawned instanceof LivingEntity spirit)) continue;

                double spawnR = 6.0 + serverLevel.random.nextDouble() * 4.0;
                double spawnAngle = s * (Math.PI * 2.0 / spawnCount);
                double sx = center.x + Math.cos(spawnAngle) * spawnR;
                double sz = center.z + Math.sin(spawnAngle) * spawnR;
                spirit.setPos(sx, center.y, sz);
                boostAttributes(spirit);
                serverLevel.addFreshEntity(spirit);

                FoolingAbility.SPIRIT_WORLD_SUMMONS.put(
                        spirit.getUUID(),
                        serverLevel.getGameTime() + SERVANT_DURATION_TICKS);

                // Per-spirit: vortex cocoon materializing (20 ticks after phase 2)
                final double finalSx = sx, finalSz = sz;
                final int delay = s * 4;
                ServerScheduler.scheduleDelayed(delay, () -> {
                    // Central FLASH at spirit spawn location
                    serverLevel.sendParticles(ParticleTypes.FLASH, finalSx, center.y + 1.5, finalSz, 1, 0, 0, 0, 0);
                    // Tight cocoon ring collapsing inward — spirit forming
                    for (int ring = 0; ring < 3; ring++) {
                        double cocoonR = 2.5 - ring * 0.5;
                        for (int i = 0; i < 40; i++) {
                            double a = i * Math.PI * 2.0 / 40 + ring * 0.7;
                            double ry = center.y + ring * 1.2;
                            serverLevel.sendParticles(ring == 0 ? VOID_BLACK : SPIRIT_PURPLE,
                                    finalSx + Math.cos(a) * cocoonR, ry, finalSz + Math.sin(a) * cocoonR,
                                    1, 0, 0.05, 0, 0);
                        }
                    }
                    // Upward SOUL column through the spirit's body
                    for (double hy = 0; hy < 4.0; hy += 0.5)
                        serverLevel.sendParticles(ParticleTypes.SOUL,
                                finalSx, center.y + hy, finalSz, 1, 0.3, 0, 0.3, 0.04);
                    // Spirit's "aura" ring at feet (persistent-looking but 1 frame)
                    for (int i = 0; i < 32; i++) {
                        double a = i * Math.PI * 2.0 / 32;
                        serverLevel.sendParticles(GOLD_SIGIL,
                                finalSx + Math.cos(a) * 1.5, center.y + 0.2, finalSz + Math.sin(a) * 1.5,
                                1, 0, 0, 0, 0);
                    }
                }, serverLevel);
            }
        }, serverLevel);
    }

    private static void boostAttributes(LivingEntity spirit) {
        AttributeInstance hp = spirit.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) { hp.setBaseValue(500.0); spirit.setHealth(500.0f); }
        AttributeInstance dmg = spirit.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dmg != null) dmg.setBaseValue(120.0);
        AttributeInstance armor = spirit.getAttribute(Attributes.ARMOR);
        if (armor != null) armor.setBaseValue(25.0);
        AttributeInstance spd = spirit.getAttribute(Attributes.MOVEMENT_SPEED);
        if (spd != null) spd.setBaseValue(1.5);
        AttributeInstance range = spirit.getAttribute(Attributes.FOLLOW_RANGE);
        if (range != null) range.setBaseValue(64.0);
    }
}


