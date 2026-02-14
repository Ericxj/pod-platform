import { defineOverridesPreferences } from '@vben/preferences';

/**
 * @description 项目配置文件 - 纯后端动态菜单（商业生产版）
 * permissionMode = BACK：accessMode: 'backend'，禁止 ROLE/STATIC/混合
 */
export const overridesPreferences = defineOverridesPreferences({
  app: {
    name: import.meta.env.VITE_APP_TITLE,
    accessMode: 'backend', // BACK：仅后端菜单/权限，禁止前端静态路由与角色映射
    defaultHomePath: '/empty-menus',
  },
});
