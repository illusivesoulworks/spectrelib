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
import com.illusivesoulworks.spectrelib.config.SpectreConfigPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;

public class ConfigSyncPacket extends SpectreConfigPayload {

  public ConfigSyncPacket(byte[] contents, String fileName) {
    super(contents, fileName);
  }

  public ConfigSyncPacket(FriendlyByteBuf buf) {
    super(buf);
  }

  public void encoder(FriendlyByteBuf buffer) {
    this.write(buffer);
  }

  public static ConfigSyncPacket decoder(FriendlyByteBuf buffer) {
    return new ConfigSyncPacket(buffer);
  }

  public static void messageConsumer(ConfigSyncPacket packet, CustomPayloadEvent.Context ctx) {
    ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
        () -> () -> SpectreConfigNetwork.acceptSyncedConfigs(packet.contents, packet.fileName)));
    ctx.setPacketHandled(true);
  }
}
