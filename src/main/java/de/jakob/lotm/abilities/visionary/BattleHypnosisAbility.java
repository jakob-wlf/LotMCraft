package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.abilities.demoness.CharmAbility;
import de.jakob.lotm.abilities.visionary.passives.MetaAwarenessAbility;
import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.AbilitySelectionPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BattleHypnosisAbility extends SelectableAbility {
    public BattleHypnosisAbility(String id) {
        super(id, 2);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 6));
    }

    @Override
    public float getSpiritualityCost() {
        return 250;
    }

    private final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(250 / 255f, 201 / 255f, 102 / 255f),
            1.25f
    );

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.battle_hypnosis.single",
                "ability.lotmcraft.battle_hypnosis.aoe"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        switch (selectedAbility){
            case 0 -> single(level, entity);
            case 1 -> aoe(level, entity);
        }
    }

    private void single(Level level, LivingEntity entity){
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20* (int) Math.max(multiplier(entity)/4,1), 2);

        if(target == null) {
            if(entity instanceof ServerPlayer player) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.translatable("ability.lotmcraft.frenzy.no_target").withColor(0xFFff124d));
                player.connection.send(packet);
            }
            return;
        }

        if(level.isClientSide) {
            ParticleUtil.createParticleSpirals((ClientLevel) level, dust, target.position(), target.getBbWidth() + .25, target.getBbWidth() + .25, target.getEyeHeight(), 1, 5, 30, 15, 1);
            return;
        }


        // BH vs Charm: if BH caster has lower or equal sequence, BH prevails and removes charm
        UUID charmCasterUUID = CharmAbility.getCharmed().get(target.getUUID());

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        int targetSeq = BeyonderData.getSequence(target);
        if(BeyonderData.getPathway(target).equals("visionary") && targetSeq < entitySeq){
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.dream_traversal.failed").withColor(0xFFff124d));

            if(targetSeq <= 1 && target instanceof ServerPlayer targetPlayer && entity instanceof ServerPlayer entityPlayer){
                MetaAwarenessAbility.onDivined(entityPlayer, targetPlayer);
            }

            return;
        }

        if(charmCasterUUID != null) {
            Entity charmCasterEntity = ((ServerLevel) level).getEntity(charmCasterUUID);
            int charmCasterSeq = charmCasterEntity instanceof LivingEntity livingCharmCaster ? BeyonderData.getSequence(livingCharmCaster) : LOTMCraft.NON_BEYONDER_SEQ;
            if(entitySeq <= charmCasterSeq) {
                CharmAbility.removeCharm(target.getUUID());
            }
        }

        switch (random.nextInt(3)) {
            case 0 -> freezeTarget((ServerLevel) level, entity, target);
            case 1 -> weakenAndMoveAroundTarget((ServerLevel) level, entity, target);
            case 2 -> stopBeyonderPowersForTarget((ServerLevel) level, entity, target);
        }
    }

    private void aoe(Level level, LivingEntity entity){
        if(level.isClientSide) {
            ParticleUtil.spawnParticles((ClientLevel) level, dust, entity.position(), 1300, 17, 3, 17, 0);
            return;
        }

        var nearby = AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, entity.position(), 20 * multiplier(entity));

        for(var target : nearby) {
            // BH vs Charm: if BH caster has lower or equal sequence, BH prevails and removes charm
            UUID charmCasterUUID = CharmAbility.getCharmed().get(target.getUUID());

            int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
            int targetSeq = BeyonderData.getSequence(target);
            if(BeyonderData.getPathway(target).equals("visionary") && targetSeq < entitySeq){
                AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.dream_traversal.failed").withColor(0xFFff124d));

                if(targetSeq <= 1 && target instanceof ServerPlayer targetPlayer && entity instanceof ServerPlayer entityPlayer){
                    MetaAwarenessAbility.onDivined(entityPlayer, targetPlayer);
                }

                return;
            }

            if (charmCasterUUID != null) {
                Entity charmCasterEntity = ((ServerLevel) level).getEntity(charmCasterUUID);
                int charmCasterSeq = charmCasterEntity instanceof LivingEntity livingCharmCaster ? BeyonderData.getSequence(livingCharmCaster) : LOTMCraft.NON_BEYONDER_SEQ;
                if (entitySeq <= charmCasterSeq) {
                    CharmAbility.removeCharm(target.getUUID());
                }
            }

            switch (random.nextInt(3)) {
                case 0 -> freezeTarget((ServerLevel) level, entity, target);
                case 1 -> weakenAndMoveAroundTarget((ServerLevel) level, entity, target);
                case 2 -> stopBeyonderPowersForTarget((ServerLevel) level, entity, target);
            }
        }
    }

    private void stopBeyonderPowersForTarget(ServerLevel level, LivingEntity entity, LivingEntity target) {
        if(!BeyonderData.isBeyonder(target)) {
            switch (random.nextInt(2)) {
                case 0 -> weakenAndMoveAroundTarget(level, entity, target);
                case 1 -> freezeTarget(level, entity, target);
            }
            return;
        }

        AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.battle_hypnosis.stop_beyonder_powers").withColor(0xf5c56c));

        DisabledAbilitiesComponent component = target.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
        component.disableAbilityUsageForTime("battle_hypnosis_disable_beyonder_powers", 20 * 9, target);
    }

    private void weakenAndMoveAroundTarget(ServerLevel level, LivingEntity entity, LivingEntity target) {
        AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.battle_hypnosis.weaken").withColor(0xf5c56c));

        BeyonderData.addModifier(target, "battle_hypnosis_weaken", .4);
        ServerScheduler.scheduleDelayed(20 * 12, () -> BeyonderData.removeModifier(target, "battle_hypnosis_weaken"));

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        final UUID[] taskIdHolder = new UUID[1];
        taskIdHolder[0] = ServerScheduler.scheduleForDuration(0, 5, 20 * 8, () -> {
            if(InteractionHandler.isInteractionPossible(new Location(target.position(), level), "purification", entitySeq)) {
                target.removeEffect(MobEffects.WEAKNESS);
                BeyonderData.removeModifier(target, "battle_hypnosis_weaken");
                if(taskIdHolder[0] != null) ServerScheduler.cancel(taskIdHolder[0]);
                return;
            }

            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20, 5, false, false, true));

            target.setDeltaMovement(new Vec3((random.nextDouble() - .5) * 2, (random.nextDouble() - .5) * .15, (random.nextDouble() - .5) * 2).scale(.75));
            target.hurtMarked = true;
        }, level);
    }

    private void freezeTarget(ServerLevel level, LivingEntity entity, LivingEntity target) {
        AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.battle_hypnosis.stop").withColor(0xf5c56c));

        DisabledAbilitiesComponent component = target.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
        component.disableAbilityUsageForTime("battle_hypnosis_freeze", 20 * 3, target);

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        final UUID[] taskIdHolder = new UUID[1];
        taskIdHolder[0] = ServerScheduler.scheduleForDuration(0, 1, 20 * 5, () -> {
            if(InteractionHandler.isInteractionPossible(new Location(target.position(), level), "purification", entitySeq)) {
                target.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                component.enableAbilityUsage("battle_hypnosis_freeze");
                if(taskIdHolder[0] != null) ServerScheduler.cancel(taskIdHolder[0]);
                return;
            }

            target.setDeltaMovement(0, 0, 0);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 10, false, false, true));
            target.hurtMarked = true;
        }, level);
    }

    @Override
    public void nextAbility(LivingEntity entity){
        if(getAbilityNames().length == 0)
            return;

        if(!selectedAbilities.containsKey(entity.getUUID())) {
            selectedAbilities.put(entity.getUUID(), 0);
        }

        int selectedAbility = selectedAbilities.get(entity.getUUID());
        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        selectedAbility++;
        if(selectedAbility >= getAbilityNames().length) {
            selectedAbility = 0;
        }

        if((entitySeq > 4 && selectedAbility >= 0)){
            selectedAbility = 0;
        }

        selectedAbilities.put(entity.getUUID(), selectedAbility);
        PacketHandler.sendToServer(new AbilitySelectionPacket(getId(), selectedAbility));
    }

    @Override
    public void previousAbility(LivingEntity entity){
        if(getAbilityNames().length == 0)
            return;

        if(!selectedAbilities.containsKey(entity.getUUID())) {
            selectedAbilities.put(entity.getUUID(), 0);
        }

        int selectedAbility = selectedAbilities.get(entity.getUUID());
        selectedAbility--;
        if(selectedAbility <= -1) {
            selectedAbility = getAbilityNames().length - 1;
        }

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        if((entitySeq > 4 && selectedAbility >= 0)){
            selectedAbility = 0;
        }

        selectedAbilities.put(entity.getUUID(), selectedAbility);
        PacketHandler.sendToServer(new AbilitySelectionPacket(getId(), selectedAbility));
    }
}
