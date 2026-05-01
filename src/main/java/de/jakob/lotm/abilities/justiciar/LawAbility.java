package de.jakob.lotm.abilities.justiciar;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class LawAbility extends SelectableAbility {

    public static final Set<UUID>         SOLACE_KILLED      = Collections.newSetFromMap(new ConcurrentHashMap<>());
    public static final Map<UUID, String> LAST_USED_ABILITY  = new ConcurrentHashMap<>();

    private static final DustParticleOptions GOLD_DUST      = new DustParticleOptions(new Vector3f(1.0f, 0.78f, 0.0f), 1.4f);
    private static final DustParticleOptions PALE_GOLD_DUST = new DustParticleOptions(new Vector3f(1.0f, 0.93f, 0.5f), 1.0f);
    private static final DustParticleOptions DIM_DUST       = new DustParticleOptions(new Vector3f(0.45f, 0.35f, 0.05f), 1.2f);
    private static final DustParticleOptions BRIGHT_DUST    = new DustParticleOptions(new Vector3f(1.0f, 0.95f, 0.6f), 1.6f);
    private static final DustParticleOptions SEAL_DUST      = new DustParticleOptions(new Vector3f(0.9f, 0.55f, 0.0f), 1.3f);
    private static final DustParticleOptions HOLY_DUST      = new DustParticleOptions(new Vector3f(1.0f, 1.0f, 0.85f), 1.5f);

    public LawAbility(String id) {
        super(id, 5f, "law");
        interactionRadius = 40;
        hasOptimalDistance = false;
        postsUsedAbilityEventManually = true;
        canBeCopied = false;
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("justiciar", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 800;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.law.weaken_mysticism",
                "ability.lotmcraft.law.enhance_mysticism",
                "ability.lotmcraft.law.solace",
                "ability.lotmcraft.law.law_of_sealing"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;

        switch (abilityIndex) {
            case 0 -> weakenMysticism(serverLevel, entity);
            case 1 -> enhanceMysticism(serverLevel, entity);
            case 2 -> solace(serverLevel, entity);
            case 3 -> lawOfSealing(serverLevel, entity);
        }

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, entity.position(), entity, this, interactionFlags, interactionRadius, 20 * 2));
    }

    private void weakenMysticism(ServerLevel serverLevel, LivingEntity entity) {
        int duration = 20 * 30 * (int) Math.max(multiplier(entity) / 4, 1);
        List<LivingEntity> nearby = AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), 40);

        nearby.stream()
                .filter(BeyonderData::isBeyonder)
                .forEach(e -> e.getData(ModAttachments.MULTIPLIER_MODIFIER_COMPONENT)
                        .addMultiplierForTime("law_weaken", 0.25f, duration));
        entity.getData(ModAttachments.MULTIPLIER_MODIFIER_COMPONENT)
                .addMultiplierForTime("law_weaken", 0.25f, duration);

        serverLevel.playSound(null, entity.blockPosition(), SoundEvents.BEACON_DEACTIVATE,   SoundSource.PLAYERS, 1.2f, 0.45f);
        serverLevel.playSound(null, entity.blockPosition(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.PLAYERS, 0.7f, 0.6f);
        serverLevel.playSound(null, entity.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.9f, 0.4f);

        Vec3 center = entity.position().add(0, 1, 0);
        ParticleUtil.spawnSphereParticles(serverLevel, DIM_DUST,  center, 1.5, 30);
        ParticleUtil.spawnSphereParticles(serverLevel, GOLD_DUST, center, 1.0, 18);
        serverLevel.sendParticles(ParticleTypes.SMOKE, center.x, center.y, center.z, 20, 0.6, 0.6, 0.6, 0.03);

        Location casterLoc = new Location(entity.position(), serverLevel);
        ServerScheduler.scheduleForDuration(0, 6, 40, () -> {
            for (LivingEntity e : nearby) {
                if (!e.isAlive()) continue;
                Vec3 ep = e.position().add(0, 1, 0);
                serverLevel.sendParticles(DIM_DUST, ep.x, ep.y, ep.z, 2, 0.25, 0.3, 0.25, 0);
                serverLevel.sendParticles(ParticleTypes.SMOKE, ep.x, ep.y, ep.z, 1, 0.15, 0.2, 0.15, 0.01);
            }
        }, null, serverLevel, () -> AbilityUtil.getTimeInArea(entity, casterLoc));

        broadcastToNearby(serverLevel, entity, Component.translatable("ability.lotmcraft.law.weaken_declared").withStyle(ChatFormatting.GOLD));
    }

    private void enhanceMysticism(ServerLevel serverLevel, LivingEntity entity) {
        int duration = 20 * 60 * (int) Math.max(multiplier(entity) / 4, 1);
        List<LivingEntity> nearby = AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), 40);

        nearby.stream()
                .filter(BeyonderData::isBeyonder)
                .forEach(e -> e.getData(ModAttachments.MULTIPLIER_MODIFIER_COMPONENT)
                        .addMultiplierForTime("law_enhance", 2.5f, duration));
        entity.getData(ModAttachments.MULTIPLIER_MODIFIER_COMPONENT)
                .addMultiplierForTime("law_enhance", 2.5f, duration);

        serverLevel.playSound(null, entity.blockPosition(), SoundEvents.BEACON_ACTIVATE,      SoundSource.PLAYERS, 1.0f, 0.55f);
        serverLevel.playSound(null, entity.blockPosition(), SoundEvents.TOTEM_USE,            SoundSource.PLAYERS, 0.6f, 0.5f);
        serverLevel.playSound(null, entity.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.6f);

        Vec3 center = entity.position().add(0, 1, 0);
        ParticleUtil.spawnSphereParticles(serverLevel, BRIGHT_DUST, center, 2.0, 40);
        ParticleUtil.spawnSphereParticles(serverLevel, GOLD_DUST,   center, 1.3, 26);
        ParticleUtil.spawnCircleParticles(serverLevel, GOLD_DUST,   entity.position().add(0, 0.05, 0), 1.5, 28);
        serverLevel.sendParticles(ParticleTypes.ENCHANT, center.x, center.y, center.z, 50, 0.8, 0.8, 0.8, 0.15);

        Location casterLoc = new Location(entity.position(), serverLevel);
        ServerScheduler.scheduleForDuration(0, 5, 50, () -> {
            for (LivingEntity e : nearby) {
                if (!e.isAlive()) continue;
                Vec3 ep = e.position().add(0, 1, 0);
                serverLevel.sendParticles(BRIGHT_DUST,       ep.x, ep.y, ep.z, 2, 0.2, 0.35, 0.2, 0);
                serverLevel.sendParticles(ParticleTypes.ENCHANT, ep.x, ep.y, ep.z, 3, 0.3, 0.4, 0.3, 0.08);
            }
        }, null, serverLevel, () -> AbilityUtil.getTimeInArea(entity, casterLoc));

        broadcastToNearby(serverLevel, entity, Component.translatable("ability.lotmcraft.law.enhance_declared").withStyle(ChatFormatting.GOLD));
    }

    private void solace(ServerLevel serverLevel, LivingEntity caster) {
        broadcastToNearby(serverLevel, caster, Component.translatable("ability.lotmcraft.law.solace_declared").withStyle(ChatFormatting.GOLD));

        serverLevel.playSound(null, caster.blockPosition(), SoundEvents.BEACON_ACTIVATE,       SoundSource.PLAYERS, 1.5f, 0.4f);
        serverLevel.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,  SoundSource.PLAYERS, 1.2f, 0.35f);
        serverLevel.playSound(null, caster.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL,    SoundSource.PLAYERS, 0.6f, 1.1f);

        Vec3 casterCenter = caster.position().add(0, 1, 0);
        ParticleUtil.spawnSphereParticles(serverLevel, HOLY_DUST,  casterCenter, 2.0, 36);
        ParticleUtil.spawnSphereParticles(serverLevel, GOLD_DUST,  casterCenter, 1.2, 22);
        ParticleUtil.spawnCircleParticles(serverLevel, HOLY_DUST,  caster.position().add(0, 0.05, 0), 2.5, 32);
        serverLevel.sendParticles(ParticleTypes.ENCHANT, casterCenter.x, casterCenter.y, casterCenter.z, 60, 1.0, 0.8, 1.0, 0.18);

        List<LivingEntity> nearby = AbilityUtil.getNearbyEntities(caster, serverLevel, caster.position(), 40 * (int) Math.max(multiplier(caster) / 4, 1));
        for (LivingEntity e : nearby) {
            if (!e.getType().is(EntityTypeTags.UNDEAD)) continue;

            Vec3 ep = e.position().add(0, 1, 0);
            ParticleUtil.spawnSphereParticles(serverLevel, HOLY_DUST, ep, 1.0, 24);
            ParticleUtil.spawnSphereParticles(serverLevel, GOLD_DUST, ep, 0.7, 16);
            serverLevel.sendParticles(ParticleTypes.ENCHANT, ep.x, ep.y, ep.z, 30, 0.4, 0.4, 0.4, 0.14);
            serverLevel.sendParticles(ParticleTypes.FLASH, ep.x, ep.y, ep.z, 2, 0.2, 0.2, 0.2, 0);
            serverLevel.playSound(null, e.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.8f, 0.6f);

            SOLACE_KILLED.add(e.getUUID());
            if (e instanceof ServerPlayer) {
                e.hurt(serverLevel.damageSources().magic(), Float.MAX_VALUE);
            } else {
                e.kill();
            }
            UUID uuid = e.getUUID();
            ServerScheduler.scheduleDelayed(2, () -> SOLACE_KILLED.remove(uuid), serverLevel);
        }
    }

    private void lawOfSealing(ServerLevel serverLevel, LivingEntity caster) {
        LivingEntity target = AbilityUtil.getTargetEntity(caster, 20 * (int) Math.max(multiplier(caster) / 4, 1), 1.5f);
        if (target == null) {
            if (caster instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.translatable("ability.lotmcraft.law.sealing_no_target").withStyle(ChatFormatting.RED));
            }
            return;
        }

        String abilityId = LAST_USED_ABILITY.get(target.getUUID());
        if (abilityId == null) {
            if (caster instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.translatable("ability.lotmcraft.law.sealing_no_ability").withStyle(ChatFormatting.RED));
            }
            return;
        }

        target.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT)
                .disableSpecificAbilityForTime(abilityId, "law_of_sealing", 20 * 60 * 2 * (int) Math.max(multiplier(caster) / 4, 1));

        serverLevel.playSound(null, caster.blockPosition(),  SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.3f, 0.5f);
        serverLevel.playSound(null, caster.blockPosition(),  SoundEvents.BEACON_ACTIVATE,      SoundSource.PLAYERS, 0.7f, 0.6f);
        serverLevel.playSound(null, target.blockPosition(),  SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 0.4f);
        serverLevel.playSound(null, target.blockPosition(),  SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.PLAYERS, 0.6f, 0.8f);

        Vec3 from = caster.getEyePosition();
        Vec3 to   = target.getEyePosition();
        ParticleUtil.drawParticleLine(serverLevel, SEAL_DUST,  from, to, 0.12, 1);
        ParticleUtil.drawParticleLine(serverLevel, GOLD_DUST,  from, to, 0.20, 1);
        serverLevel.sendParticles(ParticleTypes.ENCHANT, from.x, from.y, from.z, 18, 0.3, 0.3, 0.3, 0.1);

        Vec3 tp = target.position().add(0, 1, 0);
        ParticleUtil.spawnSphereParticles(serverLevel, SEAL_DUST,  tp, 1.0, 26);
        ParticleUtil.spawnSphereParticles(serverLevel, GOLD_DUST,  tp, 0.7, 16);
        ParticleUtil.spawnCircleParticles(serverLevel, SEAL_DUST,  target.position().add(0, 0.05, 0), 1.1, 22);
        serverLevel.sendParticles(ParticleTypes.ENCHANT, tp.x, tp.y, tp.z, 24, 0.4, 0.4, 0.4, 0.08);

        Location targetLoc = new Location(target.position(), serverLevel);
        ServerScheduler.scheduleForDuration(0, 5, 30, () -> {
            Vec3 pos = target.position().add(0, 1, 0);
            double angle = (System.currentTimeMillis() * 0.004) % (2 * Math.PI);
            for (int i = 0; i < 3; i++) {
                double a = angle + (2 * Math.PI * i / 3.0);
                double ox = Math.cos(a) * 0.55;
                double oz = Math.sin(a) * 0.55;
                serverLevel.sendParticles(SEAL_DUST, pos.x + ox, pos.y, pos.z + oz, 1, 0, 0.05, 0, 0);
            }
        }, null, serverLevel, () -> AbilityUtil.getTimeInArea(caster, targetLoc));

        String abilityName = abilityId;
        de.jakob.lotm.abilities.core.Ability sealed = LOTMCraft.abilityHandler.getById(abilityId);
        if (sealed != null) abilityName = sealed.getName().getString();

        if (caster instanceof ServerPlayer player) {
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.law.sealing_broadcast_prefix")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(target.getDisplayName().getString()).withStyle(ChatFormatting.WHITE))
                    .append(Component.translatable("ability.lotmcraft.law.sealing_broadcast_middle").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(abilityName).withStyle(ChatFormatting.YELLOW))
                    .append(Component.translatable("ability.lotmcraft.law.sealing_broadcast_suffix").withStyle(ChatFormatting.GOLD)));
        }
        if (target instanceof ServerPlayer targetPlayer) {
            targetPlayer.sendSystemMessage(Component.translatable("ability.lotmcraft.law.sealing_target_prefix")
                    .withStyle(ChatFormatting.RED)
                    .append(Component.literal(abilityName).withStyle(ChatFormatting.YELLOW))
                    .append(Component.translatable("ability.lotmcraft.law.sealing_target_suffix").withStyle(ChatFormatting.RED)));
        }
    }

    private static void broadcastToNearby(ServerLevel level, LivingEntity source, Component msg) {
        level.getServer().getPlayerList().getPlayers().forEach(p -> {
            if (p.level().equals(level) && p.distanceTo(source) <= 40) {
                p.sendSystemMessage(msg);
            }
        });
    }

    @SubscribeEvent
    public static void onAbilityUsed(AbilityUsedEvent event) {
        if (event.getAbility() == null || event.getEntity() == null) return;
        LAST_USED_ABILITY.put(event.getEntity().getUUID(), event.getAbility().getId());
    }
}