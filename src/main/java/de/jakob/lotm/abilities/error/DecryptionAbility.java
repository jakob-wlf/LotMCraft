package de.jakob.lotm.abilities.error;

import de.jakob.lotm.abilities.ToggleAbilityItem;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncDecryptionLookedAtEntitiesAbilityPacket;
import de.jakob.lotm.network.packets.toClient.SyncSpectatingAbilityPacket;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.mixin.EntityAccessor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.*;

public class DecryptionAbility extends ToggleAbilityItem {

    public DecryptionAbility(Properties properties) {
        super(properties);

        canBeCopied = false;
        canBeUsedByNPC = false;
    }

    @Override
    protected void start(Level level, LivingEntity entity) {
        if(!level.isClientSide) {
            if(entity instanceof ServerPlayer player) {
                PacketHandler.sendToPlayer(player, new SyncDecryptionLookedAtEntitiesAbilityPacket(true, -1));
            }
            return;
        }

        entity.playSound(SoundEvents.BELL_RESONATE, 1, 1);
    }

    @Override
    protected void tick(Level level, LivingEntity entity) {
        if(!(entity instanceof ServerPlayer player) || level.isClientSide)
            return;

        LivingEntity lookedAt = AbilityUtil.getTargetEntity(entity, 40, 1.2f);

        PacketHandler.sendToPlayer(player, new SyncDecryptionLookedAtEntitiesAbilityPacket(true, lookedAt == null ? -1 : lookedAt.getId()));

        entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 17, 1, false, false, false));

        Component message = Component.translatable("item.lotmcraft.decryption_ability").withColor(0x5cff68);
        player.displayClientMessage(message, true);
    }

    @Override
    protected void stop(Level level, LivingEntity entity) {
        if(!level.isClientSide) {
            if(entity instanceof ServerPlayer player) {
                PacketHandler.sendToPlayer(player, new SyncDecryptionLookedAtEntitiesAbilityPacket(false, -1));
            }
            return;
        }

    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "error", 7
        ));
    }

    @Override
    protected float getSpiritualityCost() {
        return .25f;
    }

}
