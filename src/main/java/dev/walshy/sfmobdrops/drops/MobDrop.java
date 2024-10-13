package dev.walshy.sfmobdrops.drops;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MobDrop {

    @Nullable
    private final EntityType dropsFrom;
    private final boolean allMobs;
    @Nullable
    private final String entityName;
    @Nullable
    private final NamespacedKey entityNbtTag;
    @Nonnull
    private final Set<Drop> drops;
}
