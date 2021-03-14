package com.github.steveice10.netty.handler.ssl;

import java.security.AlgorithmConstraints;
import javax.net.ssl.SSLParameters;

final class Java7SslParametersUtils {
  static void setAlgorithmConstraints(SSLParameters sslParameters, Object algorithmConstraints) {
    sslParameters.setAlgorithmConstraints((AlgorithmConstraints)algorithmConstraints);
  }
}
