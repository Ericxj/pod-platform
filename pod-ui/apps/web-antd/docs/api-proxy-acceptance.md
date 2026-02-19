# /api 代理验收（Vite dev 不再返回 "No static resource"）

## 1) 确认 localhost:5666 为 Vite dev server

- 在 `pod-ui/apps/web-antd` 执行：`pnpm dev`（或 `pnpm vite --mode development`）。
- 浏览器访问 `http://localhost:5666`，能打开前端登录页。
- 端口来自 `.env.development` 中的 `VITE_PORT=5666`。

## 2) Vite 配置：`/api` 代理到 Spring Boot

- 文件：`pod-ui/apps/web-antd/vite.config.mts`。
- `server.proxy['/api']` 已配置：
  - `target`：默认 `http://localhost:8080`（与 pod-start 的 `server.port: 8080` 一致），可通过 `.env.development` 的 `VITE_PROXY_TARGET` 覆盖。
  - `changeOrigin: true`，`ws: true`。
- 开发环境下前端请求使用相对路径 `VITE_GLOB_API_URL=/api`，因此请求发往 `http://localhost:5666/api/xxx`，由 Vite 将 `/api` 代理到后端，不再当静态资源处理。

## 3) 验收步骤

1. **启动后端**  
   启动 Spring Boot（如 `pod-start`），确认启动日志中 Tomcat 端口（默认 8080）。

2. **启动前端**  
   在 `pod-ui/apps/web-antd` 执行 `pnpm dev`，确认控制台有 “Local: http://localhost:5666” 或等价输出。

3. **验证 /api 走代理、不走静态资源**  
   - 浏览器访问（或用 curl）：
     - `http://localhost:5666/api/iam/permissions/tree`  
       → 应返回后端 JSON（如权限树或 401），**不应**出现 "No static resource" 或前端 404 页。
     - `http://localhost:5666/api/inv/ledgers`  
       → 应返回后端 JSON（如分页数据或 401），**不应**出现 "No static resource" 或前端 404 页。
   - 若后端未启动，应得到连接错误或 502/504，而不是 "No static resource"。

4. **可选**  
   修改 `.env.development` 中 `VITE_PROXY_TARGET` 为实际后端地址（如 `http://localhost:8081`），重启 dev server 后再次访问上述两个 URL，确认仍走代理且不再走静态资源。

## 修改文件列表

| 文件 | 变更 |
|------|------|
| `vite.config.mts` | 使用 `loadEnv` 读取 `VITE_PROXY_TARGET`，默认 `http://localhost:8080`；保留并明确 `/api` 代理配置。 |
| `.env.development` | 新增 `VITE_PROXY_TARGET=http://localhost:8080` 说明与默认值。 |
