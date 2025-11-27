package de.jakob.lotm.abilities;

import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.AbilitySelectionPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

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
    public boolean useAsArtifactAbility(Level level, Player player) {
        if(level.isClientSide) {
            if(this.cooldown > 0 && cooldownsClient.containsKey(player.getUUID()) && (System.currentTimeMillis() - cooldownsClient.get(player.getUUID())) < (this.cooldown * 50L)) {
                return false;
            }
        }
        else {
            if(this.cooldown > 0 && cooldowns.containsKey(player.getUUID()) && (System.currentTimeMillis() - cooldowns.get(player.getUUID())) < (this.cooldown * 50L)) {
                return false;
            }
        }

        if(getAbilityNames().length == 0) {
            return false;
        }

        if(!level.isClientSide)
            AbilityHandler.useAbilityInArea(this, new Location(player.position(), level));

        if(level.isClientSide) cooldownsClient.put(player.getUUID(), System.currentTimeMillis());
        else cooldowns.put(player.getUUID(), System.currentTimeMillis());

        useAbility(level, player, random.nextInt(getAbilityNames().length));
        return true;
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

    @Override
    public @NotNull Component getName(ItemStack stack) {
        return Component.translatable(this.getDescriptionId(stack)).append(Component.literal(" (")).append(Component.translatable("lotm.selectable")).append(Component.literal(")"));
    }

    public void setSelectedAbility(ServerPlayer player, int selectedAbility) {
        if(getAbilityNames().length == 0)
            return;

        if(selectedAbility < 0 || selectedAbility >= getAbilityNames().length)
            return;

        selectedAbilities.put(player.getUUID(), selectedAbility);
    }
}
