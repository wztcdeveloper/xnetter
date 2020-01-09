package xnetter.http.test;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.log4j.PropertyConfigurator;

import xnetter.http.core.HttpConf;
import xnetter.http.core.HttpRouter;
import xnetter.http.core.HttpServer;
import xnetter.sock.core.Action;
import xnetter.sock.marshal.IMarshal;
import xnetter.sock.marshal.Octets;
import xnetter.sock.protocol.Protocol;
import xnetter.utils.ReflectUtil;

/**
 * Hello world!
 *
 */
public class Main {

	private static HttpConf makeConf(int port) {
		String keyFile = Main.class.getResource("/netter.keystore").getPath();
		return new HttpConf(port, true, keyFile,
				"654321", "123456");
	}

    public static void main(String[] args) {
    	try {
    		String logFile = HttpServer.class.getResource("/log4j.properties").getFile();
    		PropertyConfigurator.configure(logFile);
    		
    		//new HttpServer(5555, "xnetter.http.test").start();
			new HttpServer(makeConf(5555), "xnetter.http.test").start();

    		while (true) {
    			Thread.sleep(100);
    		}
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }
}
