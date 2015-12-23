package li.fyun.client;

import org.junit.Test;

import java.io.*;

/**
 * Created by fyunli on 15/12/23.
 */
public class UploadClientTest {

    @Test
    public void testUpload() {
        try {
            String filename = "/Users/fyunli/Downloads/Java Servlet Tutorial Cookbook.pdf";
            File file = new File(filename);

            new UploadClient().upload(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
