package org.ergoutree.ergoutreeimagebox.service;

import javafx.scene.image.Image;
import java.awt.image.BufferedImage;

/**
 * 图像变换服务，处理图像的几何变换操作
 */
public class TransformationService extends ImageService {
    
    /**
     * 调整图像大小
     * @param image 源图像
     * @param width 新宽度
     * @param height 新高度
     * @return 调整大小后的图像
     */
    public Image resizeImage(Image image, int width, int height) {
        BufferedImage inputImage = toBufferedImage(image);
        BufferedImage outputImage = new BufferedImage(width, height, inputImage.getType());
        
        java.awt.Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(inputImage, 0, 0, width, height, null);
        g2d.dispose();
        
        return toFXImage(outputImage);
    }
    
    /**
     * 旋转图像
     * @param image 源图像
     * @param angle 旋转角度 (90, 180, 270)
     * @return 旋转后的图像
     */
    public Image rotateImage(Image image, int angle) {
        BufferedImage inputImage = toBufferedImage(image);
        
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        
        BufferedImage outputImage;
        if (angle == 90 || angle == 270) {
            outputImage = new BufferedImage(height, width, inputImage.getType());
        } else {
            outputImage = new BufferedImage(width, height, inputImage.getType());
        }
        
        java.awt.Graphics2D g2d = outputImage.createGraphics();
        
        // 设置旋转中心和角度
        if (angle == 90) {
            g2d.translate(height, 0);
            g2d.rotate(Math.PI / 2);
        } else if (angle == 180) {
            g2d.translate(width, height);
            g2d.rotate(Math.PI);
        } else if (angle == 270) {
            g2d.translate(0, width);
            g2d.rotate(3 * Math.PI / 2);
        }
        
        g2d.drawImage(inputImage, 0, 0, null);
        g2d.dispose();
        
        return toFXImage(outputImage);
    }
    
    /**
     * 裁剪图像
     * @param image 源图像
     * @param x 裁剪起始X坐标
     * @param y 裁剪起始Y坐标
     * @param width 裁剪宽度
     * @param height 裁剪高度
     * @return 裁剪后的图像
     */
    public Image cropImage(Image image, int x, int y, int width, int height) {
        // 验证参数
        if (x < 0 || y < 0 || width <= 0 || height <= 0 || 
            x + width > image.getWidth() || y + height > image.getHeight()) {
            throw new IllegalArgumentException("裁剪参数超出图片范围");
        }
        
        BufferedImage inputImage = toBufferedImage(image);
        BufferedImage croppedImage = inputImage.getSubimage(x, y, width, height);
        
        return toFXImage(croppedImage);
    }
}
