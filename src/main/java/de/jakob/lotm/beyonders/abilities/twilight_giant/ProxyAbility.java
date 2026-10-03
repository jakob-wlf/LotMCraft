package de.jakob.lotm.beyonders.abilities.twilight_giant;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.CopiedAbilityComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.MultiplierModifierComponent;
import de.jakob.lotm.attachments.UniquenessComponent;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.twilight_giant.handlers.TwilightAging;
import de.jakob.lotm.beyonders.sefirah.SefirahHandler;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.OpenPlayerDivinationScreenPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.playerMap.StoredData;
import de.jakob.lotm.util.data.PlayerInfo;
import de.jakob.lotm.util.data.PlayerSelectionWorkType;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.CopiedAbilityHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ProxyAbility extends Ability {

    private static final double RANGE = 50.0D;
    private static final double STAT_BONUS = 0.25D;
    private static final double RIVER_WEAKNESS = -0.8D;
    private static final float RIVER_MODIFIER = 0.1F;
    private static final int GRANTED_ABILITIES = 8;
    private static final int EMPLOY_TICKS = 20 * 60 * 10;
    private static final int WEAK_TICKS = 20 * 60 * 60 * 2;
    private static final String RIVER = "river_of_eternal_darkness";
    private static final String EMPLOY_UNTIL = "lotmcraft_river_employ_until";
    private static final String WEAK_UNTIL = "lotmcraft_river_weak_until";
    private static final String WEAK_MODIFIER = "river_proxy";
    static final String COPY_TYPE = "proxy";

    private static final Map<UUID, UUID> pending = new HashMap<>();

    private static final Stat[] STATS = {
            new Stat(Attributes.ATTACK_DAMAGE, "attack"),
            new Stat(Attributes.ATTACK_SPEED, "attack_speed"),
            new Stat(Attributes.ARMOR, "armor"),
            new Stat(Attributes.ARMOR_TOUGHNESS, "toughness"),
            new Stat(Attributes.MOVEMENT_SPEED, "speed"),
            new Stat(Attributes.MAX_HEALTH, "health"),
            new Stat(Attributes.KNOCKBACK_RESISTANCE, "knockback")
    };

    public ProxyAbility(String id) {
        super(id, 60);
        canBeCopied = false;
        canBeUsedByNPC = false;
        cannotBeStolen = true;
        canBeReplicated = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("twilight_giant", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 6000;
    }

    @Override
    public Component getDescription() {
        if (ClientBeyonderCache.localSequence() <= 0) {
            return Component.translatable("lotmcraft.proxy_ability.river_description").withStyle(ChatFormatting.DARK_GRAY);
        }
        return super.getDescription();
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof ServerPlayer player)) return;
        if (BeyonderData.getSequence(player) <= 0) {
            employRiver(player);
            return;
        }
        if (stored(player.getUUID()) != null) {
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.proxy.already").withColor(TwilightAging.TWILIGHT_TEXT));
            return;
        }
        List<PlayerInfo> patrons = new ArrayList<>();
        for (ServerPlayer other : serverLevel.players()) {
            if (other == player || other.distanceTo(player) > RANGE || !isPatron(other)) continue;
            patrons.add(new PlayerInfo(other.getUUID(), other.getGameProfile().getName()));
        }
        if (patrons.isEmpty()) {
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.proxy.none").withColor(TwilightAging.TWILIGHT_TEXT));
            return;
        }
        PacketHandler.sendToPlayer(player, new OpenPlayerDivinationScreenPacket(patrons, PlayerSelectionWorkType.PROXY));
    }

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("lotm_proxy_accept")
                .then(Commands.argument("caster", StringArgumentType.word())
                        .executes(context -> respond(context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "caster"), true))));
        dispatcher.register(Commands.literal("lotm_proxy_decline")
                .then(Commands.argument("caster", StringArgumentType.word())
                        .executes(context -> respond(context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "caster"), false))));
        dispatcher.register(Commands.literal("lotm_proxy_break")
                .executes(context -> breakPatronage(context.getSource().getPlayerOrException())));
        dispatcher.register(Commands.literal("lotm_proxy_who")
                .executes(context -> showProxy(context.getSource().getPlayerOrException())));
    }

    private static void employRiver(ServerPlayer player) {
        long now = player.server.overworld().getGameTime();
        if (player.getPersistentData().getLong(EMPLOY_UNTIL) > now) {
            if (!player.hasInfiniteMaterials()) BeyonderData.incrementSpirituality(player, 6000f);
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.proxy.river_active").withColor(TwilightAging.TWILIGHT_TEXT));
            return;
        }
        if (player.getPersistentData().getLong(WEAK_UNTIL) > now) {
            if (!player.hasInfiniteMaterials()) BeyonderData.incrementSpirituality(player, 6000f);
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.proxy.river_spent").withColor(TwilightAging.TWILIGHT_TEXT));
            return;
        }
        player.getPersistentData().putLong(EMPLOY_UNTIL, now + EMPLOY_TICKS);
        player.getPersistentData().putLong(WEAK_UNTIL, now + EMPLOY_TICKS + WEAK_TICKS);
        writePledge(player, RIVER);
        releaseWeak(player);
        player.sendSystemMessage(Component.translatable("ability.lotmcraft.proxy.river_open"));
    }

    private static void maintainRiver(ServerPlayer player) {
        long now = player.server.overworld().getGameTime();
        long employ = player.getPersistentData().getLong(EMPLOY_UNTIL);
        long weak = player.getPersistentData().getLong(WEAK_UNTIL);
        if (employ > now) {
            if (!RIVER.equals(pledgedSefirot(player))) writePledge(player, RIVER);
            releaseWeak(player);
            return;
        }
        if (employ != 0L) {
            player.getPersistentData().remove(EMPLOY_UNTIL);
            if (RIVER.equals(pledgedSefirot(player))) {
                writePledge(player, "");
                player.sendSystemMessage(Component.translatable("ability.lotmcraft.proxy.river_closed"));
            }
        } else if (!pledgedSefirot(player).isEmpty()) {
            writePledge(player, "");
        }
        if (weak > now) {
            ensureWeak(player);
            return;
        }
        if (weak != 0L) {
            player.getPersistentData().remove(WEAK_UNTIL);
            clearWeak(player);
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.proxy.river_recovered"));
        }
    }

    private static void ensureWeak(ServerPlayer player) {
        applyAttributes(player, "river", RIVER_WEAKNESS);
        MultiplierModifierComponent component = player.getData(ModAttachments.MULTIPLIER_MODIFIER_COMPONENT);
        MultiplierModifierComponent.MultiplierModifier existing = component.modifiers.get(WEAK_MODIFIER);
        if (existing != null && existing.amount() == 1 && existing.multiplier() == RIVER_MODIFIER) return;
        while (component.modifiers.containsKey(WEAK_MODIFIER)) BeyonderData.removeModifier(player, WEAK_MODIFIER);
        BeyonderData.addModifier(player, WEAK_MODIFIER, RIVER_MODIFIER);
    }

    private static void releaseWeak(ServerPlayer player) {
        AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
        boolean restore = health != null && health.hasModifier(statId("river", "health"));
        clearWeak(player);
        if (restore) player.setHealth(player.getMaxHealth());
    }

    private static void clearWeak(ServerPlayer player) {
        clearAttributes(player, "river");
        MultiplierModifierComponent component = player.getData(ModAttachments.MULTIPLIER_MODIFIER_COMPONENT);
        while (component.modifiers.containsKey(WEAK_MODIFIER)) BeyonderData.removeModifier(player, WEAK_MODIFIER);
    }

    public static void select(ServerPlayer caster, UUID patronId) {
        if (stored(caster.getUUID()) != null) {
            AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.proxy.already").withColor(TwilightAging.TWILIGHT_TEXT));
            return;
        }
        ServerPlayer patron = caster.server.getPlayerList().getPlayer(patronId);
        if (patron == null || !isPatron(patron) || patron.distanceTo(caster) > RANGE) {
            AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.proxy.none").withColor(TwilightAging.TWILIGHT_TEXT));
            return;
        }
        pending.put(patron.getUUID(), caster.getUUID());
        Component accept = Component.translatable("ability.lotmcraft.proxy.accept")
                .withStyle(style -> style.withColor(0x4CAF50).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lotm_proxy_accept " + caster.getUUID())));
        Component decline = Component.translatable("ability.lotmcraft.proxy.decline")
                .withStyle(style -> style.withColor(0xF44336).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lotm_proxy_decline " + caster.getUUID())));
        patron.sendSystemMessage(Component.translatable("ability.lotmcraft.proxy.offer", caster.getGameProfile().getName()).append(" ").append(accept).append(" ").append(decline));
        AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.proxy.offered", patron.getGameProfile().getName()).withColor(TwilightAging.TWILIGHT_TEXT));
    }

    private static int pledge(ServerPlayer player, String id) {
        if (!RIVER.equals(id) || BeyonderData.getSequence(player) > 0) return 0;
        employRiver(player);
        return 1;
    }

    private static boolean ownGroup(LivingEntity entity) {
        String pledge = pledgedSefirot(entity);
        if (pledge.isEmpty() || RIVER.equals(pledge)) return false;
        String pathway = BeyonderData.getPathway(entity);
        for (String member : SefirahHandler.getPathwaysForSefirot(pledge)) {
            if (member.equals(pathway)) return true;
        }
        return false;
    }

    public static String patronPathway(LivingEntity entity) {
        Bond bond = stored(entity.getUUID());
        return bond == null ? null : bond.pathway;
    }

    public static UUID patronId(LivingEntity entity) {
        Bond bond = stored(entity.getUUID());
        return bond == null ? null : bond.patron;
    }

    public static String pledgedSefirot(LivingEntity entity) {
        if (entity.level().isClientSide()) return ClientBeyonderCache.getPledgedSefirot(entity.getUUID());
        if (BeyonderData.playerMap == null) return "";
        return BeyonderData.playerMap.get(entity.getUUID()).map(data -> data.pledgedSefirot() == null ? "" : data.pledgedSefirot()).orElse("");
    }

    public static boolean hasPlayerPatron(LivingEntity entity) {
        if (entity.level().isClientSide()) return ClientBeyonderCache.hasPlayerPatron(entity.getUUID());
        return patronId(entity) != null;
    }

    public static boolean blocksAccommodation(LivingEntity entity) {
        return ownGroup(entity);
    }

    static void applyAttributes(LivingEntity entity, String prefix, double bonus) {
        for (Stat stat : STATS) {
            AttributeInstance instance = entity.getAttribute(stat.attribute);
            if (instance == null) continue;
            ResourceLocation id = statId(prefix, stat.name);
            instance.removeModifier(id);
            instance.addTransientModifier(new AttributeModifier(id, bonus, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    static void clearAttributes(LivingEntity entity, String prefix) {
        for (Stat stat : STATS) {
            AttributeInstance instance = entity.getAttribute(stat.attribute);
            if (instance != null) instance.removeModifier(statId(prefix, stat.name));
        }
    }

    static void grantCopies(ServerPlayer player, List<Ability> abilities, String type, UUID owner) {
        int granted = 0;
        for (Ability ability : abilities) {
            if (granted >= (COPY_TYPE.equals(type) ? GRANTED_ABILITIES : 5)) break;
            if (!ability.canBeCopied) continue;
            CopiedAbilityHelper.addAbility(player, new CopiedAbilityComponent.CopiedAbilityData(ability.getId(), type, -1, owner.toString()));
            granted++;
        }
    }

    static void removeCopies(ServerPlayer player, String type, UUID owner) {
        CopiedAbilityComponent component = player.getData(ModAttachments.COPIED_ABILITY_COMPONENT);
        String ownerId = owner.toString();
        component.getAbilities().removeIf(data -> type.equals(data.copyType()) && ownerId.equals(data.originalOwnerUUID()));
        CopiedAbilityHelper.syncToClient(player);
    }

    private static int respond(ServerPlayer patron, String casterId, boolean accepted) {
        UUID casterUuid;
        try {
            casterUuid = UUID.fromString(casterId);
        } catch (IllegalArgumentException exception) {
            return 0;
        }
        if (!casterUuid.equals(pending.get(patron.getUUID()))) return 0;
        pending.remove(patron.getUUID());
        ServerPlayer caster = patron.server.getPlayerList().getPlayer(casterUuid);
        if (!accepted) {
            if (caster != null) caster.sendSystemMessage(Component.translatable("ability.lotmcraft.proxy.declined", patron.getGameProfile().getName()));
            return 1;
        }
        if (caster == null) return 0;
        bind(caster, patron);
        return 1;
    }

    private static void bind(ServerPlayer caster, ServerPlayer patron) {
        clearBond(caster, false);
        remember(caster, patron.getUUID());
        applyAttributes(caster, "proxy", STAT_BONUS);
        stripProxyCopies(caster);
        caster.sendSystemMessage(Component.translatable("ability.lotmcraft.proxy.bound", patron.getGameProfile().getName()));
        patron.sendSystemMessage(Component.translatable("ability.lotmcraft.proxy.accepted", caster.getGameProfile().getName()));
    }

    private static int breakPatronage(ServerPlayer player) {
        boolean any = false;
        Bond own = stored(player.getUUID());
        if (own != null) {
            ServerPlayer patron = player.server.getPlayerList().getPlayer(own.patron);
            clearBond(player, false);
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.proxy.ended"));
            if (patron != null) patron.sendSystemMessage(Component.translatable("ability.lotmcraft.proxy.released", player.getGameProfile().getName()));
            any = true;
        }
        for (UUID casterId : castersOf(player.getUUID())) {
            ServerPlayer caster = player.server.getPlayerList().getPlayer(casterId);
            if (caster != null) {
                clearBond(caster, false);
                caster.sendSystemMessage(Component.translatable("ability.lotmcraft.proxy.ended"));
                player.sendSystemMessage(Component.translatable("ability.lotmcraft.proxy.released", caster.getGameProfile().getName()));
            } else {
                forget(casterId);
            }
            any = true;
        }
        if (!pledgedSefirot(player).isEmpty() && player.getPersistentData().getLong(EMPLOY_UNTIL) <= player.server.overworld().getGameTime()) {
            writePledge(player, "");
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.proxy.pledge_released"));
            any = true;
        }
        if (!any) player.sendSystemMessage(Component.translatable("ability.lotmcraft.proxy.none_bound"));
        return any ? 1 : 0;
    }

    private static int showProxy(ServerPlayer player) {
        boolean any = false;
        Bond own = stored(player.getUUID());
        if (own != null) {
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.proxy.your_patron", nameOf(player, own.patron)));
            any = true;
        }
        String pledge = pledgedSefirot(player);
        if (!pledge.isEmpty()) {
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.proxy.your_pledge", Component.translatable("lotm.sefirot." + pledge)));
            any = true;
        }
        for (UUID casterId : castersOf(player.getUUID())) {
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.proxy.your_proxy", nameOf(player, casterId)));
            any = true;
        }
        if (!any) player.sendSystemMessage(Component.translatable("ability.lotmcraft.proxy.no_proxy"));
        return any ? 1 : 0;
    }

    private static String nameOf(ServerPlayer viewer, UUID id) {
        ServerPlayer online = viewer.server.getPlayerList().getPlayer(id);
        if (online != null) return online.getGameProfile().getName();
        return viewer.server.getProfileCache().get(id).map(profile -> profile.getName()).orElse("offline");
    }

    private static void clearBond(ServerPlayer caster, boolean tell) {
        Bond bond = stored(caster.getUUID());
        forget(caster.getUUID());
        if (bond == null) return;
        clearAttributes(caster, "proxy");
        removeCopies(caster, COPY_TYPE, bond.patron);
        if (tell) caster.sendSystemMessage(Component.translatable("ability.lotmcraft.proxy.lost"));
        PacketHandler.syncBeyonderDataToPlayer(caster);
    }

    private static boolean isPatron(ServerPlayer player) {
        UniquenessComponent uniqueness = player.getData(ModAttachments.UNIQUENESS_COMPONENT);
        return uniqueness.hasUniqueness() || BeyonderData.getSequence(player) == 0;
    }

    private static ResourceLocation statId(String prefix, String name) {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "hand_of_god_" + prefix + "_" + name);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        clearBond(player, false);
        for (UUID casterId : castersOf(player.getUUID())) {
            ServerPlayer caster = player.server.getPlayerList().getPlayer(casterId);
            if (caster != null) clearBond(caster, true);
            else forget(casterId);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        maintainRiver(player);
        stripProxyCopies(player);
        Bond bond = stored(player.getUUID());
        if (bond == null) return;
        applyAttributes(player, "proxy", STAT_BONUS);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 10 != 0) return;
        if (player.tickCount % 20 == 0) {
            maintainRiver(player);
            if (BeyonderData.getSequence(player) > 0) stripProxyCopies(player);
        }
        Bond bond = stored(player.getUUID());
        if (bond == null) return;
        ServerLevel level = player.serverLevel();
        int color = BeyonderData.pathwayInfos.containsKey(bond.pathway) ? BeyonderData.pathwayInfos.get(bond.pathway).color() : TwilightAging.TWILIGHT_TEXT;
        DustParticleOptions dust = new DustParticleOptions(new Vector3f(((color >> 16) & 255) / 255f, ((color >> 8) & 255) / 255f, (color & 255) / 255f), 1.2f);
        Vec3 look = player.getLookAngle();
        Vec3 flat = new Vec3(look.x, 0, look.z);
        if (flat.lengthSqr() < 1.0E-4) flat = new Vec3(0, 0, 1);
        flat = flat.normalize();
        Vec3 hand = player.getEyePosition().add(new Vec3(-flat.z, 0, flat.x).scale(0.45)).add(look.scale(0.35)).add(0, -0.35, 0);
        level.sendParticles(dust, hand.x, hand.y, hand.z, 2, 0.05, 0.05, 0.05, 0);
    }

    private static void stripProxyCopies(ServerPlayer player) {
        CopiedAbilityComponent component = player.getData(ModAttachments.COPIED_ABILITY_COMPONENT);
        if (component.getAbilities().removeIf(data -> COPY_TYPE.equals(data.copyType()))) {
            CopiedAbilityHelper.syncToClient(player);
        }
    }

    private static Bond stored(UUID caster) {
        if (BeyonderData.playerMap == null) return null;
        String patronId = BeyonderData.playerMap.get(caster).map(StoredData::patron).orElse("");
        if (patronId == null || patronId.isEmpty()) return null;
        try {
            return bondFrom(UUID.fromString(patronId));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static Bond bondFrom(UUID patron) {
        String pathway = "twilight_giant";
        int sequence = 0;
        if (BeyonderData.playerMap != null) {
            StoredData data = BeyonderData.playerMap.get(patron).orElse(null);
            if (data != null) {
                String uniqueness = data.uniqueness();
                pathway = uniqueness != null && !uniqueness.isBlank() && !uniqueness.equals("none") ? uniqueness : data.pathway();
                if (data.sequence() != null) sequence = data.sequence();
            }
        }
        return new Bond(patron, pathway, sequence);
    }

    private static void remember(ServerPlayer caster, UUID patron) {
        if (BeyonderData.playerMap == null) return;
        if (!BeyonderData.playerMap.contains(caster)) BeyonderData.playerMap.put(caster);
        writePatron(caster.getUUID(), patron.toString());
        PacketHandler.syncBeyonderDataToPlayer(caster);
    }

    private static void forget(UUID caster) {
        writePatron(caster, "");
    }

    private static void writePledge(ServerPlayer player, String pledge) {
        if (BeyonderData.playerMap == null) return;
        if (!BeyonderData.playerMap.contains(player)) BeyonderData.playerMap.put(player);
        StoredData data = BeyonderData.playerMap.get(player.getUUID()).orElse(null);
        if (data == null) return;
        BeyonderData.playerMap.put(player.getUUID(), StoredData.builder.copyFrom(data).pledgedSefirot(pledge).build());
        PacketHandler.syncBeyonderDataToPlayer(player);
    }

    private static void writePatron(UUID caster, String patron) {
        if (BeyonderData.playerMap == null) return;
        StoredData data = BeyonderData.playerMap.get(caster).orElse(null);
        if (data == null) return;
        BeyonderData.playerMap.put(caster, StoredData.builder.copyFrom(data).patron(patron).build());
    }

    private static List<UUID> castersOf(UUID patron) {
        List<UUID> casters = new ArrayList<>();
        if (BeyonderData.playerMap == null) return casters;
        String id = patron.toString();
        for (Map.Entry<UUID, StoredData> entry : BeyonderData.playerMap.entrySet()) {
            if (id.equals(entry.getValue().patron())) casters.add(entry.getKey());
        }
        return casters;
    }

    private record Stat(Holder<Attribute> attribute, String name) {
    }

    private record Bond(UUID patron, String pathway, int sequence) {
    }
}
