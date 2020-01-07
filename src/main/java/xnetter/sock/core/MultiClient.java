package xnetter.sock.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多个客户端的抽象类，这里可以连接多个服务器
 * @author majikang
 * @create 2019-11-05
 */
public abstract class MultiClient {
	
	private final Dispatcher<?> dispatcher;
	private final Coder.Factory coderFactory;
	private final Handler.Factory handlerFactory;
	
	private Map<Integer, DynamicClient> id2Clients = new ConcurrentHashMap<>();
	private Map<String, DynamicClient> name2Clients = new ConcurrentHashMap<>();
	private List<DynamicClient> clients = new ArrayList<>();

	public MultiClient(Dispatcher<?> dispatcher) {
		this(dispatcher, Coder.Factory.DEFAULT, Handler.Factory.DEFAULT);
	}
	
	public MultiClient(Dispatcher<?> dispatcher, Coder.Factory coderFactory, Handler.Factory handlerFactory) {
		this.dispatcher = dispatcher;	
		this.coderFactory = coderFactory;
		this.handlerFactory = handlerFactory;
	}
	
	public Map<Integer, DynamicClient> getId2Clients() {
		return new HashMap<Integer, DynamicClient>(id2Clients);
	}

	public Map<String, DynamicClient> getName2Clients() {
		return new HashMap<String, DynamicClient>(name2Clients);
	}

	public List<DynamicClient> getClients() {
		return new ArrayList<DynamicClient>(clients);
	}

	public DynamicClient getClientById(int remoteId) {
		return id2Clients.get(remoteId);
	}

	public DynamicClient getClientByName(String name) {
		return name2Clients.get(name);
	}

	protected void onAddClient(DynamicClient client) {
		
	}

	protected void onDelClient(DynamicClient client) {
		unregistClient(client);
	}
	
	public <T> boolean send(int remoteId, T msg) {
    	DynamicClient s = id2Clients.get(remoteId);
        return s != null && s.send(msg);
	}
	
	public <T> boolean send(int remoteId, Collection<T> msgs) {
    	DynamicClient s = id2Clients.get(remoteId);
        return s != null && s.send(msgs);
	}
	
	public <T> void broadcast(T msg) {
		getClients().forEach(s -> s.send(msg));
	}
	
	public <T> void broadcast(Collection<T> msgs) {
		getClients().forEach(s -> s.send(msgs));
	}
	
	private DynamicClient makeClient(String name, Manager.Conf conf) {
		return new DynamicClient(name, conf, dispatcher, coderFactory, handlerFactory);
	}
	
    public synchronized void updateClients(Map<String, Manager.Conf> confs) throws InterruptedException {
    	Map<String, DynamicClient> newClients = new HashMap<>();
    			
        for (Entry<String, Manager.Conf> entry : confs.entrySet()) {
        	DynamicClient s = name2Clients.get(entry.getKey());
            if (s != null) {
            	newClients.put(entry.getKey(), s);
            } else {
                s = makeClient(entry.getKey(), entry.getValue());
                newClients.put(entry.getKey(), s);
                s.start();
            }
        }

        // 把原来存在的，现在不存在的关闭
        for (Entry<String, DynamicClient> entry : name2Clients.entrySet()) {
            if (!confs.containsKey(entry.getKey())) {
            	entry.getValue().close();
            }
        }
        name2Clients = newClients;
    }
	
    /**
     * 建立连接后，需要主动调用该接口注册Client
     * @param remoteId
     * @param client
     */
	protected synchronized void registClient(int remoteId, DynamicClient client) {
        // 如果 remoteId 为 0,这是非法值
        // 如果 id2Clients或者clients包含了client, 说明多次注册
        // 如果 name2Clients 未包含此对象,说明已经在updateServer删除后才注册的
        if (remoteId == 0) {
        	client.close();
        	throw new RuntimeException("remoteId can't be zero.");
        }
        
        if (id2Clients.containsKey(remoteId)) {
        	client.close();
        	throw new RuntimeException(String.format("remoteId(%d) has existed in id2Clients.", remoteId));
        }
        
        if (clients.contains(client)) {
        	client.close();
        	throw new RuntimeException(String.format("client(%d) has existed in id2Clients.", client.toString()));
        }
        
        if (name2Clients.get(client.getName()) != client) {
        	client.close();
        	throw new RuntimeException(String.format("client name(%s) doesn't exist in name2Clients.", client.getName()));
        }

        client.setRemoteId(remoteId);
        clients.add(client);
        id2Clients.put(remoteId, client);
    }
	
	private synchronized void unregistClient(DynamicClient client) {
        int serverId = client.getRemoteId();
        if (serverId == 0) {
            return;
        }
        
        clients.remove(client);
        id2Clients.remove(client.getRemoteId());
	}
	
	public class DynamicClient extends Client {

		private final String name;
        private volatile int remoteId;
        
		protected DynamicClient(String name, Conf conf, Dispatcher<?> dispatcher, 
				Coder.Factory coderFactory, Handler.Factory handlerFactory) {
			super(conf, dispatcher, coderFactory, handlerFactory);
			
			this.name = name;
		}

        public String getName() {
            return name;
        }

        public int getRemoteId() {
            return remoteId;
        }

        public void setRemoteId(int remoteId) {
            this.remoteId = remoteId;
        }
        
		@Override
		protected void onAddHandler(Handler handler) {
			// TODO Auto-generated method stub
			MultiClient.this.onAddClient(this);
		}

		@Override
		protected void onDelHandler(Handler handler) {
			// TODO Auto-generated method stub
			MultiClient.this.onDelClient(this);
		}
		
	}
}
