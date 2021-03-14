package com.github.steveice10.netty.handler.ssl;

import java.util.List;
import java.util.Set;

public interface CipherSuiteFilter {
  String[] filterCipherSuites(Iterable<String> paramIterable, List<String> paramList, Set<String> paramSet);
}
