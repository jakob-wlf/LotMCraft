package de.jakob.lotm.abilities.hanged;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class FleshBloodFusionAbility extends ToggleAbility {
    private static final Map<String, Integer> REQUIREMENTS =
            Map.of(HangedPathwayConstants.PATHWAY_ID, HangedPathwayConstants.SEQUENCE_ROSE_BISHOP);
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LOTMCraft.MOD_ID, "textures/abilities/parasitation_ability.png");
    private static final float SPIRITUALITY_COST = 170.0f;
    private static final float MAX_RANGE_SCALE_SEQ1 = 3.0f;
    private static final float MAX_BUFF_SCALE_SEQ1 = 4.0f;
    private static final double POOL_HALF_WIDTH = 0.55;
    private static final double POOL_HEIGHT = 0.18;
    private static final double BASE_POOL_SPEED = 0.42;
    private static final double MAX_POOL_SPEED_SEQ1 = 1.18;
    private static final double POOL_ACCELERATION = 0.12;

    private static final Map<UUID, UUID> HOSTS = new HashMap<>();
    private static final Set<UUID> LIQUEFIED = new HashSet<>();
    private static final Map<UUID, PoolFormState> PLAYER_POOL_STATES = new HashMap<>();

    private static final String HOST_MESSAGE = "ability.lotmcraft.flesh_blood_fusion.host";
    private static final String LIQUEFY_MESSAGE = "ability.lotmcraft.flesh_blood_fusion.liquefy";
    private static final String EXIT_MESSAGE = "ability.lotmcraft.flesh_blood_fusion.exit";
    private static final String MONITOR_MESSAGE = "ability.lotmcraft.flesh_blood_fusion.monitor";

    public FleshBloodFusionAbility(String id) {
        super(id, "concealment", "corruption");
        canBeUsedByNPC = false;
        canBeCopied = false;
        autoClear = false;
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        float rangeScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_ROSE_BISHOP, MAX_RANGE_SCALE_SEQ1);
        LivingEntity host = AbilityUtil.getTargetEntity(entity, Math.max(4, Math.round(4 * rangeScale)), 1.8f, true, false, false);
        if (host != null && isValidHost(entity, host)) {
            HOSTS.put(entity.getUUID(), host.getUUID());
            LIQUEFIED.remove(entity.getUUID());
            if (entity instanceof Player player) {
                exitPoolForm(player);
                compressPlayer(player);
            }
            HangedEffectUtil.spawnFleshBurst(serverLevel, host.position().add(0, host.getBbHeight() * 0.45, 0), 0.7, 24);
            HangedEffectUtil.playFleshCast(serverLevel, host.position());
            AbilityUtil.sendActionBar(entity,
                    Component.translatable(HOST_MESSAGE, host.getDisplayName()).withColor(HangedPathwayConstants.pathwayColor()));
            return;
        }

        LIQUEFIED.add(entity.getUUID());
        HOSTS.remove(entity.getUUID());
        if (entity instanceof Player player) {
            enterPoolForm(player);
        }
        HangedEffectUtil.spawnFleshBurst(serverLevel, entity.position().add(0, entity.getBbHeight() * 0.4, 0), 0.7, 24);
        HangedEffectUtil.playFleshCast(serverLevel, entity.position());
        AbilityUtil.sendActionBar(entity,
                Component.translatable(LIQUEFY_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        float buffScale = HangedPathwayConstants.scaleForCurrentSequence(entity, HangedPathwayConstants.SEQUENCE_ROSE_BISHOP, MAX_BUFF_SCALE_SEQ1);
        LivingEntity host = getHost(serverLevel, entity);
        if (host != null) {
            entity.setInvisible(true);
            entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20, 0, false, false, false));
            entity.addEffect(new MobEffectInstance(ModEffects.CONCEALMENT, 20, 3, false, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20,
                    Math.min(4, Math.round((buffScale - 1.0f) * 0.35f)), false, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20,
                    Math.min(3, Math.round((buffScale - 1.0f) * 0.3f)), false, false, false));
            entity.teleportTo(host.getX(), host.getY() + host.getBbHeight() * 0.35, host.getZ());
            entity.setDeltaMovement(Vec3.ZERO);

            if (entity.tickCount % 6 == 0) {
                HangedEffectUtil.spawnFleshAura(serverLevel, host);
            }
            if (entity.tickCount % 30 == 0) {
                HangedEffectUtil.playFleshPulse(serverLevel, host.position(), 0.8f);
            }

            if (!host.isAlive() || host.isDeadOrDying()) {
                HOSTS.remove(entity.getUUID());
                LIQUEFIED.add(entity.getUUID());
                restorePlayer(entity);
                if (entity instanceof Player player) {
                    enterPoolForm(player);
                }
                return;
            }

            if (entity instanceof Player player) {
                exitPoolForm(player);
                compressPlayer(player);
            }

            AbilityUtil.sendActionBar(entity,
                    Component.translatable(MONITOR_MESSAGE, host.getDisplayName(),
                                    Math.round(host.getHealth()), Math.round(host.getMaxHealth()))
                            .withColor(HangedPathwayConstants.pathwayColor()));
            return;
        }

        if (LIQUEFIED.contains(entity.getUUID())) {
            entity.setInvisible(true);
            entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20, 0, false, false, false));
            entity.addEffect(new MobEffectInstance(ModEffects.CONCEALMENT, 20, 1, false, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20,
                    Math.min(7, 4 + Math.round((buffScale - 1.0f) * 0.55f)), false, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 20, 0, false, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20,
                    Math.min(3, Math.round((buffScale - 1.0f) * 0.3f)), false, false, false));
            entity.fallDistance = 0;
            if (entity instanceof Player player) {
                maintainPoolForm(player, buffScale);
            }
            if (entity.tickCount % 6 == 0) {
                HangedEffectUtil.spawnFleshAura(serverLevel, entity);
            }
            if (entity.tickCount % 24 == 0) {
                HangedEffectUtil.playFleshPulse(serverLevel, entity.position(), 0.7f);
            }
        }
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if (level instanceof ServerLevel serverLevel) {
            LivingEntity host = getHost(serverLevel, entity);
            if (host != null && host.isAlive()) {
                host.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.BEYONDER_GENERIC, entity), 1000.0f);
                HangedEffectUtil.spawnFleshBurst(serverLevel, host.position().add(0, host.getBbHeight() * 0.45, 0), 1.0, 30);
                Vec3 exitPos = host.position().add(host.getLookAngle().normalize().scale(-1.2));
                entity.teleportTo(exitPos.x, exitPos.y, exitPos.z);
            }
            HangedEffectUtil.playFleshPulse(serverLevel, entity.position(), 1.05f);
        }

        HOSTS.remove(entity.getUUID());
        LIQUEFIED.remove(entity.getUUID());
        entity.setInvisible(false);
        entity.removeEffect(MobEffects.INVISIBILITY);
        entity.removeEffect(MobEffects.MOVEMENT_SPEED);
        entity.removeEffect(MobEffects.WATER_BREATHING);
        entity.removeEffect(MobEffects.DAMAGE_RESISTANCE);
        entity.removeEffect(MobEffects.DAMAGE_BOOST);
        if (entity instanceof Player player) {
            exitPoolForm(player);
        }
        restorePlayer(entity);
        AbilityUtil.sendActionBar(entity,
                Component.translatable(EXIT_MESSAGE).withColor(HangedPathwayConstants.pathwayColor()));
        clearArtifactScaling(entity);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return REQUIREMENTS;
    }

    @Override
    protected float getSpiritualityCost() {
        return SPIRITUALITY_COST;
    }

    @Override
    public ResourceLocation getTextureLocation() {
        return TEXTURE;
    }

    private static boolean isValidHost(LivingEntity entity, LivingEntity target) {
        if (target instanceof Player) {
            return false;
        }

        if (!BeyonderData.isBeyonder(target)) {
            return true;
        }

        return BeyonderData.getSequence(target) >= BeyonderData.getSequence(entity);
    }

    private static LivingEntity getHost(ServerLevel serverLevel, LivingEntity entity) {
        UUID hostUUID = HOSTS.get(entity.getUUID());
        if (hostUUID == null) {
            return null;
        }

        Entity host = serverLevel.getEntity(hostUUID);
        return host instanceof LivingEntity livingHost ? livingHost : null;
    }

    private static void compressPlayer(Player player) {
        player.setBoundingBox(new AABB(
                player.getX(), player.getY(), player.getZ(),
                player.getX(), player.getY(), player.getZ()
        ));
        player.noPhysics = false;
        player.setNoGravity(false);
        player.onUpdateAbilities();
        player.hurtMarked = true;
    }

    private static void restorePlayer(LivingEntity entity) {
        if (entity instanceof Player player) {
            player.setBoundingBox(player.getDimensions(player.getPose()).makeBoundingBox(
                    player.getX(), player.getY(), player.getZ()
            ));
            player.onUpdateAbilities();
            player.hurtMarked = true;
        }
    }

    private static void enterPoolForm(Player player) {
        PLAYER_POOL_STATES.putIfAbsent(player.getUUID(), new PoolFormState(
                player.getAbilities().mayfly,
                player.getAbilities().flying,
                player.isNoGravity()
        ));
        maintainPoolForm(player, 1.0f);
    }

    private static void maintainPoolForm(Player player, float buffScale) {
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.noPhysics = false;
        player.setNoGravity(false);
        player.fallDistance = 0;
        player.setSprinting(true);
        player.setBoundingBox(new AABB(
                player.getX() - POOL_HALF_WIDTH, player.getY(), player.getZ() - POOL_HALF_WIDTH,
                player.getX() + POOL_HALF_WIDTH, player.getY() + POOL_HEIGHT, player.getZ() + POOL_HALF_WIDTH
        ));
        acceleratePoolMovement(player, buffScale);
        player.onUpdateAbilities();
        player.hurtMarked = true;
    }

    private static void acceleratePoolMovement(Player player, float buffScale) {
        Vec3 movement = player.getDeltaMovement();
        Vec3 horizontal = new Vec3(movement.x, 0.0, movement.z);
        double horizontalSpeed = horizontal.length();
        double maxSpeed = BASE_POOL_SPEED + ((MAX_POOL_SPEED_SEQ1 - BASE_POOL_SPEED) * Math.max(0.0f, buffScale - 1.0f) / (MAX_BUFF_SCALE_SEQ1 - 1.0f));

        if (horizontalSpeed > 0.015) {
            double boostedSpeed = Math.min(maxSpeed, horizontalSpeed + POOL_ACCELERATION);
            Vec3 boosted = horizontal.normalize().scale(boostedSpeed);
            double verticalSpeed = player.onGround() ? Math.min(0.0, movement.y) : Math.max(-0.35, movement.y - 0.08);
            player.setDeltaMovement(boosted.x, verticalSpeed, boosted.z);
        } else if (player.onGround()) {
            player.setDeltaMovement(movement.x * 0.7, Math.min(0.0, movement.y), movement.z * 0.7);
        }
    }

    private static void exitPoolForm(Player player) {
        PoolFormState state = PLAYER_POOL_STATES.remove(player.getUUID());
        if (state == null) {
            return;
        }

        player.getAbilities().mayfly = state.mayfly;
        player.getAbilities().flying = state.flying;
        player.setNoGravity(state.noGravity);
        player.onUpdateAbilities();
        player.hurtMarked = true;
    }

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob)) {
            return;
        }

        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        if (newTarget == null) {
            return;
        }

        if (HOSTS.containsKey(newTarget.getUUID()) || LIQUEFIED.contains(newTarget.getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (LIQUEFIED.contains(event.getEntity().getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (LIQUEFIED.contains(event.getEntity().getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (LIQUEFIED.contains(event.getEntity().getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (LIQUEFIED.contains(event.getEntity().getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLiquefiedJump(LivingEvent.LivingJumpEvent event) {
        if (LIQUEFIED.contains(event.getEntity().getUUID())) {
            Vec3 movement = event.getEntity().getDeltaMovement();
            event.getEntity().setDeltaMovement(movement.x, 0.0, movement.z);
        }
    }

    private static final class PoolFormState {
        private final boolean mayfly;
        private final boolean flying;
        private final boolean noGravity;

        private PoolFormState(boolean mayfly, boolean flying, boolean noGravity) {
            this.mayfly = mayfly;
            this.flying = flying;
            this.noGravity = noGravity;
        }
    }
}
