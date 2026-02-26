package kitminty.redefinedlight;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.ConfigValue<Boolean> EnableClock;
    public static final ModConfigSpec.ConfigValue<Boolean> EnableRain;
    public static final ModConfigSpec.ConfigValue<Integer> RainSpeed;
    public static final ModConfigSpec.ConfigValue<Integer> XRotation;
    public static final ModConfigSpec.ConfigValue<Integer> YRotation;
    public static final ModConfigSpec.ConfigValue<Integer> ZRotation;
    public static final ModConfigSpec.ConfigValue<Double> XPosition;
    public static final ModConfigSpec.ConfigValue<Double> YPosition;
    public static final ModConfigSpec.ConfigValue<Double> ZPosition;

    static {
        BUILDER.push("Configs");
        EnableClock = BUILDER.comment("Enable Clock Rotation On Halo").define("enable_halo_clock_rotation", true);
        EnableRain = BUILDER.comment("Enable Clock Rotation On Halo").define("enable_rainbow", true);
        RainSpeed = BUILDER.comment("Speed Of Rainbow").define("rain_speed", 1);
        XRotation = BUILDER.comment("X Rotation On Halo").define("halo_x_rotation", 0);
        YRotation = BUILDER.comment("Y Rotation On Halo").define("halo_y_rotation", 0);
        ZRotation = BUILDER.comment("Z Rotation On Halo").define("halo_z_rotation", 30);
        XPosition = BUILDER.comment("X Position On Halo").define("halo_x_position", 0.2);
        YPosition = BUILDER.comment("Y Position On Halo").define("halo_y_position", -0.65);
        ZPosition = BUILDER.comment("Z Position On Halo").define("halo_z_position", 0.0);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
