package com.github.steveice10.netty.handler.codec.socksx.v5;

import com.github.steveice10.netty.handler.codec.socksx.AbstractSocksMessage;
import com.github.steveice10.netty.handler.codec.socksx.SocksVersion;

public abstract class AbstractSocks5Message extends AbstractSocksMessage implements Socks5Message {
  public final SocksVersion version() {
    return SocksVersion.SOCKS5;
  }
}
