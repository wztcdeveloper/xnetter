package xnetter.http.test;


import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.log4j.PropertyConfigurator;

import xnetter.Utils;
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

    public static void main(String[] args) {
    	try {
    		String logFile = Main.class.getResource("/log4j.properties").getFile();
    		PropertyConfigurator.configure(logFile);
    		
    		//new HttpServer(5555, "xnetter.http.test").start();
			//HttpConf conf = new HttpConf(5555);
			HttpConf conf = Utils.makeConf(5555);
			conf.setUploadDir("D:\\work\\svnrepos\\wztc_work\\Code\\");
			conf.addDownloadDir("download");
			new HttpServer(conf, "xnetter.http.test").start();

    		while (true) {
    			Thread.sleep(100);
    		}
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }
}
