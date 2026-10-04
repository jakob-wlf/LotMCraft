package de.jakob.lotm.beyonders.abilities.twilight_giant.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class CombatMasteryAbility extends PassiveAbility {

    private static final int BASE_MAX_STACKS = 3;
    private static final int PUGILIST_MAX_STACKS = 5;
    private static final double KNOCKBACK_IMMUNITY_RANGE = 1.0D;
    private static final int DECAY_WINDOW = 10;
    private static final int STACK_DURATION = 20 * 30 + DECAY_WINDOW;
    private static final double BONUS_PER_STACK = 0.05D;
    private static final ResourceLocation DAMAGE_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "combat_mastery_damage");
    private static final ResourceLocation SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "combat_mastery_attack_speed");

    private static final Set<UUID> awaitingHit = new HashSet<>();
    private static final Map<UUID, Long> lastMissTick = new HashMap<>();

    private static CombatMasteryAbility instance;

    public CombatMasteryAbility(String id) {
        super(id);
        instance = this;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("twilight_giant", 9));
    }

    @Override
    public MutableComponent getName() {
        if (showsAsGod()) return Component.translatable("ability.lotmcraft.god_of_combat").withStyle(ChatFormatting.BOLD);
        return super.getName();
    }

    @Override
    public MutableComponent getDescription() {
        if (showsAsGod()) return Component.translatable("ability.lotmcraft.god_of_combat.description");
        return super.getDescription();
    }

    private static boolean showsAsGod() {
        return ClientBeyonderCache.localSequence() <= 0;
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide()) return;
        if (entity.tickCount % 10 != 0) return;
        MobEffectInstance effect = entity.getEffect(ModEffects.COMBAT_MASTERY);
        if (effect != null && effect.getDuration() <= DECAY_WINDOW) {
            setStacks(entity, effect.getAmplifier(), STACK_DURATION);
            return;
        }
        syncModifiers(entity, getStacks(entity));
    }

    @Override
    public void onPassiveAbilityRemoved(LivingEntity entity, ServerLevel serverLevel) {
        awaitingHit.remove(entity.getUUID());
        lastMissTick.remove(entity.getUUID());
        setStacks(entity, 0, 0);
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel serverLevel) || !applies(player)) return;
        if (!(event.getTarget() instanceof LivingEntity)) {
            loseStack(player);
            return;
        }
        UUID id = player.getUUID();
        awaitingHit.add(id);
        ServerScheduler.scheduleDelayed(1, () -> {
            if (awaitingHit.remove(id) && !player.isRemoved() && applies(player)) loseStack(player);
        }, serverLevel);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingDamageEvent.Post event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof LivingEntity attacker) || source.getDirectEntity() != attacker) return;
        if (attacker.level().isClientSide() || event.getNewDamage() <= 0) return;
        if (!source.is(DamageTypes.PLAYER_ATTACK) && !source.is(DamageTypes.MOB_ATTACK)) return;
        if (!applies(attacker)) return;
        boolean awaited = awaitingHit.remove(attacker.getUUID());
        if (!awaited) return;
        setStacks(attacker, getStacks(attacker) + 1, STACK_DURATION);
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || event.getHand() != InteractionHand.MAIN_HAND) return;
        if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START) return;
        if (applies(player)) loseStack(player);
    }

    @SubscribeEvent
    public static void onKnockBack(LivingKnockBackEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || !(BeyonderData.getSequence(entity) <=8) || !applies(entity)) return;
        if (isWithinRange(entity, entity.getLastHurtByMob()) || isWithinRange(entity, entity.getLastHurtMob())) {
            event.setCanceled(true);
        }
    }

    public static void onAirSwing(ServerPlayer player) {
        if (applies(player)) loseStack(player);
    }

    private static void loseStack(LivingEntity entity) {
        MobEffectInstance effect = entity.getEffect(ModEffects.COMBAT_MASTERY);
        if (effect == null) return;
        long now = entity.level().getGameTime();
        Long previous = lastMissTick.put(entity.getUUID(), now);
        if (previous != null && now - previous <= 1L) return;
        setStacks(entity, effect.getAmplifier(), effect.getDuration());
    }

    private static int getStacks(LivingEntity entity) {
        MobEffectInstance effect = entity.getEffect(ModEffects.COMBAT_MASTERY);
        return effect == null ? 0 : effect.getAmplifier() + 1;
    }

    private static boolean isWithinRange(LivingEntity entity, LivingEntity other) {
        return other != null && other.isAlive() && entity.getBoundingBox().inflate(KNOCKBACK_IMMUNITY_RANGE).intersects(other.getBoundingBox());
    }

    private static void setStacks(LivingEntity entity, int stacks, int duration) {
        int cap = BeyonderData.getSequence(entity) <= 0 ? Integer.MAX_VALUE - 1 : BeyonderData.getSequence(entity) <= 8 ? PUGILIST_MAX_STACKS : BASE_MAX_STACKS;
        int clamped = Mth.clamp(stacks, 0, cap);
        entity.removeEffect(ModEffects.COMBAT_MASTERY);
        if (clamped > 0) {
            entity.addEffect(new MobEffectInstance(ModEffects.COMBAT_MASTERY, duration, clamped - 1, false, false, true));
        }
        syncModifiers(entity, clamped);
    }

    private static void syncModifiers(LivingEntity entity, int stacks) {
        syncModifier(entity, Attributes.ATTACK_DAMAGE, DAMAGE_MODIFIER_ID, stacks);
        syncModifier(entity, Attributes.ATTACK_SPEED, SPEED_MODIFIER_ID, stacks);
    }

    private static void syncModifier(LivingEntity entity, Holder<Attribute> attribute, ResourceLocation id, int stacks) {
        AttributeInstance attributeInstance = entity.getAttribute(attribute);
        if (attributeInstance == null) return;
        double amount = BONUS_PER_STACK * stacks;
        AttributeModifier current = attributeInstance.getModifier(id);
        if (current != null && current.amount() == amount) return;
        attributeInstance.removeModifier(id);
        if (stacks > 0) {
            attributeInstance.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    private static boolean applies(LivingEntity entity) {
        return instance != null && instance.shouldApplyTo(entity);
    }
}
