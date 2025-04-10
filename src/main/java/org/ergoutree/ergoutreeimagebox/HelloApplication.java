package org.ergoutree.ergoutreeimagebox;

import java.io.IOException;

import org.ergoutree.ergoutreeimagebox.model.HistoryManager;
import org.ergoutree.ergoutreeimagebox.model.ImageModel;
import org.ergoutree.ergoutreeimagebox.service.FormatConversionService;
import org.ergoutree.ergoutreeimagebox.service.ImageEnhancementService;
import org.ergoutree.ergoutreeimagebox.service.ImageService;
import org.ergoutree.ergoutreeimagebox.service.TransformationService;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * 应用程序主类
 */
public class HelloApplication extends Application {
    // 模型
    private ImageModel imageModel;
    private HistoryManager historyManager;
    
    // 服务
    private ImageService imageService;
    private FormatConversionService formatService;
    private TransformationService transformService;
    private ImageEnhancementService enhancementService;

    @Override
    public void start(Stage stage) throws IOException {
        // 初始化模型
        imageModel = new ImageModel();
        historyManager = new HistoryManager(5); // 保存最多5步操作历史，支持撤销
        
        // 初始化服务
        imageService = new ImageService();
        formatService = new FormatConversionService();
        transformService = new TransformationService();
        enhancementService = new ImageEnhancementService();

        // 加载FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
        
        // 配置控制器工厂，注入依赖
        fxmlLoader.setControllerFactory(controllerClass -> {
            if (controllerClass == HelloController.class) {
                return new HelloController(
                    imageModel,
                    historyManager,
                    imageService,
                    formatService,
                    transformService,
                    enhancementService
                );
            }
            throw new IllegalArgumentException("未知的控制器类: " + controllerClass.getName());
        });
        
        // 创建场景
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        stage.setTitle("ErgouTreeImageToolsbox");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
} 