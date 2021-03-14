package net.optifine.http;

public interface HttpListener {
  void finished(HttpRequest paramHttpRequest, HttpResponse paramHttpResponse);
  
  void failed(HttpRequest paramHttpRequest, Exception paramException);
}
