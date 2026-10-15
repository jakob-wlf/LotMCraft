package de.jakob.lotm.beyonders.abilities.death;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.common.DangerPremonitionDivinationAbility;
import de.jakob.lotm.beyonders.abilities.common.DivinationAbility;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.util.data.PlayerSelectionWorkType;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class SpiritCommunicationAbility extends SelectableAbility {

    private DangerPremonitionDivinationAbility divinationAbility;

    public SpiritCommunicationAbility(String id) {
        super(id, 7f);
        canBeCopied = false;
        canBeReplicated = false;
        cannotBeStolen = true;
        canBeUsedInArtifact = false;
        canBeShared = false;
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
        if(level.isClientSide) return;
        if(divinationAbility == null)
            divinationAbility = (DangerPremonitionDivinationAbility) LOTMCraft.abilityHandler.getById("danger_premonition_divination_ability");

        if(divinationAbility == null) return;

        divinationAbility.useAbility((ServerLevel) level, entity);
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

        LivingEntity target = AbilityUtil.getTargetEntity(entity, (int) (20* multiplier(entity)), 1.5f);
        if (target == null) return;

        int casterSeq = BeyonderData.getSequence(entity);
        int targetSeq = BeyonderData.getSequence(target);
        if (targetSeq <= casterSeq - 1) return;

        ParticleUtil.createParticleSpirals(ParticleTypes.LARGE_SMOKE, new EntityLocation(target), target.getBbWidth(), target.getBbWidth(), target.getEyeHeight(), .35, 6, 20 * 5, 8, 1);

        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (int) (60* multiplier(entity)), 100, false, true, true));
        target.setTicksFrozen(target.getTicksRequiredToFreeze() + 60);
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, (int) (60* multiplier(entity)), 1, false, true, true));
    }
}
