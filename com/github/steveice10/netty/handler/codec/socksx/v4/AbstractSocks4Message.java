package com.github.steveice10.netty.handler.codec.socksx.v4;

import com.github.steveice10.netty.handler.codec.socksx.AbstractSocksMessage;
import com.github.steveice10.netty.handler.codec.socksx.SocksVersion;

public abstract class AbstractSocks4Message extends AbstractSocksMessage implements Socks4Message {
  public final SocksVersion version() {
    return SocksVersion.SOCKS4a;
  }
}
