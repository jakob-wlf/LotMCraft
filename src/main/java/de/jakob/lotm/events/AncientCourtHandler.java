package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.AbilityUseEvent;
import de.jakob.lotm.entity.custom.ability_entities.justiciar_pathway.AncientCourtEntity;
import de.jakob.lotm.entity.custom.ability_entities.justiciar_pathway.AncientCourtEntity.CourtProhibitionType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.PotionItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class AncientCourtHandler {

    // ── Helper ────────────────────────────────────────────────────────────────

    private static AncientCourtEntity getCourtFor(LivingEntity entity) {
        for (AncientCourtEntity court : AncientCourtEntity.ACTIVE_COURTS) {
            if (court.isEntityInCourt(entity)) return court;
        }
        return null;
    }

    private static boolean hasProhibition(LivingEntity entity, CourtProhibitionType type) {
        AncientCourtEntity court = getCourtFor(entity);
        return court != null && court.currentProhibition == type;
    }

    private static void sendMessage(LivingEntity entity, String prohibition) {
        if (entity instanceof ServerPlayer sp) {
            sp.sendSystemMessage(Component.translatable("lotmcraft.ancient_court.prohibited", prohibition)
                    .withStyle(ChatFormatting.GOLD));
        }
    }

    // ── TELEPORTING ───────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onTeleport(EntityTeleportEvent event) {
        if (!(event.getEntity() instanceof LivingEntity le)) return;
        if (!hasProhibition(le, CourtProhibitionType.TELEPORTING)) return;
        event.setCanceled(true);
        WorldJudgmentHandler.escalate(le);
        sendMessage(le, "Teleporting");
    }

    // ── PROJECTILES ───────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Projectile projectile)) return;
        if (!(projectile.getOwner() instanceof LivingEntity shooter)) return;
        if (!hasProhibition(shooter, CourtProhibitionType.PROJECTILES)) return;
        event.setCanceled(true);
    }

    // ── EATING / DRINKING ─────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onItemUseStart(LivingEntityUseItemEvent.Start event) {
        LivingEntity entity = event.getEntity();
        boolean isFood = event.getItem().getFoodProperties(entity) != null;
        boolean isPotion = event.getItem().getItem() instanceof PotionItem;

        if (isFood && hasProhibition(entity, CourtProhibitionType.EATING)) {
            event.setCanceled(true);
            WorldJudgmentHandler.escalate(entity);
            sendMessage(entity, "Eating");
        } else if (isPotion && hasProhibition(entity, CourtProhibitionType.DRINKING)) {
            event.setCanceled(true);
            WorldJudgmentHandler.escalate(entity);
            sendMessage(entity, "Drinking");
        }
    }

    // ── BUILDING ──────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof LivingEntity le)) return;
        if (!hasProhibition(le, CourtProhibitionType.BUILDING)) return;
        event.setCanceled(true);
        WorldJudgmentHandler.escalate(le);
        sendMessage(le, "Building");
    }

    // ── DESTRUCTION ───────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (!hasProhibition(player, CourtProhibitionType.DESTRUCTION)) return;
        event.setCanceled(true);
        WorldJudgmentHandler.escalate(player);
        sendMessage(player, "Destruction");
    }

    // ── FALL DAMAGE ───────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onFallDamage(LivingFallEvent event) {
        if (!hasProhibition(event.getEntity(), CourtProhibitionType.FALL_DAMAGE)) return;
        event.setCanceled(true);
        // Fall damage prohibition is beneficial — no escalation
    }

    // ── SPEAKING ──────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        if (!hasProhibition(player, CourtProhibitionType.SPEAKING)) return;
        event.setCanceled(true);
        WorldJudgmentHandler.escalate(player);
        sendMessage(player, "Speaking");
    }

    // ── HEALING ───────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        if (!hasProhibition(event.getEntity(), CourtProhibitionType.HEALING)) return;
        event.setCanceled(true);
        WorldJudgmentHandler.escalate(event.getEntity());
    }

    // ── POSITIVE EFFECTS / NEGATIVE EFFECTS ──────────────────────────────────

    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (!(event.getEntity() instanceof LivingEntity le)) return;
        boolean beneficial = event.getEffectInstance().getEffect().value().isBeneficial();
        if (beneficial && hasProhibition(le, CourtProhibitionType.POSITIVE_EFFECTS)) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        } else if (!beneficial && hasProhibition(le, CourtProhibitionType.NEGATIVE_EFFECTS)) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    // ── INTERACTION ───────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!hasProhibition(player, CourtProhibitionType.INTERACTION)) return;
        event.setCanceled(true);
        WorldJudgmentHandler.escalate(player);
        sendMessage(player, "Interaction");
    }

    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!hasProhibition(player, CourtProhibitionType.INTERACTION)) return;
        event.setCanceled(true);
        WorldJudgmentHandler.escalate(player);
        sendMessage(player, "Interaction");
    }

    // ── ABILITY BASED PROHIBITIONS ────────────────────────────────────────────
    // Handles: BLINKING, CONCEALMENT, SEALING, SWAPPING_ABILITIES

    @SubscribeEvent
    public static void onAbilityUse(AbilityUseEvent event) {
        if (!(event.getEntity() instanceof LivingEntity le)) return;
        AncientCourtEntity court = getCourtFor(le);
        if (court == null) return;

        String id = event.getAbility().getId();
        CourtProhibitionType prohibition = court.currentProhibition;

        if (prohibition == CourtProhibitionType.SWAPPING_ABILITIES) {
            event.setCanceled(true);
            WorldJudgmentHandler.escalate(le);
            sendMessage(le, "Swapping Abilities");
            return;
        }

        if (prohibition == CourtProhibitionType.BLINKING && id.equals("blink_ability")) {
            event.setCanceled(true);
            WorldJudgmentHandler.escalate(le);
            sendMessage(le, "Blinking");
            return;
        }

        if (prohibition == CourtProhibitionType.CONCEALMENT && (
                id.equals("concealment_ability") || id.equals("invisibility_ability")
                || id.equals("lesser_concealment_ability") || id.equals("shadow_concealment_ability")
                || id.equals("identity_concealment_ability"))) {
            event.setCanceled(true);
            WorldJudgmentHandler.escalate(le);
            sendMessage(le, "Concealment");
            return;
        }

        if (prohibition == CourtProhibitionType.SEALING && id.equals("sealing_ability")) {
            event.setCanceled(true);
            WorldJudgmentHandler.escalate(le);
            sendMessage(le, "Sealing");
        }
    }
}
