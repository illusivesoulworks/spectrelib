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

import com.illusivesoulworks.spectrelib.SpectreConstants;
import com.illusivesoulworks.spectrelib.config.SpectreConfigPayload;
import javax.annotation.Nonnull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class ConfigSyncPacket extends SpectreConfigPayload implements CustomPacketPayload {

  public static final ResourceLocation ID = new ResourceLocation(SpectreConstants.MOD_ID, "sync");

  public ConfigSyncPacket(byte[] contents, String fileName) {
    super(contents, fileName);
  }

  public ConfigSyncPacket(FriendlyByteBuf buf) {
    super(buf);
  }

  @Nonnull
  @Override
  public ResourceLocation id() {
    return ID;
  }
}
