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
public class ServerMain {

    public static void main(String[] args) {
    	try {
    		String logFile = ServerMain.class.getResource("/log4j.properties").getFile();
    		PropertyConfigurator.configure(logFile);
    		
    		//new HttpServer(5555, "xnetter.http.test").start();
			//HttpConf conf = new HttpConf(5555);

			HttpConf conf = Utils.makeConf(5555);
			// 指定客户端可上传的文件保存路径
			conf.setUploadDir("D:\\work\\svnrepos\\wztc_work\\Code\\");
			// 指定可在客户端下载的文件路径
			conf.addDownloadDir("download");
			// 指定可展示在客户端的文件路径
			conf.addDisplayDir("resources");

			HttpServer server = new HttpServer(conf, "xnetter.http.test");
			server.registFilter(new HttpValidFilter());
			server.start();

    		while (true) {
    			Thread.sleep(100);
    		}
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }
}
