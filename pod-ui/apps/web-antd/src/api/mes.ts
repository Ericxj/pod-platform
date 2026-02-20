import { requestClient } from '#/api/request';

// ---------- P1.4 工单 API (work-orders) ----------
export interface WorkOrderRecord {
  id?: number;
  workOrderNo?: string;
  fulfillmentId?: number;
  sourceType?: string;
  sourceNo?: string;
  status?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface WorkOrderLineRecord {
  id?: number;
  workOrderId?: number;
  lineNo?: number;
  skuId?: number;
  qty?: number;
  producedQty?: number;
  scrapQty?: number;
  status?: string;
}

export interface WorkOrderOpRecord {
  id?: number;
  workOrderId?: number;
  stepNo?: number;
  opCode?: string;
  status?: string;
  startAt?: string;
  endAt?: string;
}

export interface MesReportRecord {
  id?: number;
  workOrderId?: number;
  workOrderLineId?: number;
  opCode?: string;
  goodQty?: number;
  scrapQty?: number;
  createdAt?: string;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

export function pageWorkOrders(params: { current?: number; size?: number; status?: string }) {
  return requestClient.get<PageResult<WorkOrderRecord>>('/mes/work-orders', { params });
}

export function getWorkOrder(id: number) {
  return requestClient.get<WorkOrderRecord>(`/mes/work-orders/${id}`);
}

export function getWorkOrderLines(id: number) {
  return requestClient.get<WorkOrderLineRecord[]>(`/mes/work-orders/${id}/lines`);
}

export function getWorkOrderOps(id: number) {
  return requestClient.get<WorkOrderOpRecord[]>(`/mes/work-orders/${id}/ops`);
}

export function getWorkOrderReports(id: number) {
  return requestClient.get<MesReportRecord[]>(`/mes/work-orders/${id}/reports`);
}

export function releaseWorkOrder(id: number) {
  return requestClient.post(`/mes/work-orders/${id}/release`);
}

export function startWorkOrder(id: number) {
  return requestClient.post(`/mes/work-orders/${id}/start`);
}

export function reportWorkOrder(id: number, body: { lineId: number; goodQty?: number; scrapQty?: number; opCode?: string; workstationId?: number }) {
  return requestClient.post(`/mes/work-orders/${id}/report`, body);
}

export function cancelWorkOrder(id: number) {
  return requestClient.post(`/mes/work-orders/${id}/cancel`);
}

export function createWorkOrderFromFulfillment(fulfillmentId: number) {
  return requestClient.post<number>(`/mes/work-orders/from-fulfillment/${fulfillmentId}`);
}

// ---------- 兼容旧 (workOrders) ----------
export const getWorkOrders = (params: any) => {
  return requestClient.get('/mes/work-orders', { params });
};

export function startOperation(id: number, opId: number) {
  return requestClient.post(`/mes/workOrders/${id}/operations/${opId}/start`);
}

export function finishOperation(id: number, opId: number) {
  return requestClient.post(`/mes/workOrders/${id}/operations/${opId}/finish`);
}
