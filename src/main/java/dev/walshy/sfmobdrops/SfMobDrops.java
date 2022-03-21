package dev.walshy.sfmobdrops;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import net.guizhanss.guizhanlib.updater.GuizhanBuildsUpdater;
import org.bstats.bukkit.Metrics;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class SfMobDrops extends JavaPlugin implements Listener {

    private static SfMobDrops instance;

    private final Set<Drop> drops = new HashSet<>();

    @Override
    public void onEnable() {
        setInstance(this);
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
        }

        if (getConfig().getBoolean("settings.autoUpdate", true) && getDescription().getVersion().startsWith("Build")) {
            new GuizhanBuildsUpdater(this, getFile(), "SlimefunGuguProject", "SFMobDrops", "main", false).start();
        }

        new Metrics(this, 11950);

        loadConfig();

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new Guis(), this);

        getCommand("mobdrops").setExecutor(new MobDropsCommand());
    }

    @Override
    public void onDisable() {
        setInstance(null);
    }

    protected void loadConfig() {
        final Set<Drop> newSet = new HashSet<>();

        //noinspection unchecked
        final List<Map<String, Object>> list = (List<Map<String, Object>>) getConfig().getList("drops");
        if (list == null || list.isEmpty()) return;

        for (Map<String, Object> map : list) {
            // If some arguments are invalid then just miss it.
            if (!validateArguments(map)) {
                continue;
            }

            final EntityType type = entityFromString((String) map.get("entity"));
            final String slimefunId = (String) map.get("slimefunItem");
            final double chance = getDouble(map.get("chance"));

            final String entityName = (String) map.get("name");
            final String nbtTag = (String) map.get("nbtTag");
            final int amount = map.get("amount") == null ? 1 : (int) map.get("amount");

            newSet.add(new Drop(type, slimefunId, chance, entityName, nbtTag, amount));
        }

        this.drops.clear();
        this.drops.addAll(newSet);
        getLogger().info("已加载 " + this.drops.size() + " 个掉落物配置!");
    }

    @EventHandler
    public void onMobDeath(@Nonnull EntityDeathEvent e) {
        final Drop drop = findDropFromEntity(e.getEntity());

        if (drop != null && ThreadLocalRandom.current().nextDouble(100) <= drop.getChance()) {
            final SlimefunItem item = SlimefunItem.getById(drop.getSlimefunId());

            if (item != null && !item.isDisabledIn(e.getEntity().getWorld())) {
                final ItemStack dropping = item.getItem().clone();
                dropping.setAmount(drop.getAmount());

                e.getDrops().add(dropping);
            }
        }
    }

    private boolean validateArguments(@Nonnull Map<String, Object> map) {
        final String entity = (String) map.get("entity");
        final String sfItem = (String) map.get("slimefunItem");
        final double chance = getDouble(map.get("chance"));

        final String nbtTag = (String) map.get("nbtTag");
        final int amount = map.get("amount") == null ? 1 : (int) map.get("amount");

        // Required
        if (entity == null || sfItem == null) {
            getLogger().warning("未指定 'entity', 'slimefunItem' 以及 'chance'!");
            return false;
        }

        if (!Constants.CONSTANT.matcher(entity).matches()) {
            getLogger().warning("实体类型格式不正确!");
            return false;
        }

        if (!Constants.CONSTANT.matcher(sfItem).matches()) {
            getLogger().warning("粘液科技物品 ID 格式不正确!");
            return false;
        }

        if (chance < 1 || chance > 100) {
            getLogger().warning("掉落几率无效，有效值为：0-100");
            return false;
        }

        // Not required
        if (nbtTag != null && !Constants.NAMESPACE.matcher(nbtTag).matches()) {
            getLogger().warning("NBT标签格式不正确!");
            return false;
        }

        if (amount < 1 || amount > 64) {
            getLogger().warning("数量无效，有效值为：0-64");
            return false;
        }

        // Validate values
        if (entityFromString(entity) == null) {
            getLogger().warning("无效的实体类型: " + entity + " - 可通过该网址查询: "
                + "https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html");
            return false;
        }

        if (SlimefunItem.getById(sfItem) == null) {
            getLogger().warning("无效的粘液科技物品 ID: " + sfItem + " - 可通过该网址查询: "
                + "https://slimefun-helper.guizhanss.cn/");
            return false;
        }

        return true;
    }

    @Nullable
    private EntityType entityFromString(@Nonnull String str) {
        try {
            return EntityType.valueOf(str);
        } catch (IllegalArgumentException e) {
            getLogger().log(Level.WARNING, "无效的实体类型: {0}", str);
            return null;
        }
    }

    @Nullable
    private Drop findDropFromEntity(@Nonnull LivingEntity entity) {
        for (Drop drop : this.drops) {
            if (entity.getType() == drop.getDropsFrom()) {
                if (drop.getEntityName() != null && entity.getCustomName() != null
                    && !Constants.color(drop.getEntityName()).equals(entity.getCustomName())
                ) {
                    continue;
                }

                if (drop.getEntityNbtTag() != null && entity.getPersistentDataContainer().getKeys().stream()
                    .noneMatch(key -> key.toString().equals(drop.getEntityNbtTag()))
                ) {
                    continue;
                }

                return drop;
            }
        }
        return null;
    }

    private double getDouble(@Nonnull Object obj) {
        return obj instanceof Integer ? (int) obj : (double) obj;
    }

    @Nonnull
    public Set<Drop> getDrops() {
        return drops;
    }

    @Nonnull
    public static SfMobDrops getInstance() {
        return instance;
    }

    private static void setInstance(SfMobDrops ins) {
        instance = ins;
    }
}
