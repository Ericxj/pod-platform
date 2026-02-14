package com.pod.iam.dto;

import java.io.Serializable;

/**
 * 权限点校验结果：是否冲突（tenant 维度 + deleted=0）。
 */
public class PermissionValidateResultDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 是否通过（无冲突） */
    private boolean valid;
    /** 冲突说明，如 permCodeConflict / menuPathConflict / apiConflict */
    private String message;

    public PermissionValidateResultDto() {
    }

    public PermissionValidateResultDto(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public static PermissionValidateResultDto ok() {
        return new PermissionValidateResultDto(true, null);
    }

    public static PermissionValidateResultDto conflict(String message) {
        return new PermissionValidateResultDto(false, message);
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
