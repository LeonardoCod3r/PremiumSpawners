package centralworks.cache.simple;

import centralworks.quests.models.PlayerQuests;
import centralworks.quests.structure.CraftInterpreter;
import centralworks.quests.structure.CraftQuest;
import centralworks.quests.structure.CraftQuestRule;
import centralworks.events.BoosterActiveEvent;
import centralworks.models.EntityStacked;
import centralworks.lib.ListCache;
import com.google.gson.JsonObject;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class Interpreters extends ListCache<CraftInterpreter<?>> {

    private static Interpreters me;

    public static Interpreters get() {
        return me == null ? me = new Interpreters() : me;
    }

    public void loadDefaults() {
        final CraftInterpreter<BlockBreakEvent> mining = new CraftInterpreter<>("mining");
        mining.setListener(new Listener() {
            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void on(BlockBreakEvent event) {
                final Player p = event.getPlayer();
                final PlayerQuests playerQuests = mining.getPlayerQuests(p.getName());
                playerQuests.findQuestsByInterpreter("mining").forEach(questData -> {
                    if (questData.isActive() && !questData.isCompleted()) {
                        questData.findRulesByInterpreter("mining").forEach(questRule -> {
                            final CraftQuest quest = questData.getQuest();
                            final CraftQuestRule rule = quest.getRules().stream().filter(craftQuestRule -> craftQuestRule.getId().equalsIgnoreCase(questRule.getId())).findFirst().get();
                            final int i = rule.getValueAsInteger();
                            if (questRule.addValueAsInteger(1) >= i) questRule.setCompleted(true);
                            if (questData.isCompleted()) playerQuests.nextQuest(questData);
                        });
                    }
                });
            }
        });
        mining.commit();

        final CraftInterpreter<BlockPlaceEvent> place = new CraftInterpreter<>("place");
        place.setListener(new Listener() {
            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void on(BlockPlaceEvent event) {
                final Player p = event.getPlayer();
                final PlayerQuests playerQuests = place.getPlayerQuests(p.getName());
                playerQuests.findQuestsByInterpreter("place").forEach(questData -> {
                    if (questData.isActive() && !questData.isCompleted()) {
                        questData.findRulesByInterpreter("place").forEach(questRule -> {
                            final CraftQuest quest = questData.getQuest();
                            final CraftQuestRule rule = quest.getRules().stream().filter(craftQuestRule -> craftQuestRule.getId().equalsIgnoreCase(questRule.getId())).findFirst().get();
                            final int i = rule.getValueAsInteger();
                            if (questRule.addValueAsInteger(1) >= i) questRule.setCompleted(true);
                            if (questData.isCompleted()) playerQuests.nextQuest(questData);
                        });
                    }
                });
            }
        });
        place.commit();

        final CraftInterpreter<BoosterActiveEvent> activeBoosterSpawner = new CraftInterpreter<>("activeBoosterSpawner");
        activeBoosterSpawner.setListener(new Listener() {
            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void on(BoosterActiveEvent event) {
                final Player p = event.getPlayer();
                final PlayerQuests playerQuests = place.getPlayerQuests(p.getName());
                playerQuests.findQuestsByInterpreter("activeBoosterSpawner").forEach(questData -> {
                    if (questData.isActive() && !questData.isCompleted()) {
                        questData.findRulesByInterpreter("activeBoosterSpawner").forEach(questRule -> {
                            final CraftQuest quest = questData.getQuest();
                            final CraftQuestRule rule = quest.getRules().stream().filter(craftQuestRule -> craftQuestRule.getId().equalsIgnoreCase(questRule.getId())).findFirst().get();
                            final int i = rule.getValueAsInteger();
                            if (questRule.addValueAsInteger(1) >= i) questRule.setCompleted(true);
                            if (questData.isCompleted()) playerQuests.nextQuest(questData);
                        });
                    }
                });
            }
        });
        activeBoosterSpawner.commit();

        final CraftInterpreter<PlayerDeathEvent> killPlayer = new CraftInterpreter<>("killPlayer");
        killPlayer.setListener(new Listener() {
            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void on(PlayerDeathEvent event) {
                final Player p = event.getEntity().getKiller();
                if (p != null) {
                    final PlayerQuests playerQuests = place.getPlayerQuests(p.getName());
                    playerQuests.findQuestsByInterpreter("killPlayer").forEach(questData -> {
                        if (questData.isActive() && !questData.isCompleted()) {
                            questData.findRulesByInterpreter("killPlayer").forEach(questRule -> {
                                final CraftQuest quest = questData.getQuest();
                                final CraftQuestRule rule = quest.getRules().stream().filter(craftQuestRule -> craftQuestRule.getId().equalsIgnoreCase(questRule.getId())).findFirst().get();
                                final int i = rule.getValueAsInteger();
                                if (questRule.addValueAsInteger(1) >= i) questRule.setCompleted(true);
                                if (questData.isCompleted()) playerQuests.nextQuest(questData);
                            });
                        }
                    });
                }
            }
        });
        killPlayer.commit();

        final CraftInterpreter<EntityDeathEvent> killEntity = new CraftInterpreter<>("killEntity");
        killEntity.setListener(new Listener() {
            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void on(EntityDeathEvent event) {
                final Player p = event.getEntity().getKiller();
                if (p != null && !(event.getEntity() instanceof Player)) {
                    final PlayerQuests playerQuests = place.getPlayerQuests(p.getName());
                    playerQuests.findQuestsByInterpreter("killEntity").forEach(questData -> {
                        if (questData.isActive() && !questData.isCompleted()) {
                            questData.findRulesByInterpreter("killEntity").forEach(questRule -> {
                                final CraftQuest quest = questData.getQuest();
                                final CraftQuestRule rule = quest.getRules().stream().filter(craftQuestRule -> craftQuestRule.getId().equalsIgnoreCase(questRule.getId())).findFirst().get();
                                final double i = rule.getValueAsDouble();
                                if (questRule.addValueAsDouble(new EntityStacked(event.getEntity()).getStack()) >= i)
                                    questRule.setCompleted(true);
                                if (questData.isCompleted()) playerQuests.nextQuest(questData);
                            });
                        }
                    });
                }
            }
        });
        killEntity.commit();

        final CraftInterpreter<CraftItemEvent> craft = new CraftInterpreter<>("craft");
        craft.setListener(new Listener() {
            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void on(CraftItemEvent event) {
                final Player p = ((Player) event.getWhoClicked());
                final PlayerQuests playerQuests = place.getPlayerQuests(p.getName());
                playerQuests.findQuestsByInterpreter("craft").forEach(questData -> {
                    if (questData.isActive() && !questData.isCompleted()) {
                        questData.findRulesByInterpreter("craft").forEach(questRule -> {
                            final CraftQuest quest = questData.getQuest();
                            final CraftQuestRule rule = quest.getRules().stream().filter(craftQuestRule -> craftQuestRule.getId().equalsIgnoreCase(questRule.getId())).findFirst().get();
                            final int i = rule.getValueAsInteger();
                            if (questRule.addValueAsInteger(event.getRecipe().getResult().getAmount()) >= i)
                                questRule.setCompleted(true);
                            if (questData.isCompleted()) playerQuests.nextQuest(questData);
                        });
                    }
                });
            }
        });
        craft.commit();

        final CraftInterpreter<BlockBreakEvent> miningSpecific = new CraftInterpreter<>("miningSpecific");
        miningSpecific.setListener(new Listener() {
            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void on(BlockBreakEvent event) {
                final Player p = event.getPlayer();
                final PlayerQuests playerQuests = mining.getPlayerQuests(p.getName());
                playerQuests.findQuestsByInterpreter("miningSpecific").forEach(questData -> {
                    if (questData.isActive() && !questData.isCompleted()) {
                        questData.findRulesByInterpreter("miningSpecific").forEach(questRule -> {
                            final CraftQuest quest = questData.getQuest();
                            final CraftQuestRule rule = quest.getRules().stream().filter(craftQuestRule -> craftQuestRule.getId().equalsIgnoreCase(questRule.getId())).findFirst().get();
                            final JsonObject additionalInformation = rule.getAdditionalInformation();
                            final int id = additionalInformation.get("id").getAsInt();
                            final int data = additionalInformation.get("data").getAsInt();
                            if (new ItemStack(id, 1, (short) data).isSimilar(event.getBlock().getState().getData().toItemStack())) {
                                final int i = rule.getValueAsInteger();
                                if (questRule.addValueAsInteger(1) >= i) questRule.setCompleted(true);
                                if (questData.isCompleted()) playerQuests.nextQuest(questData);
                            }
                        });
                    }
                });
            }
        });
        miningSpecific.commit();

        final CraftInterpreter<BlockPlaceEvent> placeSpecific = new CraftInterpreter<>("placeSpecific");
        placeSpecific.setListener(new Listener() {
            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void on(BlockPlaceEvent event) {
                final Player p = event.getPlayer();
                final PlayerQuests playerQuests = mining.getPlayerQuests(p.getName());
                playerQuests.findQuestsByInterpreter("placeSpecific").forEach(questData -> {
                    if (questData.isActive() && !questData.isCompleted()) {
                        questData.findRulesByInterpreter("placeSpecific").forEach(questRule -> {
                            final CraftQuest quest = questData.getQuest();
                            final CraftQuestRule rule = quest.getRules().stream().filter(craftQuestRule -> craftQuestRule.getId().equalsIgnoreCase(questRule.getId())).findFirst().get();
                            final JsonObject additionalInformation = rule.getAdditionalInformation();
                            final int id = additionalInformation.get("id").getAsInt();
                            final int data = additionalInformation.get("data").getAsInt();
                            if (new ItemStack(id, 1, (short) data).isSimilar(event.getBlock().getState().getData().toItemStack())) {
                                final int i = rule.getValueAsInteger();
                                if (questRule.addValueAsInteger(1) >= i) questRule.setCompleted(true);
                                if (questData.isCompleted()) playerQuests.nextQuest(questData);
                            }
                        });
                    }
                });
            }
        });
        placeSpecific.commit();

        final CraftInterpreter<EntityDeathEvent> killEntitySpecific = new CraftInterpreter<>("killEntitySpecific");
        killEntitySpecific.setListener(new Listener() {
            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void on(EntityDeathEvent event) {
                final Player p = event.getEntity().getKiller();
                if (p != null && !(event.getEntity() instanceof Player)) {
                    final PlayerQuests playerQuests = place.getPlayerQuests(p.getName());
                    playerQuests.findQuestsByInterpreter("killEntitySpecific").forEach(questData -> {
                        if (questData.isActive() && !questData.isCompleted()) {
                            questData.findRulesByInterpreter("killEntitySpecific").forEach(questRule -> {
                                final CraftQuest quest = questData.getQuest();
                                final CraftQuestRule rule = quest.getRules().stream().filter(craftQuestRule -> craftQuestRule.getId().equalsIgnoreCase(questRule.getId())).findFirst().get();
                                final JsonObject additionalInformation = rule.getAdditionalInformation();
                                final EntityType type = EntityType.valueOf(additionalInformation.get("type").getAsString());
                                if (event.getEntityType() == type) {
                                    final double i = rule.getValueAsDouble();
                                    if (questRule.addValueAsDouble(new EntityStacked(event.getEntity()).getStack()) >= i)
                                        questRule.setCompleted(true);
                                    if (questData.isCompleted()) playerQuests.nextQuest(questData);
                                }
                            });
                        }
                    });
                }
            }
        });
        killEntitySpecific.commit();
    }

}
