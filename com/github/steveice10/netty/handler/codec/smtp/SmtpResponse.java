package com.github.steveice10.netty.handler.codec.smtp;

import java.util.List;

public interface SmtpResponse {
  int code();
  
  List<CharSequence> details();
}
