import { requestClient } from '#/api/request';

export interface ShipmentRecord {
  id?: number;
  shipmentNo?: string;
  outboundId?: number;
  fulfillmentId?: number;
  sourceType?: string;
  sourceNo?: string;
  carrierCode?: string;
  serviceCode?: string;
  trackingNo?: string;
  labelUrl?: string;
  labelFormat?: string;
  status?: string;
  failReason?: string;
  createdAt?: string;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

export function pageShipments(params: { current?: number; size?: number; status?: string }) {
  return requestClient.get<PageResult<ShipmentRecord>>('/tms/shipments', { params });
}

export function getShipmentDetail(id: number) {
  return requestClient.get<ShipmentRecord>(`/tms/shipments/${id}`);
}

export function createShipmentFromOutbound(outboundId: number) {
  return requestClient.post<number>(`/tms/shipments/from-outbound/${outboundId}`);
}

export function createLabel(shipmentId: number) {
  return requestClient.post(`/tms/shipments/${shipmentId}/label/create`);
}

export function syncToChannel(shipmentId: number) {
  return requestClient.post(`/tms/shipments/${shipmentId}/sync/channel`);
}

export interface CarrierRecord {
  id?: number;
  carrierCode?: string;
  carrierName?: string;
  status?: string;
}

export function getCarriers() {
  return requestClient.get<CarrierRecord[]>('/tms/carriers');
}

// ---------- 兼容旧 ----------
export const getShipments = (params: any) => requestClient.get('/tms/shipments', { params });

export const generateLabel = (id: number) => requestClient.post(`/tms/shipments/${id}/label/create`);

export const ackPlatform = (id: number) => requestClient.post(`/tms/shipments/${id}/sync/channel`);

// ---------- 回传任务 (Channel Shipment Ack) ----------
export interface ChannelAckRecord {
  id?: number;
  channel?: string;
  amazonOrderId?: string;
  packageReferenceId?: string;
  carrierCode?: string;
  carrierName?: string;
  trackingNo?: string;
  shipDateUtc?: string;
  status?: string;
  retryCount?: number;
  nextRetryAt?: string;
  lastAttemptAt?: string;
  responseCode?: number;
  errorCode?: string;
  errorMessage?: string;
  requestPayloadJson?: string;
  responseBody?: string;
  createdAt?: string;
  /** P1.6++ C 多包裹：WMS pack id */
  wmsPackId?: number;
  /** P1.6++ C 本包裹 orderItemId+quantity 快照 JSON */
  orderItemsJson?: string;
  /** P1.6++ D 自愈 */
  selfHealAttempted?: boolean;
  selfHealAction?: string;
  selfHealAt?: string;
  retry404Count?: number;
}

export function pageAcks(params: { current?: number; size?: number; channel?: string; status?: string; orderId?: string; trackingNo?: string }) {
  return requestClient.get<PageResult<ChannelAckRecord>>('/tms/acks', { params });
}

export function getAckDetail(id: number) {
  return requestClient.get<ChannelAckRecord>(`/tms/acks/${id}`);
}

export function retryAck(id: number) {
  return requestClient.post(`/tms/acks/${id}/retry`);
}

export function createAckFromOutbound(outboundId: number) {
  return requestClient.post<number>('/tms/acks/createFromOutbound', { outboundId });
}
