package de.jakob.lotm.abilities.death;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.abilities.common.SpiritVisionAbility;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.entity.custom.spirits.SpiritBaneEntity;
import de.jakob.lotm.entity.custom.spirits.SpiritBizarroBaneEntity;
import de.jakob.lotm.entity.custom.spirits.SpiritBlueWizardEntity;
import de.jakob.lotm.entity.custom.spirits.SpiritBubblesEntity;
import de.jakob.lotm.entity.custom.spirits.SpiritDervishEntity;
import de.jakob.lotm.entity.custom.spirits.SpiritGhostEntity;
import de.jakob.lotm.entity.custom.spirits.SpiritMalmouthEntity;
import de.jakob.lotm.entity.custom.spirits.SpiritTranslucentWizardEntity;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncEyeOfDeathAbilityPacket;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AbilityUtilClient;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class EyeOfDeathAbility extends ToggleAbility {

    /** Players with Eye of Death currently active (server-side). */
    public static final HashSet<UUID> activePlayers = new HashSet<>();


    public EyeOfDeathAbility(String id) {
        super(id);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("death", 8));
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            activePlayers.add(entity.getUUID());
            if (entity instanceof ServerPlayer player) {
                PacketHandler.sendToPlayer(player, new SyncEyeOfDeathAbilityPacket(true, -1));
            }
            return;
        }
        entity.playSound(SoundEvents.ENCHANTMENT_TABLE_USE, 1, 1);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (!level.isClientSide() && InteractionHandler.isInteractionPossibleStrictlyHigher(new Location(entity.position(), (ServerLevel) level), "purification", BeyonderData.getSequence(entity), -1)) {
            stop(level, entity);
            return;
        }

        if (level.isClientSide()) {
            return;
        }
        if (!(entity instanceof ServerPlayer player)) return;

        LivingEntity lookedAt = AbilityUtil.getTargetEntity(entity, 40, 1.2f);
        PacketHandler.sendToPlayer(player, new SyncEyeOfDeathAbilityPacket(true, lookedAt == null ? -1 : lookedAt.getId()));

        entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 25, 1, false, false, false));
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if (level.isClientSide) {
            entity.playSound(SoundEvents.ENCHANTMENT_TABLE_USE, 1, 1);
        } else {
            activePlayers.remove(entity.getUUID());
            if (!(entity instanceof ServerPlayer player)) return;

            player.removeEffect(MobEffects.NIGHT_VISION);

            PacketHandler.sendToPlayer(player, new SyncEyeOfDeathAbilityPacket(false, -1));
        }
    }

    @Override
    protected float getSpiritualityCost() {
        return 0.5f;
    }

    public static boolean isSpiritEntity(LivingEntity entity) {
        return entity instanceof SpiritBaneEntity
                || entity instanceof SpiritBizarroBaneEntity
                || entity instanceof SpiritBlueWizardEntity
                || entity instanceof SpiritBubblesEntity
                || entity instanceof SpiritDervishEntity
                || entity instanceof SpiritGhostEntity
                || entity instanceof SpiritMalmouthEntity
                || entity instanceof SpiritTranslucentWizardEntity;
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        if (!activePlayers.contains(attacker.getUUID())) return;

        LivingEntity target = event.getEntity();
        boolean isUndead = target.getType().is(EntityTypeTags.UNDEAD);
        boolean isSpirit = isSpiritEntity(target);

        if (isUndead || isSpirit) {
            event.setAmount(event.getAmount() * 1.35f);
        }
    }
}
