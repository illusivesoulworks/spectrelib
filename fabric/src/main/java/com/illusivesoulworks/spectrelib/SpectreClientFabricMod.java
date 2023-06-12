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

import com.illusivesoulworks.spectrelib.config.SpectreConfigEvents;
import com.illusivesoulworks.spectrelib.config.SpectreConfigNetwork;
import com.illusivesoulworks.spectrelib.config.SpectreLibInitializer;
import com.illusivesoulworks.spectrelib.platform.FabricConfigHelper;
import java.io.File;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.impl.entrypoint.EntrypointUtils;
import net.minecraft.client.main.GameConfig;

public class SpectreClientFabricMod implements ClientModInitializer {

  @Override
  public void onInitializeClient() {
    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {

      if (!client.isLocalServer()) {
        SpectreConfigEvents.onUnloadServer();
      }
    });
    ClientPlayNetworking.registerGlobalReceiver(SpectreFabricMod.CONFIG_SYNC,
        (client, handler, buf, responseSender) -> {
          buf.retain();
          client.execute(() -> SpectreConfigNetwork.handleConfigSync(buf));
        });
  }

  public static void prepareConfigs(GameConfig gameConfig) {
    File file = gameConfig.location.gameDirectory;
    FabricConfigHelper.gameDir = file.toPath();
    EntrypointUtils.invoke("spectrelib", SpectreLibInitializer.class,
        SpectreLibInitializer::onInitializeConfig);
    SpectreConfigEvents.onLoadGlobal();
  }
}
