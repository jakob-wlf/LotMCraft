package de.jakob.lotm.abilities.wheel_of_fortune.calamities;

import de.jakob.lotm.entity.custom.MeteorEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class Meteor extends Calamity{
    @Override
    public Component getName() {
        return Component.translatable("lotm.calamity.tornado");
    }

    @Override
    public void spawnCalamity(ServerLevel level, Vec3 position, float multiplier, boolean griefing) {
        MeteorEntity meteor = new MeteorEntity(level, 1.6f, 15 * multiplier, 2, null, griefing, 7, 12);
        meteor.setPos(position);
        level.addFreshEntity(meteor);
    }
}
