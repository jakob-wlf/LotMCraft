package de.jakob.lotm.abilities.wheel_of_fortune;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.wheel_of_fortune.calamities.Calamity;
import de.jakob.lotm.abilities.wheel_of_fortune.calamities.Earthquake;
import de.jakob.lotm.abilities.wheel_of_fortune.calamities.Meteor;
import de.jakob.lotm.abilities.wheel_of_fortune.calamities.Tornado;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class CalamityAttractionAbility extends Ability {
    public CalamityAttractionAbility(String id) {
        super(id, 10);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("wheel_of_fortune", 6));
    }

    @Override
    public float getSpiritualityCost() {
        return 190;
    }

    private final Calamity[] calamities = new Calamity[]{new Tornado(), new Earthquake(), new Meteor()};

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if(entity instanceof ServerPlayer player) {
            Component actionBarText = Component.translatable("ability.lotmcraft.passive_calamity_attraction.approaching_calamity").withColor(0xFFc0f6fc);
            ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(actionBarText);
            player.connection.send(packet);
        }

        Vec3 targetPos = AbilityUtil.getTargetLocation(entity, 14, 2, true);


        ServerScheduler.scheduleDelayed(random.nextInt(31, 60), () -> {
            Calamity calamity = calamities[random.nextInt(calamities.length)];
            calamity.spawnCalamity(serverLevel, targetPos, (float) BeyonderData.getMultiplier(entity), BeyonderData.isGriefingEnabled(entity));
        }, serverLevel);
    }
}
