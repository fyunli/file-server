package li.fyun.server;

import com.google.gson.Gson;
import li.fyun.StreamUtils;
import li.fyun.UploadHeader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fyunli on 15/12/23.
 */
public class FileServer {

    static Logger logger = LoggerFactory.getLogger(FileServer.class);
    static final Gson GSON = new Gson();

    private ExecutorService executorService;//线程池
    private int port;//监听端口
    private boolean quit = false;//退出
    private ServerSocket server;

    public FileServer(int port) {
        this.port = port;
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 50);
        logger.debug("file server started.");
    }

    public void quit() {
        this.quit = true;
        try {
            server.close();
            logger.debug("file server stoped.");
        } catch (IOException e) {
        }
    }

    public void start() throws Exception {
        server = new ServerSocket(port);
        while (!quit) {
            try {
                Socket socket = server.accept();
                executorService.execute(new SocketTask(socket));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private final class SocketTask implements Runnable {
        private Socket socket = null;

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
                    int position = 0;

                    if (log == null) {//如果不存在上传记录,为文件添加跟踪记录
                        String path = new SimpleDateFormat("yyyyMMdd").format(new Date());
                        File dir = new File("file/" + path);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        file = new File(dir, uploadHeader.getFilename());

                        // TODO: handle the duplicate file name

                        logger.debug("file {}", file.getAbsolutePath());
                        log = FileUploadLog.saveLog(uploadHeader.getSourceId(), file);
                    } else {// 如果存在上传记录,读取已经上传的数据长度
                        file = new File(log.getFilePath());//从上传记录中得到文件的路径
                        if (file.exists()) {
                            File logFile = new File(file.getParentFile(), file.getName() + ".log");
                            if (logFile.exists()) {
                                Properties properties = new Properties();
                                properties.load(new FileInputStream(logFile));
                                position = Integer.valueOf(properties.getProperty("length"));//读取已经上传的数据长度

                                logger.debug("position {}", position);
                            }
                        }
                    }
                    logger.debug("log file path {}", log.getFilePath());

                    OutputStream outStream = socket.getOutputStream();
                    String response = "sourceid=" + uploadHeader.getSourceId() + ";position=" + position + "\r\n";

                    //服务器收到客户端的请求信息后，给客户端返回响应信息：sourceid=1274773833264;position=0
                    //sourceid由服务器端生成，唯一标识上传的文件，position指示客户端从文件的什么位置开始上传
                    outStream.write(response.getBytes());

                    RandomAccessFile fileOutStream = new RandomAccessFile(file, "rwd");
                    if (position == 0) {
                        fileOutStream.setLength(Long.valueOf(uploadHeader.getContentLength()));//设置文件长度
                    }

                    fileOutStream.seek(position);//指定从文件的特定位置开始写入数据
                    byte[] buffer = new byte[1024];
                    int len = -1;
                    int length = position;
                    while ((len = inStream.read(buffer)) != -1) {//从输入流中读取数据写入到文件中
                        fileOutStream.write(buffer, 0, len);
                        length += len;
                        Properties properties = new Properties();
                        properties.put("length", String.valueOf(length));
                        FileOutputStream logFile = new FileOutputStream(new File(file.getParentFile(), file.getName() + ".log"));
                        properties.store(logFile, null);//实时记录已经接收的文件长度
                        logFile.close();
                    }
                    if (length == fileOutStream.length()) {
                        FileUploadLog.deleteLog(uploadHeader.getSourceId());
                    }

                    fileOutStream.close();
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
    }


    public static void main(String[] args) {
        FileServer s = new FileServer(7878);
        try {
            s.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
