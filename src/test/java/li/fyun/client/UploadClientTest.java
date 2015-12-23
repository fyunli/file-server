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
            String filename = "/Users/fyunli/Downloads/ASshenzhen-20140718-wubo.7z";
            File file = new File(filename);

            new UploadClient().upload(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
