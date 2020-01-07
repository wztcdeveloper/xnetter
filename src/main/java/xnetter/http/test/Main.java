package xnetter.http.test;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.log4j.PropertyConfigurator;

import xnetter.http.core.HttpRouter;
import xnetter.http.core.HttpServer;
import xnetter.sock.core.Processor;
import xnetter.sock.marshal.IMarshal;
import xnetter.sock.marshal.Octets;
import xnetter.sock.protocol.Protocol;
import xnetter.utils.ReflectUtil;

/**
 * Hello world!
 *
 */
public class Main {
	public static class Object1 extends Protocol {

		@Override
		public void marshal(Octets bs) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void unmarshal(Octets bs) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public IMarshal newObject() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getTypeId() {
			// TODO Auto-generated method stub
			return 0;
		}
		
	}
	
	public static class AP implements Processor<Object1> {

		@Override
		public void process(Object1 msg) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
    public static void main(String[] args) {
    	try {
    		AP ap = new AP();

    		Class<?> Object1Class = Object1.class;
    		Class<Protocol> pClass = (Class<Protocol>)Object1Class;
    		
    		String path = HttpServer.class.getResource("/").getPath();
    		PropertyConfigurator.configure(path + "/log4j.properties");
    		
    		new HttpServer().start(5555, new HttpRouter("xnetter.http.test"));

    		while (true) {
    			Thread.sleep(100);
    		}
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }
}
