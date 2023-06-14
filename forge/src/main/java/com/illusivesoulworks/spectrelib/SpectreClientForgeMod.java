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

package com.illusivesoulworks.spectrelib;

import com.illusivesoulworks.spectrelib.config.SpectreConfig;
import com.illusivesoulworks.spectrelib.config.SpectreConfigEvents;
import com.illusivesoulworks.spectrelib.config.SpectreConfigTracker;
import com.illusivesoulworks.spectrelib.config.client.screen.ModConfigSelectScreen;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;

public class SpectreClientForgeMod {

  public static void setup() {
    MinecraftForge.EVENT_BUS.addListener(SpectreClientForgeMod::onPlayerLoggedOut);
    ModList.get().forEachModContainer((modId, modContainer) -> {
      Map<String, Map<SpectreConfig.Type, Set<SpectreConfig>>> configs =
          SpectreConfigTracker.INSTANCE.getConfigsByMod();
      Map<SpectreConfig.Type, Set<SpectreConfig>> modConfigs = configs.get(modId);

      if (modConfigs != null && !modConfigs.isEmpty()) {
        int count = modConfigs.values().stream().mapToInt(Set::size).sum();
        SpectreConstants.LOG.info("Registering config screens for mod {} with {} config(s)", modId,
            count);
        String displayName = modContainer.getModInfo().getDisplayName();
        modContainer.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory(
                (mc, screen) -> new ModConfigSelectScreen(modConfigs, screen,
                    Component.literal(displayName))));
      }
    });
  }

  private static void onPlayerLoggedOut(final ClientPlayerNetworkEvent.LoggingOut evt) {

    if (!Minecraft.getInstance().isLocalServer()) {
      SpectreConfigEvents.onUnloadServer();
    }
  }
}
