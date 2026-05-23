package de.jakob.lotm.abilities.wheel_of_fortune;

import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.attachments.LuckComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class LuckPerceptionAbility extends ToggleAbility {
    public LuckPerceptionAbility(String id){
        super(id);

        canBeUsedByNPC = false;
        autoClear = false;
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        if (entity.tickCount % 10 != 0) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20, 1.5f, true);
        if(target == null){
            LuckComponent luck = entity.getData(ModAttachments.LUCK_COMPONENT.get());
            String name = entity.hasCustomName()
                    ? entity.getCustomName().getString()
                    : entity.getType().getDescription().getString();

            AbilityUtil.sendActionBar(entity, Component.literal(
                    "§d" + name + " §7| Luck: " + luck.getLuck()));
            return;
        }

        int seq = AbilityUtil.getSeqWithArt(entity, this);
        int targetSeq = BeyonderData.getSequence(target);
        String path = BeyonderData.getPathway(target);

        if(VisionaryHandler.shouldStayInvisible(seq, target)){
            AbilityUtil.sendActionBar(entity, Component.literal(""));
            return;
        }

        if(path.equals("wheel_of_fortune") && targetSeq < seq){
            AbilityUtil.sendActionBar(entity, Component.literal(""));
            return;
        }

        if(AbilityUtil.isTargetSignificantlyStronger(seq, targetSeq)){
            AbilityUtil.sendActionBar(entity, Component.literal(""));
            return;
        }

        LuckComponent luck = target.getData(ModAttachments.LUCK_COMPONENT.get());
        String name = target.hasCustomName()
                ? target.getCustomName().getString()
                : target.getType().getDescription().getString();

        AbilityUtil.sendActionBar(entity, Component.literal(
                "§d" + name + " §7| Luck: " + luck.getLuck()
        ));
    }

    @Override
    public void start(Level level, LivingEntity entity) {}

    @Override
    public void stop(Level level, LivingEntity entity) {
        clearArtifactScaling(entity);
        AbilityUtil.sendActionBar(entity, Component.literal(""));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("wheel_of_fortune", 5));
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }
}
