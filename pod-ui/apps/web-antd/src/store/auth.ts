import type { Recordable, UserInfo } from '@vben/types';

import { ref } from 'vue';
import { useRouter } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';

import { LOGIN_PATH } from '@vben/constants';
import { preferences } from '@vben/preferences';
import { resetAllStores, useAccessStore, useUserStore } from '@vben/stores';
import { resetStaticRoutes } from '@vben/utils';

import { notification } from 'ant-design-vue';
import { defineStore } from 'pinia';
import { nextTick } from 'vue';

import { getUserInfoApi, loginApi, logoutApi } from '#/api';
import { $t } from '#/locales';
import { useFactoryStore } from '#/store';
import { generateAccess } from '#/router/access';
import { accessRoutes, routes } from '#/router/routes';

/** 清除与登录态相关的持久化 key（pinia-plugin-persistedstate 的 key 为 namespace-${storeId}） */
function clearLoginStateStorage() {
  const suffixes = [
    '-core-access',
    '-core-tabbar',
    '-core-user',
    '-auth',
    '-factory',
  ];
  const clear = (storage: Storage) => {
    const keys: string[] = [];
    for (let i = 0; i < storage.length; i++) {
      const k = storage.key(i);
      if (k && suffixes.some((s) => k.endsWith(s))) keys.push(k);
    }
    keys.forEach((k) => storage.removeItem(k));
  };
  try {
    clear(localStorage);
    clear(sessionStorage);
  } catch (e) {
    if (import.meta.env.DEV) console.warn('[Auth] clearLoginStateStorage:', e);
  }
}

