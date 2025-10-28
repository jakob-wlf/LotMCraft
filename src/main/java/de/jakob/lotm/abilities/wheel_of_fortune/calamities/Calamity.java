package de.jakob.lotm.abilities.wheel_of_fortune.calamities;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public abstract class Calamity {
    public abstract Component getName();
    public abstract void spawnCalamity(ServerLevel level, Vec3 position, float multiplier, boolean griefing);
}
