package de.jakob.lotm.beyonders.abilities.wheel_of_fortune;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.calamities.Calamity;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.calamities.Earthquake;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.calamities.Meteor;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.calamities.Tornado;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class CalamityAttractionAbility extends SelectableAbility {
    public CalamityAttractionAbility(String id) {
        super(id, 10);

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(10000f, 4700f, 3500f, 2500f, 2500f, 2000f, 2000f));

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(4, 5, 6, 7, 8, 9, 10));

        baseDamage = 3.5f;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("wheel_of_fortune", 6));
    }

    @Override
    public float getSpiritualityCost() {
        return 190;
    }

    private final Calamity[] calamities = new Calamity[]{new Tornado(), new Earthquake(), new Meteor()};

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.calamity_attraction.calamity",
                "ability.lotmcraft.calamity_attraction.unluck"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        switch (selectedAbility){
            case 0 -> calamity(level, entity);
            case 1 -> unluck(level, entity);
        }
    }

    private void unluck(Level level, LivingEntity entity){
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if(!(entity instanceof ServerPlayer player)) return;

        var component = player.getData(ModAttachments.LUCK_COMPONENT.get());
        component.addLuckWithMin(-500, -5200);
    }

    private void calamity(Level level, LivingEntity entity){
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if(entity instanceof ServerPlayer player) {
            Component actionBarText = Component.translatable("ability.lotmcraft.passive_calamity_attraction.approaching_calamity").withColor(0xFFc0f6fc);
            ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(actionBarText);
            player.connection.send(packet);
        }

        Vec3 targetPos = AbilityUtil.getTargetLocation(entity, baseDistance, 2, true);

        AtomicReference<Float> damage = new AtomicReference<>(baseDamage);

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        ServerScheduler.scheduleDelayed(random.nextInt(31, 60), () -> {
            Calamity calamity = calamities[random.nextInt(calamities.length)];

            if(calamity instanceof Meteor)
                damage.set(25f);

            calamity.spawnCalamity(serverLevel, targetPos, damage.get(), BeyonderData.isGriefingEnabled(entity), entitySeq <= 3 ? entity : null);
        }, serverLevel);
    }
}
