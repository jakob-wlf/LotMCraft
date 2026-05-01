package de.jakob.lotm.abilities.justiciar;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class PunishmentAbility extends Ability {

    public static final Map<UUID, UUID> CASTER_TO_TARGET = new ConcurrentHashMap<>();
    public static final Map<UUID, UUID> TARGET_TO_CASTER = new ConcurrentHashMap<>();
    public static final Map<UUID, Long> PUNISHMENT_EXPIRY = new ConcurrentHashMap<>();

    private static final int DURATION = 20 * 60 * 5; // 5 minutes
    private static final Random RAND = new Random();

    public PunishmentAbility(String id) {
        super(id, 5f, "punishment");
        interactionRadius = 20;
        hasOptimalDistance = false;
        postsUsedAbilityEventManually = true;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("justiciar", 5));
    }

    @Override
    protected float getSpiritualityCost() {
        return 150;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;
        UUID casterUUID = entity.getUUID();

        // Toggle off if already active
        if (CASTER_TO_TARGET.containsKey(casterUUID)) {
            cancelPunishment(casterUUID, serverLevel);
            if (entity instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.translatable("ability.lotmcraft.punishment.cancelled").withStyle(ChatFormatting.YELLOW));
            }
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20*(int) Math.max(multiplier(entity)/4,1), 1.5f);
        if (target == null) {
            if (entity instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.translatable("ability.lotmcraft.punishment.no_target").withStyle(ChatFormatting.RED));
            }
            return;
        }

        UUID targetUUID = target.getUUID();
        CASTER_TO_TARGET.put(casterUUID, targetUUID);
        TARGET_TO_CASTER.put(targetUUID, casterUUID);
        PUNISHMENT_EXPIRY.put(casterUUID, serverLevel.getGameTime() + DURATION);

        // Dawn-like glow on caster for punishment duration
        entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, DURATION, 0, false, false));

        if (entity instanceof ServerPlayer player) {
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.punishment.marked_prefix")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(target.getDisplayName().getString()).withStyle(ChatFormatting.WHITE))
                    .append(Component.translatable("ability.lotmcraft.punishment.marked_suffix").withStyle(ChatFormatting.GOLD)));
        }

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, entity.position(), entity, this, interactionFlags, interactionRadius, 20 * 2));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public static void cancelPunishment(UUID casterUUID, ServerLevel serverLevel) {
        UUID targetUUID = CASTER_TO_TARGET.remove(casterUUID);
        if (targetUUID != null) TARGET_TO_CASTER.remove(targetUUID);
        PUNISHMENT_EXPIRY.remove(casterUUID);

        ServerPlayer casterPlayer = serverLevel.getServer().getPlayerList().getPlayer(casterUUID);
        if (casterPlayer != null) {
            casterPlayer.removeEffect(MobEffects.GLOWING);
        }
    }

    private static void triggerPunishment(UUID casterUUID, LivingEntity target, ServerLevel serverLevel, String reason) {
        Long expiry = PUNISHMENT_EXPIRY.get(casterUUID);
        if (expiry == null || serverLevel.getGameTime() > expiry) {
            cancelPunishment(casterUUID, serverLevel);
            return;
        }

        ServerPlayer casterPlayer = serverLevel.getServer().getPlayerList().getPlayer(casterUUID);
        if (casterPlayer != null) {
            applyRandomBuff(casterPlayer);
            casterPlayer.sendSystemMessage(Component.translatable("ability.lotmcraft.punishment.triggered_prefix")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.translatable(reason).withStyle(ChatFormatting.WHITE)));
        }

        applyRandomDebuff(target);

        serverLevel.sendParticles(ParticleTypes.END_ROD,
                target.getX(), target.getY() + target.getBbHeight() / 2.0, target.getZ(),
                15, 0.5, 0.5, 0.5, 0.05);
        serverLevel.sendParticles(ParticleTypes.FLASH,
                target.getX(), target.getY() + target.getBbHeight() / 2.0, target.getZ(),
                3, 0.3, 0.3, 0.3, 0.0);
    }

    private static void applyRandomBuff(LivingEntity entity) {
        entity.addEffect(switch (RAND.nextInt(5)) {
            case 0 -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 10, 1);
            case 1 -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 10, 1);
            case 2 -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 10, 1);
            case 3 -> new MobEffectInstance(MobEffects.REGENERATION, 20 * 10, 1);
            default -> new MobEffectInstance(MobEffects.ABSORPTION, 20 * 10, 1);
        });
        BeyonderData.addModifierWithTimeLimit(entity, "punishment_buff", 1.2, 20*5);
    }

    private static void applyRandomDebuff(LivingEntity entity) {
        entity.addEffect(switch (RAND.nextInt(5)) {
            case 0 -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 10, 1);
            case 1 -> new MobEffectInstance(MobEffects.WEAKNESS, 20 * 10, 1);
            case 2 -> new MobEffectInstance(MobEffects.BLINDNESS, 20 * 5, 0);
            case 3 -> new MobEffectInstance(MobEffects.POISON, 20 * 5, 1);
            default -> new MobEffectInstance(MobEffects.WITHER, 20 * 5, 0);
        });
        BeyonderData.addModifierWithTimeLimit(entity, "punishment_debuff", 0.7, 20*5);
    }

    // ── Condition 1: Thorns — punishment target attacks the caster ───────────

    @SubscribeEvent
    public static void onDamagePre(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) event.getEntity().level();

        LivingEntity hurt = event.getEntity();
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof LivingEntity livingAttacker)) return;

        UUID hurtUUID = hurt.getUUID();
        UUID attackerUUID = livingAttacker.getUUID();

        // Only trigger if hurt entity is a caster AND the attacker is their marked target
        UUID markedTarget = CASTER_TO_TARGET.get(hurtUUID);
        if (markedTarget == null || !markedTarget.equals(attackerUUID)) return;

        float amount = event.getNewDamage();
        event.setNewDamage(0f);
        livingAttacker.hurt(serverLevel.damageSources().magic(), amount);

        triggerPunishment(hurtUUID, livingAttacker, serverLevel, "ability.lotmcraft.punishment.reason_attacking");
    }

    // ── Condition 2: Large area ability / restriction attempt ─────────────────

    @SubscribeEvent
    public static void onAbilityUsed(AbilityUsedEvent event) {
        if (event.getLevel().isClientSide) return;
        ServerLevel serverLevel = event.getLevel();

        LivingEntity user = event.getEntity();
        if (user == null) return;
        UUID userUUID = user.getUUID();
        UUID casterUUID = TARGET_TO_CASTER.get(userUUID);
        if (casterUUID == null) return;

        ServerPlayer casterPlayer = serverLevel.getServer().getPlayerList().getPlayer(casterUUID);
        if (casterPlayer == null) return;

        // Condition: large area ability (radius >= 30) used near caster
        if (event.getInteractionRadius() >= 30 && casterPlayer.distanceTo(user) <= 60) {
            triggerPunishment(casterUUID, user, serverLevel, "ability.lotmcraft.punishment.reason_large_area");
            return;
        }

        // Condition: restriction ability used near caster
        List<String> flags = event.getInteractionFlags();
        if ((flags.contains("imprison") || flags.contains("confinement"))
                && casterPlayer.distanceTo(user) <= event.getInteractionRadius()) {
            triggerPunishment(casterUUID, user, serverLevel, "ability.lotmcraft.punishment.reason_restriction");
        }
    }

    // ── Condition 3: Killing ──────────────────────────────────────────────────

    @SubscribeEvent
    public static void onEntityKilled(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) event.getEntity().level();

        Entity killer = event.getSource().getEntity();
        if (!(killer instanceof LivingEntity livingKiller)) return;

        UUID killerUUID = livingKiller.getUUID();
        UUID casterUUID = TARGET_TO_CASTER.get(killerUUID);
        if (casterUUID == null) return;

        triggerPunishment(casterUUID, livingKiller, serverLevel, "ability.lotmcraft.punishment.reason_kill");
    }

    // ── Condition 4: Arson — target places fire ───────────────────────────────

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        Entity placer = event.getEntity();
        if (!(placer instanceof LivingEntity livingPlacer)) return;

        if (event.getPlacedBlock().getBlock() != Blocks.FIRE
                && event.getPlacedBlock().getBlock() != Blocks.SOUL_FIRE) return;

        UUID placerUUID = livingPlacer.getUUID();
        UUID casterUUID = TARGET_TO_CASTER.get(placerUUID);
        if (casterUUID == null) return;

        triggerPunishment(casterUUID, livingPlacer, serverLevel, "ability.lotmcraft.punishment.reason_arson");
    }
}
