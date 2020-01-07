package xnetter.sock.test;

import xnetter.sock.marshal.Octets;
import xnetter.sock.protocol.Protocol;

public final class CChallenge extends Protocol {
    public static final int TYPEID = (int) 14727;

    public final int getTypeId() {
        return (int) TYPEID;
    }

    public int index;
    public int reserve;
    
    public CChallenge() {}

    public CChallenge(int index) {
        this.index = index;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getName()).append("{");
        sb.append(",index:").append(index);
        sb.append(",reserve:").append(reserve);
        sb.append("}");
        return sb.toString();
    }

    public void marshal(Octets bs) {
        bs.writeInt(index);
        bs.writeInt(reserve);
    }

    public void unmarshal(Octets bs) {
        index = bs.readInt();
        reserve = bs.readInt();
    }

    public CChallenge newObject() {
        return new CChallenge();
    }
}
