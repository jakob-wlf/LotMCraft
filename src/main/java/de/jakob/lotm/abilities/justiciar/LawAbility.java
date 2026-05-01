package de.jakob.lotm.abilities.justiciar;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.MultiplierModifierComponent;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class LawAbility extends SelectableAbility {

    /** UUIDs being killed by Solace/Execution — revival abilities skip these. */
    public static final Set<UUID> SOLACE_KILLED = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /** Last ability ID used per entity UUID, for Law of Sealing. */
    public static final Map<UUID, String> LAST_USED_ABILITY = new ConcurrentHashMap<>();



    public LawAbility(String id) {
        super(id, 5f, "law");
        interactionRadius = 40;
        hasOptimalDistance = false;
        postsUsedAbilityEventManually = true;
        canBeCopied = false;
        canBeShared=false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("justiciar", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 800;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.law.weaken_mysticism",
                "ability.lotmcraft.law.enhance_mysticism",
                "ability.lotmcraft.law.solace",
                "ability.lotmcraft.law.law_of_sealing"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;

        switch (abilityIndex) {
            case 0 -> weakenMysticism(serverLevel, entity);
            case 1 -> enhanceMysticism(serverLevel, entity);
            case 2 -> solace(serverLevel, entity);
            case 3 -> lawOfSealing(serverLevel, entity);
        }

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, entity.position(), entity, this, interactionFlags, interactionRadius, 20 * 2));
    }

    // ── Weaken Mysticism, Enhance Reality ────────────────────────────────────

    private void weakenMysticism(ServerLevel serverLevel, LivingEntity entity) {
        int WEAKEN_DURATION = 20 * 30*(int) Math.max(multiplier(entity)/4,1);
        List<LivingEntity> nearby = AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), 40);
        nearby.stream()
                .filter(BeyonderData::isBeyonder)
                .forEach(e -> e.getData(ModAttachments.MULTIPLIER_MODIFIER_COMPONENT)
                        .addMultiplierForTime("law_weaken", 0.25f, WEAKEN_DURATION));

        // Apply to caster too
        entity.getData(ModAttachments.MULTIPLIER_MODIFIER_COMPONENT)
                .addMultiplierForTime("law_weaken", 0.25f, WEAKEN_DURATION);

        broadcastToNearby(serverLevel, entity, Component.translatable("ability.lotmcraft.law.weaken_declared").withStyle(ChatFormatting.GOLD));
    }

    // ── Weaken Reality, Enhance Mysticism ────────────────────────────────────

    private void enhanceMysticism(ServerLevel serverLevel, LivingEntity entity) {
        int ENHANCE_DURATION = 20 * 60*(int) Math.max(multiplier(entity)/4,1);
        List<LivingEntity> nearby = AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), 40);
        nearby.stream()
                .filter(BeyonderData::isBeyonder)
                .forEach(e -> e.getData(ModAttachments.MULTIPLIER_MODIFIER_COMPONENT)
                        .addMultiplierForTime("law_enhance", 2.5f, ENHANCE_DURATION));

        // Apply to caster too
        entity.getData(ModAttachments.MULTIPLIER_MODIFIER_COMPONENT)
                .addMultiplierForTime("law_enhance", 2.5f, ENHANCE_DURATION);

        broadcastToNearby(serverLevel, entity, Component.translatable("ability.lotmcraft.law.enhance_declared").withStyle(ChatFormatting.GOLD));
    }

    // ── Solace ────────────────────────────────────────────────────────────────

    private void solace(ServerLevel serverLevel, LivingEntity caster) {
        // Broadcast the solemn declaration
        broadcastToNearby(serverLevel, caster, Component.translatable("ability.lotmcraft.law.solace_declared").withStyle(ChatFormatting.GOLD));

        List<LivingEntity> nearby = AbilityUtil.getNearbyEntities(caster, serverLevel, caster.position(), 40*(int) Math.max(multiplier(caster)/4,1));
        for (LivingEntity e : nearby) {
            if (!e.getType().is(EntityTypeTags.UNDEAD)) continue;

            SOLACE_KILLED.add(e.getUUID());
            if (e instanceof ServerPlayer) {
                e.hurt(serverLevel.damageSources().magic(), Float.MAX_VALUE);
            } else {
                e.kill();
            }
            // Clean up after 2 ticks to allow death event to fire first
            UUID uuid = e.getUUID();
            de.jakob.lotm.util.scheduling.ServerScheduler.scheduleDelayed(2, () -> SOLACE_KILLED.remove(uuid));
        }
    }

    // ── Law of Sealing ────────────────────────────────────────────────────────

    private void lawOfSealing(ServerLevel serverLevel, LivingEntity caster) {
        LivingEntity target = AbilityUtil.getTargetEntity(caster, 20*(int) Math.max(multiplier(caster)/4,1), 1.5f);
        if (target == null) {
            if (caster instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.translatable("ability.lotmcraft.law.sealing_no_target").withStyle(ChatFormatting.RED));
            }
            return;
        }

        String abilityId = LAST_USED_ABILITY.get(target.getUUID());
        if (abilityId == null) {
            if (caster instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.translatable("ability.lotmcraft.law.sealing_no_ability").withStyle(ChatFormatting.RED));
            }
            return;
        }

        target.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT)
                .disableSpecificAbilityForTime(abilityId, "law_of_sealing", 20 * 60*2*(int) Math.max(multiplier(caster)/4,1));

        String abilityName = abilityId;
        de.jakob.lotm.abilities.core.Ability sealed = LOTMCraft.abilityHandler.getById(abilityId);
        if (sealed != null) abilityName = sealed.getName().getString();

        if (caster instanceof ServerPlayer player) {
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.law.sealing_broadcast_prefix")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(target.getDisplayName().getString()).withStyle(ChatFormatting.WHITE))
                    .append(Component.translatable("ability.lotmcraft.law.sealing_broadcast_middle").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(abilityName).withStyle(ChatFormatting.YELLOW))
                    .append(Component.translatable("ability.lotmcraft.law.sealing_broadcast_suffix").withStyle(ChatFormatting.GOLD)));
        }
        if (target instanceof ServerPlayer targetPlayer) {
            targetPlayer.sendSystemMessage(Component.translatable("ability.lotmcraft.law.sealing_target_prefix")
                    .withStyle(ChatFormatting.RED)
                    .append(Component.literal(abilityName).withStyle(ChatFormatting.YELLOW))
                    .append(Component.translatable("ability.lotmcraft.law.sealing_target_suffix").withStyle(ChatFormatting.RED)));
        }
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private static void broadcastToNearby(ServerLevel level, LivingEntity source, Component msg) {
        level.getServer().getPlayerList().getPlayers().forEach(p -> {
            if (p.level().equals(level) && p.distanceTo(source) <= 40) {
                p.sendSystemMessage(msg);
            }
        });
    }

    // ── Track last-used abilities for Law of Sealing ──────────────────────────

    @SubscribeEvent
    public static void onAbilityUsed(AbilityUsedEvent event) {
        if (event.getAbility() == null || event.getEntity() == null) return;
        LAST_USED_ABILITY.put(event.getEntity().getUUID(), event.getAbility().getId());
    }
}
