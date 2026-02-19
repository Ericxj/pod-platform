import { requestClient } from '#/api/request';

// ---------- P1.3 生产图任务 / 生产文件 ----------
export interface ArtJobRecord {
  id?: number;
  artJobNo?: string;
  fulfillmentId?: number;
  fulfillmentLineId?: number;
  status?: string;
  retryCount?: number;
  lastErrorCode?: string;
  lastErrorMsg?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProductionFileRecord {
  id?: number;
  artJobId?: number;
  fileNo?: string;
  fileType?: string;
  format?: string;
  fileUrl?: string;
  fileHash?: string;
  widthPx?: number;
  heightPx?: number;
  dpi?: number;
  status?: string;
  createdAt?: string;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

export function pageArtJobs(params: { current?: number; size?: number; status?: string }) {
  return requestClient.get<PageResult<ArtJobRecord>>('/art/jobs', { params });
}

export function getArtJob(id: number) {
  return requestClient.get<ArtJobRecord>(`/art/jobs/${id}`);
}

export function retryArtJob(id: number) {
  return requestClient.post(`/art/jobs/${id}/retry`);
}

export function createArtJobsForFulfillment(fulfillmentId: number) {
  return requestClient.post<number[]>(`/art/jobs/from-fulfillment/${fulfillmentId}`);
}

export function pageProductionFiles(params: { current?: number; size?: number; jobId?: number }) {
  return requestClient.get<PageResult<ProductionFileRecord>>('/art/files', { params });
}

export function getProductionFile(id: number) {
  return requestClient.get<ProductionFileRecord>(`/art/files/${id}`);
}

// ---------- 兼容旧 ----------
export const getArtJobs = (params: any) => {
  return requestClient.get('/art/artJobs/page', { params });
};

export const getRenderTasks = (params: any) => {
  return requestClient.get('/art/renderTasks', { params });
};

export const retryRenderTask = (id: number) => {
  return requestClient.post(`/art/renderTasks/${id}/retry`);
};
