package easysocket.serialize;

import java.nio.ByteBuffer;

public interface Serializable {
	void serialize(ByteBuffer bb);
}
