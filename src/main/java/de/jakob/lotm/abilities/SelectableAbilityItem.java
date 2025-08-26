package de.jakob.lotm.abilities;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class SelectableAbilityItem extends AbilityItem{

    protected final HashMap<UUID, Integer> selectedAbilities = new HashMap<>();

    public SelectableAbilityItem(Properties properties, float cooldown) {
        super(properties, cooldown);
    }

    @Override
    public abstract Map<String, Integer> getRequirements();

    @Override
    protected abstract float getSpiritualityCost();

    protected float[] getSpiritualityCostIndividual() {
        float[] cost = new float[getAbilityNames().length];
        for(int i = 0; i < getAbilityNames().length; i++) {
            cost[i] = getSpiritualityCost();
        }
        return cost;
    };

    protected abstract String[] getAbilityNames();

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(!(entity instanceof Player)) {
            useAbility(level, entity, random.nextInt(getAbilityNames().length));
        }

        if(!selectedAbilities.containsKey(entity.getUUID())) {
            selectedAbilities.put(entity.getUUID(), 0);
        }

        int selectedAbility = selectedAbilities.get(entity.getUUID());
        useAbility(level, entity, selectedAbility);
    }

    public String getSelectedAbility(LivingEntity entity) {
        if(getAbilityNames().length == 0)
            return "";

        if(!selectedAbilities.containsKey(entity.getUUID())) {
            selectedAbilities.put(entity.getUUID(), 0);
        }

        int selectedAbility = selectedAbilities.get(entity.getUUID());
        return getAbilityNames()[selectedAbility];
    }

    protected abstract void useAbility(Level level, LivingEntity entity, int abilityIndex);

    //called client sided
    public void nextAbility(LivingEntity entity) {
        if(getAbilityNames().length == 0)
            return;

        if(!selectedAbilities.containsKey(entity.getUUID())) {
            selectedAbilities.put(entity.getUUID(), 0);
        }

        int selectedAbility = selectedAbilities.get(entity.getUUID());
        selectedAbility++;
        if(selectedAbility >= getAbilityNames().length) {
            selectedAbility = 0;
        }
        selectedAbilities.put(entity.getUUID(), selectedAbility);
    }

    //called client sided
    public void previousAbility(LivingEntity entity) {
        if(getAbilityNames().length == 0)
            return;

        if(!selectedAbilities.containsKey(entity.getUUID())) {
            selectedAbilities.put(entity.getUUID(), 0);
        }

        int selectedAbility = selectedAbilities.get(entity.getUUID());
        selectedAbility--;
        if(selectedAbility <= -1) {
            selectedAbility = getAbilityNames().length - 1;
        }
        selectedAbilities.put(entity.getUUID(), selectedAbility);
    }
}
