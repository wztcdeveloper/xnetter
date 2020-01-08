package xnetter.sock.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xnetter.sock.core.Coder;
import xnetter.sock.core.Handler;
import xnetter.sock.core.Manager;
import xnetter.sock.marshal.MarshalException;
import xnetter.sock.marshal.Octets;
import xnetter.utils.ClassScaner;
import xnetter.utils.TimeUtil;

/**
 * 对协议进行编解码
 * @author majikang
 * @create 2019-12-05
 */
public class ProtocolCoder extends Coder {

	private static final Logger logger = LoggerFactory.getLogger(ProtocolCoder.class);
	private static final ThreadLocal<Octets> localOss
            = ThreadLocal.withInitial(() -> new Octets(10240));
	
	protected final int maxMsgSize;
    protected final Octets inputBuff;
    protected final Octets oneMessageBuff;
    
    private final Map<Integer, Protocol> protocols = new HashMap<>();

	public ProtocolCoder(Manager manager, Handler handler, String packageName) {
		super(manager, handler);
		
		this.maxMsgSize = manager.conf.maxMsgSize;
		this.inputBuff = new Octets(10240);
        this.oneMessageBuff = new Octets();
      
        if (StringUtils.isNotEmpty(packageName)) {
        	regist(packageName);
        }
	}

    /**
     * 递归扫描所有的Protocol注册
     * @param packageName Protocol对应的包
     */
	public void regist(String packageName) {
		try {
			for (Class<?> clazz : ClassScaner.scan(packageName, Protocol.class, true)) {
				if (!Modifier.isAbstract(clazz.getModifiers())) {
					regist((Protocol)clazz.newInstance());
				}
			}
		} catch (Exception ex) {
			logger.error("regist error.", ex);
		}
	}
	
	public void regist(Protocol p) {
		if (this.protocols.put(p.getTypeId(), p) != null) {
			throw new RuntimeException("protocol duplicate for class: " + p.getTypeId());
        }
	}
	
	public void unregist(Protocol p) {
		this.protocols.remove(p.getTypeId());
	}
	
    private static Octets getAndClear() {
        Octets bs = localOss.get();
        bs.clear();
        return bs;
    }

    /**
     * 将msg序列化到字节流ByteBuf里面，并发送
     * @param msg
     * @param out
     * @throws Exception
     */
	@Override
    protected void toEncode(Object msg, ByteBuf out) throws Exception {
        Octets outputBuff = getAndClear();
        if (msg instanceof Octets) {
            ((Octets) msg).writeTo(out);
        } else if (msg instanceof Protocol) {
        	final Protocol m = (Protocol) msg;
            Protocol.encode(m, outputBuff);
            outputBuff.writeTo(out);
        } else {
        	logger.error("encode error: not support {}", msg.getClass().getName());
        }
    }

    /**
     * 根据协议号ID，从protocols获得创建相应的协议对象，
     * 并从ByteBuf字节流中读取，反序列化到协议对象
     * @param ctx
     * @param in
     * @param out 可能同时有多个协议对象
     * @throws Exception
     */
    @Override
    protected void toDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        inputBuff.readFrom(in);
        
        String remoteAddress = getRemoteAddress(ctx.channel());
        while (inputBuff.nonEmpty()) {
            int mark = inputBuff.readerIndex();
            
            final int size;
            try {
                size = inputBuff.readCompactUint();
                if (size > maxMsgSize) {
                	logger.error("remote[{}] decode size:{} exceed maxsize:{}", remoteAddress, size, maxMsgSize);
                    ctx.close();
                    return;
                }
            } catch (MarshalException e) {
                inputBuff.rollbackReadIndex(mark);
                logger.info("remote[{}] read head not enough.", remoteAddress);
                break;
            } catch (Exception e) {
                e.printStackTrace();
                ctx.close();
                return;
            }
            

            if (size > inputBuff.size()) {
            	logger.info("remote[{}] read body not enough. size:{} remain:{}", remoteAddress, size, inputBuff.size());
                inputBuff.rollbackReadIndex(mark);
                break;
            }
            
            try {
                oneMessageBuff.wrapRead(inputBuff, size);
                
                int msgHeadIndex = oneMessageBuff.readerIndex();
                final Octets os = oneMessageBuff;
                int type = os.readCompactUint();
                
                if (type == KeepAlive.TYPEID) {
                	if (handler != null) {
                		handler.recvKeepAlive(TimeUtil.nowWithMilli());
                	}
                } else {
                	Protocol msg = protocols.get(type);
                    if (msg == null) {
                        oneMessageBuff.rollbackReadIndex(msgHeadIndex);
                        if (!manager.onUnknownMessage(handler, type, os)) {
                        	logger.error("remote[{}] onUnknownMessage type:{} size:{}", remoteAddress, type, size);
                            ctx.close();
                            return;
                        }
                        continue;
                    }

                    msg = (Protocol)msg.newObject();
                    msg.unmarshal(os);
                    out.add(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
                ctx.close();
                return;
            }
        }

        if (inputBuff.empty()) {
            inputBuff.clear();
        }
    }
    
	private String getRemoteAddress(Channel channel) {
		if (channel != null) {
			InetSocketAddress isa = (InetSocketAddress) channel.remoteAddress();
			if (isa != null) {
				return String.format("%s:%d", isa.getAddress().getHostAddress(), isa.getPort());
			}
		} 
		
		return String.format("%s:%d", manager.conf.ip, manager.conf.port);
	}
}
