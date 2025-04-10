package org.ergoutree.ergoutreeimagebox.model;

import javafx.scene.image.Image;

/**
 * 图像状态类，用于保存图像处理的历史状态，支持撤销功能
 */
public record ImageState(Image image) {
}
