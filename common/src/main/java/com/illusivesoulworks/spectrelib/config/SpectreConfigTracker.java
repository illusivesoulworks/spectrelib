/*
 * Copyright (C) 2022 Illusive Soulworks
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; you
 * may only use version 2.1 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <https://www.gnu.org/licenses/>.
 */

package com.illusivesoulworks.spectrelib.config;

import static com.illusivesoulworks.spectrelib.config.SpectreConfigLoader.CONFIG;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.google.common.collect.ImmutableMap;
import com.illusivesoulworks.spectrelib.SpectreConstants;
import com.illusivesoulworks.spectrelib.platform.Services;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.io.FilenameUtils;

public class SpectreConfigTracker {

  public static final SpectreConfigTracker INSTANCE = new SpectreConfigTracker();

  private final ConcurrentHashMap<String, SpectreConfig> files = new ConcurrentHashMap<>();
  private final EnumMap<SpectreConfig.Type, Set<SpectreConfig>> configsByType =
      new EnumMap<>(SpectreConfig.Type.class);
  private final ConcurrentHashMap<String, Map<SpectreConfig.Type, Set<SpectreConfig>>>
      configsByMod = new ConcurrentHashMap<>();
  private final Map<String, Map<String, Object>> defaultConfigs = new ConcurrentHashMap<>();

  private SpectreConfigTracker() {

    for (SpectreConfig.Type value : SpectreConfig.Type.values()) {
      this.configsByType.put(value, Collections.synchronizedSet(new LinkedHashSet<>()));
    }
  }

  void track(final SpectreConfig config) {

    if (config.getType() == SpectreConfig.Type.CLIENT && Services.CONFIG.isDedicatedServer()) {
      return;
    }
    if (this.files.containsKey(config.getFileName())) {
      SpectreConstants.LOG.error(CONFIG, "Detected config file conflict {} between {} and {}",
          config.getFileName(), this.files.get(config.getFileName()).getModId(), config.getModId());
      throw new RuntimeException("Conflicting config files");
    }
    this.files.put(config.getFileName(), config);
    this.configsByType.get(config.getType()).add(config);
    this.configsByMod.computeIfAbsent(config.getModId(),
        (k) -> new EnumMap<>(SpectreConfig.Type.class)).computeIfAbsent(config.getType(),
        (k) -> Collections.synchronizedSet(new LinkedHashSet<>())).add(config);
    SpectreConstants.LOG.debug(CONFIG, "Config file {} for {} added to tracking",
        config.getFileName(), config.getModId());
  }

  void loadGlobalConfigs() {
    SpectreConstants.LOG.debug(CONFIG, "Loading global configs");
    this.files.values().forEach(config -> {
      SpectreConstants.LOG.trace(CONFIG, "Loading config file type {} at {} for {}",
          config.getType(), config.getFileName(), config.getModId());
      Path path = Services.CONFIG.getGlobalConfigPath();
      boolean alreadyExists = Files.exists(path.resolve(config.getFileName()));
      final CommentedFileConfig configData = read(path).apply(config);
      SpectreConfig.InstanceType type = SpectreConfig.InstanceType.GLOBAL;
      config.setConfigData(type, configData, !alreadyExists);
      config.fireLoad(false);
      config.save(type);
    });
  }

  void loadServerConfigs(MinecraftServer server) {
    Path configDir = Services.CONFIG.getServerConfigPath(server);
    AtomicReference<SpectreConfig.InstanceType> type =
        new AtomicReference<>(SpectreConfig.InstanceType.SERVER);
    SpectreConstants.LOG.debug(CONFIG, "Loading {} configs from {} on server", type.get().id(),
        configDir);
    this.files.values().forEach(config -> {
      Path dir = null;
      String name = config.getFileName();
      Path configPath = configDir.resolve(name);

      if (Files.exists(configPath)) {
        dir = configDir;
      } else {
        Path globalDir = Services.CONFIG.getGlobalConfigPath();
        Path globalPath = globalDir.resolve(name);

        if (Files.exists(globalPath)) {
          type.set(SpectreConfig.InstanceType.GLOBAL);
          dir = globalDir;
        }
      }

      if (dir != null) {
        SpectreConstants.LOG.trace(CONFIG, "Loading config file type {} at {} from {} for {}",
            config.getType(), config.getFileName(), dir, config.getModId());
        final CommentedFileConfig configData = read(dir).apply(config);
        config.setConfigData(type.get(), configData, false);
        config.fireLoad(false);
        config.save(type.get());
      }
    });
  }

  void unloadServerConfigs() {
    SpectreConstants.LOG.debug(CONFIG, "Unloading server configs");
    this.files.values().forEach(config -> {
      SpectreConstants.LOG.trace(CONFIG, "Unloading config file type {} at {}", config.getType(),
          config.getFileName());
      config.save(SpectreConfig.InstanceType.SERVER);
      config.clearServerConfigData();
    });
  }

