package de.jakob.lotm.abilities.wheel_of_fortune.calamities;

import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.TornadoEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class Tornado extends Calamity{
    @Override
    public Component getName() {
        return Component.translatable("lotm.calamity.tornado");
    }

    @Override
    public void spawnCalamity(ServerLevel level, Vec3 position, float multiplier) {
        TornadoEntity tornado = new TornadoEntity(ModEntities.TORNADO.get(), level, .4f, 16 * multiplier, null);
        tornado.setPos(position);
        level.addFreshEntity(tornado);
    }
}
