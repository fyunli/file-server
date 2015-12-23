package li.fyun.server;

import com.google.gson.Gson;
import li.fyun.ResponseMessage;
import li.fyun.StreamUtils;
import li.fyun.UploadHeader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by fyunli on 15/12/23.
 */
public class SocketTask implements Runnable {

    static Logger logger = LoggerFactory.getLogger(SocketTask.class);

    private Socket socket = null;
    static final Gson GSON = new Gson();

    public SocketTask(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            logger.debug("accepted connection " + socket.getInetAddress() + ":" + socket.getPort());

            PushbackInputStream inStream = new PushbackInputStream(socket.getInputStream());

            String jsonHeader = StreamUtils.readLine(inStream);
            logger.debug("header: {}", jsonHeader);

            // TODO: validate file
            if (StringUtils.isNotBlank(jsonHeader)) {
                UploadHeader uploadHeader = GSON.fromJson(jsonHeader, UploadHeader.class);

                FileUploadLog log = null;
                if (StringUtils.isNotBlank(uploadHeader.getSourceId())) {
                    log = FileUploadLog.findlog(uploadHeader.getSourceId()); //查找上传的文件是否存在上传记录
                }

                File file = null;
                if (log == null) {//如果不存在上传记录,为文件添加跟踪记录
                    file = createFile(uploadHeader);

                    // TODO: handle the duplicate file name

                    log = FileUploadLog.createLog(uploadHeader.getSourceId(), file);
                } else {// 如果存在上传记录,读取已经上传的数据长度
                    file = new File(log.getAbsolutePath());//从上传记录中得到文件的路径
                    if (!file.exists()) {
                        log.setLength(0); // 文件找不到,复位
                    }
                }
                logger.debug("uploading file {}", file.getAbsolutePath());

                OutputStream outStream = socket.getOutputStream();

                String response = getResponseMessage(log);

                //服务器收到客户端的请求信息后，给客户端返回响应信息：position指示客户端从文件的什么位置开始上传
                outStream.write(response.getBytes());

                receiveFile(inStream, uploadHeader.getContentLength(), file, log);
                inStream.close();
                outStream.close();
                file = null;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private File createFile(UploadHeader uploadHeader) {
        String path = new SimpleDateFormat("yyyyMMdd").format(new Date());
        File dir = new File("file/" + path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, uploadHeader.getFilename());
    }

    private String getResponseMessage(FileUploadLog log) {
        ResponseMessage responseMessage = new ResponseMessage(
                0,
                "start upload from the position " + log.getLength(),
                log.getSourceId(),
                log.getLength()
        );
        return GSON.toJson(responseMessage) + "\r\n";
    }

    private void receiveFile(PushbackInputStream inStream, long contentLength, File file, FileUploadLog log)
            throws IOException {
        RandomAccessFile fileOutStream = new RandomAccessFile(file, "rwd");
        long position = log.getLength();
        if (position == 0) {
            fileOutStream.setLength(contentLength);
        }

        fileOutStream.seek(position);//指定从文件的特定位置开始写入数据
        byte[] buffer = new byte[1024];
        int len = -1;
        long length = position;
        while ((len = inStream.read(buffer)) != -1) {//从输入流中读取数据写入到文件中
            fileOutStream.write(buffer, 0, len);
            length += len;
            log.setLength(length);
        }

        if (length == fileOutStream.length()) {
            FileUploadLog.deleteLog(log.getSourceId());
        }

        fileOutStream.close();
    }
}