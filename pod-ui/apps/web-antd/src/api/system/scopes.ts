import { requestClient } from '#/api/request';

export interface DataScopeQueryDto {
  scopeIds: number[];
}

export interface DataScopeUpdateDto {
  subjectType: string;
  subjectId: number;
  scopeType: string;
  scopeIds: number[];
}

const BASE = '/iam/dataScopes';

export function getDataScopes(params: { subjectType: string; subjectId: number; scopeType: string }) {
  return requestClient.get<DataScopeQueryDto>(BASE, { params });
}

export function putDataScopes(body: DataScopeUpdateDto) {
  return requestClient.put(BASE, body);
}
