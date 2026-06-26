package de.jakob.lotm.beyonders.abilities.darkness;

import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForgeMod;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdentityConcealmentAbility extends SelectableAbility {

    public IdentityConcealmentAbility(String id) {
        super(id, 5);
        this.canBeCopied = false;
        canBeReplicated = false;
        canBeUsedInArtifact = false;
        autoClear = false;
        cannotBeStolen = true;
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("darkness", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 3000;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.identityconcealment.conceal_identity"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (!(entity instanceof Player)) abilityIndex = 0;

        switch(abilityIndex) {
            case 0 -> concealIdentity(level, entity);
        }
    }

    private void concealIdentity(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(entity instanceof ServerPlayer caster)) return;

        level.playSound(null,
                caster.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.BLOCKS,
                10.0f,
                1.0f);

        LivingEntity targetEntity = AbilityUtil.getTargetEntity(entity, 16, 2);

        ServerPlayer targetPlayer = (targetEntity instanceof ServerPlayer sp) ? sp : caster;

        AttributeInstance attribute = targetPlayer.getAttribute(NeoForgeMod.NAMETAG_DISTANCE);
        if (attribute != null) {
            if (attribute.getValue() == 0) {
                attribute.setBaseValue(64);

                if (targetPlayer.getServer() != null) {
                    targetPlayer.getServer().getPlayerList().broadcastAll(
                            new ClientboundPlayerInfoUpdatePacket(
                                    EnumSet.of(
                                            ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,
                                            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED,
                                            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY,
                                            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE,
                                            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME
                                    ),
                                    List.of(targetPlayer)
                            )
                    );
                }
            } else {
                attribute.setBaseValue(0);

                if (targetPlayer.getServer() != null) {
                    targetPlayer.getServer().getPlayerList().broadcastAll(
                            new ClientboundPlayerInfoRemovePacket(List.of(targetPlayer.getUUID()))
                    );
                }
            }
        }
    }
}