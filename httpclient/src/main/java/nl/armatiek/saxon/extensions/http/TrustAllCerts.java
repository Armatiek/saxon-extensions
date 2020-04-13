package nl.armatiek.saxon.extensions.http;

import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class TrustAllCerts {
  
  private static TrustManager[] trustAllTrustManager = new TrustManager[] { new X509TrustManager() {
    
    @Override
    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
      return new java.security.cert.X509Certificate[]{};
    }

    @Override
    public void checkClientTrusted(X509Certificate[] certs, String authType) { }

    @Override
    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
    
  }};
  
  private static HostnameVerifier trustAllHostnameVerifier = new HostnameVerifier() {
    
    @Override
    public boolean verify(String hostname, SSLSession session) {
      return true;
    }
    
  };
  
  private static SSLContext sslContext;
  static {
    try {
      sslContext = SSLContext.getInstance("SSL");
      sslContext.init(null, trustAllTrustManager, new java.security.SecureRandom());
    } catch (Exception e) {
      //
    }
  }
  
  public static void setTrustAllCerts(final OkHttpClient.Builder clientBuilder) {
    clientBuilder
      .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllTrustManager[0])
      .hostnameVerifier(trustAllHostnameVerifier);
  }

}