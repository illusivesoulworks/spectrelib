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
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;

public class SpectreForgePacketHandler {

  private static final int PROTOCOL_VERSION = 1;

  public static SimpleChannel INSTANCE;

  public static void setup() {
    INSTANCE = ChannelBuilder.named(new ResourceLocation(SpectreConstants.MOD_ID, "main"))
        .networkProtocolVersion(PROTOCOL_VERSION)
        .clientAcceptedVersions((status, version) -> true)
        .serverAcceptedVersions((status, version) -> true).simpleChannel();

    INSTANCE.messageBuilder(ConfigSyncPacket.class)
        .encoder(ConfigSyncPacket::encoder)
        .decoder(ConfigSyncPacket::decoder)
        .consumerNetworkThread(ConfigSyncPacket::messageConsumer)
        .add();
  }
}
