import { requestClient } from '#/api/request';

export const getOutbounds = (params: any) => {
  return requestClient.get('/wms/outbounds', { params });
};

export const getOutboundDetail = (id: number) => {
    return requestClient.get(`/wms/outbounds/${id}`);
};

export const createPickTask = (id: number) => {
    return requestClient.post(`/wms/outbounds/${id}/pickTask`);
};

export const packOutbound = (id: number) => {
    return requestClient.post(`/wms/outbounds/${id}/pack`);
};

export const shipOutbound = (id: number) => {
    return requestClient.post(`/wms/outbounds/${id}/ship`);
};

export const getPickTaskDetail = (id: number) => {
    return requestClient.get(`/wms/pickTasks/${id}`);
};

export const confirmPickLine = (id: number, lineId: number, qty: number) => {
    return requestClient.post(`/wms/pickTasks/${id}/lines/${lineId}/confirm`, { qtyActual: qty });
};

export const completePickTask = (id: number) => {
    return requestClient.post(`/wms/pickTasks/${id}/complete`);
};
