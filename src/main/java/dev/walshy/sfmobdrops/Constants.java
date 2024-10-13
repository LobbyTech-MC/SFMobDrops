package dev.walshy.sfmobdrops;

import java.util.regex.Pattern;

public final class Constants {

    public static final Pattern CONSTANT = Pattern.compile("[A-Z0-9_]+");
    public static final Pattern NAMESPACE = Pattern.compile("[a-z0-9_]+:[a-z0-9_]+");

    private Constants() {}
}
