package de.jakob.lotm.beyonders.abilities.wheel_of_fortune.calamities;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public abstract class Calamity {
    public abstract Component getName();

    public final void spawnCalamity(ServerLevel level, Vec3 position, float multiplier, boolean griefing) {
        spawnScaledCalamity(level, position, multiplier, griefing, 1);
    }

    public final void spawnCalamity(ServerLevel level, Vec3 position, float multiplier, boolean griefing, int sequence) {
        spawnScaledCalamity(
                level,
                position,
                multiplier * getDamageScale(sequence),
                griefing,
                getRangeScale(sequence));
    }

    protected abstract void spawnScaledCalamity(
            ServerLevel level, Vec3 position, float damageMultiplier, boolean griefing, float rangeScale);

    public static float getDamageScale(int sequence) {
        return switch (sequence) {
            case 6 -> 1;
            case 5 -> 1.1f;
            case 4 -> 1.25f;
            case 3 -> 1.45f;
            case 2 -> 1.7f;
            case 1 -> 2;
            case 0 -> 2.4f;
            default -> 1;
        };
    }

    public static float getRangeScale(int sequence) {
        return switch (sequence) {
            case 6 -> 1;
            case 5 -> 1.1f;
            case 4 -> 1.2f;
            case 3 -> 1.35f;
            case 2 -> 1.5f;
            case 1 -> 1.7f;
            case 0 -> 2;
            default -> 1;
        };
    }
}
