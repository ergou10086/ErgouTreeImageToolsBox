package org.ergoutree.ergoutreeimagebox.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import java.io.File;

/**
 * 图像模型类，存储当前图像和相关信息
 */
public class ImageModel {
    private final ObjectProperty<Image> currentImage = new SimpleObjectProperty<>();
    private File currentImageFile;
    
    public ImageModel() {
    }
    
    public Image getCurrentImage() {
        return currentImage.get();
    }
    
    public void setCurrentImage(Image image) {
        currentImage.set(image);
    }
    
    public ObjectProperty<Image> currentImageProperty() {
        return currentImage;
    }
    
    public File getCurrentImageFile() {
        return currentImageFile;
    }
    
    public void setCurrentImageFile(File file) {
        this.currentImageFile = file;
    }
    
    public boolean hasImage() {
        return getCurrentImage() != null;
    }
    
    /**
     * 创建当前图像状态的副本
     * @return 图像状态副本
     */
    public ImageState createState() {
        if (!hasImage()) {
            return null;
        }
        return new ImageState(getCurrentImage());
    }
    
    /**
     * 从状态恢复图像
     * @param state 要恢复的图像状态
     */
    public void restoreState(ImageState state) {
        if (state != null) {
            setCurrentImage(state.image());
        }
    }
}
