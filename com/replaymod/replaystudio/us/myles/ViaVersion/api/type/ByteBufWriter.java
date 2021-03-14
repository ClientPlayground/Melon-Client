package com.replaymod.replaystudio.us.myles.ViaVersion.api.type;

import com.github.steveice10.netty.buffer.ByteBuf;

public interface ByteBufWriter<T> {
  void write(ByteBuf paramByteBuf, T paramT) throws Exception;
}
