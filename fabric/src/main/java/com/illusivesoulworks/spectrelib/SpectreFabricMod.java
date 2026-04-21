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
import com.illusivesoulworks.spectrelib.config.SpectreConfigPayload;
import java.util.List;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class SpectreFabricMod implements ModInitializer {

  @Override
  public void onInitialize() {
    PayloadTypeRegistry.clientboundPlay()
        .register(SpectreConfigPayload.TYPE, SpectreConfigPayload.STREAM_CODEC);
    ServerLifecycleEvents.SERVER_STARTING.register(SpectreConfigEvents::onLoadServer);
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> SpectreConfigEvents.onUnloadServer());
    ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
      ServerPlayer serverPlayer = handler.getPlayer();
      List<SpectreConfigPayload> configData = SpectreConfigNetwork.getConfigSync();

      if (!configData.isEmpty()) {

        for (SpectreConfigPayload configDatum : configData) {
          ServerPlayNetworking.send(serverPlayer, configDatum);
        }
      }
    });
  }
}
