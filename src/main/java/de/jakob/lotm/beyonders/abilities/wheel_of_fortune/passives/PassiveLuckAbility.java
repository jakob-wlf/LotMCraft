package de.jakob.lotm.beyonders.abilities.wheel_of_fortune.passives;

import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityHandler;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityItem;
import de.jakob.lotm.attachments.LuckComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class PassiveLuckAbility extends PassiveAbilityItem {


    public PassiveLuckAbility(Properties properties) {
        super(properties);

    }

    public static int getNormalLuckForEntity(LivingEntity livingEntity) {
        if(!(PassiveAbilityHandler.PASSIVE_LUCK.get() instanceof PassiveLuckAbility instance)) {
            return 0;
        }

        if(!instance.shouldApplyTo(livingEntity)) {
            return 0;
        }

        int sequence = BeyonderData.getSequence(livingEntity);
        if (sequence < 0 || sequence > 9) {
            return 0;
        }

        return instance.getLuckLevelForSequence(sequence);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "wheel_of_fortune", 7
        ));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        int sequence = BeyonderData.getSequence(entity);

        if (sequence < 0 || sequence > 9) {
            return;
        }
        LuckComponent component = entity.getData(ModAttachments.LUCK_COMPONENT.get());

        if(entity.tickCount % 200 == 0) {
            if (component.getLuck() < getLuckLevelForSequence(sequence)) {
                int rate = getLuckRate(sequence);

                component.setLuck(component.getLuck() + rate);
            }
        }
    }

    private int getLuckLevelForSequence(int sequence) {
        return switch (sequence) {
            case 7 -> 360;
            case 6 -> 600;
            case 5 -> 720;
            case 4 -> 1200;
            case 3 -> 1560;
            case 2 -> 2040;
            case 1 -> 2280;
            case 0 -> 3000;
            default -> 0;
        };
    }

    private int getLuckRate(int seq){
        return switch (seq){
            case 7 -> 1;
            case 6 -> 1;
            case 5 -> 30;
            case 4 -> 100;
            case 3 -> 125;
            case 2 -> 200;
            case 1 -> 250;
            case 0 -> 300;
            default -> 0;
        };
    }
}