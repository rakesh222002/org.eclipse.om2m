package org.onem2m.sdt.args;

//import org.apache.commons.codec.binary.Base64;
import org.onem2m.sdt.types.DataType;

public class BlobArg extends ValuedArg<byte[]> {

	public BlobArg(String name) {
		super(name, DataType.Blob);
	}

//	public void setValue(String v) {
//		setValue(Base64.decodeBase64(v));
//	}
//	
//	public void setValue(byte[] v) {
//		setValue(Base64.encodeBase64(v));
//	}

}
