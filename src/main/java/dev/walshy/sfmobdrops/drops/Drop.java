package dev.walshy.sfmobdrops.drops;

import javax.annotation.Nonnull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Drop {

    @Nonnull
    private final String slimefunItem;
    private final double chance;
    private final int amount;
}
