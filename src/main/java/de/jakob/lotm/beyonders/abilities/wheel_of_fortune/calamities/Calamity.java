package de.jakob.lotm.beyonders.abilities.wheel_of_fortune.calamities;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public abstract class Calamity {
    protected boolean isEnvisioned = false;

    public abstract Component getName();
    public abstract void spawnCalamity(ServerLevel level, Vec3 position, float damage , boolean griefing, @Nullable LivingEntity caster);

    public void setEnvisioned(boolean value){
        isEnvisioned = value;
    }
}
