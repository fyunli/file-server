package li.fyun.server;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fyunli on 15/12/23.
 */
class FileUploadLog {

    private static final Map<String, FileUploadLog> simpleMapCache = new HashMap<String, FileUploadLog>();

    private String sourceId;
    private String absolutePath;
    private long length;

    private FileUploadLog(String sourceId, String absolutePath) {
        this.sourceId = sourceId;
        this.absolutePath = absolutePath;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public static FileUploadLog findlog(String sourceId) {
        return simpleMapCache.get(sourceId);
    }

    public static FileUploadLog createLog(String sourceId, File saveFile) {
        FileUploadLog fileLog = new FileUploadLog(sourceId, saveFile.getAbsolutePath());
        simpleMapCache.put(sourceId, fileLog);
        return fileLog;
    }

    public static void deleteLog(String sourceId) {
        if (simpleMapCache.containsKey(sourceId)) {
            simpleMapCache.remove(sourceId);
        }
    }

}
