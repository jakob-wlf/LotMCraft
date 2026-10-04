package de.jakob.lotm.beyonders.abilities.twilight_giant;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.beyonders.abilities.sun.UnshadowedDomainAbility;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AllyUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class LightConcealmentAbility extends Ability {


    private static final int SEARCH_RADIUS = 24;
    private static final int MIN_LIGHT = 10;
    private static final double ALLY_RADIUS = 8.0D;
    private static final double UNSHADOWED_RADIUS = 40.0D;
    private static final int UNSHADOWED_DURATION = 20 * 30;
    private static final ResourceLocation HIDDEN_ID = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "light_concealment_hidden");

    private static final Map<UUID, Hidden> hidden = new HashMap<>();
    private static final List<Zone> unshadowedZones = new ArrayList<>();

    public LightConcealmentAbility(String id) {
        super(id, 8);
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("twilight_giant", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1100;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof ServerPlayer player)) return;
        if (hidden.containsKey(player.getUUID())) {
            leave(player);
            BeyonderData.incrementSpirituality(player, getSpiritualityCost());
            return;
        }
        if (inUnshadowedDomain(serverLevel, player.position())) {
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.light_concealment.unshadowed").withColor(0xFFFFF3D6));
            return;
        }
        BlockPos light = findLight(serverLevel, player.blockPosition());
        if (light == null) {
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.light_concealment.no_light").withColor(0xFFFFF3D6));
            return;
        }

        hide(serverLevel, player, light);
        if (player.isShiftKeyDown()) {
            for (ServerPlayer ally : serverLevel.getPlayers(p -> p != player && p.distanceTo(player) <= ALLY_RADIUS && AllyUtil.areAllies(player, p))) {
                hide(serverLevel, ally, light);
            }
        }
    }

    public static boolean isHidden(LivingEntity entity) {
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        return speed != null && speed.hasModifier(HIDDEN_ID);
    }

    public static void leave(ServerPlayer player) {
        Hidden state = hidden.remove(player.getUUID());
        for (AttributeInstance attribute : new AttributeInstance[]{player.getAttribute(Attributes.MOVEMENT_SPEED), player.getAttribute(Attributes.JUMP_STRENGTH)}) {
            if (attribute != null) attribute.removeModifier(HIDDEN_ID);
        }
        if (state == null) return;
        player.setNoGravity(false);
        player.removeEffect(MobEffects.INVISIBILITY);
        Vec3 exit = exitPosition(player.serverLevel(), state.light());
        player.connection.teleport(exit.x, exit.y, exit.z, player.getYRot(), player.getXRot());
        player.resetFallDistance();
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, player.getSoundSource(), 1f, 1.4f);
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (hidden.containsKey(event.getEntity().getUUID())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onAbilityUsed(AbilityUsedEvent event) {
        if (!(event.getAbility() instanceof UnshadowedDomainAbility)) return;
        ServerLevel level = event.getLevel();
        unshadowedZones.add(new Zone(level, event.getPosition(), level.getGameTime() + UNSHADOWED_DURATION));
        for (ServerPlayer player : level.getPlayers(p -> hidden.containsKey(p.getUUID()) && p.position().distanceTo(event.getPosition()) <= UNSHADOWED_RADIUS)) {
            leave(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Hidden state = hidden.get(player.getUUID());
        if (state == null) return;
        ServerLevel level = player.serverLevel();
        if (level != state.level() || !lit(level, state.light()) || inUnshadowedDomain(level, player.position())) {
            leave(player);
            return;
        }
        Vec3 center = Vec3.atBottomCenterOf(state.light());
        if (player.position().distanceToSqr(center) > 0.25) {
            player.connection.teleport(center.x, center.y, center.z, player.getYRot(), player.getXRot());
        }
        player.setDeltaMovement(Vec3.ZERO);
        player.resetFallDistance();
        if (player.tickCount % 20 == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, false, false));
            level.sendParticles(player, ParticleTypes.END_ROD, true, center.x, center.y, center.z, 3, 0.2, 0.2, 0.2, 0.01);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) leave(player);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) leave(player);
    }

    private static void hide(ServerLevel level, ServerPlayer player, BlockPos light) {
        if (hidden.containsKey(player.getUUID())) return;
        hidden.put(player.getUUID(), new Hidden(level, light));
        level.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1, player.getZ(), 1, 0, 0, 0, 0);
        for (AttributeInstance attribute : new AttributeInstance[]{player.getAttribute(Attributes.MOVEMENT_SPEED), player.getAttribute(Attributes.JUMP_STRENGTH)}) {
            if (attribute == null) continue;
            attribute.removeModifier(HIDDEN_ID);
            attribute.addTransientModifier(new AttributeModifier(HIDDEN_ID, -1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        player.setNoGravity(true);
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, false, false));
        Vec3 center = Vec3.atBottomCenterOf(light);
        player.connection.teleport(center.x, center.y, center.z, player.getYRot(), player.getXRot());
        level.playSound(null, light, SoundEvents.BEACON_POWER_SELECT, player.getSoundSource(), 1f, 1.8f);
        AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.light_concealment.hidden").withColor(0xFFFFF3D6));
    }

    private static BlockPos findLight(ServerLevel level, BlockPos origin) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-SEARCH_RADIUS, -SEARCH_RADIUS, -SEARCH_RADIUS), origin.offset(SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS))) {
            if (!lit(level, pos)) continue;
            double distance = pos.distSqr(origin);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = pos.immutable();
            }
        }
        return best;
    }

    private static boolean lit(ServerLevel level, BlockPos pos) {
        return level.getLightEmission(pos) >= MIN_LIGHT || level.getMaxLocalRawBrightness(pos) >= MIN_LIGHT;
    }

    private static Vec3 exitPosition(ServerLevel level, BlockPos light) {
        for (BlockPos pos : new BlockPos[]{light, light.above(), light.north(), light.south(), light.east(), light.west(), light.below()}) {
            if (level.getBlockState(pos).getCollisionShape(level, pos).isEmpty() && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()) {
                return Vec3.atBottomCenterOf(pos);
            }
        }
        return Vec3.atBottomCenterOf(light.above());
    }

    private static boolean inUnshadowedDomain(ServerLevel level, Vec3 position) {
        long now = level.getGameTime();
        unshadowedZones.removeIf(zone -> zone.expires() < now);
        return unshadowedZones.stream().anyMatch(zone -> zone.level() == level && zone.center().distanceTo(position) <= UNSHADOWED_RADIUS);
    }

    private record Hidden(ServerLevel level, BlockPos light) {
    }

    private record Zone(ServerLevel level, Vec3 center, long expires) {
    }
}
