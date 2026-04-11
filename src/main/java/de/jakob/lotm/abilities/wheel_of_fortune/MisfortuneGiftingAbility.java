package de.jakob.lotm.abilities.wheel_of_fortune;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.attachments.LuckComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class MisfortuneGiftingAbility extends Ability {
    public MisfortuneGiftingAbility(String id) {
        super(id, 2, "unluck");
        canBeCopied = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("wheel_of_fortune", 5));
    }

    @Override
    public float getSpiritualityCost() {
        return 120;
    }

    private static final DustParticleOptions dust = new DustParticleOptions(new Vector3f(201 / 255f, 150 / 255f, 79 / 255f), 1.5f);

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20, 2);

        if(target == null) {
            if(entity instanceof ServerPlayer player) {
                Component actionBarText = Component.translatable("ability.lotmcraft.misfortune_gifting.no_target").withColor(0xFFc0f6fc);
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(actionBarText);
                player.connection.send(packet);
            }

            return;
        }

        double eyeHeight = target.getEyeHeight();
        ParticleUtil.spawnParticles(serverLevel, dust, target.position().add(0, eyeHeight / 2, 0), 120, .3, eyeHeight / 2, .3, 0);

        int amplifier = Math.round(multiplier(entity) * 250);
        LuckComponent luckComponent = target.getData(ModAttachments.LUCK_COMPONENT.get());
        luckComponent.addLuckWithMin(-amplifier, (int) (-amplifier * 1.25f));
    }
}
