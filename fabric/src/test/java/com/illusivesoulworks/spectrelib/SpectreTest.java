package com.illusivesoulworks.spectrelib;

import com.illusivesoulworks.spectrelib.config.SpectreConfig;
import com.illusivesoulworks.spectrelib.config.SpectreConfigLoader;
import com.illusivesoulworks.spectrelib.config.SpectreLibInitializer;
import com.illusivesoulworks.spectrelib.config.SpectreTestConfig;

public class SpectreTest implements SpectreLibInitializer {

  @Override
  public void onInitializeConfig() {
    SpectreConfigLoader.add(SpectreConfig.Type.CLIENT, SpectreTestConfig.CLIENT_SPEC,
        "spectretest");
    SpectreConfigLoader.add(SpectreConfig.Type.COMMON, SpectreTestConfig.COMMON_SPEC,
        "spectretest");
    SpectreConfigLoader.add(SpectreConfig.Type.SERVER, SpectreTestConfig.SERVER_SPEC,
        "spectretest");
  }
}
