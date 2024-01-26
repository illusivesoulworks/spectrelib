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
import com.illusivesoulworks.spectrelib.network.ConfigSyncPacket;
import com.illusivesoulworks.spectrelib.network.SpectreClientPayloadHandler;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;

@Mod(SpectreConstants.MOD_ID)
public class SpectreNeoForgeMod {

  public SpectreNeoForgeMod(IEventBus eventBus) {
    NeoForge.EVENT_BUS.addListener(this::onServerAboutToStart);
    NeoForge.EVENT_BUS.addListener(this::onServerStopped);
    NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
    eventBus.addListener(this::loadConfigs);
    eventBus.addListener(this::clientSetup);
    eventBus.addListener(this::registerPayloadHandler);
  }

  private void registerPayloadHandler(final RegisterPayloadHandlerEvent evt) {
    evt.registrar(SpectreConstants.MOD_ID).play(ConfigSyncPacket.ID, ConfigSyncPacket::new,
        handler -> handler.client(SpectreClientPayloadHandler.getInstance()::handleData));
  }

  private void loadConfigs(final NewRegistryEvent evt) {
    SpectreConfigEvents.onLoadGlobal();
  }

  private void clientSetup(final FMLClientSetupEvent evt) {
    SpectreClientNeoForgeMod.setup();
  }

  private void onServerAboutToStart(final ServerAboutToStartEvent evt) {
    SpectreConfigEvents.onLoadServer(evt.getServer());
  }

  private void onServerStopped(final ServerStoppedEvent evt) {
    SpectreConfigEvents.onUnloadServer();
  }

  private void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent evt) {
    Player player = evt.getEntity();

    if (player instanceof ServerPlayer serverPlayer) {
      List<FriendlyByteBuf> configData = SpectreConfigNetwork.getConfigSync();

      if (!configData.isEmpty()) {

        for (FriendlyByteBuf configDatum : configData) {
          PacketDistributor.PLAYER.with(serverPlayer).send(new ConfigSyncPacket(configDatum));
        }
        PacketDistributor.PLAYER.with(serverPlayer)
            .send(new ConfigSyncPacket(new FriendlyByteBuf(Unpooled.buffer())));
      }
    }
  }
}
