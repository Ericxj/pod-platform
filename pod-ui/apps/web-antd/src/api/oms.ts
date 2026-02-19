import { requestClient } from '#/api/request';

export namespace OmsApi {
  export interface Order {
    id: number;
    orderNo: string;
    status: string;
    createdAt: string;
    // other fields
  }

  export interface OrderSearchParams {
    orderNo?: string;
    status?: string;
    startTime?: string;
    endTime?: string;
    page?: number;
    pageSize?: number;
  }
}

export const getOrderList = (params: OmsApi.OrderSearchParams) => {
  return requestClient.get('/oms/orders', { params });
};

export const createFulfillment = (orderId: number) => {
  return requestClient.post(`/oms/orders/${orderId}/fulfillment`);
};

export const getFulfillmentList = (params: any) => {
    return requestClient.get('/ful/fulfillments', { params });
};

export const releaseFulfillment = (id: number) => {
    return requestClient.post(`/ful/fulfillments/${id}/release`);
};

// P1.1 统一订单
export interface UnifiedOrderRecord {
  id?: number;
  unifiedOrderNo?: string;
  channel?: string;
  shopId?: number;
  externalOrderId?: string;
  orderStatus?: string;
  orderCreatedAt?: string;
  buyerName?: string;
  totalAmount?: number;
  items?: Array<{ id?: number; lineNo?: number; skuId?: number; skuCode?: string; platformSkuCode?: string; quantity?: number; itemStatus?: string }>;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

export function pageUnifiedOrders(params: { current?: number; size?: number; channel?: string; shopId?: string; externalOrderId?: string; orderStatus?: string }) {
  return requestClient.get<PageResult<UnifiedOrderRecord>>('/oms/unified-orders', { params });
}

export function getUnifiedOrder(id: number) {
  return requestClient.get<UnifiedOrderRecord>(`/oms/unified-orders/${id}`);
}

// P1.1 异常队列
export interface OrderHoldRecord {
  id?: number;
  holdType?: string;
  status?: string;
  reasonCode?: string;
  reasonMsg?: string;
  channel?: string;
  shopId?: string;
  externalOrderId?: string;
  externalSku?: string;
  unifiedOrderId?: number;
  unifiedOrderItemId?: number;
  resolveSkuId?: number;
  resolveSkuCode?: string;
  resolvedAt?: string;
  createdAt?: string;
}

export function pageHolds(params: { current?: number; size?: number; type?: string; status?: string; channel?: string; shopId?: string }) {
  return requestClient.get<PageResult<OrderHoldRecord>>('/oms/holds', { params });
}

export function getHold(id: number) {
  return requestClient.get<OrderHoldRecord>(`/oms/holds/${id}`);
}

export function resolveHold(id: number, skuId: number) {
  return requestClient.post(`/oms/holds/${id}/resolve`, { skuId });
}

// P1.2 Fulfillment
export interface FulfillmentRecord {
  id?: number;
  fulfillmentNo?: string;
  unifiedOrderId?: number;
  channel?: string;
  shopId?: number;
  externalOrderId?: string;
  status?: string;
  warehouseId?: number;
  items?: Array<{ id?: number; lineNo?: number; skuId?: number; qty?: number; reservedQty?: number; reserveStatus?: string; status?: string }>;
}

export function pageFulfillments(params: { current?: number; size?: number; status?: string; fulfillmentNo?: string }) {
  return requestClient.get<PageResult<FulfillmentRecord>>('/ful/fulfillments', { params });
}

export function getFulfillment(id: number) {
  return requestClient.get<FulfillmentRecord>(`/ful/fulfillments/${id}`);
}

export function retryReserveFulfillment(id: number) {
  return requestClient.post(`/ful/fulfillments/${id}/reserve/retry`, {});
}

export function cancelFulfillment(id: number) {
  return requestClient.post(`/ful/fulfillments/${id}/cancel`, {});
}
