package de.jakob.lotm.beyonders.abilities.wheel_of_fortune.calamities;

import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.TornadoEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class Tornado extends Calamity{
    @Override
    public Component getName() {
        return Component.translatable("lotm.calamity.tornado");
    }

    @Override
    protected void spawnScaledCalamity(ServerLevel level, Vec3 position, float damageMultiplier, boolean griefing, float rangeScale) {
        TornadoEntity tornado = new TornadoEntity(
                ModEntities.TORNADO.get(), level, .4f, 16 * damageMultiplier, null, null, rangeScale);
        tornado.setPos(position);
        level.addFreshEntity(tornado);
    }
}
