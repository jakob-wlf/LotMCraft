package de.jakob.lotm.beyonders.abilities.visionary;

import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class TelepathyAbility extends ToggleAbility {

    public TelepathyAbility(String id) {
        super(id);
        autoClear = false;
        canBeUsedByNPC = false;
    }

    @Override
    public float getSpiritualityCost() {
        return 1;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 8));
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        if (VisionaryHandler.shouldBeAffectedWithMindWorldSeal(entitySeq)) {
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.mind_world_authority_ability.is_sealed")
                            .withColor(0xFFff124d));
        }
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(entity instanceof ServerPlayer player)) return;
        if (entity.tickCount % 10 != 0) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20 * (int) Math.max(multiplier(entity) / 4, 1), 1.5f, true, true);

        if (target == null) {
            AbilityUtil.sendActionBar(entity, Component.literal(""));
            return;
        }

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        if (VisionaryHandler.shouldStayInvisible(entitySeq, target)) return;
        if (VisionaryHandler.shouldFailAndTrigger(entitySeq, entity, target, this, false)) return;
        if (AbilityUtil.isTargetSignificantlyStronger(entitySeq, BeyonderData.getSequence(target))) return;

        if (VisionaryHandler.shouldBeAffectedWithMindWorldSeal(entitySeq)) {
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.mind_world_authority_ability.is_sealed")
                            .withColor(0xFFff124d));
            cancel((ServerLevel) level, player);
            return;
        }

        performTelepathy(player, target, entitySeq);
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        AbilityUtil.sendActionBar(entity, Component.literal(""));
        clearArtifactScaling(entity);
    }

    public static void performTelepathy(ServerPlayer entity, LivingEntity target, int entitySeq) {
        SanityComponent sanity = target.getData(ModAttachments.SANITY_COMPONENT);
        int sanityPercent = Math.round(sanity.getSanity() * 100);

        String sanityColor = sanityPercent >= 80 ? "§a"
                : sanityPercent >= 50 ? "§e"
                : sanityPercent >= 20 ? "§c"
                : "§4";

        String name = target.hasCustomName()
                ? target.getCustomName().getString()
                : target.getType().getDescription().getString();

        int targetSeq = BeyonderData.getSequence(target);
        int diff = entitySeq - targetSeq;

        var plague = target.getData(ModAttachments.MENTAL_PLAGUE.get());
        boolean shouldRenderPlague = plague.hasMentalPlague()
                && (plague.isOwner(entity) || plague.getSequence() >= entitySeq);

        StringBuilder sb = new StringBuilder();

        sb.append("§7[ §f").append(name).append(" §7]");
        sb.append("  §7Sanity: ").append(sanityColor).append(sanityPercent).append("%");

        if (diff <= 0 && entitySeq <= 4) {
            sb.append("  §7Pathway: §6").append(BeyonderData.getPathway(target));
            sb.append("  §7Seq: §6").append(targetSeq);

            if (shouldRenderPlague) {
                sb.append("  §c⚠ Plague§7: ")
                        .append("§c").append(plague.getOwnerName())
                        .append(" §7(Seq §c").append(plague.getSequence()).append("§7")
                        .append(", Stage §c").append(plague.getStage()).append("§7)");
            }
        }

        AbilityUtil.sendActionBar(entity, Component.literal(sb.toString()));
    }
}