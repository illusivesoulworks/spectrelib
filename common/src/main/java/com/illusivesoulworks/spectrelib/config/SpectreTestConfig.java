package com.illusivesoulworks.spectrelib.config;

import java.util.Arrays;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorMaterials;
import org.apache.commons.lang3.tuple.Pair;

public class SpectreTestConfig {

  public static final SpectreConfigSpec CLIENT_SPEC;
  public static final SpectreConfigSpec COMMON_SPEC;
  public static final SpectreConfigSpec SERVER_SPEC;
  public static final Test CLIENT_TEST;
  public static final Test COMMON_TEST;
  public static final Test SERVER_TEST;

  static {
    Pair<Test, SpectreConfigSpec> testSpecPair =
        new SpectreConfigSpec.Builder().configure(Test::new);
    CLIENT_SPEC = testSpecPair.getRight();
    CLIENT_TEST = testSpecPair.getLeft();

    testSpecPair =
        new SpectreConfigSpec.Builder().configure(Test::new);
    COMMON_SPEC = testSpecPair.getRight();
    COMMON_TEST = testSpecPair.getLeft();

    testSpecPair =
        new SpectreConfigSpec.Builder().configure(Test::new);
    SERVER_SPEC = testSpecPair.getRight();
    SERVER_TEST = testSpecPair.getLeft();
  }

  public static class Test {

    public final SpectreConfigSpec.IntValue intValue;
    public final SpectreConfigSpec.DoubleValue doubleValue;
    public final SpectreConfigSpec.LongValue longValue;
    public final SpectreConfigSpec.BooleanValue booleanValue;
    public final SpectreConfigSpec.EnumValue<ArmorMaterials> enumValue;
    public final SpectreConfigSpec.ConfigValue<String> stringValue;
    public final SpectreConfigSpec.ConfigValue<List<? extends String>> stringList;
    public final SpectreConfigSpec.ConfigValue<List<? extends String>> validatedList;

    public Test(SpectreConfigSpec.Builder builder) {

      this.intValue = builder.comment("Integer Value Comment").defineInRange("intVal", 0, -10, 10);
      this.doubleValue =
          builder.comment("Double Value Comment").defineInRange("doubleVal", 0.0D, -10.0D, 10.0D);
      this.booleanValue = builder.comment("Boolean Value Comment").define("booleanValue", false);
      this.longValue =
          builder.comment("Long Value Comment").defineInRange("longValue", 0L, -10L, 10L);

      builder.comment("Nested Comment").push("nested");
      this.stringValue =
          builder.comment("String Value Comment").define("stringValue", "String Value");
      this.enumValue =
          builder.comment("Enum Value Comment").defineEnum("enumValue", ArmorMaterials.CHAIN);
      builder.pop();

      builder.push("second nested");
      this.stringList = builder.comment("String List Comment")
          .defineList("stringList", Arrays.asList("first", "second", "third"),
              s -> s instanceof String);
      this.validatedList = builder.comment("Validated List Comment").defineList("listOfItems",
          Arrays.asList("minecraft:diamond", "minecraft:emerald", "minecraft:stone"),
          s -> s instanceof String s1 && ResourceLocation.isValidResourceLocation(s1));
      builder.pop();
    }
  }
}
