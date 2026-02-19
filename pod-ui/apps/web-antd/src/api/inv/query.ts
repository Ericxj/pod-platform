import { requestClient } from '#/api/request';

export interface BalanceRecord {
  id?: number;
  warehouseId?: number;
  locationId?: number;
  skuId?: number;
  onHandQty?: number;
  allocatedQty?: number;
  availableQty?: number;
  updatedAt?: string;
  [key: string]: unknown;
}

export interface ReservationRecord {
  id?: number;
  bizType?: string;
  bizNo?: string;
  lineNo?: number;
  warehouseId?: number;
  skuId?: number;
  qty?: number;
  consumedQty?: number;
  status?: string;
  updatedAt?: string;
  [key: string]: unknown;
}

export interface LedgerRecord {
  id?: number;
  txnType?: string;
  bizType?: string;
  bizNo?: string;
  warehouseId?: number;
  skuId?: number;
  deltaQty?: number;
  beforeOnHand?: number;
  afterOnHand?: number;
  beforeAllocated?: number;
  afterAllocated?: number;
  createdAt?: string;
  [key: string]: unknown;
}

export interface InvPageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

const Api = {
  Balances: '/inv/balances',
  Reservations: '/inv/reservations',
  Ledgers: '/inv/ledgers',
};

export function pageBalances(params: {
  current?: number;
  size?: number;
  warehouseId?: number;
  skuId?: number;
  keyword?: string;
}) {
  return requestClient.get<InvPageResult<BalanceRecord>>(Api.Balances, { params });
}

export function pageReservations(params: {
  current?: number;
  size?: number;
  bizType?: string;
  bizNo?: string;
  skuId?: number;
  status?: string;
}) {
  return requestClient.get<InvPageResult<ReservationRecord>>(Api.Reservations, { params });
}

export function pageLedgers(params: {
  current?: number;
  size?: number;
  bizType?: string;
  bizNo?: string;
  action?: string;
  skuId?: number;
}) {
  return requestClient.get<InvPageResult<LedgerRecord>>(Api.Ledgers, { params });
}
