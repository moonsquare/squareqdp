package com.qdevelop.utils.cache;

public class SerializCacheBean implements java.io.Serializable{
	private static final long serialVersionUID = -7496630787358561885L;
	Object value;
	public SerializCacheBean(Object obj){
		value = obj;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	
}
