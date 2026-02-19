# INV 动态路由修复验收（pod-platform）

## 修改文件列表

| 文件 | 说明 |
|------|------|
| `pod-ui/apps/web-antd/src/router/access.ts` | 归一化 component、resolveView 多候选 + 错误带候选 key、normalizeMenuComponents 前置 |
| `pod-ui/packages/utils/src/helpers/generate-routes-backend.ts` | normalizeViewPath 统一反斜杠为 `/` |

## 关键改动摘要

### access.ts

- **normalizeViewPathForMatch**：首行 `path.replace(/\\/g, '/')`，与 generate-routes-backend 一致。
- **normalizeComponent**：trim、去前导/尾随 `/`、去 `.vue`、`\`→`/`、合并连续 `/`；LAYOUT/IFrameView 保留。
- **resolveView**：兼容 `'/inv/balance/index'` / `'/inv/balance/index.vue'` / `'/inv/balance/index/index'`；尝试顺序 `/{path}.vue` → `/{path}/index.vue`；报错时附带候选 key 列表。
- **normalizeMenuComponents(menus)**：在 `resolveComponentFallback` 前对整棵菜单树做 component 归一化（仅叶子节点）。
- **流程**：拉取 menus → `normalizeMenuComponents(menus)` → `resolveComponentFallback(menus, pageMap)`。

### generate-routes-backend.ts

- **normalizeViewPath**：首行 `path.replace(/\\/g, '/')`，保证 Windows 下 glob 键与 component 一致。

## 验收步骤

1. **登录**：使用已配置 INV 菜单/权限的账号登录。
2. **菜单挂载**：侧栏出现「库存」等 INV 菜单，控制台无 `View component not found: inv/balance/index`。
3. **INV 三页**：点击「库存余额」「预占管理」「库存台账」分别进入对应页面。
4. **回归 system**：进入「系统管理」→「用户管理」等，确认 `/system/user/index` 等仍正常。

## 后端 component 约定

以下形式均可被正确解析（以余额为例）：

- `'/inv/balance/index'`、`'/inv/balance/index.vue'`、`'inv/balance/index'`、`'/inv/balance/index/'`

解析优先级：`/{path}.vue` → `/{path}/index.vue`。
