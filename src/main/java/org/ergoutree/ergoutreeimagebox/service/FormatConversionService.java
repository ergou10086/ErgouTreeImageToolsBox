package org.ergoutree.ergoutreeimagebox.service;

import javafx.scene.image.Image;
import javafx.embed.swing.SwingFXUtils;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 格式转换服务，处理图像格式相关的操作
 */
public class FormatConversionService extends ImageService {
    
    /**
     * 转换图像格式
     * @param image 源图像
     * @param outputFile 输出文件
     * @param format 目标格式
     * @throws IOException 如果转换失败
     */
    public void convertFormat(Image image, File outputFile, String format) throws IOException {
        saveImage(image, outputFile, format);
    }
    
    /**
     * 压缩图像
     * @param image 源图像
     * @param outputFile 输出文件
     * @param quality 压缩质量 (0.1-1.0)
     * @throws IOException 如果压缩失败
     */
    public void compressImage(Image image, File outputFile, float quality) throws IOException {
        BufferedImage bufferedImage = toBufferedImage(image);
        
        // 创建 ImageWriter 并设置压缩质量
        ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
        javax.imageio.ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
        jpgWriteParam.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
        jpgWriteParam.setCompressionQuality(quality);
        
        // 输出压缩图片
        try (FileOutputStream fos = new FileOutputStream(outputFile);
             ImageOutputStream ios = ImageIO.createImageOutputStream(fos)) {
            jpgWriter.setOutput(ios);
            jpgWriter.write(null, new javax.imageio.IIOImage(bufferedImage, null, null), jpgWriteParam);
            jpgWriter.dispose();
        }
    }
    
    /**
     * 将RGBA图像转换为RGB图像
     * @param image 源RGBA图像
     * @return 转换后的RGB图像
     */
    public Image convertRGBAToRGB(Image image) {
        BufferedImage inputImage = toBufferedImage(image);
        
        // 检查是否有Alpha通道
        if (!inputImage.getColorModel().hasAlpha()) {
            return image; // 如果没有Alpha通道，直接返回原图像
        }
        
        BufferedImage rgbImage = new BufferedImage(
                inputImage.getWidth(),
                inputImage.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        
        // 使用白色背景
        java.awt.Graphics2D g2d = rgbImage.createGraphics();
        g2d.setColor(java.awt.Color.WHITE);
        g2d.fillRect(0, 0, inputImage.getWidth(), inputImage.getHeight());
        g2d.drawImage(inputImage, 0, 0, null);
        g2d.dispose();
        
        return toFXImage(rgbImage);
    }
    
    /**
     * 将RGB图像转换为灰度图
     * @param image 源RGB图像
     * @return 转换后的灰度图
     */
    public Image convertToGrayscale(Image image) {
        BufferedImage bufferedImage = toBufferedImage(image);
        BufferedImage grayImage = new BufferedImage(
                bufferedImage.getWidth(),
                bufferedImage.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);
        
        grayImage.getGraphics().drawImage(bufferedImage, 0, 0, null);
        
        return toFXImage(grayImage);
    }
}
