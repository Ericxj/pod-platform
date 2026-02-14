import { requestClient } from '#/api/request';

export interface TenantRecord {
  id: number;
  tenantCode: string;
  tenantName: string;
  status?: string;
  planType?: string;
  planExpireAt?: string;
}

export interface TenantPageQuery {
  current?: number;
  size?: number;
  keyword?: string;
  status?: string;
}

export interface TenantPageResult {
  records: TenantRecord[];
  total: number;
  size: number;
  current: number;
}

const BASE = '/iam/tenants';

export function getTenantPage(params: TenantPageQuery) {
  return requestClient.get<TenantPageResult>(BASE, { params });
}

export function getTenant(id: number) {
  return requestClient.get<TenantRecord>(`${BASE}/${id}`);
}

export function createTenant(data: Partial<TenantRecord>) {
  return requestClient.post(BASE, data);
}

export function updateTenant(id: number, data: Partial<TenantRecord>) {
  return requestClient.put(`${BASE}/${id}`, data);
}

export function deleteTenant(id: number) {
  return requestClient.delete(`${BASE}/${id}`);
}
