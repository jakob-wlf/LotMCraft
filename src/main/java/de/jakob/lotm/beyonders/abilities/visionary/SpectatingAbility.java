package de.jakob.lotm.beyonders.abilities.visionary;

import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncSpectatingAbilityS2CPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
        autoClear = false;
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
        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        if(VisionaryHandler.shouldBeAffectedWithMindWorldSeal(entitySeq)){
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.mind_world_authority_ability.is_sealed")
                            .withColor(0xFFff124d));
            return;
        }

        if(!level.isClientSide) {
            if(entity instanceof ServerPlayer player) {
                PacketHandler.sendToPlayer(player, new SyncSpectatingAbilityS2CPacket(true, -1));
            }
            return;
        }

        entity.playSound(SoundEvents.AMETHYST_BLOCK_BREAK, 3, .01f);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(!(entity instanceof ServerPlayer player) || level.isClientSide)
            return;

        LivingEntity lookedAt = AbilityUtil.getTargetEntity(entity, 40, 1.2f, true, true);

        int seq = AbilityUtil.getSeqWithArt(entity, this);
        if(lookedAt != null) {
            if (VisionaryHandler.shouldStayInvisible(seq, lookedAt)){
                return;
            }
            else if(VisionaryHandler.shouldFailAndTrigger(seq, entity, lookedAt, this, false)){
                return;
            }
            else if(AbilityUtil.isTargetSignificantlyStronger(seq, BeyonderData.getSequence(lookedAt))){
                return;
            }
        }

        if(VisionaryHandler.shouldBeAffectedWithMindWorldSeal(seq)){
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.mind_world_authority_ability.is_sealed")
                            .withColor(0xFFff124d));
            cancel((ServerLevel) level, player);
            return;
        }

        PacketHandler.sendToPlayer(player, new SyncSpectatingAbilityS2CPacket(true, lookedAt == null ? -1 : lookedAt.getId()));

        entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 17, 1, false, false, false));
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if(!level.isClientSide) {
            if(entity instanceof ServerPlayer player) {
                PacketHandler.sendToPlayer(player, new SyncSpectatingAbilityS2CPacket(false, -1));
            }

            clearArtifactScaling(entity);
        }

    }
}
