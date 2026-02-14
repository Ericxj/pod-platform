package com.pod.iam.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MenuValidationResultDto implements Serializable {
    private boolean ok;
    private List<ValidationError> errors = new ArrayList<>();
    private List<ValidationError> warnings = new ArrayList<>();

    public boolean isOk() { return ok; }
    public void setOk(boolean ok) { this.ok = ok; }
    public List<ValidationError> getErrors() { return errors; }
    public void setErrors(List<ValidationError> errors) { this.errors = errors; }
    public List<ValidationError> getWarnings() { return warnings; }
    public void setWarnings(List<ValidationError> warnings) { this.warnings = warnings; }

    public void addError(String code, String message, Long menuId, String menuName, String path, String component) {
        this.errors.add(new ValidationError(code, message, menuId, menuName, path, component));
        this.ok = false;
    }

    public void addWarning(String code, String message, Long menuId, String menuName, String path, String component) {
        this.warnings.add(new ValidationError(code, message, menuId, menuName, path, component));
    }

    public static class ValidationError implements Serializable {
        private String code;
        private String message;
        private Long menuId;
        private String menuName;
        private String path;
        private String component;

        public ValidationError() {}
        public ValidationError(String code, String message, Long menuId, String menuName, String path, String component) {
            this.code = code;
            this.message = message;
            this.menuId = menuId;
            this.menuName = menuName;
            this.path = path;
            this.component = component;
        }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Long getMenuId() { return menuId; }
        public void setMenuId(Long menuId) { this.menuId = menuId; }
        public String getMenuName() { return menuName; }
        public void setMenuName(String menuName) { this.menuName = menuName; }
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public String getComponent() { return component; }
        public void setComponent(String component) { this.component = component; }
    }
}
