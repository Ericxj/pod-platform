import { requestClient } from '#/api/request';

// ---------- P1.5 出库单 API ----------
export interface OutboundRecord {
  id?: number;
  outboundNo?: string;
  fulfillmentId?: number;
  sourceType?: string;
  sourceNo?: string;
  status?: string;
  warehouseId?: number;
  lines?: OutboundLineRecord[];
  createdAt?: string;
}

export interface OutboundLineRecord {
  id?: number;
  outboundId?: number;
  lineNo?: number;
  skuId?: number;
  qty?: number;
  qtyPicked?: number;
  packedQty?: number;
  qtyShipped?: number;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

export interface PickingLineDto {
  lineId: number;
  pickedQty: number;
}

export function pageOutbounds(params: { current?: number; size?: number; status?: string }) {
  return requestClient.get<PageResult<OutboundRecord>>('/wms/outbounds', { params });
}

export function getOutboundDetail(id: number) {
  return requestClient.get<OutboundRecord>(`/wms/outbounds/${id}`);
}

export function createOutboundFromFulfillment(fulfillmentId: number) {
  return requestClient.post<number>(`/wms/outbounds/from-fulfillment/${fulfillmentId}`);
}

export function startPicking(id: number) {
  return requestClient.post(`/wms/outbounds/${id}/picking/start`);
}

export function confirmPicking(id: number, body: PickingLineDto[]) {
  return requestClient.post(`/wms/outbounds/${id}/picking/confirm`, body);
}

export function packOutbound(id: number) {
  return requestClient.post(`/wms/outbounds/${id}/pack`);
}

export function shipOutbound(id: number, body: { carrierCode?: string; trackingNo?: string }) {
  return requestClient.post(`/wms/outbounds/${id}/ship`, body || {});
}

export function cancelOutbound(id: number) {
  return requestClient.post(`/wms/outbounds/${id}/cancel`);
}

// ---------- 兼容旧 ----------
export const getOutbounds = (params: any) => requestClient.get('/wms/outbounds', { params });

export const createPickTask = (id: number) => requestClient.post(`/wms/outbounds/${id}/pickTask`);

export const getPickTaskDetail = (id: number) => requestClient.get(`/wms/pickTasks/${id}`);

export const confirmPickLine = (id: number, lineId: number, qty: number) => {
  return requestClient.post(`/wms/pickTasks/${id}/lines/${lineId}/confirm`, { qtyActual: qty });
};

export const completePickTask = (id: number) => requestClient.post(`/wms/pickTasks/${id}/complete`);
