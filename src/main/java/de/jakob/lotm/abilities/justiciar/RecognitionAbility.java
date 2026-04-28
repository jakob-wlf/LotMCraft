package de.jakob.lotm.abilities.justiciar;

import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.attachments.LuckComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class RecognitionAbility extends ToggleAbility {

    public RecognitionAbility(String id) {
        super(id);
        canBeCopied = true;
        canBeUsedByNPC = false;
        canBeReplicated = true;
        cannotBeStolen = false;
        doesNotIncreaseDigestion = true;
        canBeUsedInArtifact = true;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("justiciar", 8));
    }

    @Override
    public float getSpiritualityCost() {
        return 0;
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            entity.sendSystemMessage(Component.literal("Recognition activated."));
        }
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        if (entity.tickCount % 10 != 0) return;
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 40*(int) Math.max(multiplier(entity)/4,1), 1.5f, true);

        if (target == null) {
            AbilityUtil.sendActionBar(entity, Component.literal(""));
            return;
        }

        int seq = AbilityUtil.getSeqWithArt(entity, this);
        int targetSeq = BeyonderData.getSequence(target);
        String path = BeyonderData.getPathway(target);

        if (path.equals("justiciar") && targetSeq < seq) {
            AbilityUtil.sendActionBar(entity, Component.literal(""));
            return;
        }

        if (seq > targetSeq) {
            AbilityUtil.sendActionBar(entity, Component.literal(""));
            return;
        }

        String name = target.hasCustomName()
                ? target.getCustomName().getString()
                : target.getType().getDescription().getString();

        AbilityUtil.sendActionBar(entity, Component.literal(
                "§d" + name + " §7| Pathway: " + path +" §7| Sequence: " + targetSeq
        ));
    }
    @Override
    public void stop(Level level, LivingEntity entity) {
        clearArtifactScaling(entity);
        AbilityUtil.sendActionBar(entity, Component.literal(""));
        entity.sendSystemMessage(Component.literal("Recognition deactivated."));
    }
}
