package de.jakob.lotm.abilities.justiciar;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class JusticeLanguageAbility extends SelectableAbility {

    // Tracks players who have Flog active (next attack triggers bleeding)
    public static final Set<UUID> FLOG_ACTIVE = ConcurrentHashMap.newKeySet();

    public JusticeLanguageAbility(String id) {
        super(id, 4f, "justice_language");
        interactionRadius = 20;
        hasOptimalDistance = false;
        postsUsedAbilityEventManually = true;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("justiciar", 6));
    }

    @Override
    protected float getSpiritualityCost() {
        return 200;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.justice_language.maintain_secrecy",
                "ability.lotmcraft.justice_language.death",
                "ability.lotmcraft.justice_language.flog"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;

        switch (abilityIndex) {
            case 0 -> maintainSecrecy(serverLevel, entity);
            case 1 -> death(serverLevel, entity);
            case 2 -> flog(serverLevel, entity);
        }
    }

    private void maintainSecrecy(ServerLevel serverLevel, LivingEntity caster) {
        LivingEntity target = AbilityUtil.getTargetEntity(caster, 20*(int) Math.max(multiplier(caster)/4,1), 1.4f);
        if (target == null) return;
        Vec3 startPos = caster.getEyePosition().subtract(0, .2, 0).add(caster.getLookAngle().normalize());
        serverLevel.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, caster.getSoundSource(), 2.0f, .5f);
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 600, 2, false, false));
        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 600, 1, false, false));

        if (caster instanceof ServerPlayer sp) {
            sp.sendSystemMessage(Component.translatable("ability.lotmcraft.justice_language.secrecy_established").withStyle(ChatFormatting.GOLD));
        }

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, caster.position(), caster, this, interactionFlags, 20, 20 * 2));
    }

    private void death(ServerLevel serverLevel, LivingEntity caster) {
        LivingEntity target = AbilityUtil.getTargetEntity(caster, 20*(int) Math.max(multiplier(caster)/4,1), 1.4f);
        if (target == null) return;

        int targetSeq = BeyonderData.getSequence(target);
        int seq = BeyonderData.getSequence(caster);
        double failChance = 0;
        if (targetSeq<seq) {
            failChance = 0.4f;
        } else if (targetSeq == seq) {
            failChance = 0.6f;
        } else {
            failChance = 0.8f;
        }

        if (random.nextDouble() < failChance) {
            if (caster instanceof ServerPlayer sp) {
                sp.sendSystemMessage(Component.translatable("ability.lotmcraft.justice_language.verdict_failed").withStyle(ChatFormatting.RED));
            }
            return;
        }
        Vec3 startPos = caster.getEyePosition().subtract(0, .2, 0).add(caster.getLookAngle().normalize());
        serverLevel.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, caster.getSoundSource(), 2.0f, .5f);
        target.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.BEYONDER_GENERIC, caster), 1);
        target.setHealth(target.getHealth() - (target.getMaxHealth() / 4f));
        target.hurtMarked = true;
        String targetName = target.getDisplayName().getString();
        if (caster instanceof ServerPlayer sp) {
            sp.sendSystemMessage(Component.translatable("ability.lotmcraft.justice_language.death_declared",
                    Component.literal(targetName).withStyle(ChatFormatting.WHITE))
                    .withStyle(ChatFormatting.DARK_RED));
        }

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, caster.position(), caster, this, interactionFlags, 20, 20 * 2));
    }

    private void flog(ServerLevel serverLevel, LivingEntity caster) {
        FLOG_ACTIVE.add(caster.getUUID());
        Vec3 startPos = caster.getEyePosition().subtract(0, .2, 0).add(caster.getLookAngle().normalize());
        serverLevel.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, caster.getSoundSource(), 2.0f, .5f);
        if (caster instanceof ServerPlayer sp) {
            sp.sendSystemMessage(Component.translatable("ability.lotmcraft.justice_language.flog_prepared")
                    .withStyle(ChatFormatting.GOLD));
        }

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, caster.position(), caster, this, interactionFlags, 5, 20 * 2));
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        if (!FLOG_ACTIVE.remove(attacker.getUUID())) return;
        if (!(attacker.level() instanceof ServerLevel serverLevel)) return;

        LivingEntity target = event.getEntity();

        // Schedule bleeding DoT: 5 ticks x 20 interval = 5 hits over 100 ticks
        ServerScheduler.scheduleRepeating(0, 20, 5, () -> {
            if (!target.isAlive()) return;
            float dmg = target.getMaxHealth() * 0.12f;
            float newHealth = Math.max(0, target.getHealth() - dmg);
            target.setHealth(newHealth);
            target.hurtMarked = true;
        }, serverLevel, () -> target.isAlive());
    }
}
