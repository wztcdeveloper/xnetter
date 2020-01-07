package xnetter.sock.marshal;

public interface IMarshal {
	void marshal(Octets bs);

    void unmarshal(Octets bs);
    
    IMarshal newObject();
    
    default IMarshal deepClone() {
    	Octets bs = new Octets();
    	this.marshal(bs);
    	
    	IMarshal newObj = newObject();
    	newObj.unmarshal(bs);
    	return newObj;
    }
}
