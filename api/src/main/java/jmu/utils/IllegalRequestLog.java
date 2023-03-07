package jmu.utils;

import org.apache.commons.lang.StringUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 *
 */
public class IllegalRequestLog {
    public static void addCategoryStr(long id){
        String filePath = "D:\\Desktop/categoryLog.txt";
        String str = new Date().toString() + "-记录到非法参数categoryId=" + String.valueOf(id) + ",BloomFilter已拦截" + "\r\n";
        FileWriter fwriter = null;
        try {
            fwriter = new FileWriter(filePath, true);
            fwriter.write(str);

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                fwriter.flush();
                fwriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
