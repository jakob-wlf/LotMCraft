package de.jakob.lotm.abilities.door;

import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.entity.custom.ApprenticeBookEntity;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class RecordingAbility extends SelectableAbilityItem {
    public RecordingAbility(Properties properties) {
        super(properties, 8f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 6));
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.recording.record", "ability.lotmcraft.recording.get_abilities"};
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(level.isClientSide)
            return;

        switch(abilityIndex) {
            case 0 -> record((ServerLevel) level, entity);
        }
    }

    private void record(ServerLevel level, LivingEntity entity) {
        Vec3 playerDir = (new Vec3(entity.getLookAngle().x, 0, entity.getLookAngle().z)).normalize();
        Vec3 pos = VectorUtil.getRelativePosition(entity.getEyePosition().add(0, -.4, 0), playerDir, 1.2, 0, -.4);
        Vec3 dir = entity.getEyePosition().subtract(pos).normalize();

        ApprenticeBookEntity book = new ApprenticeBookEntity(level, pos, dir);
        level.addFreshEntity(book);

        ServerScheduler.scheduleForDuration(0, 1, 20 * 5, () -> {
            Vec3 currentPlayerDir = (new Vec3(entity.getLookAngle().x, 0, entity.getLookAngle().z)).normalize();
            Vec3 currentPos = VectorUtil.getRelativePosition(entity.getEyePosition().add(0, -.8, 0), currentPlayerDir, 1.1, 0, -.2);
            Vec3 currentDir = entity.getEyePosition().subtract(currentPos).normalize();
            book.setPos(currentPos);
            book.setFacingDirection(currentDir);
        }, book::discard, level);
    }
}
