package com.umberto.medicinetracking.backup;

public interface OnUploadProgress {
    void onFinishUpload(boolean success,String userMessage, String errorMessage);
}
