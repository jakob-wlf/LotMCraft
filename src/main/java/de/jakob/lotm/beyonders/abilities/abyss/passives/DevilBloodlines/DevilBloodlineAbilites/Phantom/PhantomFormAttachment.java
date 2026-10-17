package de.jakob.lotm.beyonders.abilities.abyss.passives.DevilBloodlines.DevilBloodlineAbilites.Phantom;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class PhantomFormAttachment {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, LOTMCraft.MOD_ID);
    public static final Supplier<AttachmentType<Boolean>> GHOST = ATTACHMENTS.register("phantom_ghost_form", () -> AttachmentType.builder(() -> false).sync(ByteBufCodecs.BOOL).build());
    private PhantomFormAttachment() {}
    public static boolean isGhost(Entity entity) {
        // hasData first so checking a random mob does not attach data to it
        return entity.hasData(GHOST) && entity.getData(GHOST);
    }
    public static void setGhost(Entity entity, boolean ghost) {
        entity.setData(GHOST, ghost);
    }
}