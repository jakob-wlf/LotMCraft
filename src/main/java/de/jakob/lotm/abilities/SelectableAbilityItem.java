package de.jakob.lotm.abilities;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.AbilitySelectionPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import net.minecraft.server.level.ServerPlayer;
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
    public void useAsNpcAbility(Level level, LivingEntity beyonderNPC) {
        if(!this.canBeUsedByNPC)
            return;

        if(this.cooldown > 0 && cooldowns.containsKey(beyonderNPC.getUUID()) && (System.currentTimeMillis() - cooldowns.get(beyonderNPC.getUUID())) < (this.cooldown * 50L)) {
            return;
        }

        if(BeyonderData.isAbilityDisabled(beyonderNPC))
            return;

        if(!level.isClientSide)
            AbilityHandler.useAbilityInArea(this, new Location(beyonderNPC.position(), level));

        cooldowns.put(beyonderNPC.getUUID(), System.currentTimeMillis());

        if(getAbilityNames().length == 0)
            return;

        useAbility(level, beyonderNPC, random.nextInt(getAbilityNames().length));
    }

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
        PacketHandler.sendToServer(new AbilitySelectionPacket(this, selectedAbility));
    }

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
        PacketHandler.sendToServer(new AbilitySelectionPacket(this, selectedAbility));
    }

    public void setSelectedAbility(ServerPlayer player, int selectedAbility) {
        if(getAbilityNames().length == 0)
            return;

        if(selectedAbility < 0 || selectedAbility >= getAbilityNames().length)
            return;

        selectedAbilities.put(player.getUUID(), selectedAbility);
    }
}
