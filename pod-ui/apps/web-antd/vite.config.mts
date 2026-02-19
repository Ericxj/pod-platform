import { loadEnv } from 'vite';
import { defineConfig } from '@vben/vite-config';

/**
 * Vite dev server (默认 localhost:5666) 将 /api 代理到后端 Spring Boot（Tomcat 端口，默认 8080），
 * 避免 /api/* 被当作静态资源返回 "No static resource"。
 * 后端端口可通过 .env.development 中 VITE_PROXY_TARGET 覆盖。
 */
export default defineConfig(async (config) => {
  const mode = config?.mode ?? 'development';
  const env = loadEnv(mode, process.cwd(), 'VITE_');
  const proxyTarget = env.VITE_PROXY_TARGET ?? 'http://localhost:8080';

  return {
    application: {},
    vite: {
      server: {
        proxy: {
          '/api': {
            target: proxyTarget,
            changeOrigin: true,
            ws: true,
          },
        },
      },
    },
  };
});
