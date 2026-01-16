package com.ypyglobal.xradio.stream.ssl;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: http://ypyglobal.com
 */
public class XRadioHttpsTrustManager implements X509TrustManager {

    private static final X509Certificate[] acceptedIssuers = new X509Certificate[0];
    private static TrustManager[] trustManagers;

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return acceptedIssuers;
    }

    public static void trustAllHttps() {
        try {
            if (trustManagers == null) {
                trustManagers = new TrustManager[]{new XRadioHttpsTrustManager()};
            }
            HttpsURLConnection.setDefaultHostnameVerifier((s, sslSession) -> true);
            SSLContext sSLContext = null;
            try {
                sSLContext = SSLContext.getInstance("TLS");
                sSLContext.init((KeyManager[]) null, trustManagers, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sSLContext.getSocketFactory());
            }
            catch (NoSuchAlgorithmException | KeyManagementException e) {
                e.printStackTrace();
                if (sSLContext != null) {
                    HttpsURLConnection.setDefaultSSLSocketFactory(sSLContext.getSocketFactory());
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
}
