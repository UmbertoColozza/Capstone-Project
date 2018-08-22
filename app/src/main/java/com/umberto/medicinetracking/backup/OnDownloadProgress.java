package com.umberto.medicinetracking.backup;

public interface OnDownloadProgress {
    void onFinishDownload(boolean success,String userMessage,String errorMessage);
}
