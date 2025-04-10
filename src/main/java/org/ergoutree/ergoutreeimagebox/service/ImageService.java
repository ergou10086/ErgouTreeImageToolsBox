package org.ergoutree.ergoutreeimagebox.service;

import javafx.scene.image.Image;
import javafx.embed.swing.SwingFXUtils;
import org.ergoutree.ergoutreeimagebox.model.ImageModel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 图像服务基类，提供基本的图像操作功能
 */
public class ImageService {
    
    /**
     * 加载图像
     * @param file 图像文件
     * @return 加载的图像
     * @throws IOException 如果加载失败
     */
    public Image loadImage(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("文件不能为空");
        }
        return new Image(file.toURI().toString());
    }
    
    /**
     * 保存图像
     * @param image 要保存的图像
     * @param file 保存的文件
     * @param format 图像格式
     * @throws IOException 如果保存失败
     */
    public void saveImage(Image image, File file, String format) throws IOException {
        if (image == null || file == null || format == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        ImageIO.write(bufferedImage, format, file);
    }
    
    /**
     * 将JavaFX Image转换为BufferedImage
     * @param image JavaFX图像
     * @return BufferedImage
     */
    protected BufferedImage toBufferedImage(Image image) {
        return SwingFXUtils.fromFXImage(image, null);
    }
    
    /**
     * 将BufferedImage转换为JavaFX Image
     * @param bufferedImage BufferedImage
     * @return JavaFX图像
     */
    protected Image toFXImage(BufferedImage bufferedImage) {
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }
}
