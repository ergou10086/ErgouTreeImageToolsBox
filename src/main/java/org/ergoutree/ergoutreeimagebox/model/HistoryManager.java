package org.ergoutree.ergoutreeimagebox.model;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 历史管理器，管理图像处理的历史状态，支持撤销功能
 */
public class HistoryManager {
    private final Deque<ImageState> history;
    private final int maxHistorySize;
    private final Deque<ImageState> redoHistory; // 保存被撤销的状态，用于重做功能
    
    /**
     * 创建一个历史管理器
     * @param maxHistorySize 最大历史记录数量
     */
    public HistoryManager(int maxHistorySize) {
        this.history = new ArrayDeque<>(maxHistorySize);
        this.redoHistory = new ArrayDeque<>(maxHistorySize);
        this.maxHistorySize = maxHistorySize;
    }
    
    /**
     * 保存图像状态到历史记录
     * @param state 要保存的图像状态
     */
    public void saveState(ImageState state) {
        if (state == null) {
            return;
        }
        
        // 清空重做历史
        redoHistory.clear();
        
        // 如果历史记录已满，移除最旧的状态
        if (history.size() >= maxHistorySize) {
            history.removeLast();
        }
        
        // 添加新状态到历史记录的开头
        history.addFirst(state);
    }
    
    /**
     * 获取上一个图像状态（撤销）
     * @return 上一个图像状态，如果没有则返回null
     */
    public ImageState undo() {
        if (history.isEmpty()) {
            return null;
        }
        
        // 移除并保存最近的状态到重做历史
        ImageState state = history.removeFirst();
        redoHistory.addFirst(state);
        
        // 如果还有历史状态，返回新的当前状态
        return history.isEmpty() ? null : history.peekFirst();
    }
    
    /**
     * 检查是否可以撤销
     * @return 如果有历史状态可以撤销则返回true
     */
    public boolean canUndo() {
        return history.size() > 1; // 至少需要两个状态才能撤销
    }
    
    /**
     * 获取可撤销的步数
     * @return 可撤销的步数
     */
    public int getUndoStepsAvailable() {
        return Math.max(0, history.size() - 1); // 当前状态不算在内
    }
    
    /**
     * 清空历史记录
     */
    public void clear() {
        history.clear();
        redoHistory.clear();
    }
    
    /**
     * 获取历史记录大小
     * @return 历史记录中的状态数量
     */
    public int size() {
        return history.size();
    }
    
    /**
     * 获取最大历史记录大小
     * @return 最大历史记录数量
     */
    public int getMaxHistorySize() {
        return maxHistorySize;
    }
}
