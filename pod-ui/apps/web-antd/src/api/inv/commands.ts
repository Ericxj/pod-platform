import { requestClient } from '#/api/request';

export interface ReserveParams {
  bizType: string;
  bizNo: string;
  warehouseId: number;
  locationId?: number;
  skuId: number;
  qty: number;
  lineNo?: number;
  remark?: string;
  idempotencyKey?: string;
}

export interface ReleaseParams {
  reservationId: number;
  qty?: number;
  remark?: string;
  idempotencyKey?: string;
}

export interface ConsumeReservedParams {
  reservationId: number;
  qty: number;
  remark?: string;
  idempotencyKey?: string;
}

export interface AdjustParams {
  balanceId: number;
  delta: number;
  refNo?: string;
  bizType?: string;
  bizNo?: string;
  remark?: string;
  idempotencyKey?: string;
}

export function reserve(params: ReserveParams) {
  return requestClient.post('/inv/reservations', params);
}

export function release(params: ReleaseParams) {
  return requestClient.post('/inv/reservations/release', params);
}

export function consumeReserved(params: ConsumeReservedParams) {
  return requestClient.post('/inv/consumeReserved', params);
}

export function adjust(params: AdjustParams) {
  const body = {
    ...params,
    refNo: params.refNo ?? `ADJ-${Date.now()}`,
  };
  return requestClient.post('/inv/adjust', body);
}