  static void tryConfigFileLoad(FileConfig configData) {
    try {
      configData.load();
    } catch (ParsingException e) {
      try {
        Path path = configData.getNioPath();
        backupConfig(path);
        Files.delete(path);
        configData.load();
        SpectreConstants.LOG.warn("Configuration file {} could not be parsed. Correcting", path);
        return;
      } catch (Throwable t) {
        e.addSuppressed(t);
      }
      throw e;
    }
  }

  private static void backupConfig(Path path) {

    if (Files.exists(path)) {
      Path dir = path.getParent();
      String fileName = path.getFileName().toString();
      String extension = FilenameUtils.getExtension(fileName) + ".bak";
      fileName = FilenameUtils.removeExtension(fileName);

      try {
        Files.copy(path, dir.resolve(fileName + "." + extension));
      } catch (IOException e) {
        SpectreConstants.LOG.warn("Failed to backup configuration file {}", path, e);
      }
    }
  }

  private void tryDefaultConfigLoad(SpectreConfig modConfig) {
    String fileName = modConfig.getFileName();
    Path path = Services.CONFIG.getBackwardsCompatiblePath().resolve(fileName);

    if (Files.exists(path)) {

      try (CommentedFileConfig config = CommentedFileConfig.of(path)) {
        config.load();
        Map<String, Object> values = config.valueMap();

        if (values != null && !values.isEmpty()) {
          this.defaultConfigs.put(fileName.intern(), ImmutableMap.copyOf(values));
        }
        SpectreConstants.LOG.info(CONFIG, "Loaded default config values from file at path {}",
            path);
      } catch (Exception e) {
        SpectreConstants.LOG.error(CONFIG,
            "Error loading default config values from file at path {}", path);
        e.printStackTrace();
      }
    }
  }

  Function<SpectreConfig, CommentedFileConfig> read(Path basePath) {
    return (config) -> {
      final Path configPath = basePath.resolve(config.getFileName());
      final CommentedFileConfig configData =
          CommentedFileConfig.builder(configPath, TomlFormat.instance()).sync().
              preserveInsertionOrder().
              autosave().
              onFileNotFound(this::setupConfigFile).
              writingMode(WritingMode.REPLACE).
              build();
      SpectreConstants.LOG.debug(CONFIG, "Built TOML config for {}", configPath);
      try {
        tryConfigFileLoad(configData);
      } catch (ParsingException ex) {
        SpectreConstants.LOG.error(CONFIG, "Error loading TOML config for {}", configPath);
        throw new ConfigLoadingException(config, ex);
      }
      this.tryDefaultConfigLoad(config);
      SpectreConstants.LOG.debug(CONFIG, "Loaded TOML config file {}", configPath);
      return configData;
    };
  }

  private boolean setupConfigFile(final Path path, final ConfigFormat<?> conf) throws IOException {
    Files.createDirectories(path.getParent());
    Path p = Services.CONFIG.getBackwardsCompatiblePath().resolve(path.getFileName());

    if (Files.exists(p)) {
      SpectreConstants.LOG.info(CONFIG, "Loading default config file from path {}", p);
      Files.copy(p, path);
    } else {
      Files.createFile(path);
      conf.initEmptyFile(path);
    }
    return true;
  }

  public Map<String, Map<String, Object>> getDefaultConfigs() {
    return ImmutableMap.copyOf(this.defaultConfigs);
  }

  public Map<String, Map<SpectreConfig.Type, Set<SpectreConfig>>> getConfigsByMod() {
    return ImmutableMap.copyOf(this.configsByMod);
  }

  public void acceptSyncedConfigs(String fileName, byte[] data) {
    SpectreConfig configFile = this.files.get(fileName);

    if (configFile != null) {
      final CommentedConfig configData =
          TomlFormat.instance().createParser().parse(new ByteArrayInputStream(data));
      configFile.setConfigData(SpectreConfig.InstanceType.SERVER, configData, false);
      configFile.fireLoad(true);
    }
  }

  public Map<String, byte[]> getConfigSync() {
    return this.configsByType.get(SpectreConfig.Type.SERVER).stream().collect(
        Collectors.toMap(SpectreConfig::getFileName, file -> {
          try {
            return Files.readAllBytes(file.getFullPath());
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }));
  }

  private static class ConfigLoadingException extends RuntimeException {

    public ConfigLoadingException(SpectreConfig config, Exception cause) {
      super("Failed loading config file " + config.getFileName() + " of type " + config.getType() +
          " for " + config.getModId(), cause);
    }
  }
}
