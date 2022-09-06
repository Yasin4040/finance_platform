package com.jtyjy.finance.manager.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Stack;

/**
 * Description:
 * Created by ZiYao Lee on 2021/11/16.
 * Time: 17:58
 */
public class FileUtils {
    public static final int BUFFER_SIZE = 16384;

    public static String getFileType(String fileName){
        return fileName.substring(fileName.lastIndexOf(".")+1);
    }

    public static String getFileName(String fileId) {
        String fileName = fileId.substring(fileId.lastIndexOf('/')+1);
        return fileName;
    }


    /**
     *
     * @author wufugui
     * 创建时间:2015-6-11 下午01:57:55
     * @param fileSize
     * @return
     */
    public static String getSizeStr(Long fileSize) {
        String str="";
        fileSize = fileSize == null ? 0 :fileSize;
        if(fileSize<1024*1024){
            BigDecimal bd= BigDecimal.valueOf(Float.valueOf(fileSize)/1024.0);
            bd = bd.setScale(2, RoundingMode.HALF_UP);
            str=bd+" kb";
        }else{
            BigDecimal bd= BigDecimal.valueOf(Float.valueOf(fileSize)/(1024.0*1024.0));
            bd = bd.setScale(2, RoundingMode.HALF_UP);
            str=bd+" M";
        }
        return str;
    }

    /**
     * 得到文件的后缀   如 .zip,包含.
     * @author wufugui
     * 创建时间:2014-12-27 上午11:36:10
     * @param fileName
     * @return
     */
    public static String getFileType1(String fileName){
        return fileName.substring(fileName.lastIndexOf("."));
    }

    public static String getFileNameKey(String fileName){
        return fileName.lastIndexOf(".")==-1?fileName: fileName.substring(0,fileName.lastIndexOf("."));
    }

    /**
     * 计算出上传文件的大小（字节）
     * @author wufugui
     * 创建时间:2014-9-19 下午02:32:22
     * @param file
     * @return
     */
    public static long getSize(File file) {
        if (!file.exists())
            return 0L;
        if (file.isFile())
            return file.length();
        long length = 0L;
        Stack<File> stack = new Stack<File>();
        stack.push(file);
        while (!stack.isEmpty()) {
            File file2 = (File) stack.pop();
            File[] fileArray = file2.listFiles();
            for (int i = 0; i < fileArray.length; i++)
                if (fileArray[i].isFile())
                    length += fileArray[i].length();
                else
                    stack.push(fileArray[i]);
        }
        return length;
    }

    /**
     * 计算出上传文件的大小（字节）
     * @author wufugui
     * 创建时间:2014-9-19 下午02:32:22
     * @param path
     * @return
     */
    public static long getSize(String path) {
        path = path.replace("\\", "\\\\");
        File file = new File(path);
        if (!file.exists())
            return 0L;
        if (file.isFile())
            return file.length();
        long length = 0L;
        Stack<File> stack = new Stack<File>();
        stack.push(file);
        while (!stack.isEmpty()) {
            File file2 = (File) stack.pop();
            File[] fileArray = file2.listFiles();
            for (int i = 0; i < fileArray.length; i++)
                if (fileArray[i].isFile())
                    length += fileArray[i].length();
                else
                    stack.push(fileArray[i]);
        }
        return length;
    }

    /**
     * 复制文件到服务器
     * @author wufugui
     * 创建时间:2014-9-19 下午04:48:43
     * @param src
     * @param dst
     */
    public static void copy(File src, File dst) {
        try {
            try (InputStream in = new BufferedInputStream(new FileInputStream(src),
                    BUFFER_SIZE); OutputStream out = new BufferedOutputStream(new FileOutputStream(dst),
                    BUFFER_SIZE)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                while (in.read(buffer) > 0)
                    out.write(buffer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 合并任数量的图片成一张图片
     *
     * @param isHorizontal
     *            true代表水平合并，fasle代表垂直合并
     * @param imgs
     *            待合并的图片数组
     * @return
     * @throws IOException
     */
    public static BufferedImage mergeImage(boolean isHorizontal, List<BufferedImage> imgs) throws IOException {
        // 生成新图片
        BufferedImage destImage = null;
        // 计算新图片的长和高
        int allw = 0, allh = 0, allwMax = 0, allhMax = 0;
        // 获取总长、总宽、最长、最宽
        for (int i = 0; i < imgs.size(); i++) {
            BufferedImage img = imgs.get(i);
            allw += img.getWidth();
            allh += img.getHeight();
            if (img.getWidth() > allwMax) {
                allwMax = img.getWidth();
            }
            if (img.getHeight() > allhMax) {
                allhMax = img.getHeight();
            }
        }
        // 创建新图片
        if (isHorizontal) {
            destImage = new BufferedImage(allw, allhMax, BufferedImage.TYPE_INT_RGB);
        } else {
            destImage = new BufferedImage(allwMax, allh, BufferedImage.TYPE_INT_RGB);
        }
        // 合并所有子图片到新图片
        int wx = 0, wy = 0;
        for (int i = 0; i < imgs.size(); i++) {
            BufferedImage img = imgs.get(i);
            int w1 = img.getWidth();
            int h1 = img.getHeight();
            // 从图片中读取RGB
            int[] ImageArrayOne = new int[w1 * h1];
            ImageArrayOne = img.getRGB(0, 0, w1, h1, ImageArrayOne, 0, w1); // 逐行扫描图像中各个像素的RGB到数组中
            if (isHorizontal) { // 水平方向合并
                destImage.setRGB(wx, 0, w1, h1, ImageArrayOne, 0, w1); // 设置上半部分或左半部分的RGB
            } else { // 垂直方向合并
                destImage.setRGB(0, wy, w1, h1, ImageArrayOne, 0, w1); // 设置上半部分或左半部分的RGB
            }
            wx += w1;
            wy += h1;
        }
        return destImage;
    }


    /**
     * BufferedImage转byte[]
     *
     * @param bImage BufferedImage对象
     * @return byte[]
     * @auth zhy
     */
    public static byte[] imageToBytes(BufferedImage bImage) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(bImage, "jpg", out);
        } catch (IOException e) {
            //log.error(e.getMessage());
        }
        return out.toByteArray();
    }

    public static void main(String[] args) {
        String title = "group1/M00/00/80/wKgADFTZZ0mAeyYFAAAavQ2m398669.xml";
        String fileName = title.substring(title.lastIndexOf('/')+1);
        System.out.println(fileName);
    }
}
