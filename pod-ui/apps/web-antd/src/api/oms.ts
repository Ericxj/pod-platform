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
