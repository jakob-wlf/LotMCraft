package de.jakob.lotm.dimension.abyss.fog;

import de.jakob.lotm.LOTMCraft;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class AbyssFogNetwork {
    public static volatile float clientFar = 140.0f;
    public record AbyssFogPayload(float far) implements CustomPacketPayload {
        public static final Type<AbyssFogPayload> TYPE =new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "abyss_fog"));
        public static final StreamCodec<ByteBuf, AbyssFogPayload> STREAM_CODEC = ByteBufCodecs.FLOAT.map(AbyssFogPayload::new, AbyssFogPayload::far);
        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {return TYPE;}
    }
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(AbyssFogPayload.TYPE,AbyssFogPayload.STREAM_CODEC,(payload, context) -> context.enqueueWork(() -> clientFar = payload.far()));
    }
}
