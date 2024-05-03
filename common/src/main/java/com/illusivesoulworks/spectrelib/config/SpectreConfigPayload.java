package com.illusivesoulworks.spectrelib.config;

import com.illusivesoulworks.spectrelib.SpectreConstants;
import javax.annotation.Nonnull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class SpectreConfigPayload implements CustomPacketPayload {

  public static final Type<SpectreConfigPayload> TYPE =
      new Type<>(new ResourceLocation(SpectreConstants.MOD_ID, "sync"));
  public static final StreamCodec<RegistryFriendlyByteBuf, SpectreConfigPayload>
      STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.BYTE_ARRAY,
          packet -> packet.contents,
          ByteBufCodecs.STRING_UTF8,
          packet -> packet.fileName,
          SpectreConfigPayload::new);

  public final String fileName;
  public final byte[] contents;

  public SpectreConfigPayload(byte[] contents, String fileName) {
    this.fileName = fileName;
    this.contents = contents;
  }

  public SpectreConfigPayload(FriendlyByteBuf buf) {
    this(buf.readByteArray(), buf.readUtf());
  }

  @Nonnull
  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
