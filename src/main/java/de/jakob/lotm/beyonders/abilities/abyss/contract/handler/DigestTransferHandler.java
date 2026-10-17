package de.jakob.lotm.beyonders.abilities.abyss.contract.handler;

import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;

public final class DigestTransferHandler {
    public static final float TAX = 0.5f;
    public enum Result { DONE, INSUFFICIENT, INVALID }
    private DigestTransferHandler() {}
    // percent is 1-100. Moves digestion from giver to receiver.
    public static Result transfer(ServerPlayer giver, ServerPlayer receiver, int percent) {
        if (percent < 1 || percent > 100) return Result.INVALID;
        float amount = percent / 100f;
        float giverHas = BeyonderData.getDigestionProgress(giver);
        if (giverHas + 0.0001f < amount) return Result.INSUFFICIENT;
        int giverSeq = BeyonderData.getSequence(giver);
        int receiverSeq = BeyonderData.getSequence(receiver);
        // Lower sequence number = stronger.
        // Strong -> weak 1/4 per step, weak -> strong 1/4 per step.
        double multiplier = Math.pow(2, receiverSeq - giverSeq - 2);
        float received = (float) (amount * (1f - TAX) * multiplier);
        BeyonderData.setDigestionProgress(giver, Math.max(0f, giverHas - amount));
        float receiverNew = Math.min(1f, BeyonderData.getDigestionProgress(receiver) + received);
        BeyonderData.setDigestionProgress(receiver, receiverNew);
        return Result.DONE;
    }
}