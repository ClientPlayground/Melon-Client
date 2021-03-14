package com.github.steveice10.netty.handler.codec.smtp;

import java.util.List;

public interface SmtpRequest {
  SmtpCommand command();
  
  List<CharSequence> parameters();
}
