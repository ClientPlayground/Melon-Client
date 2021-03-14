package com.github.steveice10.netty.handler.ssl;

import java.security.Principal;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.security.auth.x500.X500Principal;

final class OpenSslExtendedKeyMaterialManager extends OpenSslKeyMaterialManager {
  private final X509ExtendedKeyManager keyManager;
  
  OpenSslExtendedKeyMaterialManager(X509ExtendedKeyManager keyManager, String password) {
    super(keyManager, password);
    this.keyManager = keyManager;
  }
  
  protected String chooseClientAlias(ReferenceCountedOpenSslEngine engine, String[] keyTypes, X500Principal[] issuer) {
    return this.keyManager.chooseEngineClientAlias(keyTypes, (Principal[])issuer, engine);
  }
  
  protected String chooseServerAlias(ReferenceCountedOpenSslEngine engine, String type) {
    return this.keyManager.chooseEngineServerAlias(type, null, engine);
  }
}
