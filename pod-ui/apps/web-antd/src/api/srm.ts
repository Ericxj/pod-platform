import { requestClient } from '#/api/request';

export interface PageResult<T> {
  records: T[];
  total: number;
  current?: number;
  size?: number;
}

export interface SupplierRecord {
  id?: number;
  supplierCode?: string;
  supplierName?: string;
  contactName?: string;
  phone?: string;
  email?: string;
  address?: string;
  status?: string;
  remark?: string;
}

export interface PurchaseOrderRecord {
  id?: number;
  poNo?: string;
  supplierId?: number;
  currency?: string;
  status?: string;
  totalQty?: number;
  totalAmount?: number;
  expectedArriveDate?: string;
  lines?: PurchaseOrderLineRecord[];
}

export interface PurchaseOrderLineRecord {
  id?: number;
  poId?: number;
  lineNo?: number;
  skuId?: number;
  skuCode?: string;
  skuName?: string;
  qtyOrdered?: number;
  unitPrice?: number;
  qtyReceived?: number;
}

const SRM = '/srm';

export function getSupplierPage(params: { current?: number; size?: number; keyword?: string; status?: string }) {
  return requestClient.get<PageResult<SupplierRecord>>(SRM + '/suppliers', { params });
}

export function getSupplierList(status?: string) {
  return requestClient.get<SupplierRecord[]>(SRM + '/suppliers/list', { params: status ? { status } : {} });
}

export function getSupplier(id: number) {
  return requestClient.get<SupplierRecord>(`${SRM}/suppliers/${id}`);
}

export function createSupplier(data: Partial<SupplierRecord>) {
  return requestClient.post<SupplierRecord>(SRM + '/suppliers', data);
}

export function updateSupplier(id: number, data: Partial<SupplierRecord>) {
  return requestClient.put(`${SRM}/suppliers/${id}`, data);
}

export function enableSupplier(id: number) {
  return requestClient.post(`${SRM}/suppliers/${id}/enable`);
}

export function disableSupplier(id: number) {
  return requestClient.post(`${SRM}/suppliers/${id}/disable`);
}

export function getPurchaseOrderPage(params: { current?: number; size?: number; supplierId?: number; status?: string }) {
  return requestClient.get<PageResult<PurchaseOrderRecord>>(SRM + '/purchase-orders', { params });
}

export function getPurchaseOrder(id: number) {
  return requestClient.get<PurchaseOrderRecord>(`${SRM}/purchase-orders/${id}`);
}

export function createPurchaseOrder(data: { supplierId: number; currency?: string; expectedArriveDate?: string }) {
  return requestClient.post<PurchaseOrderRecord>(SRM + '/purchase-orders', data);
}

export function addPurchaseOrderLine(poId: number, data: { skuId: number; qtyOrdered: number; unitPrice: number }) {
  return requestClient.post(`${SRM}/purchase-orders/${poId}/lines`, data);
}

export function updatePurchaseOrderLine(poId: number, lineId: number, data: { qtyOrdered?: number; unitPrice?: number }) {
  return requestClient.put(`${SRM}/purchase-orders/${poId}/lines/${lineId}`, data);
}

export function submitPurchaseOrder(id: number) {
  return requestClient.post(`${SRM}/purchase-orders/${id}/submit`);
}

export function approvePurchaseOrder(id: number) {
  return requestClient.post(`${SRM}/purchase-orders/${id}/approve`);
}

export function cancelPurchaseOrder(id: number) {
  return requestClient.post(`${SRM}/purchase-orders/${id}/cancel`);
}

export function closePurchaseOrder(id: number) {
  return requestClient.post(`${SRM}/purchase-orders/${id}/close`);
}
