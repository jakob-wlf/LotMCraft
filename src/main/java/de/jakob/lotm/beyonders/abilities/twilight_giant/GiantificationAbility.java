package de.jakob.lotm.beyonders.abilities.twilight_giant;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.network.packets.toClient.UseAbilityPacket;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class GiantificationAbility extends ToggleAbility {

    private static final double SCALE_BONUS = 9.0D;
    private static final double STEP_HEIGHT = 9.0D;
    private static final double STAT_BONUS = 0.25D;
    private static final double SPEED_BONUS = 0.25D;
    private static final float RESISTANCE = 1.25f;
    private static final double STRIDE = 4.0D;
    private static final double STOMP_RADIUS = 20.0D;
    private static final double STOMP_HEIGHT = 5.0D;
    private static final float SHAKE_RADIUS = 48f;
    private static final float SHAKE_INTENSITY = 1.5f;
    private static final int SHAKE_TICKS = 10;
    private static final ResourceLocation SCALE_ID = id("gtiant_scale");
    private static final ResourceLocation STEP_ID = id("tgiant_step");
    private static final ResourceLocation DAMAGE_ID = id("tgiant_damage");
    private static final ResourceLocation SPEED_ID = id("tgiant_speed");

    private static final Map<UUID, Stride> strides = new HashMap<>();

    private static GiantificationAbility instance;

    public GiantificationAbility(String id) {
        super(id);
        instance = this;
        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(250f, 160f, 100f));
        baseDamage = 20;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("twilight_giant", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 100;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide()) {
            ClientHandler.applyCameraShakeToPlayersInRadius(SHAKE_INTENSITY, SHAKE_TICKS, (ClientLevel) level, entity.position(), SHAKE_RADIUS);
            return;
        }
        super.onAbilityUse(level, entity);
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        setModifier(entity, Attributes.SCALE, SCALE_ID, SCALE_BONUS);
        setModifier(entity,Attributes.STEP_HEIGHT,STEP_ID,STEP_HEIGHT);
        setModifier(entity, Attributes.ATTACK_DAMAGE, DAMAGE_ID, STAT_BONUS);
        setModifier(entity, Attributes.MOVEMENT_SPEED, SPEED_ID, SPEED_BONUS);
        strides.put(entity.getUUID(), new Stride(entity.position()));
        level.playSound(null, entity.blockPosition(), SoundEvents.RAVAGER_ROAR, entity.getSoundSource(), 3f, 0.5f);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        Stride stride = strides.computeIfAbsent(entity.getUUID(), uuid -> new Stride(entity.position()));
        Vec3 position = entity.position();
        stride.walked += Math.sqrt(Math.pow(position.x - stride.last.x, 2) + Math.pow(position.z - stride.last.z, 2));
        stride.last = position;
        if (!entity.onGround() || stride.walked < STRIDE) return;
        stride.walked = 0;
        stomp(serverLevel, entity);
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        removeModifier(entity, Attributes.SCALE, SCALE_ID);
        removeModifier(entity, Attributes.STEP_HEIGHT, STEP_ID);
        removeModifier(entity, Attributes.ATTACK_DAMAGE, DAMAGE_ID);
        removeModifier(entity, Attributes.MOVEMENT_SPEED, SPEED_ID);
        strides.remove(entity.getUUID());
        level.playSound(null, entity.blockPosition(), SoundEvents.BEACON_DEACTIVATE, entity.getSoundSource(), 1.5f, 0.6f);
    }

    private void stomp(ServerLevel level, LivingEntity entity) {
        Vec3 feet = entity.position();
        BlockState ground = level.getBlockState(BlockPos.containing(feet).below());
        if (!ground.isAir()) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), feet.x, feet.y + 0.2, feet.z, 80, STOMP_RADIUS / 2, 0.3, STOMP_RADIUS / 2, 0.2);
        }
        level.sendParticles(ParticleTypes.EXPLOSION, feet.x, feet.y + 0.5, feet.z, 3, STOMP_RADIUS / 3, 0.2, STOMP_RADIUS / 3, 0);
        level.playSound(null, entity.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), entity.getSoundSource(), 2f, 0.4f);
        PacketHandler.sendToNearbyPlayers(new UseAbilityPacket(getId(), entity.getId()), level, feet, SHAKE_RADIUS);

        float damage = Float.isFinite(baseDamage) && baseDamage > 0f ? baseDamage : 20f;
        for (LivingEntity target : AbilityUtil.getNearbyEntities(entity, level, feet, STOMP_RADIUS)) {
            if (!target.onGround() || target.getY() > feet.y + STOMP_HEIGHT || !AbilityUtil.mayDamage(entity, target)) continue;
            DamageSource source = ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, entity);
            if (AbilityUtil.isTargetSignificantlyWeaker(entity, target)) {
                crush(target, source);
                continue;
            }
            target.invulnerableTime = 0;
            target.hurt(source, damage);
            if (!Float.isFinite(target.getHealth()) || (target.getHealth() <= 0f && target.deathTime <= 0)) crush(target, source);
        }
    }

    private static void crush(LivingEntity target, DamageSource source) {
        if (target.isRemoved() || target.deathTime > 0) return;
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) return;
        target.invulnerableTime = 0;
        target.setHealth(0f);
        target.die(source);
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (instance == null || victim.level().isClientSide() || !instance.isActiveForEntity(victim)) return;
        if (event.getSource().is(DamageTypes.FALL)) {
            event.setCanceled(true);
            return;
        }
        event.setAmount(event.getAmount() / RESISTANCE);
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "giantification_" + name);
    }

    private static void setModifier(LivingEntity entity, Holder<Attribute> attribute, ResourceLocation id, double amount) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) return;
        instance.removeModifier(id);
        instance.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    private static void removeModifier(LivingEntity entity, Holder<Attribute> attribute, ResourceLocation id) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) instance.removeModifier(id);
    }

    private static final class Stride {
        private Vec3 last;
        private double walked;

        private Stride(Vec3 last) {
            this.last = last;
        }
    }
}
