package de.jakob.lotm.abilities.death;

import de.jakob.lotm.abilities.common.DivinationAbility;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.util.PlayerSelectionWorkType;
import de.jakob.lotm.util.data.EntityLocation;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.OpenPlayerDivinationScreenPacket;
import de.jakob.lotm.network.packets.toClient.OpenStructureDivinationScreenPacket;
import de.jakob.lotm.network.packets.toClient.SyncDangerPremonitionAbilityPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.PlayerInfo;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class SpiritCommunicationAbility extends SelectableAbility {

    public SpiritCommunicationAbility(String id) {
        super(id, 7f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("death", 8));
    }

    @Override
    protected float getSpiritualityCost() {
        return 10;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.divination.danger_premonition",
                "ability.lotmcraft.divination.structure_divination",
                "ability.lotmcraft.divination.player_divination",
                "ability.lotmcraft.spirit_communication.spectral_bind"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (!level.isClientSide && level instanceof net.minecraft.server.level.ServerLevel serverLevel
                && InteractionHandler.isInteractionPossibleStrictlyHigher(new Location(entity.position(), serverLevel), "purification", BeyonderData.getSequence(entity), -1)) return;

        switch (abilityIndex) {
            case 0 -> dangerPremonition(level, entity);
            case 1 -> structureDivination(level, entity);
            case 2 -> playerDivination(level, entity);
            case 3 -> spectralBind(level, entity);
        }
    }

    // --- Divination sub-modes (copied from DivinationAbility) ---

    private void dangerPremonition(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        if (DivinationAbility.dangerPremonitionActive.contains(entity.getUUID())) {
            DivinationAbility.dangerPremonitionActive.remove(entity.getUUID());
            return;
        }

        if (!(entity instanceof ServerPlayer player)) return;

        DivinationAbility.dangerPremonitionActive.add(entity.getUUID());
        PacketHandler.sendToPlayer(player, new SyncDangerPremonitionAbilityPacket(true));

        AtomicBoolean stop = new AtomicBoolean(false);
        ServerScheduler.scheduleUntil((ServerLevel) level, () -> {
            if (!DivinationAbility.dangerPremonitionActive.contains(entity.getUUID())) {
                stop.set(true);
            }
            if (BeyonderData.getSpirituality(player) < 2) {
                DivinationAbility.dangerPremonitionActive.remove(entity.getUUID());
                stop.set(true);
            }
            if (stop.get()) {
                PacketHandler.sendToPlayer(player, new SyncDangerPremonitionAbilityPacket(false));
            }
            BeyonderData.reduceSpirituality(player, .5f);
        }, 2, null, stop);
    }

    private void playerDivination(Level level, LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) return;
        var server = player.getServer();
        if (server == null) return;

        List<PlayerInfo> players = server.getPlayerList().getPlayers().stream()
                .filter(p -> p != player)
                .map(p -> new PlayerInfo(p.getUUID(), p.getGameProfile().getName()))
                .toList();

        PacketDistributor.sendToPlayer(player, new OpenPlayerDivinationScreenPacket(players, PlayerSelectionWorkType.DIVINATION));
    }

    private void structureDivination(Level level, Entity entity) {
        if (!(entity instanceof ServerPlayer player)) return;

        Registry<Structure> registry = player.serverLevel().registryAccess()
                .registry(Registries.STRUCTURE).orElseThrow();

        List<String> structureIds = registry.holders()
                .map(holder -> holder.key().location().toString())
                .sorted()
                .toList();

        PacketDistributor.sendToPlayer(player, new OpenStructureDivinationScreenPacket(structureIds));
    }


    private void spectralBind(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20* (int) Math.max(multiplier(entity)/4,1), 1.5f);
        if (target == null) return;

        int casterSeq = BeyonderData.getSequence(entity);
        int targetSeq = BeyonderData.getSequence(target);
        if (targetSeq <= casterSeq - 1) return;

        ParticleUtil.createParticleSpirals(ParticleTypes.LARGE_SMOKE, new EntityLocation(target), target.getBbWidth(), target.getBbWidth(), target.getEyeHeight(), .35, 6, 20 * 5, 8, 1);

        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60* (int) Math.max(multiplier(entity)/4,1), 100, false, true, true));
        target.setTicksFrozen(target.getTicksRequiredToFreeze() + 60);
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60* (int) Math.max(multiplier(entity)/4,1), 1, false, true, true));
    }
}
