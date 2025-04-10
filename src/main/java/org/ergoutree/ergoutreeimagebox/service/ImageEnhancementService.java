package org.ergoutree.ergoutreeimagebox.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 * 图像增强服务，处理图像的增强和特效操作
 */
public class ImageEnhancementService extends ImageService {
    
    /**
     * 添加水印
     * @param image 源图像
     * @param watermarkText 水印文本
     * @return 添加水印后的图像
     */
    public Image addWatermark(Image image, String watermarkText) {
        BufferedImage bufferedImage = toBufferedImage(image);
        
        Graphics2D g2d = bufferedImage.createGraphics();
        
        // 设置水印样式
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 30));
        g2d.setColor(new Color(255, 255, 255, 128)); // 半透明白色
        
        // 计算水印位置（右下角）
        FontMetrics fontMetrics = g2d.getFontMetrics();
        int x = bufferedImage.getWidth() - fontMetrics.stringWidth(watermarkText) - 20;
        int y = bufferedImage.getHeight() - fontMetrics.getHeight() + 10;
        
        // 绘制水印
        g2d.drawString(watermarkText, x, y);
        g2d.dispose();
        
        return toFXImage(bufferedImage);
    }
    
    /**
     * 图像降噪（使用中值滤波）
     * @param image 源图像
     * @return 降噪后的图像
     */
    public Image denoiseImage(Image image) {
        // 将JavaFX图像转换为BufferedImage
        BufferedImage inputImage = SwingFXUtils.fromFXImage(image, null);
        
        // 确定正确的输出图像类型
        int imageType = inputImage.getType();
        if (imageType == BufferedImage.TYPE_CUSTOM) {
            // 如果是自定义类型，使用标准RGB或ARGB
            imageType = inputImage.getColorModel().hasAlpha() ? 
                    BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        }
        
        // 创建输出图像，使用确定的图像类型
        BufferedImage outputImage = new BufferedImage(
                inputImage.getWidth(),
                inputImage.getHeight(),
                imageType);
        
        int radius = 1; // 滤波半径
        
        // 处理图像的每个像素
        for (int y = 0; y < inputImage.getHeight(); y++) {
            for (int x = 0; x < inputImage.getWidth(); x++) {
                // 收集邻域像素
                int[] redValues = new int[(2 * radius + 1) * (2 * radius + 1)];
                int[] greenValues = new int[(2 * radius + 1) * (2 * radius + 1)];
                int[] blueValues = new int[(2 * radius + 1) * (2 * radius + 1)];
                int[] alphaValues = new int[(2 * radius + 1) * (2 * radius + 1)];
                int count = 0;
                
                // 获取邻域像素值
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dx = -radius; dx <= radius; dx++) {
                        int nx = x + dx;
                        int ny = y + dy;
                        
                        // 边界检查
                        if (nx >= 0 && nx < inputImage.getWidth() && ny >= 0 && ny < inputImage.getHeight()) {
                            int rgb = inputImage.getRGB(nx, ny);
                            alphaValues[count] = (rgb >> 24) & 0xFF;
                            redValues[count] = (rgb >> 16) & 0xFF;
                            greenValues[count] = (rgb >> 8) & 0xFF;
                            blueValues[count] = rgb & 0xFF;
                            count++;
                        }
                    }
                }
                
                // 排序并取中值
                java.util.Arrays.sort(redValues, 0, count);
                java.util.Arrays.sort(greenValues, 0, count);
                java.util.Arrays.sort(blueValues, 0, count);
                java.util.Arrays.sort(alphaValues, 0, count);
                
                int medianIndex = count / 2;
                int medianAlpha = alphaValues[medianIndex];
                int medianRed = redValues[medianIndex];
                int medianGreen = greenValues[medianIndex];
                int medianBlue = blueValues[medianIndex];
                
                // 设置输出像素
                int rgb;
                if (inputImage.getColorModel().hasAlpha()) {
                    rgb = (medianAlpha << 24) | (medianRed << 16) | (medianGreen << 8) | medianBlue;
                } else {
                    rgb = (255 << 24) | (medianRed << 16) | (medianGreen << 8) | medianBlue;
                }
                
                outputImage.setRGB(x, y, rgb);
            }
        }
        
        // 转换回JavaFX图像
        return SwingFXUtils.toFXImage(outputImage, null);
    }
    
    /**
     * 压缩图片
     * @param image 要压缩的图片
     * @param quality 压缩质量（0.0-1.0）
     * @return 压缩后的图片
     * @throws IOException 如果压缩过程中发生IO错误
     * 注意，压缩图片不支持rgba，会自动转换为rgb
     */
    public Image compressImage(Image image, float quality) throws IOException {
        // 将JavaFX图像转换为BufferedImage
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        
        // 处理带Alpha通道的图像 - 转换为RGB模式
        BufferedImage rgbImage;
        if (bufferedImage.getColorModel().hasAlpha()) {
            rgbImage = new BufferedImage(
                bufferedImage.getWidth(),
                bufferedImage.getHeight(),
                BufferedImage.TYPE_INT_RGB);
            
            // 使用白色背景
            Graphics2D g2d = rgbImage.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
            g2d.drawImage(bufferedImage, 0, 0, null);
            g2d.dispose();
        } else {
            rgbImage = bufferedImage;
        }
        
        // 使用JPG格式进行压缩
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);
        
        // 将压缩结果写入ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(rgbImage, null, null), param);
        } finally {
            writer.dispose();
        }
        
        // 从压缩后的字节流创建新图像
        byte[] compressedData = outputStream.toByteArray();
        return new Image(new ByteArrayInputStream(compressedData));
    }
} 