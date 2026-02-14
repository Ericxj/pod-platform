import { requestClient } from '#/api/request';

export interface UserDto {
  id?: number;
  username: string;
  realName: string;
  email?: string;
  phone?: string;
  status?: string;
  roleIds?: number[];
  createdAt?: string;
  password?: string;
}

export interface UserPageQuery {
  pageNum: number;
  pageSize: number;
  username?: string;
  realName?: string;
  phone?: string;
  status?: string;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

enum Api {
  User = '/iam/users',
}

export function getUserPage(params: UserPageQuery) {
  return requestClient.get<PageResult<UserDto>>(Api.User, { params });
}

export function getUser(id: number) {
  return requestClient.get<UserDto>(`${Api.User}/${id}`);
}

export function createUser(data: UserDto) {
  return requestClient.post(Api.User, data);
}

export function updateUser(data: UserDto) {
  return requestClient.put(Api.User, data);
}

export function deleteUser(id: number) {
  return requestClient.delete(`${Api.User}/${id}`);
}

export function resetPassword(id: number, password: string) {
  return requestClient.put(`${Api.User}/${id}/password`, { password });
}

export interface UserFactoryScopesDto {
  factoryIds: number[];
}

export function getUserFactoryScopes(userId: number) {
  return requestClient.get<UserFactoryScopesDto>(`${Api.User}/${userId}/factoryScopes`);
}

export function putUserFactoryScopes(userId: number, body: UserFactoryScopesDto) {
  return requestClient.put(`${Api.User}/${userId}/factoryScopes`, body);
}

export function getUserRoles(userId: number) {
  return requestClient.get<number[]>(`${Api.User}/${userId}/roles`);
}

export function putUserRoles(userId: number, roleIds: number[]) {
  return requestClient.put(`${Api.User}/${userId}/roles`, { roleIds });
}

export function getMyFactories() {
  return requestClient.get<number[]>('/iam/factories/my');
}
