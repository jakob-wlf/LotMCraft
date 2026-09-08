package de.jakob.lotm.beyonders.abilities.red_priest;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SteelMasteryAbility extends SelectableAbility {
    SteelSkinAbility skinAbility = null;

    private final static DustParticleOptions dust = new DustParticleOptions(new Vector3f(0.3f, 0.3f, 0.3f), 2.25f);

    public SteelMasteryAbility(String id) {
        super(id, 2);

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(1, 1, 1, 2, 3));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(2000f, 800f, 530f, 400f, 390f));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("red_priest", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 500;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.steel_mastery.steel_skin", "ability.lotmcraft.steel_mastery.steel_chains"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(level.isClientSide)
            return;
        switch (abilityIndex) {
            case 0 -> steelSkin((ServerLevel) level, entity);
            case 1 -> steelChains((ServerLevel) level, entity);
        }
    }

    private void steelChains(ServerLevel level, LivingEntity entity) {
        LivingEntity target = AbilityUtil.getTargetEntity(entity, baseDistance, 2);

        if(target == null) {
            if(entity instanceof ServerPlayer player) {
                Component actionBar = Component.translatable("ability.lotmcraft.steel_mastery.no_target").withColor(0xFF422a2a);
                sendActionBar(player, actionBar);
            }
            return;
        }

        Location loc = new Location(target.position(), target.level());

        AtomicReference<UUID> taskIdRef = new AtomicReference<>();

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        UUID taskId = ServerScheduler.scheduleForDuration(0, 5, (int) (20 * 4), () -> {
            if(entity.isDeadOrDying())
                return;

            // Blink Escape - only the bound entity can free itself
            if(InteractionHandler.isInteractionPossibleForEntity(loc, "blink_escape", entitySeq, target)) {
                ServerScheduler.cancel(taskIdRef.get());

                target.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                return;
            }

            target.setDeltaMovement(new Vec3(0, 0, 0));
            target.hurtMarked = true;
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 20, false, false, false));

            Vec3 entityPos = target.position().add(0, 1.25, 0);
            ParticleUtil.drawParticleLine(level, dust, entityPos, entityPos.add(0, -1.5, 3), .35, 1);
            ParticleUtil.drawParticleLine(level, dust, entityPos, entityPos.add(0, -1.5, -3), .35, 1);
            ParticleUtil.drawParticleLine(level, dust, entityPos, entityPos.add(3, -1.5, 0), .35, 1);
            ParticleUtil.drawParticleLine(level, dust, entityPos, entityPos.add(-3, -1.5, 0), .35, 1);

            loc.setLevel(target.level());
            loc.setPosition(target.position());
        }, null, level, () -> AbilityUtil.getTimeInArea(entity, new Location(target.position(), level)));
        taskIdRef.set(taskId);
    }

    private static void sendActionBar(ServerPlayer player, Component message) {
        ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(message);
        player.connection.send(packet);
    }

    private void steelSkin(ServerLevel level, LivingEntity entity) {
        if(skinAbility == null)
            skinAbility = (SteelSkinAbility) LOTMCraft.abilityHandler.getById("steel_skin");

        if(skinAbility == null) return;

        skinAbility.useAbility((ServerLevel) level, entity);
    }
}