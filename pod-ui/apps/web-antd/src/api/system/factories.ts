import { requestClient } from '#/api/request';

export interface FactoryRecord {
  id: number;
  tenantId?: number;
  factoryCode: string;
  factoryName: string;
  status?: string;
  countryCode?: string;
  province?: string;
  city?: string;
  address?: string;
  contactName?: string;
  contactPhone?: string;
}

export interface FactoryPageQuery {
  current?: number;
  size?: number;
  keyword?: string;
  status?: string;
  tenantId?: number;
}

export interface FactoryPageResult {
  records: FactoryRecord[];
  total: number;
  size: number;
  current: number;
}

const BASE = '/iam/factories';

export function getFactoryPage(params: FactoryPageQuery) {
  return requestClient.get<FactoryPageResult>(BASE, { params });
}

export function getFactoryAll(tenantId?: number) {
  return requestClient.get<FactoryRecord[]>(`${BASE}/all`, { params: tenantId != null ? { tenantId } : {} });
}

/** 数据权限页拉取工厂列表，仅需 iam:scope:query */
export function getFactoriesForScope(tenantId?: number) {
  return requestClient.get<FactoryRecord[]>(`${BASE}/forScope`, { params: tenantId != null ? { tenantId } : {} });
}

export function getFactory(id: number) {
  return requestClient.get<FactoryRecord>(`${BASE}/${id}`);
}

export function createFactory(data: Partial<FactoryRecord>) {
  return requestClient.post(BASE, data);
}

export function updateFactory(id: number, data: Partial<FactoryRecord>) {
  return requestClient.put(`${BASE}/${id}`, data);
}

export function deleteFactory(id: number) {
  return requestClient.delete(`${BASE}/${id}`);
}
