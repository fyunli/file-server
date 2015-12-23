package li.fyun;

import java.io.Serializable;

/**
 * Created by fyunli on 15/12/23.
 */
public class ResponseMessage implements Serializable {

    int error;
    String message;
    String sourceId;
    long position;

    public ResponseMessage(int error, String message, String sourceId, long position) {
        this.error = error;
        this.message = message;
        this.sourceId = sourceId;
        this.position = position;
    }

    public int getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getSourceId() {
        return sourceId;
    }

    public long getPosition() {
        return position;
    }
}
