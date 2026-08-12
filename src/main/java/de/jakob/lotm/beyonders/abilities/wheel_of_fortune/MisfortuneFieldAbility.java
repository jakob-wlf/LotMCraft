package de.jakob.lotm.beyonders.abilities.wheel_of_fortune;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.attachments.LuckComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MisfortuneFieldAbility extends Ability {
    public MisfortuneFieldAbility(String id) {
        super(id, 30);
        canBeShared = false;

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(35000f, 14000f, 8750f, 5800f, 6825f));

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(18, 20, 22, 27, 29));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("wheel_of_fortune", 4));
    }

    @Override
    public float getSpiritualityCost() {
        return 600;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        EffectManager.playEffect(EffectManager.Effect.MISFORTUNE_FIELD, entity.getX(), entity.getY(), entity.getZ(), serverLevel);

        Vec3 startPos = entity.position();
        float multiplier = multiplier(entity);
        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        int amplifier = getUnluck(entitySeq);

        ServerScheduler.scheduleForDuration(0, 20 * 5, 20 * 15, () -> {
            AbilityUtil.getNearbyEntities(entity, serverLevel, startPos, 20 * multiplier).forEach(e -> {

                if (!(BeyonderData.getPathway(e).equals("monster") && BeyonderData.getSequence(e) < entitySeq)) {
                    if (!(BeyonderData.getPathway(e).equals("darkness") && BeyonderData.getSequence(e) + 1 < entitySeq)) {
                        LuckComponent luckComponent = e.getData(ModAttachments.LUCK_COMPONENT.get());
                        luckComponent.addLuckWithMin(-amplifier, -amplifier * 3);
                    }
                }

            });
        });
    }

    private static int getUnluck(int seq) {
        return switch (seq) {
            case 4 -> 250;
            case 3 -> 500;
            case 2 -> 750;
            case 1 -> 1000;
            case 0 -> 1500;
            default -> 0;
        };
    }
}
