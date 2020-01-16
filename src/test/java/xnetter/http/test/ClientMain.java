package xnetter.http.test;

import xnetter.http.core.HttpClient;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public final class ClientMain {
    public static void main(String[] args)
            throws MalformedURLException,
            InterruptedException, UnsupportedEncodingException, KeyManagementException, NoSuchAlgorithmException {
        String url = "https://restapi.amap.com/v3/weather/weatherInfo?output=json&city=350212&extensions=base&key=1efb9a5b0110cd95a313903090fed3a2";
        //String url = "http://manage.mingyugh.com/home/home.html";
        new HttpClient(new HttpClient.Handler() {
            @Override
            public void onRecv(String content) {
                System.out.println(content);
            }
        }, true).get(url);
    }
}