export const useAuthStore = defineStore('auth', () => {
  const accessStore = useAccessStore();
  const userStore = useUserStore();
  const factoryStore = useFactoryStore();
  const router = useRouter();

  const loginLoading = ref(false);
  const userInfoLoading = ref(false);
  const userInfoLoaded = ref(false);

  /**
   * Helper to find first leaf route
   */
  function findFirstLeafRoute(routes: RouteRecordRaw[]): RouteRecordRaw | null {
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
   * Force fetch and mount menus.
   * - On success: 返回 accessibleRoutes，调用方负责 replace 到 targetPath。
   * - On 构建失败 (component 不存在等): 抛出，调用方跳 /empty-menus?reason=build-failed。
   * - On 后端 menus 为空: 返回 []，调用方跳 /empty-menus?reason=no-menus。
   */
  async function fetchAndMountMenus(): Promise<RouteRecordRaw[]> {
    if (import.meta.env.DEV) console.info('[Auth] Fetching menus and injecting routes...');
    const { accessibleMenus, accessibleRoutes } = await generateAccess({
      roles: userStore.userInfo?.roles || [],
      router,
      routes: accessRoutes,
    });
    accessStore.setAccessMenus(accessibleMenus);
    accessStore.setAccessRoutes(accessibleRoutes);
    accessStore.setIsAccessChecked(true);
    if (import.meta.env.DEV) {
      console.info('[Auth] Routes injected:', accessibleRoutes.length);
      const first = findFirstLeafRoute(accessibleRoutes);
      if (first) console.info('[Auth] First leaf path:', first.path);
    }
    return accessibleRoutes;
  }

  /**
   * 异步处理登录操作
   * Asynchronously handle the login process
   * @param params 登录表单数据
   */
  async function authLogin(
    params: Recordable<any>,
    onSuccess?: () => Promise<void> | void,
  ) {
    // 异步处理用户登录操作并获取 accessToken
    let userInfo: null | UserInfo = null;
    try {
      loginLoading.value = true;
      const result = await loginApi(params);
      const accessToken = result.token;

      // 如果成功获取到 accessToken
      if (accessToken) {
        accessStore.setAccessToken(accessToken);

        // 获取用户信息并存储到 accessStore 中
        // 优先使用登录接口返回的用户信息，如果缺失则调用 /me 接口
        // Compatible: check both 'me' and 'user' fields
        if (result.me) {
           userInfo = result.me;
        } else if (result.user) {
           userInfo = result.user;
        } else {
           userInfo = await getUserInfoApi();
        }

        // 补全 UserInfo 关键字段，防止路由跳转失败
        if (userInfo) {
            // 默认首页路径
            // if (!userInfo.homePath) {
            //    userInfo.homePath = '/workbench';
            // }
            // 确保 roles 存在（Vben 权限校验可能依赖）
            if (!userInfo.roles || userInfo.roles.length === 0) {
                userInfo.roles = ['admin']; // 默认角色，防止空角色被拦截
            }
            
            userInfoLoaded.value = true;
        }

        userStore.setUserInfo(userInfo);

        // Initialize Factory Settings
        // Support both nested dataScopes and flat structure
        const factoryIds = result.dataScopes?.factoryIds || result.factoryIds || [];
        const currentFactoryId = result.dataScopes?.defaultFactoryId || result.currentFactoryId;
        
        console.info('[Auth] Login success, preparing to fetch menus and routes...');

        if (factoryIds.length > 0 || currentFactoryId) {
             factoryStore.setFactoryIds(factoryIds);
             factoryStore.initFactorySettings(currentFactoryId);
             console.info('[Auth] Factory context initialized:', currentFactoryId);
        }

        if (import.meta.env.DEV) console.info('[Auth] Fetching menus and injecting routes (await)...');

        let routes: RouteRecordRaw[] = [];
        let redirectPath: string;

        try {
          routes = await fetchAndMountMenus();
        } catch (e) {
          const errMsg = e instanceof Error ? e.message : String(e);
          console.error('[Auth] Menu/route build failed:', e);
          redirectPath = `/empty-menus?reason=build-failed&message=${encodeURIComponent(errMsg)}`;
          await router.replace(redirectPath);
          if (userInfo?.realName) {
            notification.success({
              description: `${$t('authentication.loginSuccessDesc')}:${userInfo?.realName}`,
              duration: 3,
              message: $t('authentication.loginSuccess'),
            });
          }
          return { userInfo };
        }

        if (accessStore.loginExpired) {
          accessStore.setLoginExpired(false);
        }
        if (onSuccess) {
          await onSuccess();
        }

        const queryRedirect = router.currentRoute.value.query?.redirect as string | undefined;
        if (routes.length > 0) {
          const firstRoute = findFirstLeafRoute(routes);
          const resolvedRedirect =
            queryRedirect &&
            queryRedirect.startsWith('/') &&
            !queryRedirect.startsWith('/auth')
              ? decodeURIComponent(queryRedirect)
              : null;
          redirectPath =
            resolvedRedirect ??
            firstRoute?.path ??
            userInfo?.homePath ??
            preferences.app.defaultHomePath;
          const resolved = router.resolve(redirectPath);
          if (resolved.matched.length === 0 || resolved.name === 'PageNotFound' || resolved.name === 'FallbackNotFound') {
            redirectPath = firstRoute?.path ?? redirectPath;
          }
        } else {
          redirectPath = '/empty-menus?reason=no-menus';
        }

        if (import.meta.env.DEV) console.info('[Auth] redirectPath:', redirectPath);
        await router.replace(redirectPath);
        if (import.meta.env.DEV) console.info('[Auth] Replaced to:', redirectPath);

        if (userInfo?.realName) {
          notification.success({
            description: `${$t('authentication.loginSuccessDesc')}:${userInfo?.realName}`,
            duration: 3,
            message: $t('authentication.loginSuccess'),
          });
        }
      }
    } finally {
      loginLoading.value = false;
    }

    return {
      userInfo,
    };
  }

  async function logout(redirect: boolean = true) {
    // A. 通知后端登出（失败不阻塞）
    try {
      await logoutApi();
    } catch {
      // ignore
    }

    // B. 清除本地令牌与缓存
    accessStore.setAccessToken(null);
    accessStore.setRefreshToken(null);
    clearLoginStateStorage();

    // C. 重置所有 store（fail-safe：单 store 失败不中断）
    try {
      resetAllStores();
    } catch {
      // ignore
    }
    accessStore.setLoginExpired(false);
    userInfoLoaded.value = false;

    // D. 重置路由
    try {
      resetStaticRoutes(router, routes);
    } catch (e) {
      if (import.meta.env.DEV) console.warn('[Auth] resetStaticRoutes:', e);
    }

    // E. 必须：跳转登录页
    const loginQuery = redirect
      ? { redirect: encodeURIComponent(router.currentRoute.value.fullPath) }
      : {};
    try {
      await router.replace({ path: LOGIN_PATH, query: loginQuery });
    } catch (e) {
      if (import.meta.env.DEV) console.warn('[Auth] router.replace:', e);
    }

    // F. 兜底：仍未到 login 则 location 强刷
    await nextTick();
    const current = router.currentRoute.value;
    const isLogin =
      current.fullPath === LOGIN_PATH || current.fullPath.startsWith('/auth');
    if (!isLogin) {
      const hash = import.meta.env.VITE_ROUTER_HISTORY === 'hash' ? '#/auth/login' : '/auth/login';
      const url = hash.startsWith('#')
        ? `${window.location.origin}${window.location.pathname}${hash}`
        : `${window.location.origin}${hash}`;
      window.location.replace(url);
    }
  }

  async function fetchUserInfo() {
    if (userInfoLoading.value) return userStore.userInfo;
    if (userInfoLoaded.value && userStore.userInfo) return userStore.userInfo;

    let userInfo: null | UserInfo = null;
    try {
        userInfoLoading.value = true;
        console.info('[Auth] Fetching user info from backend...');
        userInfo = await getUserInfoApi();
        
        // 补全 UserInfo 关键字段，防止路由跳转失败
        if (userInfo) {
            // 默认首页路径
            // if (!userInfo.homePath) {
            //    userInfo.homePath = '/workbench';
            // }
            // 确保 roles 存在（Vben 权限校验可能依赖）
            if (!userInfo.roles || userInfo.roles.length === 0) {
                userInfo.roles = ['admin']; // 默认角色，防止空角色被拦截
            }
            userInfoLoaded.value = true;
            console.info('[Auth] User info fetched successfully');
        }
        userStore.setUserInfo(userInfo);
    } catch (error) {
        console.error('[Auth] Failed to fetch user info:', error);
        throw error;
    } finally {
        userInfoLoading.value = false;
    }
    return userInfo;
  }

  /**
   * 供 resetAllStores 调用：setup 语法 store 无 $reset，需实现 reset()。
   * 清空 token、本地缓存及本 store 的登录态，不依赖其他 store 的复位顺序。
   */
  function reset() {
    accessStore.setAccessToken(null);
    accessStore.setRefreshToken(null);
    accessStore.setAccessMenus([]);
    accessStore.setAccessRoutes([]);
    accessStore.setAccessCodes([]);
    accessStore.setIsAccessChecked(false);
    userStore.setUserInfo(null);
    if (typeof factoryStore.clear === 'function') factoryStore.clear();
    userInfoLoaded.value = false;
    loginLoading.value = false;
    userInfoLoading.value = false;
    clearLoginStateStorage();
  }

  return {
    authLogin,
    fetchUserInfo,
    loginLoading,
    logout,
    reset,
    userInfoLoaded,
    userInfoLoading,
  };
});
