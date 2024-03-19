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

package com.illusivesoulworks.spectrelib.network;

import com.illusivesoulworks.spectrelib.config.SpectreConfigNetwork;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class SpectreClientPayloadHandler {

  private static final SpectreClientPayloadHandler INSTANCE = new SpectreClientPayloadHandler();

  public static SpectreClientPayloadHandler getInstance() {
    return INSTANCE;
  }

  public void handleData(final ConfigSyncPacket packet, final PlayPayloadContext ctx) {
    ctx.workHandler().submitAsync(
            () -> SpectreConfigNetwork.acceptSyncedConfigs(packet.contents, packet.fileName))
        .exceptionally(e -> {
          ctx.packetHandler()
              .disconnect(Component.translatable("spectrelib.networking.failed", e.getMessage()));
          return null;
        });
  }
}
