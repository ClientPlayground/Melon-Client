package com.replaymod.replaystudio.us.myles.ViaVersion.api.type;

import com.github.steveice10.netty.buffer.ByteBuf;

public interface ByteBufReader<T> {
  T read(ByteBuf paramByteBuf) throws Exception;
}
