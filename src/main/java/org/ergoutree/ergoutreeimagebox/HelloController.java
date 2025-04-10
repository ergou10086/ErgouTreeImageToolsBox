package org.ergoutree.ergoutreeimagebox;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;
import java.util.Optional;

import javax.swing.JOptionPane;

import org.ergoutree.ergoutreeimagebox.model.HistoryManager;
import org.ergoutree.ergoutreeimagebox.model.ImageModel;
import org.ergoutree.ergoutreeimagebox.model.ImageState;
import org.ergoutree.ergoutreeimagebox.service.FormatConversionService;
import org.ergoutree.ergoutreeimagebox.service.ImageEnhancementService;
import org.ergoutree.ergoutreeimagebox.service.ImageService;
import org.ergoutree.ergoutreeimagebox.service.ScreenshotService;
import org.ergoutree.ergoutreeimagebox.service.TransformationService;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * 主控制器类，处理UI交互和图像处理功能
 */
public class HelloController {
    // FXML注入的UI组件
    @FXML
    private BorderPane mainPane;
    
    @FXML
    private ImageView imagePreview;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private ComboBox<String> formatComboBox;
    
    @FXML
    private Button undoButton;
    
    @FXML
    private Label undoStepsLabel; // 显示可撤销的步数
    
    // 依赖注入的服务和模型
    private final ImageModel imageModel;
    private final HistoryManager historyManager;
    private final ImageService imageService;
    private final FormatConversionService formatService;
    private final TransformationService transformService;
    private final ImageEnhancementService enhancementService;
    
    // 构造器注入
    public HelloController(
            ImageModel imageModel,
            HistoryManager historyManager,
            ImageService imageService,
            FormatConversionService formatService,
            TransformationService transformService,
            ImageEnhancementService enhancementService) {
        this.imageModel = imageModel;
        this.historyManager = historyManager;
        this.imageService = imageService;
        this.formatService = formatService;
        this.transformService = transformService;
        this.enhancementService = enhancementService;
    }
    
    @FXML
    public void initialize() {
        // 初始化格式下拉框
        formatComboBox.getItems().addAll("JPG", "PNG", "BMP", "GIF");
        formatComboBox.setValue("PNG");
        
        // 设置默认状态
        statusLabel.setText("请选择一张图片开始操作");
        
        // 初始化撤销按钮状态
        updateUndoButton();
        
        // 如果撤销步数标签存在，则初始化
        if (undoStepsLabel != null) {
            updateUndoStepsLabel();
        }
        
        // 监听图片变化
        imageModel.currentImageProperty().addListener((obs, oldVal, newVal) -> {
            imagePreview.setImage(newVal);
            updateUndoButton();
            updateUndoStepsLabel();
        });
    }
    
