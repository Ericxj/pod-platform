# 后端菜单 component 填写规范

供 `/api/iam/me/menus` 返回的 `menus[].component` 与前端 `src/views` 目录一致，避免「View component not found」。

## 契约（与前端约定）

| 场景 | component 值 | 说明 |
|------|--------------|------|
| 目录/布局节点（有 children） | `LAYOUT` 或 `Layout` | 前端统一映射为 BasicLayout，**建议统一用 `LAYOUT`** |
| 页面叶子节点 | **禁止前导 `/`** | 例如 `system/user/index`，对应前端 `src/views/system/user/index.vue` |

## 规则

1. **根/目录节点**：`component` 统一为 `LAYOUT`（大写），前端会映射为 `#/layouts/basic.vue`。
2. **叶子节点**：
   - 去掉前导 `/`：不要返回 `/system/user/index`，应返回 `system/user/index`。
   - 与前端路径一一对应：`system/user/index` → `src/views/system/user/index.vue`；`system/role/index` → `src/views/system/role/index.vue`。
3. **未配置 component 时**：后端可按 `menu_path` 推导，例如 path 为 `/system/user` 时输出 `system/user/index`。
4. **非法值**：含 `..`、`\`、`: `、`http` 或以 `api` 开头的 component 会被后端归一化为占位路径，前端可能仍报错，请勿使用。

## 示例（正确）

```json
{
  "name": "System",
  "path": "/system",
  "component": "LAYOUT",
  "children": [
    {
      "name": "UserManage",
      "path": "/system/user",
      "component": "system/user/index"
    }
  ]
}
```

## 前端解析（参考）

- `LAYOUT` / `layout` → BasicLayout
- `system/user/index` → `import.meta.glob('../views/**/*.vue')` 中的 `../views/system/user/index.vue`
- 若找不到对应 `.vue`，前端会抛出 `View component not found: xxx` 并提示用户
