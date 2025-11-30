package de.jakob.lotm.abilities.door.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.dimension.SpiritWorldHandler;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class SpiritWorldAwarenessAbility extends PassiveAbilityItem {
    public SpiritWorldAwarenessAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 5));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        ResourceKey<Level> spiritWorld = ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "spirit_world"));
        ServerLevel spiritWorldLevel = serverLevel.getServer().getLevel(spiritWorld);
        if (spiritWorldLevel == null) {
            return;
        }

        if(entity.level().dimension().equals(ModDimensions.SPIRIT_WORLD_DIMENSION_KEY)) {
            Vec3 posInSpiritWorld = entity.position();
            Vec3 overworldCoords = SpiritWorldHandler.getCoordinatesInOverworld(posInSpiritWorld);
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.spirit_world_awareness.overworld_coordinates").append(": ").append(
                    Component.literal(
                            "(" +
                                    (int) overworldCoords.x + ", " +
                                    (int) overworldCoords.y + ", " +
                                    (int) overworldCoords.z +
                                    ")"
                    )
            ).withColor(0xdb11ed));
        }
    }
}
