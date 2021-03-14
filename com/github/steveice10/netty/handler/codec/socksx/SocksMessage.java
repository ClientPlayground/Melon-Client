package com.github.steveice10.netty.handler.codec.socksx;

import com.github.steveice10.netty.handler.codec.DecoderResultProvider;

public interface SocksMessage extends DecoderResultProvider {
  SocksVersion version();
}