    @FXML
    private void onOpenImageClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择图片");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("图片文件", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif")
        );
        
        Stage stage = (Stage) mainPane.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            try {
                // 直接使用JavaFX的Image构造函数
                Image image = new Image(selectedFile.toURI().toString());
                imageModel.setCurrentImage(image);
                imageModel.setCurrentImageFile(selectedFile);
                saveCurrentState();
                statusLabel.setText("已加载图片: " + selectedFile.getName());
            } catch (Exception e) {
                showAlert("错误", "无法加载图片", e.getMessage());
            }
        }
    }
    
    @FXML
    private void onSaveImageClick() {
        if (!imageModel.hasImage()) {
            showAlert("提示", "请先选择图片", "请先打开一张图片再进行操作");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存图片");
        String format = formatComboBox.getValue().toLowerCase();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(format.toUpperCase() + " 文件", "*." + format)
        );
        
        Stage stage = (Stage) mainPane.getScene().getWindow();
        File outputFile = fileChooser.showSaveDialog(stage);
        
        if (outputFile != null) {
            try {
                imageService.saveImage(imageModel.getCurrentImage(), outputFile, format);
                statusLabel.setText("已保存图片: " + outputFile.getName());
            } catch (Exception e) {
                showAlert("错误", "保存失败", e.getMessage());
            }
        }
    }
    
    @FXML
    private void onConvertToGrayscaleClick() {
        if (!imageModel.hasImage()) {
            showAlert("提示", "请先选择图片", "请先打开一张图片再进行操作");
            return;
        }
        
        try {
            Image processed = formatService.convertToGrayscale(imageModel.getCurrentImage());
            updateImageAndSaveState(processed, "已转换为灰度图");
        } catch (Exception e) {
            showAlert("错误", "转换失败", e.getMessage());
        }
    }
    
    @FXML
    private void onConvertRGBAToRGBClick() {
        if (!imageModel.hasImage()) {
            showAlert("提示", "请先选择图片", "请先打开一张图片再进行操作");
            return;
        }
        
        try {
            Image processed = formatService.convertRGBAToRGB(imageModel.getCurrentImage());
            updateImageAndSaveState(processed, "已转换为RGB图像");
        } catch (Exception e) {
            showAlert("错误", "转换失败", e.getMessage());
        }
    }
    
    @FXML
    private void onResizeImageClick() {
        if (!imageModel.hasImage()) {
            showAlert("提示", "请先选择图片", "请先打开一张图片再进行操作");
            return;
        }
        
        Dialog<double[]> dialog = createResizeDialog();
        Optional<double[]> result = dialog.showAndWait();
        
        result.ifPresent(dimensions -> {
            try {
                Image resized = transformService.resizeImage(
                    imageModel.getCurrentImage(), 
                    (int)dimensions[0], 
                    (int)dimensions[1]
                );
                updateImageAndSaveState(resized, 
                    String.format("已调整图片大小为 %d x %d", (int)dimensions[0], (int)dimensions[1]));
            } catch (Exception e) {
                showAlert("错误", "调整大小失败", e.getMessage());
            }
        });
    }
    
    @FXML
    private void onRotateImageClick() {
        if (!imageModel.hasImage()) {
            showAlert("提示", "请先选择图片", "请先打开一张图片再进行操作");
            return;
        }
        
        Dialog<Integer> dialog = createRotateDialog();
        Optional<Integer> result = dialog.showAndWait();
        
        result.ifPresent(angle -> {
            try {
                Image rotated = transformService.rotateImage(imageModel.getCurrentImage(), angle);
                updateImageAndSaveState(rotated, "已旋转图片 " + angle + " 度");
            } catch (Exception e) {
                showAlert("错误", "旋转失败", e.getMessage());
            }
        });
    }
    
    @FXML
    private void onCropImageClick() {
        if (!imageModel.hasImage()) {
            showAlert("提示", "请先选择图片", "请先打开一张图片再进行操作");
            return;
        }
        
        Dialog<double[]> dialog = createCropDialog();
        Optional<double[]> result = dialog.showAndWait();
        
        result.ifPresent(params -> {
            try {
                Image cropped = transformService.cropImage(
                    imageModel.getCurrentImage(),
                    (int)params[0], (int)params[1], (int)params[2], (int)params[3]
                );
                updateImageAndSaveState(cropped, "已裁剪图片");
            } catch (Exception e) {
                showAlert("错误", "裁剪失败", e.getMessage());
            }
        });
    }
    
    @FXML
    private void onCompressImageClick() {
        if (!imageModel.hasImage()) {
            showAlert("提示", "请先选择图片", "请先打开一张图片再进行操作");
            return;
        }
        // 显示提示对话框
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示");
        alert.setHeaderText("压缩注意事项");
        alert.setContentText("压缩不支持rgba，rgba图像会自动转换为rgb三通");
        alert.showAndWait();


        Dialog<Double> dialog = createCompressionDialog();
        Optional<Double> result = dialog.showAndWait();
        
        result.ifPresent(quality -> {
            try {
                Image compressed = enhancementService.compressImage(
                    imageModel.getCurrentImage(), 
                    quality.floatValue()
                );
                updateImageAndSaveState(compressed, 
                    String.format("已压缩图片 (质量: %.1f)", quality));
            } catch (Exception e) {
                showAlert("错误", "压缩失败", e.getMessage());
            }
        });
    }
    
    @FXML
    private void onAddWatermarkClick() {
        if (!imageModel.hasImage()) {
            showAlert("提示", "请先选择图片", "请先打开一张图片再进行操作");
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog("二狗树图像工具箱");
        dialog.setTitle("添加水印");
        dialog.setHeaderText("请输入水印文本");
        dialog.setContentText("水印文本:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(text -> {
            try {
                Image watermarked = enhancementService.addWatermark(
                    imageModel.getCurrentImage(), 
                    text
                );
                updateImageAndSaveState(watermarked, "已添加水印");
            } catch (Exception e) {
                showAlert("错误", "添加水印失败", e.getMessage());
            }
        });
    }
    
    @FXML
    private void onDenoiseImageClick() {
        if (!imageModel.hasImage()) {
            showAlert("提示", "请先选择图片", "请先打开一张图片再进行操作");
            return;
        }
        
        try {
            Image denoised = enhancementService.denoiseImage(imageModel.getCurrentImage());
            updateImageAndSaveState(denoised, "已对图片进行降噪处理");
        } catch (Exception e) {
            showAlert("错误", "降噪失败", e.getMessage());
        }
    }
    
    @FXML
    private void onConvertFormatClick() {
        if (!imageModel.hasImage()) {
            showAlert("提示", "请先选择图片", "请先打开一张图片再进行操作");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存转换后的图片");
        String format = formatComboBox.getValue().toLowerCase();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(format.toUpperCase() + " 文件", "*." + format)
        );
        
        Stage stage = (Stage) mainPane.getScene().getWindow();
        File outputFile = fileChooser.showSaveDialog(stage);
        
        if (outputFile != null) {
            try {
                formatService.convertFormat(imageModel.getCurrentImage(), outputFile, format);
                statusLabel.setText("已保存为 " + format.toUpperCase() + " 格式: " + outputFile.getName());
            } catch (Exception e) {
                showAlert("错误", "转换失败", e.getMessage());
            }
        }
    }
    
    @FXML
    private void onUndoClick() {
        ImageState previousState = historyManager.undo();
        if (previousState != null) {
            imageModel.restoreState(previousState);
            updateUndoButton();
            updateUndoStepsLabel();
            statusLabel.setText("已撤销到前一个状态 (剩余可撤销步数: " + historyManager.getUndoStepsAvailable() + ")");
        } else {
            statusLabel.setText("没有更多可撤销的操作");
        }
    }
    
    @FXML
    private void onCaptureFullScreenClick() {
        try {
            ScreenshotService screenshotService = new ScreenshotService();
            Image screenshot = screenshotService.captureFullScreen();
            String savePath = System.getProperty("user.home") + "/Pictures/Screenshots";
            screenshotService.saveScreenshot(screenshot, savePath);
            showSuccessMessage("截图已保存到: " + savePath);
        } catch (Exception e) {
            showErrorMessage("全屏截图失败: " + e.getMessage());
        }
    }

    @FXML
    private void onCaptureRegionClick() {
        try {
            // 最小化当前窗口
            Stage stage = (Stage) mainPane.getScene().getWindow();
            stage.setIconified(true);
            
            // 等待窗口最小化
            Thread.sleep(500);
            
            // 创建截图服务实例
            ScreenshotService screenshotService = new ScreenshotService();
            
            // 显示区域选择提示
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("区域截图");
            alert.setHeaderText("请按以下步骤操作");
            alert.setContentText("1. 点击确定后，将打开区域选择对话框\n2. 输入要截取的区域坐标和尺寸\n3. 点击确定开始截图");
            alert.showAndWait();
            
            // 获取屏幕尺寸
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            
            // 创建区域选择对话框
            Dialog<Rectangle> regionDialog = new Dialog<>();
            regionDialog.setTitle("选择截图区域");
            regionDialog.setHeaderText("请输入要截图的区域坐标和尺寸");
            
            // 添加按钮
            ButtonType confirmButtonType = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
            regionDialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
            
            // 创建区域选择面板
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            // 添加输入字段
            TextField xField = new TextField("0");
            TextField yField = new TextField("0");
            TextField widthField = new TextField(String.valueOf((int)screenRect.width / 2));
            TextField heightField = new TextField(String.valueOf((int)screenRect.height / 2));
            
            grid.add(new Label("X坐标:"), 0, 0);
            grid.add(xField, 1, 0);
            grid.add(new Label("Y坐标:"), 0, 1);
            grid.add(yField, 1, 1);
            grid.add(new Label("宽度:"), 0, 2);
            grid.add(widthField, 1, 2);
            grid.add(new Label("高度:"), 0, 3);
            grid.add(heightField, 1, 3);
            
            // 添加辅助说明
            Label helpText = new Label("提示: 屏幕左上角坐标为(0,0), 总宽度为" + (int)screenRect.width + ", 总高度为" + (int)screenRect.height);
            helpText.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");
            grid.add(helpText, 0, 4, 2, 1);
            
            regionDialog.getDialogPane().setContent(grid);
            
            // 设置结果转换器
            regionDialog.setResultConverter(dialogButton -> {
                if (dialogButton == confirmButtonType) {
                    try {
                        int x = Integer.parseInt(xField.getText());
                        int y = Integer.parseInt(yField.getText());
                        int width = Integer.parseInt(widthField.getText());
                        int height = Integer.parseInt(heightField.getText());
                        
                        // 验证输入值
                        if (x < 0 || y < 0 || width <= 0 || height <= 0) {
                            throw new IllegalArgumentException("坐标和大小必须是正值");
                        }
                        
                        if (x + width > screenRect.width || y + height > screenRect.height) {
                            throw new IllegalArgumentException("截图区域超出屏幕范围");
                        }
                        
                        return new Rectangle(x, y, width, height);
                    } catch (NumberFormatException e) {
                        showAlert("错误", "输入无效", "请输入有效的数字");
                        return null;
                    } catch (IllegalArgumentException e) {
                        showAlert("错误", "区域无效", e.getMessage());
                        return null;
                    }
                }
                return null;
            });
            
            // 显示对话框并获取结果
            Optional<Rectangle> result = regionDialog.showAndWait();
            
            if (result.isPresent()) {
                // 恢复窗口先显示倒计时
                stage.setIconified(false);
                
                // 显示倒计时对话框
                Alert countdownAlert = new Alert(Alert.AlertType.INFORMATION);
                countdownAlert.setTitle("准备截图");
                countdownAlert.setHeaderText("截图即将开始");
                countdownAlert.setContentText("请将窗口移至不干扰截图区域的位置，3秒后开始截图...");
                
                // 创建一个定时关闭的线程
                new Thread(() -> {
                    try {
                        Thread.sleep(3000);
                        javafx.application.Platform.runLater(() -> {
                            countdownAlert.close();
                            
                            // 执行截图
                            try {
                                Rectangle region = result.get();
                                Image screenshot = screenshotService.captureRegion(
                                    (int)region.x, (int)region.y, (int)region.width, (int)region.height
                                );
                                
                                // 更新当前图片
                                imageModel.setCurrentImage(screenshot);
                                saveCurrentState();
                                statusLabel.setText("已截取区域图片");
                                
                                // 显示成功提示
                                showSuccessMessage("区域截图成功！");
                            } catch (Exception e) {
                                showErrorMessage("区域截图失败: " + e.getMessage());
                            }
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
                
                // 显示倒计时对话框
                countdownAlert.showAndWait();
            } else {
                // 用户取消，恢复窗口
                stage.setIconified(false);
            }
        } catch (Exception e) {
            showErrorMessage("区域截图失败: " + e.getMessage());
        }
    }
    
    private Dialog<double[]> createResizeDialog() {
        Dialog<double[]> dialog = new Dialog<>();
        dialog.setTitle("调整图片大小");
        dialog.setHeaderText("请输入新的宽度和高度");
        
        // 设置按钮
        ButtonType confirmButtonType = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
        
        // 创建输入字段
        TextField widthField = new TextField(String.valueOf((int)imageModel.getCurrentImage().getWidth()));
        TextField heightField = new TextField(String.valueOf((int)imageModel.getCurrentImage().getHeight()));
        
        // 创建布局
        GridPane grid = new GridPane();
        grid.add(new Label("宽度:"), 0, 0);
        grid.add(widthField, 1, 0);
        grid.add(new Label("高度:"), 0, 1);
        grid.add(heightField, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                try {
                    double width = Double.parseDouble(widthField.getText());
                    double height = Double.parseDouble(heightField.getText());
                    return new double[]{width, height};
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });
        
        return dialog;
    }
    
    private Dialog<Integer> createRotateDialog() {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("旋转图片");
        dialog.setHeaderText("请选择旋转角度");
        
        ButtonType confirmButtonType = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
        
        ComboBox<Integer> angleComboBox = new ComboBox<>();
        angleComboBox.getItems().addAll(90, 180, 270);
        angleComboBox.setValue(90);
        
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(new Label("角度:"), angleComboBox);
        dialog.getDialogPane().setContent(vbox);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return angleComboBox.getValue();
            }
            return null;
        });
        
        return dialog;
    }
    
    private Dialog<double[]> createCropDialog() {
        Dialog<double[]> dialog = new Dialog<>();
        dialog.setTitle("裁剪图片");
        dialog.setHeaderText("请输入裁剪参数");
        
        ButtonType confirmButtonType = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
        
        TextField xField = new TextField("0");
        TextField yField = new TextField("0");
        TextField widthField = new TextField(String.valueOf((int)imageModel.getCurrentImage().getWidth()));
        TextField heightField = new TextField(String.valueOf((int)imageModel.getCurrentImage().getHeight()));
        
        GridPane grid = new GridPane();
        grid.add(new Label("X 坐标:"), 0, 0);
        grid.add(xField, 1, 0);
        grid.add(new Label("Y 坐标:"), 0, 1);
        grid.add(yField, 1, 1);
        grid.add(new Label("宽度:"), 0, 2);
        grid.add(widthField, 1, 2);
        grid.add(new Label("高度:"), 0, 3);
        grid.add(heightField, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                try {
                    double x = Double.parseDouble(xField.getText());
                    double y = Double.parseDouble(yField.getText());
                    double width = Double.parseDouble(widthField.getText());
                    double height = Double.parseDouble(heightField.getText());
                    return new double[]{x, y, width, height};
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });
        
        return dialog;
    }
    
    private Dialog<Double> createCompressionDialog() {
        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("压缩图片");
        dialog.setHeaderText("请选择压缩质量 (0.1-1.0)");
        
        ButtonType confirmButtonType = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
        
        Slider qualitySlider = new Slider(0.1, 1.0, 0.8);
        qualitySlider.setShowTickLabels(true);
        qualitySlider.setShowTickMarks(true);
        qualitySlider.setMajorTickUnit(0.1);
        qualitySlider.setBlockIncrement(0.1);
        
        Label valueLabel = new Label("0.8");
        qualitySlider.valueProperty().addListener((obs, oldVal, newVal) -> 
            valueLabel.setText(String.format("%.1f", newVal.doubleValue())));
        
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(new Label("压缩质量:"), qualitySlider, valueLabel);
        
        dialog.getDialogPane().setContent(vbox);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return qualitySlider.getValue();
            }
            return null;
        });
        
        return dialog;
    }
    
    private void updateImageAndSaveState(Image newImage, String statusMessage) {
        imageModel.setCurrentImage(newImage);
        saveCurrentState();
        statusLabel.setText(statusMessage);
    }
    
    private void saveCurrentState() {
        ImageState state = imageModel.createState();
        if (state != null) {
            historyManager.saveState(state);
            updateUndoButton();
            updateUndoStepsLabel();
        }
    }
    
    private void updateUndoButton() {
        if (undoButton != null) {
            undoButton.setDisable(!historyManager.canUndo());
            // 更新按钮文本显示可撤销步数
            undoButton.setText("撤销 (" + historyManager.getUndoStepsAvailable() + ")");
        }
    }
    
    private void updateUndoStepsLabel() {
        if (undoStepsLabel != null) {
            undoStepsLabel.setText("可撤销步数: " + historyManager.getUndoStepsAvailable());
        }
    }
    
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "成功", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "错误", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfoMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "信息", JOptionPane.INFORMATION_MESSAGE);
    }
} 