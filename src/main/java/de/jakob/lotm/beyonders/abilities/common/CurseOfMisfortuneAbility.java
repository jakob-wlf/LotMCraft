package de.jakob.lotm.beyonders.abilities.common;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.attachments.LuckComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class CurseOfMisfortuneAbility extends Ability {
    public CurseOfMisfortuneAbility(String id) {
        super(id, 12, "unluck");
        postsUsedAbilityEventManually = true;
        canBeShared = false;

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(9000f, 3800f, 2400f, 1900f, 1650f, 1200f));

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(6, 6, 7, 9, 10));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("wheel_of_fortune", 4, "darkness", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1100;
    }

    private static final DustParticleOptions dust = new DustParticleOptions(new Vector3f(201 / 255f, 150 / 255f, 79 / 255f), 1.5f);

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(entity, (int) (15 * (multiplier(entity) * multiplier(entity))), 2);

        if(target == null) {
            if(entity instanceof ServerPlayer player) {
                Component actionBarText = Component.translatable("ability.lotmcraft.misfortune_gifting.no_target").withColor(0xFFc0f6fc);
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(actionBarText);
                player.connection.send(packet);
            }

            return;
        }

        // Higher sequence opponents resist – and may fully negate – the curse
        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        int targetSeq = BeyonderData.getSequence(target);

        double failureChance = AbilityUtil.getSequenceFailureChance(entitySeq, targetSeq);

        if (ThreadLocalRandom.current().nextDouble() < failureChance) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.curse_of_misfortune.resisted").withColor(0xFFc0f6fc));
            return;
        }

        EffectManager.playEffect(EffectManager.Effect.MISFORTUNE_CURSE, target.getX(), target.getY(), target.getZ(), serverLevel);

        double eyeHeight = target.getEyeHeight();
        ParticleUtil.spawnParticles(serverLevel, dust, target.position().add(0, eyeHeight / 2, 0), 120, .3, eyeHeight / 2, .3, 0);

        int amplifier = getLuck(entitySeq);

        if (amplifier <= 0) {
            return; // Full resistance – curse has no meaningful effect
        }

        LuckComponent luckComponent = target.getData(ModAttachments.LUCK_COMPONENT);
        luckComponent.addLuckWithMin(-amplifier, -3000);
        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, target.position(), entity, target, this, interactionFlags, interactionRadius, interactionCacheTicks));
    }

    private static int getLuck(int seq){
        return switch (seq){
            case 4 -> 400;
            case 3 -> 600;
            case 2 -> 800;
            case 1 -> 1200;
            case 0 -> 1700;
            default -> 0;
        };
    }
}
