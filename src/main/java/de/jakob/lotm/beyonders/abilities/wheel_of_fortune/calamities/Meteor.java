package de.jakob.lotm.beyonders.abilities.wheel_of_fortune.calamities;

import de.jakob.lotm.entity.custom.ability_entities.MeteorEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class Meteor extends Calamity{
    @Override
    public Component getName() {
        return Component.translatable("lotm.calamity.tornado");
    }

    @Override
    protected void spawnScaledCalamity(ServerLevel level, Vec3 position, float damageMultiplier, boolean griefing, float rangeScale) {
        MeteorEntity meteor = new MeteorEntity(
                level, 1.6f, 15 * damageMultiplier, 2, null, griefing, 7 * rangeScale, 12 * rangeScale);
        meteor.setPosition(position);
        level.addFreshEntity(meteor);
    }
}
