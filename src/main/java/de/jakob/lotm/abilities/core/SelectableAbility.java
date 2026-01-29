package de.jakob.lotm.abilities.core;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.AbilitySelectionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.UUID;

public abstract class SelectableAbility extends Ability {

    protected final HashMap<UUID, Integer> selectedAbilities = new HashMap<>();

    protected SelectableAbility(String id, float cooldown) {
        super(id, cooldown);
    }

    protected abstract String[] getAbilityNames();

    public String[] getAbilityNamesCopy() {
        return getAbilityNames().clone();
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(entity instanceof Player)) {
            castSelectedAbility(level, entity, random.nextInt(getAbilityNames().length));
            return;
        }

        if(!selectedAbilities.containsKey(entity.getUUID())) {
            selectedAbilities.put(entity.getUUID(), 0);
        }

        int selectedAbility = selectedAbilities.get(entity.getUUID());
        castSelectedAbility(level, entity, selectedAbility);
    }

    protected abstract void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility);

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
        PacketHandler.sendToServer(new AbilitySelectionPacket(getId(), selectedAbility));
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

    public void setSelectedAbility(ServerPlayer player, int selectedAbility) {
        if(getAbilityNames().length == 0)
            return;

        if(selectedAbility < 0 || selectedAbility >= getAbilityNames().length)
            return;

        selectedAbilities.put(player.getUUID(), selectedAbility);
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
        PacketHandler.sendToServer(new AbilitySelectionPacket(getId(), selectedAbility));
    }

//    public void handleLeftClickInAir(Player player) {
//        if (player.level().isClientSide && getAbilityNames().length > 0) {
//            if(ClientAbilityWheelHelper.isWheelOpen() && ClientAbilityWheelHelper.getCurrentAbilityItem() == this) {
//                return;
//            }
//            ClientAbilityWheelHelper.openWheel(this, player);
//        }
//    }
}
