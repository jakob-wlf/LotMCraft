package de.jakob.lotm.beyonders.abilities.twilight_giant;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.CopiedAbilityComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.twilight_giant.handlers.TwilightAging;
import de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.gui.custom.marionettes.MarionetteMenu;
import de.jakob.lotm.gui.custom.marionettes.MarionetteMenuProvider;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AllyUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ServantsAbility extends SelectableAbility {

    private static final int RANGE = 30;
    private static final int BORROWED = 5;
    private static final String COPY_TYPE = "servant";
    private static final String BOOST = "hand_of_god_servant_boost";

    private static final Map<UUID, Map<UUID, Mark>> marks = new HashMap<>();

    public ServantsAbility(String id) {
        super(id, 20);
        canBeCopied = false;
        canBeUsedByNPC = false;
        cannotBeStolen = true;
        canBeReplicated = false;
        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(8000f, 4000f));
        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(10, 20));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("twilight_giant", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 4000;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.servants.mark",
                "ability.lotmcraft.servants.manage"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (!(level instanceof ServerLevel) || !(entity instanceof ServerPlayer player)) return;
        if (abilityIndex == 0) mark(player);
        else manage(player);
    }

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("lotm_servant_accept")
                .then(Commands.argument("master", StringArgumentType.word())
                        .executes(context -> accept(context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "master")))));
        dispatcher.register(Commands.literal("lotm_servant_boost")
                .then(Commands.argument("master", StringArgumentType.word())
                        .executes(context -> choose(context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "master"), true))));
        dispatcher.register(Commands.literal("lotm_servant_borrow")
                .then(Commands.argument("master", StringArgumentType.word())
                        .executes(context -> choose(context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "master"), false))));
        dispatcher.register(Commands.literal("lotm_servant_decline")
                .then(Commands.argument("master", StringArgumentType.word())
                        .executes(context -> decline(context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "master")))));
    }

    public static void dismiss(ServerPlayer master, UUID servantId) {
        Map<UUID, Mark> owned = marks.get(master.getUUID());
        if (owned == null) return;
        Mark mark = owned.remove(servantId);
        if (mark == null) return;
        clearMark(master, servantId, mark);
        if (owned.isEmpty()) marks.remove(master.getUUID());
        AbilityUtil.sendActionBar(master, Component.translatable("ability.lotmcraft.servants.removed", mark.name).withColor(TwilightAging.TWILIGHT_TEXT));
    }

    private static void mark(ServerPlayer player) {
        LivingEntity target = AbilityUtil.getTargetEntity(player, RANGE, 1.5f, true, true);
        if (target == null) {
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.servants.no_target").withColor(TwilightAging.TWILIGHT_TEXT));
            return;
        }
        release(player.server, target.getUUID());
        Mark mark = new Mark(target.getDisplayName().getString());
        marks.computeIfAbsent(player.getUUID(), id -> new HashMap<>()).put(target.getUUID(), mark);
        if (target instanceof ServerPlayer servant) {
            Component accept = Component.translatable("ability.lotmcraft.servants.accept")
                    .withStyle(style -> style.withColor(0x4CAF50).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lotm_servant_accept " + player.getUUID())));
            Component decline = Component.translatable("ability.lotmcraft.servants.decline")
                    .withStyle(style -> style.withColor(0xF44336).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lotm_servant_decline " + player.getUUID())));
            servant.sendSystemMessage(Component.translatable("ability.lotmcraft.servants.offer", player.getGameProfile().getName()).append(" ").append(accept).append(" ").append(decline));
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.servants.offered", mark.name).withColor(TwilightAging.TWILIGHT_TEXT));
            return;
        } else {
            applyBoost(target);
            mark.boost = true;
            syncAllies(player.server);
        }
        AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.servants.marked", mark.name).withColor(TwilightAging.TWILIGHT_TEXT));
    }

    private static void manage(ServerPlayer player) {
        Map<UUID, Mark> owned = marks.get(player.getUUID());
        if (owned == null || owned.isEmpty()) {
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.servants.empty").withColor(TwilightAging.TWILIGHT_TEXT));
            return;
        }
        List<MarionetteMenu.ServantRow> rows = new ArrayList<>();
        for (Map.Entry<UUID, Mark> entry : owned.entrySet()) {
            if (!active(entry.getValue())) continue;
            LivingEntity servant = find(player, entry.getKey());
            boolean visible = servant != null && !hidden(player, servant);
            boolean beyonder = visible && BeyonderData.isBeyonder(servant);
            String detail = servant == null ? Component.translatable("ability.lotmcraft.servants.away").getString() : describe(player, servant);
            rows.add(new MarionetteMenu.ServantRow(entry.getKey(), entry.getValue().name, detail, beyonder, beyonder ? BeyonderData.getPathway(servant) : "", beyonder ? BeyonderData.getSequence(servant) : -1));
        }
        if (rows.isEmpty()) {
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.servants.empty").withColor(TwilightAging.TWILIGHT_TEXT));
            return;
        }
        player.openMenu(MarionetteMenuProvider.servants(), buf -> MarionetteMenu.writeServants(buf, rows));
    }

    private static int accept(ServerPlayer servant, String masterId) {
        UUID masterUuid;
        try {
            masterUuid = UUID.fromString(masterId);
        } catch (IllegalArgumentException exception) {
            return 0;
        }
        Map<UUID, Mark> owned = marks.get(masterUuid);
        if (owned == null) return 0;
        Mark mark = owned.get(servant.getUUID());
        if (mark == null || mark.accepted || mark.boost || mark.borrowed) return 0;
        ServerPlayer master = servant.server.getPlayerList().getPlayer(masterUuid);
        if (master == null) return 0;
        mark.accepted = true;
        syncAllies(servant.server);
        Component boost = Component.translatable("ability.lotmcraft.servants.boost")
                .withStyle(style -> style.withColor(0x4CAF50).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lotm_servant_boost " + masterUuid)));
        Component borrow = Component.translatable("ability.lotmcraft.servants.borrow")
                .withStyle(style -> style.withColor(0xFF6A2A).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lotm_servant_borrow " + masterUuid)));
        servant.sendSystemMessage(Component.translatable("ability.lotmcraft.servants.accepted").append(" ").append(boost).append(" ").append(borrow));
        master.sendSystemMessage(Component.translatable("ability.lotmcraft.servants.accepted_master", servant.getGameProfile().getName()));
        return 1;
    }

    private static int choose(ServerPlayer servant, String masterId, boolean boost) {
        UUID masterUuid;
        try {
            masterUuid = UUID.fromString(masterId);
        } catch (IllegalArgumentException exception) {
            return 0;
        }
        Map<UUID, Mark> owned = marks.get(masterUuid);
        if (owned == null || !owned.containsKey(servant.getUUID())) return 0;
        ServerPlayer master = servant.server.getPlayerList().getPlayer(masterUuid);
        if (master == null) return 0;
        Mark mark = owned.get(servant.getUUID());
        if (mark == null || !mark.accepted || mark.boost || mark.borrowed) return 0;
        clearMark(master, servant.getUUID(), mark);
        if (boost) {
            applyBoost(servant);
            mark.boost = true;
            mark.borrowed = false;
        } else {
            borrow(master, servant);
            mark.boost = false;
            mark.borrowed = true;
        }
        servant.sendSystemMessage(Component.translatable(boost ? "ability.lotmcraft.servants.boosted" : "ability.lotmcraft.servants.borrowed"));
        return 1;
    }

    private static int decline(ServerPlayer servant, String masterId) {
        UUID masterUuid;
        try {
            masterUuid = UUID.fromString(masterId);
        } catch (IllegalArgumentException exception) {
            return 0;
        }
        Map<UUID, Mark> owned = marks.get(masterUuid);
        if (owned == null) return 0;
        Mark mark = owned.get(servant.getUUID());
        if (mark == null || mark.accepted || mark.boost || mark.borrowed) return 0;
        owned.remove(servant.getUUID());
        if (owned.isEmpty()) marks.remove(masterUuid);
        servant.sendSystemMessage(Component.translatable("ability.lotmcraft.servants.declined"));
        ServerPlayer master = servant.server.getPlayerList().getPlayer(masterUuid);
        if (master != null) master.sendSystemMessage(Component.translatable("ability.lotmcraft.servants.refused", servant.getGameProfile().getName()));
        return 1;
    }

    private static void borrow(ServerPlayer master, ServerPlayer servant) {
        String patron = ProxyAbility.patronPathway(master);
        List<Ability> abilities = new ArrayList<>();
        for (Ability ability : LOTMCraft.abilityHandler.getAllAbilitiesForEntity(master)) {
            if (!ability.canBeCopied) continue;
            boolean twilight = ability.getRequirements().containsKey("twilight_giant");
            boolean domain = patron != null && ability.getRequirements().containsKey(patron);
            if (twilight || domain) abilities.add(ability);
        }
        for (CopiedAbilityComponent.CopiedAbilityData data : master.getData(ModAttachments.COPIED_ABILITY_COMPONENT).getAbilities()) {
            if (!ProxyAbility.COPY_TYPE.equals(data.copyType())) continue;
            Ability ability = LOTMCraft.abilityHandler.getById(data.abilityId());
            if (ability != null && ability.canBeCopied && !abilities.contains(ability)) abilities.add(ability);
        }
        abilities.sort(Comparator.comparingInt(ability -> {
            if (patron != null && ability.getRequirements().containsKey(patron)) return ability.getRequirements().get(patron);
            return ability.getRequirements().getOrDefault("twilight_giant", 9);
        }));
        if (abilities.size() > BORROWED) abilities.subList(BORROWED, abilities.size()).clear();
        ProxyAbility.grantCopies(servant, abilities, COPY_TYPE, master.getUUID());
    }

    private static void applyBoost(LivingEntity servant) {
        int sequence = BeyonderData.getSequence(servant);
        if (sequence < 0 || sequence > 10) sequence = 10;
        int stronger = Math.max(0, sequence - 1);
        double current = Math.max(0.01D, BeyonderData.getMultiplierForSequence(sequence));
        double next = BeyonderData.getMultiplierForSequence(stronger);
        BeyonderData.removeModifier(servant, BOOST);
        BeyonderData.addModifier(servant, BOOST, next / current);
    }

    private static void clearMark(ServerPlayer master, UUID servantId, Mark mark) {
        clearEffects(master.server, master.getUUID(), servantId, mark);
    }

    private static void clearEffects(MinecraftServer server, UUID masterId, UUID servantId, Mark mark) {
        LivingEntity servant = find(server, servantId);
        if (servant == null) return;
        if (mark.boost) BeyonderData.removeModifier(servant, BOOST);
        if (mark.borrowed && servant instanceof ServerPlayer player) ProxyAbility.removeCopies(player, COPY_TYPE, masterId);
    }

    private static void release(MinecraftServer server, UUID servantId) {
        for (Map.Entry<UUID, Map<UUID, Mark>> entry : new ArrayList<>(marks.entrySet())) {
            Mark mark = entry.getValue().remove(servantId);
            if (mark == null) continue;
            clearEffects(server, entry.getKey(), servantId, mark);
            if (entry.getValue().isEmpty()) marks.remove(entry.getKey());
        }
    }

    private static LivingEntity find(MinecraftServer server, UUID id) {
        ServerPlayer player = server.getPlayerList().getPlayer(id);
        if (player != null && player.isAlive()) return player;
        for (ServerLevel level : server.getAllLevels()) {
            if (level.getEntity(id) instanceof LivingEntity living && living.isAlive()) return living;
        }
        return null;
    }

    private static LivingEntity find(ServerPlayer viewer, UUID id) {
        return find(viewer.server, id);
    }

    private static String describe(ServerPlayer viewer, LivingEntity servant) {
        if (hidden(viewer, servant)) return Component.translatable("ability.lotmcraft.servants.concealed").getString();
        return Math.round(servant.getHealth()) + "/" + Math.round(servant.getMaxHealth()) + "  " + servant.blockPosition().getX() + " " + servant.blockPosition().getY() + " " + servant.blockPosition().getZ();
    }

    private static boolean hidden(ServerPlayer viewer, LivingEntity servant) {
        return LightConcealmentAbility.isHidden(servant)
                || MindConcealmentAbility.isHiddenFrom(viewer, servant)
                || VisionaryHandler.shouldStayInvisible(BeyonderData.getSequence(viewer), servant);
    }

    public static boolean bound(UUID first, UUID second) {
        if (first == null || second == null || first.equals(second)) return false;
        for (Map.Entry<UUID, Map<UUID, Mark>> entry : marks.entrySet()) {
            boolean firstServant = servantOf(entry.getValue(), first);
            boolean secondServant = servantOf(entry.getValue(), second);
            boolean firstMaster = entry.getKey().equals(first);
            boolean secondMaster = entry.getKey().equals(second);
            if ((firstMaster && secondServant) || (secondMaster && firstServant) || (firstServant && secondServant)) return true;
        }
        return false;
    }

    private static void syncAllies(MinecraftServer server) {
        for (Map.Entry<UUID, Map<UUID, Mark>> entry : marks.entrySet()) {
            List<LivingEntity> group = new ArrayList<>();
            LivingEntity master = find(server, entry.getKey());
            if (master != null) group.add(master);
            for (Map.Entry<UUID, Mark> servantEntry : entry.getValue().entrySet()) {
                if (!active(servantEntry.getValue())) continue;
                LivingEntity servant = find(server, servantEntry.getKey());
                if (servant != null) group.add(servant);
            }
            for (int i = 0; i < group.size(); i++) {
                for (int j = i + 1; j < group.size(); j++) link(group.get(i), group.get(j));
            }
        }
    }

    private static void link(LivingEntity first, LivingEntity second) {
        if (AllyUtil.isAlly(first, second.getUUID()) && AllyUtil.isAlly(second, first.getUUID())) return;
        AllyUtil.makeAllies(first, second, false);
    }

    private static boolean inGraph(UUID id) {
        if (marks.containsKey(id)) return true;
        for (Map<UUID, Mark> owned : marks.values()) {
            if (servantOf(owned, id)) return true;
        }
        return false;
    }

    private static boolean servantOf(Map<UUID, Mark> owned, UUID id) {
        Mark mark = owned.get(id);
        return mark != null && active(mark);
    }

    private static boolean active(Mark mark) {
        return mark.accepted || mark.boost || mark.borrowed;
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !inGraph(player.getUUID())) return;
        syncAllies(player.server);
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || event.getEntity() instanceof ServerPlayer || !(event.getEntity() instanceof LivingEntity living)) return;
        if (!inGraph(living.getUUID()) || !(living.level() instanceof ServerLevel level)) return;
        syncAllies(level.getServer());
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof LivingEntity living) || !(living.level() instanceof ServerLevel level)) return;
        if (living instanceof ServerPlayer master && marks.containsKey(master.getUUID())) {
            Map<UUID, Mark> owned = new HashMap<>(marks.remove(master.getUUID()));
            for (Map.Entry<UUID, Mark> entry : owned.entrySet()) clearEffects(master.server, master.getUUID(), entry.getKey(), entry.getValue());
        }
        release(level.getServer(), living.getUUID());
    }

    private static final class Mark {
        private final String name;
        private boolean accepted;
        private boolean boost;
        private boolean borrowed;

        private Mark(String name) {
            this.name = name;
        }
    }
}
