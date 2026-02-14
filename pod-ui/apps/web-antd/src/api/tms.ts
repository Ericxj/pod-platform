import { requestClient } from '#/api/request';

export const getShipments = (params: any) => {
  return requestClient.get('/tms/shipments', { params });
};

export const generateLabel = (id: number) => {
    return requestClient.post(`/tms/shipments/${id}/label`);
};

export const ackPlatform = (id: number) => {
    return requestClient.post(`/tms/shipments/${id}/ack`);
};
