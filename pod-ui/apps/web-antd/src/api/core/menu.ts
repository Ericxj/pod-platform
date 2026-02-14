import type { RouteRecordStringComponent } from '@vben/types';

import { requestClient } from '#/api/request';

export interface MenuAndPermissionResult {
  menus: RouteRecordStringComponent[];
  permissionCodes: string[];
  dataScopes?: {
    factoryIds: number[];
    defaultFactoryId: number;
  };
}

/** 1) 获取用户信息：GET /api/iam/me（在 store/auth 中通过 getUserInfoApi 调用） */

/**
 * 2) 获取权限码列表：GET /api/iam/me/perms
 */
export async function getPermsApi() {
  const res = await requestClient.get<{ permissionCodes: string[] }>('/iam/me/perms');
  return res?.permissionCodes ?? [];
}

/**
 * 3) 获取菜单树：GET /api/iam/me/menus
 * 返回格式需与 RouteRecordStringComponent 兼容（path, name, component, meta, redirect, children）
 */
export async function getMenusApi() {
  const res = await requestClient.get<MenuAndPermissionResult>('/iam/me/menus');
  return res?.menus ?? [];
}

/**
 * 获取用户所有菜单与权限（menus + permissionCodes + dataScopes），用于兼容旧逻辑
 */
export async function getUserMenusApi() {
  return requestClient.get<MenuAndPermissionResult>('/iam/me/menus');
}

/**
 * @deprecated use getMenusApi
 */
export async function getAllMenusApi() {
  return getMenusApi();
}
