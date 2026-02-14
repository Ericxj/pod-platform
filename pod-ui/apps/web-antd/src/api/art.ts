import { requestClient } from '#/api/request';

export const getArtJobs = (params: any) => {
  return requestClient.get('/art/artJobs', { params });
};

export const getRenderTasks = (params: any) => {
    return requestClient.get('/art/renderTasks', { params });
};

export const retryRenderTask = (id: number) => {
    return requestClient.post(`/art/renderTasks/${id}/retry`);
};
