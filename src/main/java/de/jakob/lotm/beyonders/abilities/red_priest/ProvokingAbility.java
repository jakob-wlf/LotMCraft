package de.jakob.lotm.beyonders.abilities.red_priest;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.quest.impl.HelpBeyonderQuest;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AbilityUtilClient;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ProvokingAbility extends Ability {
    public ProvokingAbility(String id) {
        super(id, 5, "morale_boost");

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(1, 1, 1, 1, 1, 2, 2, 3, 4));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(1600f, 640f, 400f, 200f, 200f, 125f, 100f, 90f, 50f));

        baseDamage = 7f;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("red_priest", 8));
    }

    @Override
    protected float getSpiritualityCost() {
        return 15;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        Vec3 pos = entity.getEyePosition();

        if(!level.isClientSide) {
            List<LivingEntity> nearbyEntities = AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, pos, 18);
            nearbyEntities.forEach(e -> {
                if(e instanceof Mob mob)
                    mob.setTarget(entity);

                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (int) (20 * 6* multiplier(entity)), 1, false, false, false));
                e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, (int) (20 * 6* multiplier(entity)), 1, false, false, false));
                e.hurt(ModDamageTypes.source(level, ModDamageTypes.PROVOCATION, entity), baseDamage);
            });

            if(entity instanceof ServerPlayer player){
                if(BeyonderData.getSequence(player) != 3 ||
                        !BeyonderData.getPathway(player).equals("red_priest")) return;

                if(player.getMainHandItem().getItem() instanceof BeyonderCharacteristicItem item){
                    if(item.getPathway().equals("red_priest") && item.getSequence() == 2){
                        var component = player.getData(ModAttachments.RITUALS.get());
                        if(component.getStage() != 3){
                            component.setStage(component.getStage() + 1);
                        }
                    }
                }
            }
        }
        else {
            entity.playSound(SoundEvents.PILLAGER_AMBIENT, 1.0f, 0.6f + (float) Math.random() * 0.4f);
            entity.playSound(SoundEvents.WOLF_GROWL, 1.0f, 1);
            List<LivingEntity> nearbyEntities = AbilityUtilClient.getNearbyEntities(entity, (ClientLevel) level, pos, 18);

            nearbyEntities.forEach(e -> {
                ParticleUtil.spawnParticles((ClientLevel) level, ParticleTypes.ANGRY_VILLAGER, e.getEyePosition().add(0, .5, 0), 10, 0.8, 0);
                ParticleUtil.spawnParticles((ClientLevel) level, ParticleTypes.LARGE_SMOKE, e.getEyePosition().add(0, .5, 0), 5, 0.8, 0);
            });

        }
    }
}
