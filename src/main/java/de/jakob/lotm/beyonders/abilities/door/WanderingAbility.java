package de.jakob.lotm.beyonders.abilities.door;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.network.packets.toClient.OpenWanderingSelectionScreenPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

public class WanderingAbility extends Ability {
    public WanderingAbility(String id) {
        super(id, 1);

        canBeUsedByNPC = false;
        canBeCopied = false;
        canBeReplicated = false;
        canBeShared = false;
        cannotBeStolen = true;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 3));
    }

    @Override
    public float getSpiritualityCost() {
        return 7000;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel) || !(entity instanceof ServerPlayer player))
            return;

        List<String> dimensionIds = StreamSupport.stream(serverLevel.getServer().getAllLevels().spliterator(), false)
                .filter(s -> !s.dimension().equals(ModDimensions.SEFIRAH_CASTLE_DIMENSION_KEY))
                .filter(s -> !s.dimension().equals(ModDimensions.CONCEALMENT_WORLD_DIMENSION_KEY))
                .filter(s -> !s.dimension().equals(ModDimensions.DREAM_MAZE_DIMENSION_KEY))
                .filter(s -> !s.dimension().equals(ModDimensions.SPACE_DIMENSION_KEY))
                .filter(s -> !s.dimension().equals(serverLevel.dimension()))
                .map(s -> s.dimension().location().toString())
                .toList();

        if (dimensionIds.isEmpty()) {
            ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.translatable("lotmcraft.wandering_ability.no_dimension_found").withColor(0xFF68dff7));
            player.connection.send(packet);
            return;
        }

        PacketDistributor.sendToPlayer(player, new OpenWanderingSelectionScreenPacket(dimensionIds));
    }
}
