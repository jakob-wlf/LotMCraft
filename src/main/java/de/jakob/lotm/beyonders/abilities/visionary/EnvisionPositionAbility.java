package de.jakob.lotm.beyonders.abilities.visionary;

import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.OpenEnvisionLocationScreenS2CPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.TeleportationUtil;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EnvisionPositionAbility extends SelectableAbility {

    public static final HashMap<UUID, BlockPos> coords = new HashMap<>();

    public EnvisionPositionAbility(String id) {
        super(id, 0.5f);

        canBeCopied = false;
        canBeUsedInArtifact = false;
        canBeReplicated = false;
        canBeUsedByNPC = false;
        cannotBeStolen = true;
        canBeShared = false;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.envision_position_ability.on_sight",
                "ability.lotmcraft.envision_position_ability.coordinates"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        switch (selectedAbility){
            case 0 -> onSight(level, entity);
            case 1 -> onCoords(level, entity);
        }
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 0));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1000;
    }

    private void onSight(Level level, LivingEntity entity){
        if(!(level instanceof ServerLevel serverLevel)) return;

        Vec3 targetLoc = AbilityUtil.getTargetBlock(entity, getDistancePerSeq(BeyonderData.getSequence(entity)), true).getCenter().add(0, 1, 0);
        level.playSound(null, targetLoc.x, targetLoc.y, targetLoc.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, .5f, 1);

        var validatedPos = TeleportationUtil.clampToBorder(serverLevel, targetLoc);

        entity.teleportTo(validatedPos.x, validatedPos.y, validatedPos.z);
    }

    private void onCoords(Level level, LivingEntity entity){
        if(!(level instanceof ServerLevel serverLevel)) return;
        if(!(entity instanceof ServerPlayer player)) return;

        PacketHandler.sendToPlayer(player, new OpenEnvisionLocationScreenS2CPacket());
    }

    private static double getDistancePerSeq(int seq){
        return (1 << (9 - seq));
    }
}
