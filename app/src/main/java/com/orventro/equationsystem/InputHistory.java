package com.orventro.equationsystem;

public class InputHistory {
    private String[][] inputHistory;
    private int[] selectionHistory, focusedEditTextIndexHistory;
    private int history_length, activeHistoryIndex;
    private boolean[][] visibleInputIndexHistory;


    InputHistory(int length, int size) {
        history_length = length;
        inputHistory = new String[length][size];
        visibleInputIndexHistory = new boolean[length][size];
        selectionHistory = new int[length];
        focusedEditTextIndexHistory = new int[length];
        activeHistoryIndex = length - 1;
        focusedEditTextIndexHistory[activeHistoryIndex] = 0;
        visibleInputIndexHistory[activeHistoryIndex][0] = true;
    }

    InputHistory(String[][] inputHistory, int[] selectionHistory, int[] focusedEditTextIndexHistory, boolean[][] visibleInputIndexHistory, int activeHistoryIndex){
        history_length = inputHistory.length;
        this.inputHistory = inputHistory;
        this.selectionHistory = selectionHistory;
        this.focusedEditTextIndexHistory = focusedEditTextIndexHistory;
        this.visibleInputIndexHistory = visibleInputIndexHistory;
        this.activeHistoryIndex = activeHistoryIndex;
    }

    String[] getActiveInput() {
        return inputHistory[activeHistoryIndex];
    }

    String[][] getAllInput() {
        return inputHistory;
    }

    int getActiveSelectionIndex() {
        return selectionHistory[activeHistoryIndex];
    }

    int[] getAllSelectionIndexes() {
        return selectionHistory;
    }

    int getActiveFocusedEditTextIndex() {
        return focusedEditTextIndexHistory[activeHistoryIndex];
    }

    int[] getAllFocusedEditTextIndexes() {
        return focusedEditTextIndexHistory;
    }

    boolean[] getActiveVisibleInputIndex() {
        return visibleInputIndexHistory[activeHistoryIndex];
    }

    boolean[][] getAllVisibleInputIndexes() {
        return visibleInputIndexHistory;
    }

    int getActiveHistoryIndex() {
        return activeHistoryIndex;
    }

    void moveHistory(int index) {
        if (inputHistory[activeHistoryIndex][focusedEditTextIndexHistory[activeHistoryIndex]] != null)activeHistoryIndex = Math.min(history_length-1, Math.max(0, activeHistoryIndex + index));
    }

    void updateHistory(String[] s, int selection, int activeEditTextIndex, boolean[] visibleInput) {
        if (activeHistoryIndex < history_length - 1) updateHistory(activeHistoryIndex);
        else if (activeHistoryIndex > history_length - 1) activeHistoryIndex = history_length - 1;
        for (int i = 0; i < history_length - 1; i++) {
            inputHistory[i] = inputHistory[i + 1];
            selectionHistory[i] = selectionHistory[i + 1];
            focusedEditTextIndexHistory[i] = focusedEditTextIndexHistory[i + 1];
            visibleInputIndexHistory[i] = visibleInputIndexHistory[i + 1];
        }
        inputHistory[history_length - 1] = s;
        selectionHistory[history_length - 1] = selection;
        focusedEditTextIndexHistory[history_length - 1] = activeEditTextIndex;
        visibleInputIndexHistory[history_length - 1] = visibleInput;
    }

    void updateHistory(int index) {
        for (int i = index; i >= 0; i--) {
            inputHistory[history_length - 1 - index + i] = inputHistory[index];
            selectionHistory[history_length - 1 - index + i] = selectionHistory[index];
            focusedEditTextIndexHistory[history_length - 1 - index + i] = focusedEditTextIndexHistory[index];
            visibleInputIndexHistory[history_length - 1 - index + i] = visibleInputIndexHistory[index];
        }
        activeHistoryIndex = history_length - 1;
    }
}
