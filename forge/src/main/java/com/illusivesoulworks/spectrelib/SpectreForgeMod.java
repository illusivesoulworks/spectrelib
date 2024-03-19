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
import com.illusivesoulworks.spectrelib.network.ConfigSyncPacket;
import com.illusivesoulworks.spectrelib.network.SpectreForgePacketHandler;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.NewRegistryEvent;

@Mod(SpectreConstants.MOD_ID)
public class SpectreForgeMod {

  public SpectreForgeMod() {
    MinecraftForge.EVENT_BUS.addListener(this::onServerAboutToStart);
    MinecraftForge.EVENT_BUS.addListener(this::onServerStopped);
    MinecraftForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
    IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
    eventBus.addListener(this::loadConfigs);
    eventBus.addListener(this::setup);
    eventBus.addListener(this::clientSetup);
  }

  private void loadConfigs(final NewRegistryEvent evt) {
    SpectreConfigEvents.onLoadGlobal();
  }

  private void setup(final FMLCommonSetupEvent evt) {
    SpectreForgePacketHandler.setup();
  }

  private void clientSetup(final FMLClientSetupEvent evt) {
    SpectreClientForgeMod.setup();
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
      List<SpectreConfigPayload> configData = SpectreConfigNetwork.getConfigSync();

      if (!configData.isEmpty()) {

        for (SpectreConfigPayload configDatum : configData) {
          SpectreForgePacketHandler.INSTANCE.send(
              new ConfigSyncPacket(configDatum.contents, configDatum.fileName),
              PacketDistributor.PLAYER.with(serverPlayer));
        }
      }
    }
  }
}
