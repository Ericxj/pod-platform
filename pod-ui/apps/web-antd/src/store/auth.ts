import type { Recordable, UserInfo } from '@vben/types';

import { ref } from 'vue';
import { useRouter } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';

import { LOGIN_PATH } from '@vben/constants';
import { preferences } from '@vben/preferences';
import { resetAllStores, useAccessStore, useUserStore } from '@vben/stores';

import { notification } from 'ant-design-vue';
import { defineStore } from 'pinia';

import { getUserInfoApi, loginApi, logoutApi } from '#/api';
import { $t } from '#/locales';
import { useFactoryStore } from '#/store';
import { generateAccess } from '#/router/access';
import { accessRoutes } from '#/router/routes';

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
   * Force fetch and mount menus
   */
  async function fetchAndMountMenus() {
      try {
        console.info('[Auth] Force fetching menus...');
        const { accessibleMenus, accessibleRoutes } = await generateAccess({
            roles: userStore.userInfo?.roles || [],
            router,
            routes: accessRoutes,
        });

        accessStore.setAccessMenus(accessibleMenus);
        accessStore.setAccessRoutes(accessibleRoutes);
        accessStore.setIsAccessChecked(true);

        console.info('[Auth] Menus mounted:', accessibleMenus.length);
        return accessibleRoutes;
      } catch (e) {
        console.error('[Auth] Failed to fetch menus:', e);
        return [];
      }
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

        // 注意：权限码 (AccessCodes) 现在由路由守卫中的 fetchMenuListAsync (generateAccess) 懒加载
        // 所以这里不需要提前 fetchAccessCodes
        
        console.info('[Auth] Triggering route navigation...');

        // Force fetch menus
        const routes = await fetchAndMountMenus();

        if (accessStore.loginExpired) {
          accessStore.setLoginExpired(false);
        } else {
          if (onSuccess) {
            await onSuccess();
          } else {
             // Determine where to go
             let redirectPath = userInfo?.homePath || preferences.app.defaultHomePath;
             
             // If we have dynamic routes, try to find a better default if homePath is generic
             // Or strict user requirement: "Jump to first backend menu"
             if (routes && routes.length > 0) {
                 const firstRoute = findFirstLeafRoute(routes);
                 if (firstRoute) {
                     redirectPath = firstRoute.path;
                     console.info('[Auth] Redirecting to first available menu:', redirectPath);
                 }
             }
             
             await router.push(redirectPath);
             console.info('[Auth] Navigation initiated to:', redirectPath);
          }
        }

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
    try {
      await logoutApi();
    } catch {
      // 不做任何处理
    }
    resetAllStores();
    accessStore.setLoginExpired(false);
    userInfoLoaded.value = false;

    // 回登录页带上当前路由地址
    await router.replace({
      path: LOGIN_PATH,
      query: redirect
        ? {
            redirect: encodeURIComponent(router.currentRoute.value.fullPath),
          }
        : {},
    });
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

  return {
    authLogin,
    fetchUserInfo,
    loginLoading,
    logout,
    userInfoLoaded,
    userInfoLoading,
  };
});
