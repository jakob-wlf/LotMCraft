package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.visionary.passives.MetaAwarenessAbility;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.AbilitySelectionPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SleepInducementAbility extends SelectableAbility {
    public SleepInducementAbility(String id) {
        super(id, 2);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 5));
    }

    @Override
    public float getSpiritualityCost() {
        return 90;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.sleep_inducement_ability.single",
                "ability.lotmcraft.sleep_inducement_ability.aoe"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        switch (selectedAbility){
            case 0 -> single(level, entity);
            case 1 -> aoe(level, entity);
        }
    }

    private void aoe(Level level, LivingEntity entity){
        if(!(level instanceof ServerLevel)) {
            ParticleUtil.createParticleSpirals((ClientLevel) level, dust, entity.position(), entity.getBbWidth() + .25, entity.getBbWidth() + .25, entity.getEyeHeight(), 1, 5, 30, 15, 1);
            ParticleUtil.createParticleSpirals((ClientLevel) level, dust, entity.position(), 5, entity.getBbWidth() + .25, 5, 1, 5, 30, 30, 1);
            return;
        }

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, entity.position(), 10 * (int) Math.max(multiplier(entity)/4,1)).forEach(e -> {
            if(BeyonderData.getPathway(e).equals("visionary") && BeyonderData.getSequence(e) < entitySeq){
                AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.frenzy.failed").withColor(0xFFff124d));

                if(BeyonderData.getSequence(e) <= 1 && e instanceof ServerPlayer targetPlayer && entity instanceof ServerPlayer entityPlayer){
                    MetaAwarenessAbility.onDivined(entityPlayer, targetPlayer);
                }
            }
            else{
               e.addEffect(new MobEffectInstance(ModEffects.ASLEEP, 20 * 6* (int) Math.max(multiplier(entity)/4,1), 1, false, false, false));
            }
        });
    }

    private void single(Level level, LivingEntity entity){
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 80, 2);

        if(!(level instanceof ServerLevel)) {
            if(target != null) {
                ParticleUtil.spawnCircleParticles((ClientLevel) level, dust, target.getEyePosition(), 2, 20);
                ParticleUtil.spawnCircleParticles((ClientLevel) level, dust, target.getEyePosition(), new Vec3(0, 0, 1), 2, 20);
                ParticleUtil.spawnCircleParticles((ClientLevel) level, dust, target.getEyePosition(), new Vec3(1, 0, 0), 2, 20);
            }
            return;
        }

        if(target == null) {
            if(entity instanceof ServerPlayer player) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.translatable("ability.lotmcraft.frenzy.no_target").withColor(0xFFff124d));
                player.connection.send(packet);
            }
            return;
        }

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        int targetSeq = BeyonderData.getSequence(target);
        if(BeyonderData.getPathway(target).equals("visionary") && targetSeq < entitySeq){
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.dream_traversal.failed").withColor(0xFFff124d));

            if(targetSeq <= 1 && target instanceof ServerPlayer targetPlayer && entity instanceof ServerPlayer entityPlayer){
                MetaAwarenessAbility.onDivined(entityPlayer, targetPlayer);
            }

            return;
        }

        target.addEffect(new MobEffectInstance(ModEffects.ASLEEP, 20 * 12, 1, false, false, false));
    }

    private final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(250 / 255f, 201 / 255f, 102 / 255f),
            3f
    );

    private void animateParticleLine(Location startLoc, Vec3 end, int step, int duration) {
        if(!(startLoc.getLevel() instanceof ServerLevel level))
            return;

        float distance = (float) end.distanceTo(startLoc.getPosition());
        float bezierSteps = .2f / distance;

        int maxPoints = Math.max(2, Math.min(10, (int) Math.ceil(distance * 1.5)));

        List<Vec3> points = VectorUtil.createBezierCurve(startLoc.getPosition(), end, bezierSteps, random.nextInt(1, maxPoints + 1));

        for(int k = 0; k < duration; k++) {
            for(int i = 0; i < Math.min(k, points.size() - step); i+=step) {
                for(int j = 0; j < step; j++) {
                    ParticleUtil.spawnParticles(level, dust, points.get(i + j), 1, 0, 0);
                }
            }
        }
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

        if((entitySeq > 3 && selectedAbility >= 0)){
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
        if((entitySeq > 3 && selectedAbility >= 0)){
            selectedAbility = 0;
        }

        selectedAbilities.put(entity.getUUID(), selectedAbility);
        PacketHandler.sendToServer(new AbilitySelectionPacket(getId(), selectedAbility));
    }
}
