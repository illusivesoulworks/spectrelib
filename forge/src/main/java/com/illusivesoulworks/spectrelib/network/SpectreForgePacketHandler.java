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
import com.illusivesoulworks.spectrelib.config.SpectreConfigNetwork;
import com.illusivesoulworks.spectrelib.config.SpectreConfigPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;
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

    INSTANCE.messageBuilder(SpectreConfigPayload.class)
        .encoder(
            (payload, friendlyByteBuf) -> SpectreConfigPayload.STREAM_CODEC.encode(friendlyByteBuf,
                payload))
        .decoder(SpectreConfigPayload.STREAM_CODEC::decode)
        .consumerNetworkThread(SpectreForgePacketHandler::messageConsumer)
        .add();
  }

  private static void messageConsumer(SpectreConfigPayload payload,
                                      CustomPayloadEvent.Context ctx) {
    ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
        () -> () -> SpectreConfigNetwork.acceptSyncedConfigs(payload.contents, payload.fileName)));
    ctx.setPacketHandled(true);
  }
}
