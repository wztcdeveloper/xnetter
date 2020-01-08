package xnetter.sock.test;

import xnetter.sock.marshal.Octets;
import xnetter.sock.protocol.Protocol;

public final class SChallenge extends Protocol {
    public static final int TYPEID = (int) 50713;

    public final int getTypeId() {
        return (int) TYPEID;
    }

    public int errcode;
    public int index;
    public int reserve;
    
    public SChallenge() {
    
    }

    public SChallenge(int errcode, int index) {
        this.errcode = errcode;
        this.index = index;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getName()).append("{");
        sb.append(",errcode:").append(errcode);
        sb.append(",index:").append(index);
        sb.append(",reserve:").append(reserve);
        sb.append("}");
        return sb.toString();
    }

    public void marshal(Octets bs) {
        bs.writeInt(errcode);
        bs.writeInt(index);
        bs.writeInt(reserve);
    }

    public void unmarshal(Octets bs) {
        errcode = bs.readInt();
        index = bs.readInt();
        reserve = bs.readInt();
    }

    public SChallenge newObject() {
        return new SChallenge();
    }
}
