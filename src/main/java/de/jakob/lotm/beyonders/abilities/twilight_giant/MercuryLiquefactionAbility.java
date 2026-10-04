package de.jakob.lotm.beyonders.abilities.twilight_giant;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.beyonders.abilities.twilight_giant.passives.SupernaturalResistanceAbility;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class MercuryLiquefactionAbility extends ToggleAbility {


    private static final ResourceLocation SPEED_ID = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "mercury_liquefaction_speed");
    private static final ResourceLocation SCALE_ID = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "mercury_liquefaction_scale");
    private static final double SPEED_BONUS = 0.6D;
    private static final double SCALE_PENALTY = -0.4D;
    private static final float EVASION_CHANCE = 0.3f;
    private static final double CLIMB_SPEED = 0.2D;
    private static final int FLIGHT_TICKS = 20 * 5;
    private static final DustParticleOptions MERCURY_DUST = new DustParticleOptions(new Vector3f(0.78f, 0.8f, 0.85f), 1.2f);

    private static final Map<UUID, Integer> flightLeft = new HashMap<>();

    public MercuryLiquefactionAbility(String id) {
        super(id);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("twilight_giant", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 55;
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        setModifier(entity, Attributes.MOVEMENT_SPEED, SPEED_ID, SPEED_BONUS);
        setModifier(entity, Attributes.SCALE, SCALE_ID, SCALE_PENALTY);
        flightLeft.put(entity.getUUID(), FLIGHT_TICKS);
        level.playSound(null, entity.blockPosition(), SoundEvents.BUCKET_EMPTY_LAVA, entity.getSoundSource(), 1f, 1.6f);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        Vec3 pos = entity.position();
        serverLevel.sendParticles(MERCURY_DUST, pos.x, pos.y + 0.2, pos.z, 6, 0.3, 0.1, 0.3, 0);
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        removeModifier(entity, Attributes.MOVEMENT_SPEED, SPEED_ID);
        removeModifier(entity, Attributes.SCALE, SCALE_ID);
        flightLeft.remove(entity.getUUID());
        if (entity instanceof ServerPlayer player && !player.isCreative() && !player.isSpectator()) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
        MercuryArmoryAbility.release(entity);
        level.playSound(null, entity.blockPosition(), SoundEvents.BUCKET_FILL_LAVA, entity.getSoundSource(), 1f, 1.6f);
    }

    public static boolean isLiquefied(LivingEntity entity) {
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        return speed != null && speed.hasModifier(SPEED_ID);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!isLiquefied(player)) return;
        player.resetFallDistance();
        if (player.horizontalCollision) {
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x, player.isShiftKeyDown() ? Math.max(motion.y, 0) : CLIMB_SPEED, motion.z);
        }
        if (player instanceof ServerPlayer serverPlayer) updateFlight(serverPlayer);
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide() || !isLiquefied(target)) return;
        if (event.getSource().is(DamageTypes.FALL)) {
            event.setCanceled(true);
            return;
        }
        if (event.getSource().getEntity() == null || SupernaturalResistanceAbility.isSupernatural(event.getSource())) return;
        if (target.getRandom().nextFloat() >= EVASION_CHANCE) return;
        event.setCanceled(true);
        target.level().playSound(null, target.blockPosition(), SoundEvents.BUCKET_EMPTY, target.getSoundSource(), 0.8f, 1.8f);
    }

    private static void updateFlight(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) return;
        int left = flightLeft.getOrDefault(player.getUUID(), FLIGHT_TICKS);
        if (player.onGround()) {
            left = FLIGHT_TICKS;
        } else if (player.getAbilities().flying) {
            left--;
        }
        flightLeft.put(player.getUUID(), left);
        boolean canFly = left > 0;
        if (player.getAbilities().mayfly != canFly) {
            player.getAbilities().mayfly = canFly;
            if (!canFly) player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
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
}
