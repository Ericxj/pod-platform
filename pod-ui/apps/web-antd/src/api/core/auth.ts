import { baseRequestClient, requestClient } from '#/api/request';

export namespace AuthApi {
  /** 登录接口参数 */
  export interface LoginParams {
    password?: string;
    username?: string;
    factoryId?: number;
  }

  /** 登录接口返回值 */
  export interface LoginResult {
    token: string;
    me: any;
    dataScopes: {
      factoryIds: number[];
      defaultFactoryId: number;
    };
    factoryIds?: number[]; // Compatible
    currentFactoryId?: number; // Compatible
  }

  export interface RefreshTokenResult {
    data: string;
    status: number;
  }
}

/**
 * 登录
 */
export async function loginApi(data: AuthApi.LoginParams) {
  return requestClient.post<AuthApi.LoginResult>('/iam/auth/login', data);
}

/**
 * 刷新accessToken
 */
export async function refreshTokenApi() {
  return baseRequestClient.post<AuthApi.RefreshTokenResult>('/iam/auth/refresh', {
    withCredentials: true,
  });
}

/**
 * 退出登录
 */
export async function logoutApi() {
  return baseRequestClient.post('/iam/auth/logout', {
    withCredentials: true,
  });
}

/**
 * 获取用户权限码
 * (Deprecated: merged into getMenusApi)
 */
export async function getAccessCodesApi() {
  return requestClient.get<string[]>('/iam/auth/codes'); 
}
