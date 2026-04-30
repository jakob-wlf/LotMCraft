package de.jakob.lotm.abilities.death;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncSpiritChannelingPacket;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class SpiritChannelingAbility extends SelectableAbility {

    public enum SpiritType { FROST_GHOST, EARTH_SPIRIT }

    private static final HashMap<UUID, SpiritType> capturedSpirits = new HashMap<>();
    private static final Set<UUID> glacialAegisActive = new HashSet<>();

    private static final String[] BASE_MODES = {
            "ability.lotmcraft.spirit_channeling.get_spirit",
            "ability.lotmcraft.spirit_channeling.release_spirit"
    };

    private static final String[] FROST_MODES = {
            "ability.lotmcraft.spirit_channeling.get_spirit",
            "ability.lotmcraft.spirit_channeling.release_spirit",
            "ability.lotmcraft.spirit_channeling.frozen_domain",
            "ability.lotmcraft.spirit_channeling.glacial_aegis"
    };

    private static final String[] EARTH_MODES = {
            "ability.lotmcraft.spirit_channeling.get_spirit",
            "ability.lotmcraft.spirit_channeling.release_spirit",
            "ability.lotmcraft.spirit_channeling.stone_restrainment",
            "ability.lotmcraft.spirit_channeling.earthen_fist",
            "ability.lotmcraft.spirit_channeling.quicksand",
            "ability.lotmcraft.spirit_channeling.earth_heal"
    };

    private static final DustParticleOptions FROST_DUST       = new DustParticleOptions(new Vector3f(0.5f,  0.85f, 1.0f),  2.0f);
    private static final DustParticleOptions FROST_DUST_SMALL = new DustParticleOptions(new Vector3f(0.7f,  0.92f, 1.0f),  1.0f);
    private static final DustParticleOptions EARTH_DUST       = new DustParticleOptions(new Vector3f(0.55f, 0.38f, 0.18f), 2.0f);
    private static final DustParticleOptions EARTH_DUST_SMALL = new DustParticleOptions(new Vector3f(0.55f, 0.38f, 0.18f), 1.2f);
    private static final DustParticleOptions STONE_DUST       = new DustParticleOptions(new Vector3f(0.5f,  0.5f,  0.5f),  1.5f);

    public SpiritChannelingAbility(String id) {
        super(id, 4f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("death", 7));
    }

    @Override
    protected float getSpiritualityCost() {
        return 300;
    }

    @Override
    protected String[] getAbilityNames() {
        return BASE_MODES;
    }

    private String[] getAbilityNamesForPlayer(UUID uuid) {
        if (capturedSpirits.isEmpty() && net.neoforged.fml.loading.FMLEnvironment.dist == net.neoforged.api.distmarker.Dist.CLIENT) {
            int ordinal = de.jakob.lotm.util.ClientSpiritCache.getSpiritTypeOrdinal();
            if (ordinal >= 0) {
                SpiritType[] values = SpiritType.values();
                if (ordinal < values.length) {
                    return switch (values[ordinal]) {
                        case FROST_GHOST   -> FROST_MODES;
                        case EARTH_SPIRIT  -> EARTH_MODES;
                    };
                }
            }
            return BASE_MODES;
        }
        SpiritType type = capturedSpirits.get(uuid);
        if (type == null) return BASE_MODES;
        return switch (type) {
            case FROST_GHOST  -> FROST_MODES;
            case EARTH_SPIRIT -> EARTH_MODES;
        };
    }

    @Override
    public String[] getAbilityNamesCopy() {
        return getAbilityNamesForPlayer(null);
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        if (InteractionHandler.isInteractionPossibleStrictlyHigher(new Location(entity.position(), (ServerLevel) level), "purification", de.jakob.lotm.util.BeyonderData.getSequence(entity), -1)) return;

        String[] names = getAbilityNamesForPlayer(entity.getUUID());
        int idx = selectedAbilities.getOrDefault(entity.getUUID(), 0);
        if (idx >= names.length) {
            idx = 0;
            selectedAbilities.put(entity.getUUID(), 0);
        }
        castSelectedAbility(level, entity, idx);
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (level.isClientSide) return;

        String[] names = getAbilityNamesForPlayer(entity.getUUID());
        if (abilityIndex >= names.length) return;
        String mode = names[abilityIndex];

        switch (mode) {
            case "ability.lotmcraft.spirit_channeling.get_spirit"       -> getSpirit(level, entity);
            case "ability.lotmcraft.spirit_channeling.release_spirit"   -> releaseSpirit(level, entity);
            case "ability.lotmcraft.spirit_channeling.frozen_domain"    -> frozenDomain(level, entity);
            case "ability.lotmcraft.spirit_channeling.glacial_aegis"    -> glacialAegis(level, entity);
            case "ability.lotmcraft.spirit_channeling.stone_restrainment" -> stoneRestrainment(level, entity);
            case "ability.lotmcraft.spirit_channeling.earthen_fist"     -> earthenFist(level, entity);
            case "ability.lotmcraft.spirit_channeling.quicksand"        -> quicksand(level, entity);
            case "ability.lotmcraft.spirit_channeling.earth_heal"       -> earthHeal(level, entity);
        }
    }

    @Override
    public void nextAbility(LivingEntity entity) {
        String[] names = getAbilityNamesForPlayer(entity.getUUID());
        if (names.length == 0) return;
        int idx = (selectedAbilities.getOrDefault(entity.getUUID(), 0) + 1) % names.length;
        selectedAbilities.put(entity.getUUID(), idx);
        de.jakob.lotm.network.PacketHandler.sendToServer(new de.jakob.lotm.network.packets.toServer.AbilitySelectionPacket(getId(), idx));
    }

    @Override
    public void previousAbility(LivingEntity entity) {
        String[] names = getAbilityNamesForPlayer(entity.getUUID());
        if (names.length == 0) return;
        int idx = (selectedAbilities.getOrDefault(entity.getUUID(), 0) - 1 + names.length) % names.length;
        selectedAbilities.put(entity.getUUID(), idx);
        de.jakob.lotm.network.PacketHandler.sendToServer(new de.jakob.lotm.network.packets.toServer.AbilitySelectionPacket(getId(), idx));
    }

    @Override
    public String getSelectedAbility(LivingEntity entity) {
        String[] names = getAbilityNamesForPlayer(entity.getUUID());
        if (names.length == 0) return "";
        int idx = selectedAbilities.getOrDefault(entity.getUUID(), 0);
        if (idx >= names.length) idx = 0;
        return names[idx];
    }

    @Override
    public void setSelectedAbility(ServerPlayer player, int selectedAbility) {
        String[] names = getAbilityNamesForPlayer(player.getUUID());
        if (names.length == 0) return;
        if (selectedAbility < 0 || selectedAbility >= names.length) return;
        selectedAbilities.put(player.getUUID(), selectedAbility);
    }

    // -------------------------------------------------------------------------
    // Core: Get / Release Spirit
    // -------------------------------------------------------------------------

    private void getSpirit(Level level, LivingEntity entity) {
        if (capturedSpirits.containsKey(entity.getUUID())) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.spirit_channeling.already_have").withColor(0xFF334f23));
            return;
        }

        level.playSound(null, entity.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0f, 0.8f);

        int seq = de.jakob.lotm.util.BeyonderData.getSequence(entity);
        float successChance = (seq <= 6) ? 0.75f : 0.50f;
        if (random.nextFloat() >= successChance) {
            level.playSound(null, entity.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.6f, 0.5f);
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.spirit_channeling.failed").withColor(0xFF334f23));
            return;
        }

        SpiritType type = random.nextBoolean() ? SpiritType.FROST_GHOST : SpiritType.EARTH_SPIRIT;
        capturedSpirits.put(entity.getUUID(), type);

        if (entity instanceof net.minecraft.server.level.ServerPlayer sp) {
            PacketHandler.sendToPlayer(sp, new SyncSpiritChannelingPacket(type.ordinal()));
        }

        spawnSpiritCaptureParticles((ServerLevel) level, entity, type);

        String nameKey = type == SpiritType.FROST_GHOST
                ? "ability.lotmcraft.spirit_channeling.got_frost_ghost"
                : "ability.lotmcraft.spirit_channeling.got_earth_spirit";
        AbilityUtil.sendActionBar(entity, Component.translatable(nameKey).withColor(0xFF334f23));
    }

    private void releaseSpirit(Level level, LivingEntity entity) {
        SpiritType type = capturedSpirits.remove(entity.getUUID());
        selectedAbilities.put(entity.getUUID(), 0);

        if (entity instanceof net.minecraft.server.level.ServerPlayer sp) {
            PacketHandler.sendToPlayer(sp, new SyncSpiritChannelingPacket(-1));
        }

        if (type == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.spirit_channeling.no_captured").withColor(0xFF334f23));
            return;
        }

        spawnSpiritReleaseParticles((ServerLevel) level, entity, type);

        String spiritNameKey = "ability.lotmcraft.spirit_channeling.spirit_name." + type.name().toLowerCase();
        AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.spirit_channeling.released", Component.translatable(spiritNameKey)).withColor(0xFF334f23));
    }

    private void spawnSpiritCaptureParticles(ServerLevel level, LivingEntity entity, SpiritType type) {
        Vec3 center = entity.position().add(0, entity.getEyeHeight() / 2.0, 0);

        if (type == SpiritType.FROST_GHOST) {
            level.playSound(null, entity.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.7f, 1.6f);
            level.playSound(null, entity.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.5f, 1.8f);

            ServerScheduler.scheduleForDuration(0, 1, 30, () -> {
                long t = level.getGameTime();
                double progress = (t % 30) / 30.0;
                double spiralRadius = 2.5 * (1.0 - progress);
                int rings = 3;

                for (int ring = 0; ring < rings; ring++) {
                    double yOff = ring * 0.5 - 0.5;
                    for (int i = 0; i < 12; i++) {
                        double angle = (2 * Math.PI * i / 12) + (t * 0.3) + (ring * Math.PI / rings);
                        Vec3 p = center.add(
                                Math.cos(angle) * spiralRadius,
                                yOff,
                                Math.sin(angle) * spiralRadius
                        );
                        ParticleUtil.spawnParticles(level, FROST_DUST, p, 1, 0.05);
                        ParticleUtil.spawnParticles(level, ParticleTypes.SNOWFLAKE, p, 1, 0.08);
                    }
                }
                ParticleUtil.spawnParticles(level, FROST_DUST_SMALL, center, 4, 0.2, entity.getEyeHeight() / 2.0, 0.2, 0);
            }, () -> {
                ParticleUtil.spawnParticles(level, FROST_DUST, center, 60, 1.0, entity.getEyeHeight() / 2.0, 1.0, 0.05f);
                ParticleUtil.spawnParticles(level, ParticleTypes.SNOWFLAKE, center.add(0, 0.5, 0), 40, 0.8, 0.5, 0.8, 0.02f);
                ParticleUtil.spawnParticles(level, ParticleTypes.ITEM_SNOWBALL, center, 20, 0.5);
            }, level, () -> 1.0);

        } else {
            level.playSound(null, entity.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.7f, 0.6f);
            level.playSound(null, entity.blockPosition(), SoundEvents.GRAVEL_BREAK, SoundSource.PLAYERS, 1.0f, 0.5f);
            level.playSound(null, entity.blockPosition(), SoundEvents.ROOTED_DIRT_BREAK, SoundSource.PLAYERS, 1.0f, 0.6f);

            ServerScheduler.scheduleForDuration(0, 1, 30, () -> {
                long t = level.getGameTime();
                double progress = (t % 30) / 30.0;
                double spiralRadius = 2.5 * (1.0 - progress);

                for (int i = 0; i < 16; i++) {
                    double angle = (2 * Math.PI * i / 16) + (t * 0.25);
                    double yOff = Math.sin(angle * 2 + t * 0.15) * 0.4;
                    Vec3 p = center.add(
                            Math.cos(angle) * spiralRadius,
                            yOff,
                            Math.sin(angle) * spiralRadius
                    );
                    ParticleUtil.spawnParticles(level, EARTH_DUST, p, 1, 0.06);
                    ParticleUtil.spawnParticles(level, STONE_DUST, p, 1, 0.05);
                }
                ParticleUtil.spawnParticles(level, EARTH_DUST_SMALL, center, 3, 0.15, entity.getEyeHeight() / 2.0, 0.15, 0);
            }, () -> {
                ParticleUtil.spawnParticles(level, EARTH_DUST, center, 60, 1.0, entity.getEyeHeight() / 2.0, 1.0, 0.05f);
                ParticleUtil.spawnParticles(level, STONE_DUST, center, 40, 0.8, entity.getEyeHeight() / 2.0, 0.8, 0.03f);
                ParticleUtil.spawnParticles(level, ParticleTypes.EXPLOSION, center, 3, 0.4);
            }, level, () -> 1.0);
        }
    }

    private void spawnSpiritReleaseParticles(ServerLevel level, LivingEntity entity, SpiritType type) {
        Vec3 center = entity.position().add(0, entity.getEyeHeight() / 2.0, 0);

        if (type == SpiritType.FROST_GHOST) {
            level.playSound(null, entity.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.8f, 1.4f);

            ServerScheduler.scheduleForDuration(0, 1, 25, () -> {
                long t = level.getGameTime();
                double progress = (t % 25) / 25.0;
                double expandRadius = progress * 3.0;

                for (int i = 0; i < 10; i++) {
                    double angle = (2 * Math.PI * i / 10) + (t * 0.2);
                    Vec3 p = center.add(
                            Math.cos(angle) * expandRadius,
                            Math.sin(t * 0.2 + i) * 0.3,
                            Math.sin(angle) * expandRadius
                    );
                    ParticleUtil.spawnParticles(level, FROST_DUST, p, 1, 0.05);
                    if (i % 2 == 0) ParticleUtil.spawnParticles(level, ParticleTypes.SNOWFLAKE, p, 1, 0.1);
                }
                ParticleUtil.spawnParticles(level, FROST_DUST_SMALL, center.add(0, progress * 2.0, 0), 5, 0.2);
            }, () -> {
                for (int i = 0; i < 20; i++) {
                    double angle = (2 * Math.PI * i / 20);
                    Vec3 p = center.add(Math.cos(angle) * 3.0, random.nextDouble() * 2.0, Math.sin(angle) * 3.0);
                    ParticleUtil.spawnParticles(level, FROST_DUST_SMALL, p, 3, 0.1);
                    ParticleUtil.spawnParticles(level, ParticleTypes.SNOWFLAKE, p, 2, 0.15);
                }
            }, level, () -> 1.0);

        } else {
            level.playSound(null, entity.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.8f, 0.5f);
            level.playSound(null, entity.blockPosition(), SoundEvents.GRAVEL_FALL, SoundSource.PLAYERS, 1.0f, 0.4f);

            ServerScheduler.scheduleForDuration(0, 1, 25, () -> {
                long t = level.getGameTime();
                double progress = (t % 25) / 25.0;
                double expandRadius = progress * 3.0;

                for (int i = 0; i < 14; i++) {
                    double angle = (2 * Math.PI * i / 14) + (t * 0.18);
                    Vec3 p = center.add(
                            Math.cos(angle) * expandRadius,
                            -progress * 0.5,
                            Math.sin(angle) * expandRadius
                    );
                    ParticleUtil.spawnParticles(level, EARTH_DUST, p, 1, 0.07);
                    if (i % 3 == 0) ParticleUtil.spawnParticles(level, STONE_DUST, p, 1, 0.06);
                }
                ParticleUtil.spawnParticles(level, EARTH_DUST_SMALL, center, 4, 0.2, entity.getEyeHeight() / 2.0, 0.2, 0.02f);
            }, () -> {
                for (int i = 0; i < 20; i++) {
                    double angle = (2 * Math.PI * i / 20);
                    Vec3 p = center.add(Math.cos(angle) * 3.0, random.nextDouble(), Math.sin(angle) * 3.0);
                    ParticleUtil.spawnParticles(level, EARTH_DUST, p, 3, 0.1);
                    ParticleUtil.spawnParticles(level, STONE_DUST, p, 2, 0.1);
                }
                ParticleUtil.spawnParticles(level, ParticleTypes.EXPLOSION, center, 2, 0.3);
            }, level, () -> 1.0);
        }
    }

    // -------------------------------------------------------------------------
    // Frost Ghost abilities
    // -------------------------------------------------------------------------

    private void frozenDomain(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        int casterSeq = de.jakob.lotm.util.BeyonderData.getSequence(entity);
        Vec3 startPos = entity.position();
        AtomicDouble radius = new AtomicDouble(0.5);

        ServerScheduler.scheduleForDuration(0, 2, 20 * 3, () -> {
            ParticleUtil.spawnParticles((ServerLevel) level, FROST_DUST, startPos.add(0, 1, 0), 60, radius.get(), 0.3, radius.get(), 0);
            ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.SNOWFLAKE, startPos.add(0, 0.5, 0), 30, radius.get(), 0.2, radius.get(), 0);

            for (LivingEntity nearby : AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, startPos, radius.get() + 0.5)) {
                int targetSeq = de.jakob.lotm.util.BeyonderData.getSequence(nearby);
                if (targetSeq <= casterSeq - 1) continue;

                nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 100, false, false, false));
                nearby.addEffect(new MobEffectInstance(MobEffects.JUMP, 60, 128, false, false, false));
                nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 5, 2, false, false, false));
                nearby.setTicksFrozen(nearby.getTicksRequiredToFreeze() + 40);
            }

            radius.addAndGet(0.5);
        }, null, (ServerLevel) level, () -> 1.0);
    }

    private void glacialAegis(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        if (glacialAegisActive.contains(entity.getUUID())) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.spirit_channeling.aegis_already_active").withColor(0xFF334f23));
            return;
        }

        glacialAegisActive.add(entity.getUUID());

        ServerScheduler.scheduleForDuration(0, 1, 20 * 10, () -> {
            if (!glacialAegisActive.contains(entity.getUUID())) return;
            Vec3 pos = entity.position().add(0, 1, 0);
            for (int i = 0; i < 8; i++) {
                double angle = (2 * Math.PI * i) / 8;
                Vec3 particlePos = pos.add(Math.cos(angle) * 0.8, 0, Math.sin(angle) * 0.8);
                ParticleUtil.spawnParticles((ServerLevel) level, FROST_DUST, particlePos, 1, 0.05);
            }
        }, () -> glacialAegisActive.remove(entity.getUUID()), (ServerLevel) level, () -> 1.0);

        AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.spirit_channeling.aegis_active").withColor(0xFF334f23));
    }

    // -------------------------------------------------------------------------
    // Earth Spirit abilities
    // -------------------------------------------------------------------------

    private void stoneRestrainment(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20, 1.5f);
        if (target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.spirit_channeling.no_target").withColor(0xFF334f23));
            return;
        }

        int casterSeq = de.jakob.lotm.util.BeyonderData.getSequence(entity);
        int targetSeq = de.jakob.lotm.util.BeyonderData.getSequence(target);
        if (targetSeq <= casterSeq - 1) return;

        AtomicBoolean done = new AtomicBoolean(false);

        ServerScheduler.scheduleForDuration(0, 2, 20 * 4 * (int) Math.max(multiplier(entity) / 4, 1), () -> {
            if (target.isDeadOrDying()) {
                done.set(true);
                return;
            }

            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 100, false, false, false));
            target.addEffect(new MobEffectInstance(MobEffects.JUMP, 20, 128, false, false, false));
            target.hurt(ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, entity), 1.0f);
            target.invulnerableTime = 0;

            ParticleUtil.spawnParticles((ServerLevel) level, STONE_DUST, target.position().add(0, target.getEyeHeight() / 2, 0),
                    10, 0.4, target.getEyeHeight() / 2, 0.4, 0);
            ParticleUtil.spawnParticles((ServerLevel) level, EARTH_DUST_SMALL, target.position().add(0, target.getEyeHeight() / 2, 0),
                    6, 0.3, target.getEyeHeight() / 2, 0.3, 0);
        }, null, (ServerLevel) level, () -> 1.0);
    }

    private void earthenFist(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        Vec3 startPos = entity.getEyePosition();
        Vec3 direction = entity.getLookAngle().normalize();
        AtomicBoolean hasHit = new AtomicBoolean(false);

        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = new Vec3(
                direction.z * up.y - direction.y * up.z,
                direction.x * up.z - direction.z * up.x,
                direction.y * up.x - direction.x * up.y
        ).normalize();

        for (int fist = 0; fist < 2; fist++) {
            double spread = (fist == 0) ? -0.15 : 0.15;
            Vec3 fistDir = direction.add(right.scale(spread)).normalize();
            final Vec3 finalFistDir = fistDir;

            final int[] tick = {0};
            Vec3 initialPos = startPos.add(finalFistDir.scale(1.5));
            final Vec3[] currentPos = {initialPos};

            ServerScheduler.scheduleForDuration(0, 1, 40, () -> {
                if (hasHit.get()) return;
                tick[0]++;

                Vec3 pos = currentPos[0];

                ParticleUtil.spawnParticles((ServerLevel) level, EARTH_DUST, pos, 8, 0.3);
                ParticleUtil.spawnParticles((ServerLevel) level, STONE_DUST, pos, 4, 0.2);

                if (AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 1.2f * (int) Math.max(multiplier(entity) / 4, 1),
                        DamageLookup.lookupDamage(7, 0.5) * (int) Math.max(multiplier(entity) / 4, 1), pos, true, false,
                        ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, entity))) {
                    hasHit.set(true);
                    ParticleUtil.spawnParticles((ServerLevel) level, EARTH_DUST, pos, 30, 0.5);
                    ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.EXPLOSION, pos, 3, 0.3);
                    return;
                }

                if (!level.getBlockState(net.minecraft.core.BlockPos.containing(pos)).isAir()) {
                    hasHit.set(true);
                    ParticleUtil.spawnParticles((ServerLevel) level, EARTH_DUST, pos, 20, 0.4);
                    return;
                }

                currentPos[0] = pos.add(finalFistDir.scale(0.8));
            }, null, (ServerLevel) level, () -> 1.0);
        }
    }

    private void quicksand(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 25 * (int) Math.max(multiplier(entity) / 4, 1), 1.5f);
        if (target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.spirit_channeling.no_target").withColor(0xFF334f23));
            return;
        }

        int casterSeq = de.jakob.lotm.util.BeyonderData.getSequence(entity);
        Vec3 center = target.position().add(0, 0.25, 0);

        ServerScheduler.scheduleForDuration(0, 2, 200, () -> {
            if (target.isDeadOrDying()) return;

            for (int i = 0; i < 12; i++) {
                double angle = (2 * Math.PI * i) / 12 + (level.getGameTime() * 0.05);
                double r = 2.0 + random.nextDouble() * 2.0;
                Vec3 particlePos = center.add(Math.cos(angle) * r, 0, Math.sin(angle) * r);
                ParticleUtil.spawnParticles((ServerLevel) level, EARTH_DUST_SMALL, particlePos, 2, 0.1);
            }

            for (LivingEntity nearby : AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, center, 5.0 * (int) Math.max(multiplier(entity) / 4, 1))) {
                int targetSeq = de.jakob.lotm.util.BeyonderData.getSequence(nearby);
                if (targetSeq <= casterSeq - 1) continue;

                nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 4, false, false, false));
                if (level.getBlockState(net.minecraft.core.BlockPos.containing(
                        nearby.getX(), nearby.getY() + 0.75, nearby.getZ())).isAir()) {
                    nearby.setDeltaMovement(nearby.getDeltaMovement().x, -0.1, nearby.getDeltaMovement().z);
                }
            }
        }, null, (ServerLevel) level, () -> 1.0);
    }

    private void earthHeal(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        float healAmount = entity.getMaxHealth() * 0.10f;
        entity.heal(healAmount);

        Vec3 pos = entity.position().add(0, entity.getEyeHeight() / 2, 0);
        ParticleUtil.spawnParticles((ServerLevel) level, EARTH_DUST, pos, 25, 0.4, entity.getEyeHeight() / 2, 0.4, 0);
        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.HEART, pos.add(0, 0.5, 0), 5, 0.3);
    }

    @SubscribeEvent
    public static void onGlacialAegisBlock(LivingIncomingDamageEvent event) {
        if (glacialAegisActive.contains(event.getEntity().getUUID())) {
            glacialAegisActive.remove(event.getEntity().getUUID());
            event.setCanceled(true);
        }
    }

    public static boolean hasCapturedSpirit(UUID playerUUID) {
        return capturedSpirits.containsKey(playerUUID);
    }

    public static SpiritType getCapturedSpiritType(UUID playerUUID) {
        return capturedSpirits.get(playerUUID);
    }
}