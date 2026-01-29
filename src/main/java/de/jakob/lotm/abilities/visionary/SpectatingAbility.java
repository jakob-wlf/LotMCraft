package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.abilities.ToggleAbilityItem;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncSpectatingAbilityPacket;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class SpectatingAbility extends ToggleAbility {
    public SpectatingAbility(String id) {
        super(id);

        canBeUsedByNPC = false;
    }

    @Override
    public float getSpiritualityCost() {
        return .125f;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 9));
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if(!level.isClientSide) {
            if(entity instanceof ServerPlayer player) {
                PacketHandler.sendToPlayer(player, new SyncSpectatingAbilityPacket(true, -1));
            }
            return;
        }

        entity.playSound(SoundEvents.AMETHYST_BLOCK_BREAK, 3, .01f);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(!(entity instanceof ServerPlayer player) || level.isClientSide)
            return;

        LivingEntity lookedAt = AbilityUtil.getTargetEntity(entity, 40, 1.2f);

        PacketHandler.sendToPlayer(player, new SyncSpectatingAbilityPacket(true, lookedAt == null ? -1 : lookedAt.getId()));

        entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 17, 1, false, false, false));

        Component message = Component.translatable("item.lotmcraft.spectating_ability");
        player.displayClientMessage(message, true);
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if(!level.isClientSide) {
            if(entity instanceof ServerPlayer player) {
                PacketHandler.sendToPlayer(player, new SyncSpectatingAbilityPacket(false, -1));
            }
            return;
        }

    }
}
