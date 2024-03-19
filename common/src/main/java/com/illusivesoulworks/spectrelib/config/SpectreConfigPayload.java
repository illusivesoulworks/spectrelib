package com.illusivesoulworks.spectrelib.config;

import net.minecraft.network.FriendlyByteBuf;

public class SpectreConfigPayload {

  public final String fileName;
  public final byte[] contents;

  public SpectreConfigPayload(byte[] contents, String fileName) {
    this.fileName = fileName;
    this.contents = contents;
  }

  public SpectreConfigPayload(FriendlyByteBuf buf) {
    this(buf.readByteArray(), buf.readUtf());
  }

  public void write(FriendlyByteBuf buffer) {
    buffer.writeByteArray(this.contents);
    buffer.writeUtf(this.fileName);
  }
}
