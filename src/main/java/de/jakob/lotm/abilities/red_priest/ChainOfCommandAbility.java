package de.jakob.lotm.abilities.red_priest;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.subordinates.SubordinateUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class ChainOfCommandAbility extends AbilityItem {
    public ChainOfCommandAbility(Properties properties) {
        super(properties, 5);

        canBeCopied = false;
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("red_priest", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 900;
    }

    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(.2f, .1f, .1f), 2);

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if(!(entity instanceof Player player)) {
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 3, 1.5f);
        if(target == null || target instanceof Player) {
            if(entity instanceof ServerPlayer serverPlayer) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.translatable("ability.lotmcraft.chain_of_command.no_entity_found").withColor(0xFFff124d));
                serverPlayer.connection.send(packet);
            }
            return;
        }

        if(!BeyonderData.isBeyonder(target) || BeyonderData.getSequence(target) > BeyonderData.getSequence(entity)) {
            SubordinateUtils.turnEntityIntoSubordinate(target, player);
            ParticleUtil.spawnParticles(serverLevel, dust, target.position().add(0, target.getEyeHeight() / 2, 0), 95, .5, target.getEyeHeight() / 2, .5, 0);
        }
        else {
            ParticleUtil.spawnParticles(serverLevel, dust, target.position().add(0, target.getEyeHeight() / 2, 0), 95, .5, target.getEyeHeight() / 2, .5, 0);
            if(entity instanceof ServerPlayer serverPlayer) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.translatable("ability.lotmcraft.chain_of_command.no_entity_found").withColor(0xFFff124d));
                serverPlayer.connection.send(packet);
            }
        }
    }
}
