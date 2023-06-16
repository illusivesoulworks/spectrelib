package com.illusivesoulworks.spectrelib.integration;

import com.illusivesoulworks.spectrelib.SpectreConstants;
import com.illusivesoulworks.spectrelib.config.SpectreConfig;
import com.illusivesoulworks.spectrelib.config.SpectreConfigTracker;
import com.illusivesoulworks.spectrelib.config.client.screen.ModConfigSelectScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import org.quiltmc.loader.api.QuiltLoader;

public class ModMenuPlugin implements ModMenuApi {

  @Override
  public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
    Map<String, Map<SpectreConfig.Type, Set<SpectreConfig>>> configs =
        SpectreConfigTracker.INSTANCE.getConfigsByMod();
    Map<String, ConfigScreenFactory<?>> result = new HashMap<>();
    configs.forEach((key, modConfigs) -> {
      QuiltLoader.getModContainer(key).ifPresent(modContainer -> {
        int count = modConfigs.values().stream().mapToInt(Set::size).sum();
        SpectreConstants.LOG.info("Registering config screens for mod {} with {} config(s)", key,
            count);
        String displayName = modContainer.metadata().name();
        result.put(key, screen -> new ModConfigSelectScreen(modConfigs, screen,
            Component.literal(displayName)));
      });
    });
    return result;
  }
}
