import { requestClient } from '#/api/request';

export interface PermissionTreeDto {
  id: number;
  tenantId?: number;
  factoryId?: number;
  permCode: string;
  permName: string;
  permType: string;
  menuPath?: string;
  component?: string;
  icon?: string;
  redirect?: string;
  apiMethod?: string;
  apiPath?: string;
  parentId?: number;
  sortNo?: number;
  status?: string;
  hidden?: boolean;
  keepAlive?: boolean;
  alwaysShow?: boolean;
  children?: PermissionTreeDto[];
}

export interface PermissionCreateDto {
  permCode: string;
  permName?: string;
  permType: string;
  menuPath?: string;
  component?: string;
  icon?: string;
  redirect?: string;
  apiMethod?: string;
  apiPath?: string;
  parentId?: number;
  sortNo?: number;
  status?: string;
  hidden?: boolean;
  keepAlive?: boolean;
  alwaysShow?: boolean;
}

export interface PermissionUpdateDto {
  permName?: string;
  permType?: string;
  menuPath?: string;
  component?: string;
  icon?: string;
  redirect?: string;
  apiMethod?: string;
  apiPath?: string;
  parentId?: number;
  sortNo?: number;
  status?: string;
  hidden?: boolean;
  keepAlive?: boolean;
  alwaysShow?: boolean;
}

export interface PermissionValidateResult {
  valid: boolean;
  message?: string;
}

const BASE = '/iam/permissions';

export interface PermissionPageQuery {
  current?: number;
  size?: number;
  keyword?: string;
  permType?: string;
}

export interface PermissionPageResult {
  records: PermissionTreeDto[];
  total: number;
  size: number;
  current: number;
}

export function getPermissionPage(params: PermissionPageQuery) {
  return requestClient.get<PermissionPageResult>(BASE, { params });
}

export function getPermissionTree(permType: 'MENU' | 'BUTTON' | 'API' | 'ALL' = 'ALL') {
  return requestClient.get<PermissionTreeDto[]>(`${BASE}/tree`, { params: { permType } });
}

export function getPermission(id: number) {
  return requestClient.get<PermissionTreeDto>(`${BASE}/${id}`);
}

export function createPermission(data: PermissionCreateDto) {
  return requestClient.post(BASE, data);
}

export function updatePermission(id: number, data: PermissionUpdateDto) {
  return requestClient.put(`${BASE}/${id}`, data);
}

export function deletePermission(id: number) {
  return requestClient.delete(`${BASE}/${id}`);
}

export function validatePermission(params: {
  permCode?: string;
  menuPath?: string;
  apiMethod?: string;
  apiPath?: string;
  excludeId?: number;
  permType?: string;
}) {
  return requestClient.get<PermissionValidateResult>(`${BASE}/validate`, { params });
}
