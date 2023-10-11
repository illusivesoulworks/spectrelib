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
import io.netty.buffer.Unpooled;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

public class SpectreQuiltMod implements ModInitializer {

  public static final ResourceLocation CONFIG_SYNC =
      new ResourceLocation(SpectreConstants.MOD_ID, "config_sync");

  @Override
  public void onInitialize(ModContainer modContainer) {
    ServerLifecycleEvents.STARTING.register(SpectreConfigEvents::onLoadServer);
    ServerLifecycleEvents.STOPPED.register(server -> SpectreConfigEvents.onUnloadServer());
    ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
      ServerPlayer serverPlayer = handler.getPlayer();
      List<FriendlyByteBuf> configData = SpectreConfigNetwork.getConfigSync();

      if (!configData.isEmpty()) {

        for (FriendlyByteBuf configDatum : configData) {
          ServerPlayNetworking.send(serverPlayer, CONFIG_SYNC, configDatum);
        }
        ServerPlayNetworking.send(serverPlayer, CONFIG_SYNC,
            new FriendlyByteBuf(Unpooled.buffer()));
      }
    });
  }
}
