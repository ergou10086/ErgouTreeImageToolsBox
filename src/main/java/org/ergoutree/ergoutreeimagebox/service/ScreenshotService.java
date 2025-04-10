package org.ergoutree.ergoutreeimagebox.service;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class ScreenshotService {
    /**
     * 截取全屏
     * @return 截取的图片
     */
    public Image captureFullScreen() {
        try{
            Robot robot = new Robot();
            //  Toolkit 实例获取屏幕当前对象，用此构建Rectangle矩形
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenCapture = robot.createScreenCapture(screenRect);
            return SwingFXUtils.toFXImage(screenCapture, null);
        }catch (AWTException e){
            throw new RuntimeException("全屏截图失败: " + e.getMessage());
        }
    }


    /**
     * 截取指定区域
     * @param x 起始X坐标
     * @param y 起始Y坐标
     * @param width 宽度
     * @param height 高度
     * @return 截取的图片
     */
    public Image captureRegion(int x, int y, int width, int height) {
        try {
            Robot robot = new Robot();
            Rectangle region = new Rectangle(x, y, width, height);
            BufferedImage screenCapture = robot.createScreenCapture(region);
            return SwingFXUtils.toFXImage(screenCapture, null);
        }catch (AWTException e){
            throw new RuntimeException("区域截图失败: " + e.getMessage());
        }
    }


    /**
     * 保存截图到指定目录
     * @param image 要保存的图片
     * @param directory 保存目录
     * @return 保存的文件路径
     */
    public String saveScreenshot(Image image, String directory) {
        try {
            // 确保目录存在
            File dir = new File(directory);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成文件名（使用时间戳）
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String Filename = "screenshot_" + sdf.format(new Date()) + ".png";
            File outputFile = new File(dir, Filename);

            // 保存图片
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
            javax.imageio.ImageIO.write(bufferedImage, "png", outputFile);
            return outputFile.getAbsolutePath();
        }catch (IOException e){
            throw new RuntimeException("保存截图失败: " + e.getMessage());
        }
    }
}
