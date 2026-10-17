package de.jakob.lotm.beyonders.abilities.fool;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.LuckComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.events.custom.AbilitySealedEvent;
import de.jakob.lotm.events.custom.LuckSetEvent;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

import java.util.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class GraftingAbilitiesAbility extends ToggleAbility {

    private static Map<UUID, Integer> cooldownTimer = new HashMap<>();
    private static final Map<UUID, Map<Holder<MobEffect>, Integer>> effectSpCooldowns = new HashMap<>();
    private static final Map<UUID, Map<String, Integer>> sealGraceTimers = new HashMap<>();
    private static final Map<UUID, Integer> damageGraceTimers = new HashMap<>();
    private static final Map<UUID, Float> lastGraftedDamage = new HashMap<>();
    private static int SEALING_GRACETIME = 4; // 1s
    private static int DAMAGE_GRACETIME = 4 * 2; // 2s
    private static int COOLDOWN = 4 * 7; // this is 7s, because toggled abilities tick once every 5 ticks
    private static int GRACETIME = 4 * 2;

    public GraftingAbilitiesAbility(String id) {
        super(id);
    }

    @Override
    public float getSpiritualityCost() {
        return 300;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("fool", 1));
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        cooldownTimer.put(entity.getUUID(), 0);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(!cooldownTimer.containsKey(entity.getUUID())){
            cooldownTimer.put(entity.getUUID(), 0);
        }

        if(cooldownTimer.get(entity.getUUID()) > 0){
            cooldownTimer.put(entity.getUUID(), cooldownTimer.get(entity.getUUID()) - 1);

            if(cooldownTimer.get(entity.getUUID()) == 0){
                sealGraceTimers.remove(entity.getUUID());
                damageGraceTimers.remove(entity.getUUID());
                lastGraftedDamage.remove(entity.getUUID());
                effectSpCooldowns.remove(entity.getUUID());
            }
        }

        if(cooldownTimer.get(entity.getUUID()) < 0){
            cooldownTimer.put(entity.getUUID(), 0);
        }
    }

    @Override
    public void stop(Level level, LivingEntity entity) {}

    private static LivingEntity getValidEntityForGrafting(LivingEntity entity, ServerLevel serverLevel){
        List<LivingEntity> entities = new ArrayList<>();

        // check surrounding non ally entities first - cant target same sequence
        entities = AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), 50);
        for (LivingEntity e : entities) {
            if (BeyonderData.getSequence(e) <= BeyonderData.getSequence(entity)) continue;
            return e;
        }

        // check historical projections
        for (ServerLevel dimension : entity.getServer().getAllLevels()) {
            for (Entity e : dimension.getAllEntities()) {
                if (e instanceof LivingEntity living) {
                    if (living.getPersistentData().getBoolean("VoidSummoned")
                            && living.getPersistentData().hasUUID("VoidSummonOwner")
                            && living.getPersistentData().getUUID("VoidSummonOwner").equals(entity.getUUID())) {
                        return living;
                    }
                }
            }
        }

        // check puppets
        var data = entity.getData(ModAttachments.MARIONETTE_OWNER_COMPONENT);
        for (UUID marionetteUUID : data.getMarionettes()) {
            Entity e = serverLevel.getEntity(marionetteUUID);
            if (e instanceof LivingEntity living) {
                return living;
            }
        }

        return null;
    }

    private static LivingEntity getGraftTarget(LivingEntity target, LivingEntity source, int lastPaidTimer, int graceTime) {
        if (!(target.level() instanceof ServerLevel serverLevel)) return null;

        boolean hasActiveGrafting = false;
        for (Ability var : ToggleAbility.getActiveAbilitiesForEntity(target)){
            if (var == LOTMCraft.abilityHandler.getById("grafting_abilities_ability")) {
                hasActiveGrafting = true;
                break;
            }
        }
        if (!hasActiveGrafting) return null;

        int timer = cooldownTimer.getOrDefault(target.getUUID(), 0);
        boolean inGrace = timer != 0 && lastPaidTimer >= timer && (lastPaidTimer - timer) < graceTime;
        if (!inGrace && timer != 0 && timer <= COOLDOWN - GRACETIME) return null;

        LivingEntity newTarget = getValidEntityForGrafting(target, serverLevel);
        if (newTarget == null || !newTarget.isAlive()) return null;

        // return if the source is the same as target, or source is stronger, or if source is door pathway and equal sequence
        if (source != null) {
            if (source == target
                    || BeyonderData.getSequence(source) < BeyonderData.getSequence(target)
                    || (BeyonderData.getSequence(source) <= BeyonderData.getSequence(target) && BeyonderData.getPathway(source).equals("door"))) {
                return null;
            }
        }

        return newTarget;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void takeDamage(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        LivingEntity source = event.getSource().getEntity() instanceof LivingEntity e ? e : null;

        int lastPaidTimer = damageGraceTimers.getOrDefault(target.getUUID(), -1);
        LivingEntity newTarget = getGraftTarget(target, source, lastPaidTimer, DAMAGE_GRACETIME);
        if (newTarget == null) return;

        int timer = cooldownTimer.getOrDefault(target.getUUID(), 0);
        boolean inGrace = timer != 0 && lastPaidTimer >= timer && (lastPaidTimer - timer) < DAMAGE_GRACETIME;

        // during the grace time, damage that isnt higher than the last recorded damage is grafted without consuming sp
        if (inGrace && event.getNewDamage() <= lastGraftedDamage.getOrDefault(target.getUUID(), 0f)) {
            newTarget.hurt(event.getSource(), event.getNewDamage());
            event.setNewDamage(0);
            return;
        }

        if (event.getNewDamage() < 2) return;

        float ratio = event.getNewDamage() / target.getMaxHealth();
        // every 5% reduced will increase sp cost by 5k
        int spiritualityCost = Math.max(10000, 10000 * (int)(ratio / 0.05f));

        // damage the newTarget with the same amount
        if (BeyonderData.getSpirituality(target) - spiritualityCost >= 0) {
            if (timer == 0) {
                cooldownTimer.put(target.getUUID(), COOLDOWN);
            }
            BeyonderData.reduceSpirituality(target, spiritualityCost);

            lastGraftedDamage.put(target.getUUID(), event.getNewDamage());
            if (!inGrace) {
                damageGraceTimers.put(target.getUUID(), timer == 0 ? COOLDOWN : timer);
            }

            newTarget.hurt(event.getSource(), event.getNewDamage());

            event.setNewDamage(0);
        }
    }


    @SubscribeEvent
    public static void sealedAbilities(AbilitySealedEvent event) {
        LivingEntity target = event.getTargetEntity(); //player with grafting
        LivingEntity source = event.getSourceEntity();

        String cause = event.getCause();

        if (cause.equals("cogitation")
                || cause.equals("miracle_of_resurrection_")
                || cause.equals("visionary_meditation")
                || cause.equals("reincarnation_debuff")
                || cause.equals("law_of_sealing")
                || cause.equals("apotheosis")
        ) {
            return;
        }

        // each sealing cause has its own grace time - during it the same cause is grafted without consuming sp
        Map<String, Integer> targetSealGraces = sealGraceTimers.computeIfAbsent(target.getUUID(), k -> new HashMap<>());
        int lastConsumeTimer = targetSealGraces.getOrDefault(cause, -1);

        LivingEntity newTarget = getGraftTarget(target, source, lastConsumeTimer, SEALING_GRACETIME);
        if (newTarget == null) return;

        int timer = cooldownTimer.getOrDefault(target.getUUID(), 0);
        boolean inGrace = timer != 0 && lastConsumeTimer >= timer && (lastConsumeTimer - timer) < SEALING_GRACETIME;

        int spiritualityCost = 10000 * Math.max(1, event.getDuration() / 20);
        if (inGrace || BeyonderData.getSpirituality(target) - spiritualityCost >= 0) {
            if (!inGrace) {
                if (timer == 0) {
                    cooldownTimer.put(target.getUUID(), COOLDOWN);
                }
                BeyonderData.reduceSpirituality(target, spiritualityCost);
                targetSealGraces.put(cause, timer == 0 ? COOLDOWN : timer);
            }
            event.setTargetEntity(newTarget);
        }
    }


    @SubscribeEvent
    public static void effectedByLuck(LuckSetEvent event) {
        LivingEntity target = event.getTargetEntity(); //player with grafting
        LivingEntity source = event.getSourceEntity();

        if (event.isLuckIncreased()) return;

        LivingEntity newTarget = getGraftTarget(target, source, -1, 0);
        if (newTarget == null) return;

        int timer = cooldownTimer.getOrDefault(target.getUUID(), 0);

        LuckComponent comp = target.getData(ModAttachments.LUCK_COMPONENT);
        int targetLuck = comp.getLuck();
        int difference = targetLuck - event.getLuck();

        // 1k sp for every -50 luck grafted (10k for -500)
        int spiritualityCost = Math.max(1000, 1000 * (difference / 50));

        if (BeyonderData.getSpirituality(target) - spiritualityCost >= 0) {
            if (timer == 0) {
                cooldownTimer.put(target.getUUID(), COOLDOWN);
            }
            BeyonderData.reduceSpirituality(target, spiritualityCost);

            event.setTargetEntity(newTarget);
        }
    }


    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Applicable event) {
        LivingEntity target = event.getEntity(); //player with grafting

        MobEffectInstance newEffect = event.getEffectInstance();
        if (newEffect.getEffect().value().getCategory() == MobEffectCategory.BENEFICIAL) {
            return;
        }

        Holder<MobEffect> effectHolder = newEffect.getEffect();
        Map<Holder<MobEffect>, Integer> targetEffectTimers = effectSpCooldowns.computeIfAbsent(target.getUUID(), k -> new HashMap<>());
        int lastPaidTimer = targetEffectTimers.getOrDefault(effectHolder, -1);

        LivingEntity newTarget = getGraftTarget(target, null, lastPaidTimer, 20);
        if (newTarget == null) return;

        int timer = cooldownTimer.getOrDefault(target.getUUID(), 0);

        if (lastPaidTimer != -1 && lastPaidTimer >= timer && (lastPaidTimer - timer) < 20) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            newTarget.addEffect(new MobEffectInstance(newEffect));
            return;
        }

        int amplifier = Math.min(newEffect.getAmplifier(), 20);
        int duration = newEffect.getDuration();

        int durationCost = (duration / 20) * 500; // 500 sp for every 1s
        int amplifierCost = (amplifier + 1) * 500; // 500 sp for every amp (amp 0 = 1000.. etc)

        int spiritualityCost = durationCost + amplifierCost;

        if (BeyonderData.getSpirituality(target) - spiritualityCost >= 0) {
            if (timer == 0) {
                cooldownTimer.put(target.getUUID(), COOLDOWN);
            }
            targetEffectTimers.put(effectHolder, timer == 0 ? COOLDOWN : timer);
            BeyonderData.reduceSpirituality(target, spiritualityCost);

            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            newTarget.addEffect(newEffect);
        }
    }
}
