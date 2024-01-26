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

package com.illusivesoulworks.spectrelib.platform;

import com.illusivesoulworks.spectrelib.platform.services.IConfigHelper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;

public class NeoForgeConfigHelper implements IConfigHelper {

  private static final LevelResource SERVERCONFIG = new LevelResource("serverconfig");

  @Override
  public Path getBackwardsCompatiblePath() {
    return FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath());
  }

  @Override
  public Path getGlobalConfigPath() {
    return FMLPaths.CONFIGDIR.get();
  }

  @Override
  public Path getServerConfigPath(final MinecraftServer server) {
    final Path serverConfig = server.getWorldPath(SERVERCONFIG);

    if (!Files.isDirectory(serverConfig)) {
      try {
        Files.createDirectories(serverConfig);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return serverConfig;
  }

  @Override
  public boolean isDedicatedServer() {
    return FMLLoader.getDist() == Dist.DEDICATED_SERVER;
  }
}
