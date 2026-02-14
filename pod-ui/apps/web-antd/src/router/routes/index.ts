import type { RouteRecordRaw } from 'vue-router';

import { traverseTreeValues } from '@vben/utils';

import { coreRoutes, fallbackNotFoundRoute } from './core';

// 纯后端动态模式：不加载任何静态业务路由模块，菜单与路由全部由 /api/iam/me/menus 注入
const externalRoutes: RouteRecordRaw[] = [];

/** 路由列表：仅基础路由(login/redirect/error) + 404 */
const routes: RouteRecordRaw[] = [
  ...coreRoutes,
  ...externalRoutes,
  fallbackNotFoundRoute,
];

/** 基本路由列表，这些路由不需要进入权限拦截 */
const coreRouteNames = traverseTreeValues(coreRoutes, (route) => route.name);

/** 静态业务路由（已废弃）：纯后端模式下为空，路由由 generateAccess 从后端菜单动态注入 */
const accessRoutes: RouteRecordRaw[] = [];
export { accessRoutes, coreRouteNames, routes };
