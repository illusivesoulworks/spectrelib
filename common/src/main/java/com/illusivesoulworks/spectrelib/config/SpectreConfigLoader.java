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

package com.illusivesoulworks.spectrelib.config;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class SpectreConfigLoader {

  public static final Marker CONFIG = MarkerFactory.getMarker("CONFIG");

  public static SpectreConfig add(SpectreConfig.Type type, SpectreConfigSpec spec, String modId,
                                  String fileName) {
    SpectreConfig config = new SpectreConfig(type, spec, modId, fileName);
    SpectreConfigTracker.INSTANCE.track(config);
    return config;
  }

  public static SpectreConfig add(SpectreConfig.Type type, SpectreConfigSpec spec, String modId) {
    SpectreConfig config = new SpectreConfig(type, spec, modId);
    SpectreConfigTracker.INSTANCE.track(config);
    return config;
  }
}
