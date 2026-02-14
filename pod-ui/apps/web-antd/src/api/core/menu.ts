import type { RouteRecordStringComponent } from '@vben/types';

import { requestClient } from '#/api/request';

export interface MenuAndPermissionResult {
    menus: RouteRecordStringComponent[];
    permissionCodes: string[];
    dataScopes: {
        factoryIds: number[];
        defaultFactoryId: number;
    };
}

/**
 * 获取用户所有菜单与权限
 */
export async function getUserMenusApi() {
  return requestClient.get<MenuAndPermissionResult>('/iam/me/menus');
}

/**
 * Deprecated: use getUserMenusApi
 */
export async function getAllMenusApi() {
  const result = await getUserMenusApi();
  return result.menus;
}
