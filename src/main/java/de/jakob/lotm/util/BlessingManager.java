package de.jakob.lotm.util;

import de.jakob.lotm.effect.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class BlessingManager {
    public record Blessing(String id, String pathway, int minSequence, String translationKey, int characteristicDuration, Consumer<ServerPlayer> effect) {}

    private static final Map<String, List<Blessing>> blessingsByPathway = new HashMap<>();

    static {
        registerBlessing(new Blessing("fool_protection", "fool", 9, "blessing.lotmcraft.fool_protection", 600, target -> {
            target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 0));
        }));
        registerBlessing(new Blessing("sun_cleansing", "sun", 9, "blessing.lotmcraft.sun_cleansing", 600, target -> {
            target.removeAllEffects();
            target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
        }));
        registerBlessing(new Blessing("error_agility", "error", 9, "blessing.lotmcraft.error_agility", 600, target -> {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 1));
        }));
        // Fallback/Common blessings
        registerBlessing(new Blessing("minor_healing", "none", 9, "blessing.lotmcraft.minor_healing", 0, target -> {
            target.heal(4.0f);
        }));
        registerBlessing(new Blessing("concealment", "none", 9, "blessing.lotmcraft.concealment", 600, target -> {
            target.addEffect(new MobEffectInstance(ModEffects.CONCEALMENT, 6000, 2, false, false));
        }));
    }

    private static void registerBlessing(Blessing blessing) {
        blessingsByPathway.computeIfAbsent(blessing.pathway(), k -> new ArrayList<>()).add(blessing);
    }

    public static List<Blessing> getAvailableBlessings(String pathway, int sequence) {
        List<Blessing> available = new ArrayList<>();
        // Add specific pathway blessings
        if (blessingsByPathway.containsKey(pathway)) {
            for (Blessing b : blessingsByPathway.get(pathway)) {
                if (sequence <= b.minSequence()) {
                    available.add(b);
                }
            }
        }
        // Add common blessings
        if (blessingsByPathway.containsKey("none")) {
            for (Blessing b : blessingsByPathway.get("none")) {
                if (sequence <= b.minSequence()) {
                    available.add(b);
                }
            }
        }
        return available;
    }

    public static void applyBlessing(String blessingId, ServerPlayer blesser, ServerPlayer target) {
        for (List<Blessing> list : blessingsByPathway.values()) {
            for (Blessing b : list) {
                if (b.id().equals(blessingId)) {
                    // Disable characteristic on blesser if duration > 0
                    if (b.characteristicDuration() > 0) {
                        de.jakob.lotm.attachments.BeyonderComponent beyonder = blesser.getData(de.jakob.lotm.attachments.ModAttachments.BEYONDER_COMPONENT);
                        de.jakob.lotm.attachments.ActiveBlessingComponent activeBlessings = blesser.getData(de.jakob.lotm.attachments.ModAttachments.ACTIVE_BLESSING_COMPONENT);

                        // Find the characteristic to disable (highest sequence of the required pathway)
                        int seqToDisable = -1;
                        for (de.jakob.lotm.util.playerMap.Characteristic c : beyonder.getCharacteristicList()) {
                            if (c.pathway().equals(b.pathway()) && c.stack() > c.getDisabledStacks()) {
                                if (seqToDisable == -1 || c.sequence() < seqToDisable) {
                                    seqToDisable = c.sequence();
                                }
                            }
                        }

                        if (seqToDisable != -1) {
                            beyonder.adjustDisabledStacks(b.pathway(), seqToDisable, 1);
                            activeBlessings.addDisabledCharacteristic(b.pathway(), seqToDisable, b.characteristicDuration());
                            // Sync changes
                            de.jakob.lotm.network.PacketHandler.syncBeyonderDataToPlayer(blesser);

                            // Grant blessing to target
                            de.jakob.lotm.attachments.ReceivedBlessingComponent receivedBlessings = target.getData(de.jakob.lotm.attachments.ModAttachments.RECEIVED_BLESSING_COMPONENT);
                            receivedBlessings.addBlessing(b.id(), b.pathway(), seqToDisable, b.characteristicDuration());
                        }
                    }

                    b.effect().accept(target);
                    return;
                }
            }
        }
    }
}
