import { requestClient } from '#/api/request';

export interface RoleRecord {
  id: number;
  tenantId?: number;
  factoryId?: number;
  roleCode: string;
  roleName: string;
  roleType?: string;
  status: string;
  remark?: string;
  createdAt?: string;
}

export interface RolePageQuery {
  current?: number;
  size?: number;
  keyword?: string;
  roleCode?: string;
  roleName?: string;
  status?: string;
}

export interface RolePageResult {
  records: RoleRecord[];
  total: number;
  size: number;
  current: number;
}

export interface RoleCreateDto {
  roleCode: string;
  roleName: string;
  roleType?: string;
  status?: string;
  remark?: string;
}

export interface RoleUpdateDto {
  roleName?: string;
  status?: string;
  remark?: string;
}

export interface RolePermissionsDto {
  permIds: number[];
}

const BASE = '/iam/roles';

export function getRolePage(params: RolePageQuery) {
  return requestClient.get<RolePageResult>(BASE, { params });
}

export function getRole(id: number) {
  return requestClient.get<RoleRecord>(`${BASE}/${id}`);
}

export function createRole(data: RoleCreateDto) {
  return requestClient.post(BASE, data);
}

export function updateRole(id: number, data: RoleUpdateDto) {
  return requestClient.put(`${BASE}/${id}`, data);
}

export function deleteRole(id: number) {
  return requestClient.delete(`${BASE}/${id}`);
}

export function getRolePermissions(id: number) {
  return requestClient.get<RolePermissionsDto>(`${BASE}/${id}/permissions`);
}

export function putRolePermissions(id: number, body: RolePermissionsDto) {
  return requestClient.put(`${BASE}/${id}/permissions`, body);
}

export function grantRolePermissions(id: number, body: RolePermissionsDto) {
  return requestClient.post(`${BASE}/${id}/grantPermissions`, body);
}
