package com.google.common.io;

import java.io.IOException;

@Deprecated
public interface OutputSupplier<T> {
  T getOutput() throws IOException;
}
