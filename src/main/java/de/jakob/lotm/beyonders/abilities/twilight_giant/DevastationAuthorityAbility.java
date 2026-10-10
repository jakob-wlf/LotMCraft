package de.jakob.lotm.beyonders.abilities.twilight_giant;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.AbilityUseEvent;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.beyonders.abilities.twilight_giant.handlers.TwilightAging;
import de.jakob.lotm.events.HonorificNamesEventHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class DevastationAuthorityAbility extends SelectableAbility {

    private static final float CAST_COST = 5000f;
    private static final int CHARGES = 3;
    private static final int FOLLOW_TICKS = 40;
    private static final double STRIP_RADIUS = 16.0D;
    private static final float SANITY_LOSS = 0.35f;
    private static final int PRAYER_TICKS = 20 * 10;

    private static final Map<UUID, Integer> charges = new HashMap<>();
    private static final Map<UUID, Long> followUntil = new HashMap<>();
    private static final SetArmed humanity = new SetArmed();
    private static final Map<UUID, Integer> humanityToken = new HashMap<>();

    public DevastationAuthorityAbility(String id) {
        super(id, 60);
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("twilight_giant", 0));
    }

    @Override
    protected float getSpiritualityCost() {
        return CAST_COST;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.devastation_authority.attacks",
                "ability.lotmcraft.devastation_authority.dehumanization",
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if (!(entity instanceof ServerPlayer player)) return;
        if (selectedAbility == 0) {
            charges.put(player.getUUID(), CHARGES);
            bar(player, "ability.lotmcraft.devastation_authority.attacks_armed");
            player.level().playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, player.getSoundSource(), 0.6f, 0.5f);
            return;
        }
        humanity.arm(player.getUUID());
        bar(player, "ability.lotmcraft.devastation_authority.dehumanization_armed");
        player.level().playSound(null, player.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, player.getSoundSource(), 1f, 0.4f);
    }

    public static void prayerFor(UUID target) {
        humanityToken.remove(target);
    }

    @SubscribeEvent
    public static void onUse(AbilityUseEvent event) {
        LivingEntity caster = event.getEntity();
        Ability ability = event.getAbility();
        if (event.isCanceled() || caster == null || ability == null || caster.level().isClientSide()) return;
        if (!(caster.level() instanceof ServerLevel level)) return;
        if ("devastation_authority_ability".equals(ability.getId())) return;
        Integer left = charges.get(caster.getUUID());
        if (left == null || left <= 0) return;
        if (left == 1) charges.remove(caster.getUUID());
        else charges.put(caster.getUUID(), left - 1);
        followUntil.put(caster.getUUID(), level.getGameTime() + FOLLOW_TICKS);
        for (LivingEntity target : AbilityUtil.getNearbyEntities(caster, level, caster.position(), STRIP_RADIUS)) {
            strip(caster, target);
        }
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Post event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof LivingEntity attacker)) return;
        if (attacker.level().isClientSide() || event.getNewDamage() <= 0) return;
        LivingEntity target = event.getEntity();
        Long until = followUntil.get(attacker.getUUID());
        if (until != null && attacker.level().getGameTime() <= until) strip(attacker, target);
        if (!AbilityUtil.mayDamage(attacker, target) || !humanity.consume(attacker.getUUID())) return;
        drain(target);
        if (target instanceof ServerPlayer victim && BeyonderData.playerMap != null) {
            BeyonderData.playerMap.removeHonorificName(victim);
            HonorificNamesEventHandler.clearPendingPrayers(victim.getUUID());
        }
        int token = humanityToken.merge(target.getUUID(), 1, Integer::sum);
        if (!(target.level() instanceof ServerLevel level)) return;
        ServerScheduler.scheduleDelayed(PRAYER_TICKS, () -> {
            Integer current = humanityToken.get(target.getUUID());
            if (current == null || current != token) return;
            humanityToken.remove(target.getUUID());
            if (!target.isRemoved()) drain(target);
        }, level);
    }

    private static void strip(LivingEntity caster, LivingEntity target) {
        if (target == caster || !AbilityUtil.mayDamage(caster, target)) return;
        target.removeAllEffects();
        if (target.level() instanceof ServerLevel level) ToggleAbility.cleanUp(level, target);
    }

    private static void drain(LivingEntity target) {
        var sanity = target.getData(ModAttachments.SANITY_COMPONENT);
        sanity.setSanityAndSync(sanity.getSanity() - SANITY_LOSS, target);
    }

    private static void bar(LivingEntity entity, String key) {
        AbilityUtil.sendActionBar(entity, Component.translatable(key).withColor(TwilightAging.TWILIGHT_TEXT));
    }

    private static final class SetArmed {
        private final Map<UUID, Boolean> armed = new HashMap<>();

        private void arm(UUID id) {
            armed.put(id, true);
        }

        private boolean consume(UUID id) {
            return armed.remove(id) != null;
        }
    }
}
