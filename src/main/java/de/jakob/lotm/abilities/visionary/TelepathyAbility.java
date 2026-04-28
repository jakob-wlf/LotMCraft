package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class TelepathyAbility extends ToggleAbility {

    public TelepathyAbility(String id) {
        super(id);
        autoClear = false;

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
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        // Only update every 10 ticks
        if (entity.tickCount % 10 != 0) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20* (int) Math.max(multiplier(entity)/4,1), 1.5f, true);

        if (target == null) {
            AbilityUtil.sendActionBar(entity, Component.literal(""));
            return;
        }

        if (PsychologicalInvisibilityAbility.invisiblePlayers.containsKey(target.getUUID())) {
            if (AbilityUtil.getSeqWithArt(entity, this) >=
                    PsychologicalInvisibilityAbility.invisiblePlayers.get(target.getUUID()))
                return;
        }

        SanityComponent sanity = target.getData(ModAttachments.SANITY_COMPONENT);
        int sanityPercent = Math.round(sanity.getSanity() * 100);

        String color = sanityPercent >= 80 ? "§a"
                : sanityPercent >= 50 ? "§e"
                : sanityPercent >= 20 ? "§c"
                : "§4";

        String name = target.hasCustomName()
                ? target.getCustomName().getString()
                : target.getType().getDescription().getString();

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        int targetSeq = BeyonderData.getSequence(target);
        int diff = entitySeq - targetSeq;

        AbilityUtil.sendActionBar(entity, Component.literal(
                "§d" + name + " §7| Sanity: " + color + sanityPercent + "%" +
                        ((diff <= 0 && entitySeq <= 4) ? "§7 Pathway: " + BeyonderData.getPathway(target) +
                                " Sequence: " + targetSeq : "")
        ));
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        AbilityUtil.sendActionBar(entity, Component.literal(""));

        clearArtifactScaling(entity);
    }
}
