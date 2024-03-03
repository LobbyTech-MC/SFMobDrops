package dev.walshy.sfmobdrops;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;

import dev.walshy.sfmobdrops.drops.MobDrop;
import dev.walshy.sfmobdrops.drops.Drop;

public class Config {
    
    private SfMobDrops instance;

    public Config(SfMobDrops instance) {
        this.instance = instance;
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public Set<MobDrop> loadConfig() {
        final Set<MobDrop> mobDrops = new HashSet<>();

        final List<Map<String, Object>> list = (List<Map<String, Object>>) instance.getConfig().getList("drops");
        if (list == null || list.isEmpty()) return mobDrops;

        for (Map<String, Object> map : list) {
            final MobDrop.MobDropBuilder builder = MobDrop.builder();

            final String entity = (String) map.get("entity");
            if (entity == null) {
                logSkipMsg("'entity' is not defined");
                continue;
            }

            // Load Entity
            if (!Constants.CONSTANT.asMatchPredicate().test(entity)) {
                logSkipMsg("'entity' should be in SCREAMING_SNAKE_CASE");
                continue;
            } else if (entity.equals("ALL")) {
                builder.allMobs(true);
            } else {
                EntityType type;
                try {
                    type = EntityType.valueOf(entity);
                } catch(Exception e) {
                    logSkipMsg("Invalid entity type: " + entity);
                    continue;
                }

                builder.dropsFrom(type);
            }

            // Load name (optional)
            final String name = (String) map.get("name");
            if (name != null) {
                builder.entityName(ChatColor.translateAlternateColorCodes('&', name));
            }

            // Load nbtTag (optional)
            final String nbtTag = (String) map.get("nbtTag");
            if (nbtTag != null) {
                if (!Constants.NAMESPACE.asMatchPredicate().test(nbtTag)) {
                    logSkipMsg("'nbtTag' should be a valid namespace - e.g. 'some_plugin:custom_mob'");
                    continue;
                } else {
                    NamespacedKey key = NamespacedKey.fromString(nbtTag);

                    if (key == null) {
                        logSkipMsg("Invalid nbtTag: " + nbtTag);
                        continue;
                    }

                    builder.entityNbtTag(key);
                }
            }

            // Load the mob drop
            // See if this is the new drop format
            final List<Map<String, Object>> dropsMap = (List<Map<String, Object>>) map.get("drops");
            Set<Drop> drops = dropsMap != null ? loadDrop(dropsMap) : loadLegacyDrop(map);
            if (drops == null) {
                continue;
            }
            builder.drops(drops);

            mobDrops.add(builder.build());
        }

        return mobDrops;
    }

    /*
     * New format looks like:
     * 
     * - entity: ALL
     *   drops:
     *   - slimefunItem: ANCIENT_RUNE_BLANK
     *       chance: 0.5
     *   - slimefunItem: MAGIC_LUMP_1
     *       chance: 1
     * 
     * @return Null if the drop was invalid otherwise return the drops
     */
    @Nullable
    private Set<Drop> loadDrop(@Nonnull List<Map<String, Object>> map) {
        Set<Drop> drops = new HashSet<>();

        for (Map<String, Object> dropMap : map) {
            final Drop.DropBuilder builder = Drop.builder();

            // Load Slimefun item
            final String slimefunId = (String) dropMap.get("slimefunItem");
            if (slimefunId == null) {
                logSkipMsg("'slimefunItem' is not defined");
                return null;
            }
            builder.slimefunItem(slimefunId);

            // Load chance
            Object chanceObj = dropMap.get("chance");
            if (chanceObj == null) {
                logSkipMsg("'chance' is not defined");
                return null;
            }
            builder.chance(getDouble(chanceObj));

            // Load amount (optional)
            builder.amount(getInt(dropMap.get("amount")));

            drops.add(builder.build());
        }

        return drops;
    }

    /*
     * Legacy format looks like:
     * - entity: ZOMBIE
     *   slimefunItem: MAGICAL_ZOMBIE_PILLS
     *   chance: 4.20
     * 
     * @return Null if the drop was invalid otherwise return the drops
     */
    @Nullable
    private Set<Drop> loadLegacyDrop(@Nonnull Map<String, Object> map) {
        instance.getLogger().warning("Loading legacy drop for " + map.get("entity") + ". "
            + "Please update to the new format. "
            + "The legacy format will be removed in the future."
        );
        final Drop.DropBuilder builder = Drop.builder();

        // Load Slimefun item
        final String slimefunId = (String) map.get("slimefunItem");
        if (slimefunId == null) {
            logSkipMsg("'slimefunItem' is not defined");
            return null;
        }
        builder.slimefunItem(slimefunId);

        // Load chance
        Object chanceObj = map.get("chance");
        if (chanceObj == null) {
            logSkipMsg("'chance' is not defined");
            return null;
        }
        builder.chance(getDouble(chanceObj));

        // Load amount (optional)
        builder.amount(getInt(map.get("amount")));

        return Set.of(builder.build());
    }

    private void logSkipMsg(@Nonnull String reason) {
        instance.getLogger().warning(reason + ". Skipping invalid drop");
    }

    private int getInt(@Nullable Object obj) {
        return obj == null ? 1 : (int) obj;
    }

    private double getDouble(@Nullable Object obj) {
        return obj == null ? 1 : obj instanceof Integer ? (int) obj : (double) obj;
    }
}
