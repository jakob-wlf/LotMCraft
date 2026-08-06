package de.jakob.lotm.beyonders.abilities.wheel_of_fortune.calamities;

import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.TornadoEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class Tornado extends Calamity{
    @Override
    public Component getName() {
        return Component.translatable("lotm.calamity.tornado");
    }

    @Override
    public void spawnCalamity(ServerLevel level, Vec3 position, float damage, boolean griefing, @Nullable LivingEntity caster) {
        TornadoEntity tornado = new TornadoEntity(ModEntities.TORNADO.get(), level, .4f, damage, caster);
        tornado.setPos(position);
        tornado.setEnvisioned(isEnvisioned);
        level.addFreshEntity(tornado);
    }
}
