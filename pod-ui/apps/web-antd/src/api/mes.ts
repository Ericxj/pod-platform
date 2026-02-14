import { requestClient } from '#/api/request';

export const getWorkOrders = (params: any) => {
  return requestClient.get('/mes/workOrders', { params });
};

export const releaseWorkOrder = (id: number) => {
    return requestClient.post(`/mes/workOrders/${id}/release`);
};

export const startOperation = (id: number, opId: number) => {
    return requestClient.post(`/mes/workOrders/${id}/operations/${opId}/start`);
};

export const finishOperation = (id: number, opId: number) => {
    return requestClient.post(`/mes/workOrders/${id}/operations/${opId}/finish`);
};
