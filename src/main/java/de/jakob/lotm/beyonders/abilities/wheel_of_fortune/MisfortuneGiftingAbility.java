package de.jakob.lotm.beyonders.abilities.wheel_of_fortune;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.attachments.LuckComponent;
import de.jakob.lotm.attachments.ModAttachments;
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
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MisfortuneGiftingAbility extends Ability {
    public MisfortuneGiftingAbility(String id) {
        super(id, 5);

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(8400f, 3500f, 2200f, 1750f, 1500f, 1000f));
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

        LivingEntity target = AbilityUtil.getTargetEntity(entity, baseDistance, 2);

        if(target == null) {
            if(entity instanceof ServerPlayer player) {
                Component actionBarText = Component.translatable("ability.lotmcraft.misfortune_gifting.no_target").withColor(0xFFc0f6fc);
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(actionBarText);
                player.connection.send(packet);
            }

            return;
        }

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        double eyeHeight = target.getEyeHeight();
        ParticleUtil.spawnParticles(serverLevel, dust, target.position().add(0, eyeHeight / 2, 0), 120, .3, eyeHeight / 2, .3, 0);

        int amplifier = getLuck(entitySeq);

        LuckComponent luckComponent = target.getData(ModAttachments.LUCK_COMPONENT.get());
        luckComponent.addLuckWithMin(-amplifier, -amplifier * 3);
    }

    private static int getLuck(int seq){
        return switch (seq){
            case 5 -> 100;
            case 4 -> 350;
            case 3 -> 500;
            case 2 -> 750;
            case 1 -> 1000;
            case 0 -> 1500;
            default -> 0;
        };
    }
}
