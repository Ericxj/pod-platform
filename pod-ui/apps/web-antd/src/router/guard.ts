import type { Router } from 'vue-router';

import { LOGIN_PATH } from '@vben/constants';
import { preferences } from '@vben/preferences';
import { useAccessStore, useUserStore } from '@vben/stores';
import { startProgress, stopProgress } from '@vben/utils';

import { accessRoutes, coreRouteNames } from '#/router/routes';
import { useAuthStore } from '#/store';

import { generateAccess } from './access';

/**
 * 通用守卫配置
 * @param router
 */
function setupCommonGuard(router: Router) {
  // 记录已经加载的页面
  const loadedPaths = new Set<string>();

  router.beforeEach((to) => {
    to.meta.loaded = loadedPaths.has(to.path);

    // 页面加载进度条
    if (!to.meta.loaded && preferences.transition.progress) {
      startProgress();
    }
    return true;
  });

  router.afterEach((to) => {
    // 记录页面是否加载,如果已经加载，后续的页面切换动画等效果不在重复执行

    loadedPaths.add(to.path);

    // 关闭页面加载进度条
    if (preferences.transition.progress) {
      stopProgress();
    }
  });
}

/**
 * 权限访问守卫配置
 * @param router
 */
function setupAccessGuard(router: Router) {
  router.beforeEach(async (to, from) => {
    const accessStore = useAccessStore();
    const userStore = useUserStore();
    const authStore = useAuthStore();

    // 基本路由（除“登录且已带 token”外）不进入权限拦截；登录且带 token 时先注入动态路由再 replace 离开登录页
    if (coreRouteNames.includes(to.name as string)) {
      if (to.path === LOGIN_PATH && accessStore.accessToken) {
        if (accessStore.isAccessChecked) {
          const fromQuery = decodeURIComponent((to.query?.redirect as string) || '');
          const first = findFirstLeafRoute(accessStore.accessRoutes);
          const redirectPath =
            fromQuery || first?.path || userStore.userInfo?.homePath || preferences.app.defaultHomePath;
          return { path: redirectPath, replace: true };
        }
      } else {
        return true;
      }
    }

    // accessToken 检查
    if (!accessStore.accessToken) {
      // 明确声明忽略权限访问权限，则可以访问
      if (to.meta.ignoreAccess) {
        return true;
      }

      // 没有访问权限，跳转登录页面
      if (to.fullPath !== LOGIN_PATH) {
        return {
          path: LOGIN_PATH,
          // 如不需要，直接删除 query
          query:
            to.fullPath === preferences.app.defaultHomePath
              ? {}
              : { redirect: encodeURIComponent(to.fullPath) },
          // 携带当前跳转的页面，登录后重新跳转该页面
          replace: true,
        };
      }
      return to;
    }

    // 是否已经生成过动态路由
    if (accessStore.isAccessChecked) {
      return true;
    }

    // 生成路由表
    // 当前登录用户拥有的角色标识列表
    let userInfo = userStore.userInfo;
    if (!userInfo && !authStore.userInfoLoading) {
        try {
            userInfo = await authStore.fetchUserInfo();
        } catch (error) {
            console.error('[Guard] Failed to fetch user info', error);
            // 失败时清除 token 并重定向到登录页，避免死循环
            await authStore.logout(false);
            return {
                path: LOGIN_PATH,
                replace: true,
            };
        }
    }
    
    if (!userInfo) {
      // Failed to get user info (e.g. token expired or invalid)
      // Double check to avoid loop if we are already redirecting to login
      if (to.path !== LOGIN_PATH) {
        await authStore.logout(false);
        return {
          path: LOGIN_PATH,
          query: { redirect: encodeURIComponent(to.fullPath) },
          replace: true,
        };
      }
      return true;
    }
    
    const userRoles = userInfo.roles ?? [];

    let accessibleMenus: any[] = [];
    let accessibleRoutes: any[] = [];
    try {
      const result = await generateAccess({
        roles: userRoles,
        router,
        routes: accessRoutes,
      });
      accessibleMenus = result.accessibleMenus;
      accessibleRoutes = result.accessibleRoutes;
    } catch (e) {
      const errMsg = e instanceof Error ? e.message : String(e);
      console.error('[Guard] Route build failed (component not found or similar):', e);
      const msgForQuery = errMsg.length > 400 ? `${errMsg.slice(0, 400)}…` : errMsg;
      return {
        path: '/empty-menus',
        query: { reason: 'build-failed', message: msgForQuery },
        replace: true,
      };
    }

    if (accessibleMenus.length === 0) {
      if (import.meta.env.DEV) console.warn('[Guard] No menus from backend.');
      return {
        path: '/empty-menus',
        query: { reason: 'no-menus' },
        replace: true,
      };
    }

    accessStore.setAccessMenus(accessibleMenus);
    accessStore.setAccessRoutes(accessibleRoutes);
    accessStore.setIsAccessChecked(true);

    if (import.meta.env.DEV) {
      console.info('[Guard] menus loaded:', accessibleMenus.length, 'routes injected:', accessibleRoutes.length);
      const first = findFirstLeafRoute(accessibleRoutes);
      if (first) console.info('[Guard] First leaf path:', first.path);
    }

    const isFromLogin = to.path === LOGIN_PATH || to.path.startsWith('/auth');
    let redirectPath = (from.query?.redirect
      ? decodeURIComponent(String(from.query.redirect))
      : (to.path === preferences.app.defaultHomePath
        ? userInfo.homePath || preferences.app.defaultHomePath
        : to.fullPath)) as string;
    if (isFromLogin && redirectPath.startsWith('/auth')) {
      const first = findFirstLeafRoute(accessibleRoutes);
      redirectPath = first?.path || preferences.app.defaultHomePath;
      if (import.meta.env.DEV) console.info('[Guard] From login, redirectPath:', redirectPath);
    }

    const resolved = router.resolve(redirectPath);
    const is404 = resolved.matched.length === 0 || resolved.name === 'PageNotFound' || resolved.name === 'FallbackNotFound';
    if (is404) {
      if (import.meta.env.DEV) console.warn('[Guard] Target path not found:', redirectPath);
      const firstRoute = findFirstLeafRoute(accessibleRoutes);
      redirectPath = firstRoute?.path ?? '/empty-menus';
    }

    return {
      ...router.resolve(decodeURIComponent(redirectPath)),
      replace: true,
    };
  });
}

function findFirstLeafRoute(routes: any[]): any {
    for (const route of routes) {
        if (route.children && route.children.length > 0) {
            const child = findFirstLeafRoute(route.children);
            if (child) return child;
        } else if (!route.meta?.hideInMenu && !route.redirect) {
            return route;
        }
    }
    return null;
}

/**
 * 项目守卫配置
 * @param router
 */
function createRouterGuard(router: Router) {
  /** 通用 */
  setupCommonGuard(router);
  /** 权限访问 */
  setupAccessGuard(router);
}

export { createRouterGuard };
