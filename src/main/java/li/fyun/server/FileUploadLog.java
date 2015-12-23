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
    private String filePath;
    private long position;

    private FileUploadLog(String sourceId, String filePath){
        this.sourceId = sourceId;
        this.filePath = filePath;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getFilePath() {
        return filePath;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public static FileUploadLog findlog(String sourceid) {
        return simpleMapCache.get(sourceid);
    }

    public static FileUploadLog saveLog(String sourceId, File saveFile) {
        FileUploadLog fileLog = new FileUploadLog(sourceId, saveFile.getAbsolutePath());
        simpleMapCache.put(sourceId, fileLog);
        return fileLog;
    }

    public static void deleteLog(String sourceid) {
        if (simpleMapCache.containsKey(sourceid)) simpleMapCache.remove(sourceid);
    }

}
