package xnetter.sock.protocol;

import xnetter.sock.marshal.Octets;

/**
 * 心跳协议
 * @author majikang
 * @create 2019-12-05
 */
public class KeepAlive extends Protocol {

    public static final int TYPEID = 0;

    public static final KeepAlive InsObj = new KeepAlive();
    public static final Octets InsData = Octets.wrap(new byte[]{0});

    @Override
    public int getTypeId() {
        return 0;
    }

    @Override
    public Protocol newObject() {
        return new KeepAlive();
    }

    @Override
    public void marshal(Octets bs) {
    }

    @Override
    public void unmarshal(Octets bs) {
    }
}
