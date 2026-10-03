package de.jakob.lotm.beyonders.abilities.twilight_giant;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.AbilityUseEvent;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AllyUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class MercuryArmoryAbility extends SelectableAbility {


    private static final int RANGE = 16;
    private static final double ARMOR = 20.0D;
    private static final double TOUGHNESS = 12.0D;
    private static final double KNOCKBACK_RESISTANCE = 0.4D;
    private static final int SEVERE_DIFFERENCE = 2;
    private static final int BASE_STRUGGLE = 8;
    private static final int STRUGGLE_PER_SEQUENCE = 4;
    private static final int DAMAGE_INTERVAL = 20;
    private static final double DAMAGE_SCALE = 0.15D;
    private static final int PARTICLE_POINTS = 10;
    private static final Set<String> SPEECH_ABILITIES = Set.of(
            "SirenSongAbility", "HolySongAbility", "LanguageOfFoulnessAbility",
            "CorruptingVoiceAbility", "CommandingOrdersAbility", "MidnightPoemAbility");
    private static final DustParticleOptions MERCURY_DUST = new DustParticleOptions(new Vector3f(0.78f, 0.8f, 0.85f), 1.0f);
    private static final ResourceLocation ARMOR_ID = id("armor");
    private static final ResourceLocation TOUGHNESS_ID = id("toughness");
    private static final ResourceLocation KNOCKBACK_ID = id("knockback");
    private static final ResourceLocation ROOT_ID = id("root");
    public static final ResourceLocation HIDDEN_ID = id("hidden");

    private static final Map<UUID, Coat> coats = new HashMap<>();
    private static final Map<UUID, UUID> victims = new HashMap<>();

    public MercuryArmoryAbility(String id) {
        super(id, 16);
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("twilight_giant", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1600;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.mercury_armory.armor",
                "ability.lotmcraft.mercury_armory.suffocate",
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if (!(level instanceof ServerLevel) || !(entity instanceof ServerPlayer caster)) return;
        if (coats.containsKey(caster.getUUID())) {
            release(caster);
            return;
        }
        if (!MercuryLiquefactionAbility.isLiquefied(caster)) {
            AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.mercury_armory.not_liquefied").withColor(0xFFC8CCD9));
            return;
        }
        LivingEntity target = AbilityUtil.getTargetEntity(caster, RANGE, 1.5f, true, true, true);
        if (target == null) {
            AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.mercury_armory.no_target").withColor(0xFFC8CCD9));
            return;
        }

        if (selectedAbility == 0) {
            if (!AllyUtil.areAllies(caster, target)) {
                AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.mercury_armory.not_ally").withColor(0xFFC8CCD9));
                return;
            }
            setModifier(target, Attributes.ARMOR, ARMOR_ID, ARMOR);
            setModifier(target, Attributes.ARMOR_TOUGHNESS, TOUGHNESS_ID, TOUGHNESS);
            setModifier(target, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_ID, KNOCKBACK_RESISTANCE);
            coats.put(caster.getUUID(), new Coat(target, false, false, 0));
        } else {
            if (!AbilityUtil.mayDamage(caster, target)) return;
            int difference = BeyonderData.getSequence(target) - BeyonderData.getSequence(caster);
            boolean severe = difference >= SEVERE_DIFFERENCE;
            int struggle = BASE_STRUGGLE + STRUGGLE_PER_SEQUENCE * Math.max(0, difference);
            if (severe) setModifier(target, Attributes.MOVEMENT_SPEED, ROOT_ID, -1.0D);
            coats.put(caster.getUUID(), new Coat(target, true, severe, struggle));
            victims.put(target.getUUID(), caster.getUUID());
        }
        caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, false, false));
        setModifier(caster, Attributes.MOVEMENT_SPEED, HIDDEN_ID, 0);
        level.playSound(null, target.blockPosition(), SoundEvents.BUCKET_EMPTY_LAVA, caster.getSoundSource(), 1f, 1.4f);
    }

    public static void release(LivingEntity caster) {
        Coat coat = coats.remove(caster.getUUID());
        if (coat == null) return;
        LivingEntity target = coat.target;
        removeModifier(target, Attributes.ARMOR, ARMOR_ID);
        removeModifier(target, Attributes.ARMOR_TOUGHNESS, TOUGHNESS_ID);
        removeModifier(target, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_ID);
        removeModifier(target, Attributes.MOVEMENT_SPEED, ROOT_ID);
        victims.remove(target.getUUID());
        removeModifier(caster, Attributes.MOVEMENT_SPEED, HIDDEN_ID);
        caster.removeEffect(MobEffects.INVISIBILITY);
        caster.level().playSound(null, caster.blockPosition(), SoundEvents.BUCKET_FILL_LAVA, caster.getSoundSource(), 1f, 1.4f);
    }

    public static void onAirSwing(ServerPlayer player) {
        UUID casterId = victims.get(player.getUUID());
        if (casterId == null) return;
        Coat coat = coats.get(casterId);
        if (coat == null) return;
        coat.struggleLeft--;
        player.serverLevel().sendParticles(MERCURY_DUST, player.getX(), player.getY(1.0), player.getZ(), 8, 0.4, 0.4, 0.4, 0.05);
        if (coat.struggleLeft > 0) return;
        Entity caster = player.serverLevel().getEntity(casterId);
        if (!(caster instanceof LivingEntity livingCaster)) return;
        release(livingCaster);
        Vec3 away = livingCaster.position().subtract(player.position()).multiply(1, 0, 1);
        Vec3 direction = away.lengthSqr() < 1.0E-4 ? player.getLookAngle().multiply(1, 0, 1).normalize() : away.normalize();
        livingCaster.push(direction.x * 1.5, 0.5, direction.z * 1.5);
        livingCaster.hurtMarked = true;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer caster)) return;
        Coat coat = coats.get(caster.getUUID());
        if (coat == null) return;
        LivingEntity target = coat.target;
        if (!target.isAlive() || target.level() != caster.level() || !caster.isAlive() || !MercuryLiquefactionAbility.isLiquefied(caster)) {
            release(caster);
            return;
        }

        caster.teleportTo(caster.serverLevel(), target.getX(), target.getY(), target.getZ(), Set.of(RelativeMovement.Y_ROT, RelativeMovement.X_ROT), 0f, 0f);
        caster.setDeltaMovement(Vec3.ZERO);
        if (caster.tickCount % 20 == 0) caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, false, false));
        ServerLevel level = caster.serverLevel();
        if (caster.tickCount % 2 == 0) spawnCoating(level, target, caster.tickCount);
        if (!coat.suffocating) return;

        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, coat.severe ? 9 : 2, false, false));
        if (caster.tickCount % DAMAGE_INTERVAL == 0) {
            target.hurt(ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, caster), (float) DamageLookup.lookupDamage(3, DAMAGE_SCALE));
        }
    }

    public static boolean ignoresCollision(Entity first, Entity second) {
        if (!(first instanceof LivingEntity caster) || !(second instanceof LivingEntity target)) return false;
        return coated(caster, target) || coated(target, caster);
    }

    private static boolean coated(LivingEntity caster, LivingEntity target) {
        if (!isHidden(caster)) return false;
        if (!caster.level().isClientSide()) {
            Coat coat = coats.get(caster.getUUID());
            return coat != null && coat.target.getUUID().equals(target.getUUID());
        }
        return caster.distanceToSqr(target) < 0.04D;
    }

    public static boolean isHidden(LivingEntity entity) {
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        return speed != null && speed.hasModifier(HIDDEN_ID);
    }

    private static void spawnCoating(ServerLevel level, LivingEntity target, int tick) {
        double height = target.getBbHeight();
        double radius = target.getBbWidth() * 0.7 + 0.2;
        for (int i = 0; i < PARTICLE_POINTS; i++) {
            double angle = tick * 0.3 + i * Math.PI * 2 / PARTICLE_POINTS;
            double y = height * ((tick * 0.05 + i / (double) PARTICLE_POINTS) % 1.0);
            level.sendParticles(MERCURY_DUST, target.getX() + Math.cos(angle) * radius, target.getY() + y, target.getZ() + Math.sin(angle) * radius, 1, 0, 0, 0, 0);
        }
    }

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        if (victims.containsKey(event.getPlayer().getUUID())) {
            event.setCanceled(true);
            AbilityUtil.sendActionBar(event.getPlayer(), Component.translatable("ability.lotmcraft.mercury_armory.cannot_speak").withColor(0xFFC8CCD9));
        }
    }

    @SubscribeEvent
    public static void onAbilityUse(AbilityUseEvent event) {
        LivingEntity user = event.getEntity();
        if (user == null) return;
        UUID casterId = victims.get(user.getUUID());
        if (casterId == null) return;
        Coat coat = coats.get(casterId);
        if (coat == null) return;
        if (coat.severe || SPEECH_ABILITIES.contains(event.getAbility().getClass().getSimpleName())) {
            event.setCanceled(true);
            AbilityUtil.sendActionBar(user, Component.translatable("ability.lotmcraft.mercury_armory.cannot_speak").withColor(0xFFC8CCD9));
        }
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "mercury_armory_" + name);
    }

    private static void setModifier(LivingEntity entity, Holder<Attribute> attribute, ResourceLocation id, double amount) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) return;
        instance.removeModifier(id);
        AttributeModifier.Operation operation = amount < 0 ? AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL : AttributeModifier.Operation.ADD_VALUE;
        instance.addTransientModifier(new AttributeModifier(id, amount, operation));
    }

    private static void removeModifier(LivingEntity entity, Holder<Attribute> attribute, ResourceLocation id) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) instance.removeModifier(id);
    }

    private static final class Coat {
        private final LivingEntity target;
        private final boolean suffocating;
        private final boolean severe;
        private int struggleLeft;

        private Coat(LivingEntity target, boolean suffocating, boolean severe, int struggleLeft) {
            this.target = target;
            this.suffocating = suffocating;
            this.severe = severe;
            this.struggleLeft = struggleLeft;
        }
    }
}
