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
import com.illusivesoulworks.spectrelib.platform.QuiltConfigHelper;
import java.io.File;
import net.minecraft.client.main.GameConfig;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.entrypoint.EntrypointUtil;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.networking.api.client.ClientPlayConnectionEvents;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

public class SpectreClientQuiltMod implements ClientModInitializer {

  public static void prepareConfigs(GameConfig gameConfig) {
    File file = gameConfig.location.gameDirectory;
    QuiltConfigHelper.gameDir = file.toPath();
    EntrypointUtil.invoke("spectrelib", SpectreLibInitializer.class,
        SpectreLibInitializer::onInitializeConfig);
    SpectreConfigEvents.onLoadGlobal();
  }

  @Override
  public void onInitializeClient(ModContainer mod) {
    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {

      if (!client.isLocalServer()) {
        SpectreConfigEvents.onUnloadServer();
      }
    });
    ClientPlayNetworking.registerGlobalReceiver(SpectreQuiltMod.CONFIG_SYNC,
        (client, handler, buf, responseSender) -> {
          buf.retain();
          client.execute(() -> SpectreConfigNetwork.handleConfigSync(buf));
        });
  }
}
