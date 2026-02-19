import { requestClient } from '#/api/request';

export interface SpuRecord {
  id?: number;
  spuCode?: string;
  spuName?: string;
  categoryCode?: string;
  brand?: string;
  status?: string;
}

export interface SkuRecord {
  id?: number;
  spuId?: number;
  skuCode?: string;
  skuName?: string;
  price?: number;
  weightG?: number;
  attributesJson?: string;
  status?: string;
}

export interface SkuMappingRecord {
  id?: number;
  skuId?: number;
  skuCode?: string;
  channel?: string;
  shopId?: string;
  externalSku?: string;
  externalName?: string;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

const api = {
  spu: '/prd/spu',
  sku: '/prd/sku',
  barcode: '/prd/barcode',
  bom: '/prd/bom',
  routing: '/prd/routing',
  mapping: '/prd/mapping',
};

export function pageSpu(params: { current?: number; size?: number; keyword?: string; status?: string }) {
  return requestClient.get<PageResult<SpuRecord>>(api.spu + '/page', { params });
}

export function getSpu(id: number) {
  return requestClient.get<SpuRecord>(`${api.spu}/${id}`);
}

export function createSpu(data: Partial<SpuRecord>) {
  return requestClient.post<SpuRecord>(api.spu, data);
}

export function updateSpu(id: number, data: Partial<SpuRecord>) {
  return requestClient.put(`${api.spu}/${id}`, data);
}

export function pageSku(params: { current?: number; size?: number; spuId?: number; keyword?: string; status?: string }) {
  return requestClient.get<PageResult<SkuRecord>>(api.sku + '/page', { params });
}

export function getSku(id: number) {
  return requestClient.get<SkuRecord>(`${api.sku}/${id}`);
}

export function createSku(data: Partial<SkuRecord> & { spuId: number; skuCode: string }) {
  return requestClient.post<SkuRecord>(api.sku, data);
}

export function updateSku(id: number, data: Partial<SkuRecord>) {
  return requestClient.put(`${api.sku}/${id}`, data);
}

export function activateSku(id: number) {
  return requestClient.post(`${api.sku}/${id}/activate`, {});
}

export function deactivateSku(id: number) {
  return requestClient.post(`${api.sku}/${id}/deactivate`, {});
}

export function listBarcode(skuId: number) {
  return requestClient.get(api.barcode + '/list', { params: { skuId } });
}

export function batchAddBarcode(data: { skuId: number; barcodes: string[]; barcodeType?: string; isPrimary?: number }) {
  return requestClient.post(api.barcode + '/batchAdd', data);
}

export function deleteBarcode(id: number) {
  return requestClient.delete(`${api.barcode}/${id}`);
}

export function getBom(id: number) {
  return requestClient.get(`${api.bom}/${id}`);
}

export function getBomBySkuId(skuId: number) {
  return requestClient.get(`${api.bom}/by-sku`, { params: { skuId } });
}

export function getBomItems(id: number) {
  return requestClient.get(`${api.bom}/${id}/items`);
}

export function saveBom(data: { skuId: number; versionNo?: number; remark?: string; items?: any[] }) {
  return requestClient.post(api.bom + '/save', data);
}

export function publishBom(id: number) {
  return requestClient.post(`${api.bom}/${id}/publish`, {});
}

export function unpublishBom(id: number) {
  return requestClient.post(`${api.bom}/${id}/unpublish`, {});
}

export function getRouting(id: number) {
  return requestClient.get(`${api.routing}/${id}`);
}

export function getRoutingBySkuId(skuId: number) {
  return requestClient.get(`${api.routing}/by-sku`, { params: { skuId } });
}

export function getRoutingSteps(id: number) {
  return requestClient.get(`${api.routing}/${id}/steps`);
}

export function saveRouting(data: { skuId: number; versionNo?: number; steps?: any[] }) {
  return requestClient.post(api.routing + '/save', data);
}

export function publishRouting(id: number) {
  return requestClient.post(`${api.routing}/${id}/publish`, {});
}

export function unpublishRouting(id: number) {
  return requestClient.post(`${api.routing}/${id}/unpublish`, {});
}

export function pageSkuMapping(params: { current?: number; size?: number; channel?: string; shopId?: string; externalSku?: string; skuCode?: string }) {
  return requestClient.get<PageResult<SkuMappingRecord>>(api.mapping + '/page', { params });
}

export function createSkuMapping(data: { channel: string; shopId?: string; externalSku: string; externalName?: string; skuCode: string; remark?: string }) {
  return requestClient.post<SkuMappingRecord>(api.mapping, data);
}

export function updateSkuMapping(id: number, data: { externalName?: string; remark?: string }) {
  return requestClient.put(`${api.mapping}/${id}`, data);
}

export function deleteSkuMapping(id: number) {
  return requestClient.delete(`${api.mapping}/${id}`);
}
