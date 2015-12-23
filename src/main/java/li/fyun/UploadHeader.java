package li.fyun;

import java.io.Serializable;

/**
 * Created by fyunli on 15/12/23.
 */
public class UploadHeader implements Serializable {

    long contentLength;
    String filename;
    String sourceId;

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }
}
