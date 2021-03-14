package com.github.steveice10.netty.util;

import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.Promise;

public interface AsyncMapping<IN, OUT> {
  Future<OUT> map(IN paramIN, Promise<OUT> paramPromise);
}
