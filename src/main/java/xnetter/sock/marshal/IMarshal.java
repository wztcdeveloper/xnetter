package xnetter.sock.marshal;

/**
 * 序列化接口
 * @author majikang
 * @create 2019-12-05
 */
public interface IMarshal {
	/**
	 * 将对象序列化成字节流到bs
	 * @param bs
	 */
	void marshal(Octets bs);

	/**
	 * 从bs中读取字节流，反序列化成对象
	 * @param bs
	 */
    void unmarshal(Octets bs);

	/**
	 * 创建新的对象
	 * @param
	 */
    IMarshal newObject();

	/**
	 * 采用序列化的方式深度拷贝对象
	 * @return
	 */
	default IMarshal deepClone() {
    	Octets bs = new Octets();
    	this.marshal(bs);
    	
    	IMarshal newObj = newObject();
    	newObj.unmarshal(bs);
    	return newObj;
    }
}
