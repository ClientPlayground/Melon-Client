package com.github.steveice10.netty.handler.codec.smtp;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class SmtpUtils {
  static List<CharSequence> toUnmodifiableList(CharSequence... sequences) {
    if (sequences == null || sequences.length == 0)
      return Collections.emptyList(); 
    return Collections.unmodifiableList(Arrays.asList(sequences));
  }
}
