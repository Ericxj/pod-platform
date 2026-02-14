/**
 * 该文件可自行根据业务逻辑进行调整
 */
import type { RequestClientOptions } from '@vben/request';

import { useAppConfig } from '@vben/hooks';
import { preferences } from '@vben/preferences';
import {
  authenticateResponseInterceptor,
  defaultResponseInterceptor,
  errorMessageResponseInterceptor,
  RequestClient,
} from '@vben/request';
import { useAccessStore, useUserStore } from '@vben/stores';

import { message, Modal } from 'ant-design-vue';

import { useAuthStore, useFactoryStore } from '#/store';

import { refreshTokenApi } from './core';

const { apiURL } = useAppConfig(import.meta.env, import.meta.env.PROD);

function createRequestClient(baseURL: string, options?: RequestClientOptions) {
  const client = new RequestClient({
    ...options,
    baseURL,
  });

  /**
   * 重新认证逻辑
   */
  async function doReAuthenticate() {
    console.warn('Access token or refresh token is invalid or expired. ');
    const accessStore = useAccessStore();
    const authStore = useAuthStore();
    accessStore.setAccessToken(null);
    if (
      preferences.app.loginExpiredMode === 'modal' &&
      accessStore.isAccessChecked
    ) {
      accessStore.setLoginExpired(true);
    } else {
      await authStore.logout();
    }
  }

  /**
   * 刷新token逻辑
   */
  async function doRefreshToken() {
    const accessStore = useAccessStore();
    const resp = await refreshTokenApi();
    const newToken = resp.data;
    accessStore.setAccessToken(newToken);
    return newToken;
  }

  function formatToken(token: null | string) {
    return token ? `Bearer ${token}` : null;
  }

  function generateUUID() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
      var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
      return v.toString(16);
    });
  }

  // 请求头处理
  client.addRequestInterceptor({
    fulfilled: async (config) => {
      const accessStore = useAccessStore();
      // const userStore = useUserStore();
      const factoryStore = useFactoryStore();

      config.headers.Authorization = formatToken(accessStore.accessToken);
      config.headers['Accept-Language'] = preferences.app.locale;
      
      // Add Custom Headers
      if (!config.headers['X-Request-Id']) {
        config.headers['X-Request-Id'] = generateUUID();
      }
      
      // Use FactoryStore for current factory
      if (factoryStore.currentFactoryId) {
          config.headers['X-Factory-Id'] = factoryStore.currentFactoryId;
      }
      
      return config;
    },
  });

  // Trace ID Handling & Global Error & 403 Factory Scope
  client.addResponseInterceptor({
      fulfilled: (response) => {
          // Extract Trace ID
          const traceId = response.headers?.['trace_id'] || response.headers?.['x-request-id'];
          if (traceId) {
             // Optional: Store traceId in store or log
          }
          return response;
      },
      rejected: (error) => {
          if (error?.response?.status === 403) {
             const msg = error.response.data?.msg || error.message;
             if (msg && (msg.includes('Factory Scope') || msg.includes('工厂'))) {
                 Modal.warning({
                     title: '工厂权限校验失败',
                     content: '当前操作所属工厂与选中工厂不一致，请切换工厂后重试。',
                     okText: '去切换',
                     onOk: () => {
                         // Focus on factory selector or open a modal to select factory
                     }
                 });
             }
          }
          // 409 Conflict - Idempotency
          if (error?.response?.status === 409) {
              const msg = error.response.data?.msg || '操作太频繁，请稍后重试';
              message.warning(msg);
              // Optionally return a specific error object if needed, but rejecting is standard
          }
          return Promise.reject(error);
      }
  });

  // 认证处理
  client.addResponseInterceptor(
    authenticateResponseInterceptor({
      client,
      doReAuthenticate,
      doRefreshToken,
      enableRefreshToken: true,
      formatToken,
    }),
  );

  // 错误处理
  client.addResponseInterceptor(
    errorMessageResponseInterceptor({
      msg: 'message',
    }),
  );

  // 兼容 code=200 和 code=0 (Data Unwrapping - MUST BE LAST)
  client.addResponseInterceptor({
    fulfilled: (response) => {
      const { data: rawData } = response;
      // 如果 rawData 是 Blob 或 ArrayBuffer (如下载文件)，直接返回
      if (rawData instanceof Blob || rawData instanceof ArrayBuffer) {
        return rawData;
      }
      
      const res = rawData as any;
      // 兼容 code=200 或 code=0
      if (res.code === 200 || res.code === 0) {
        return res.data;
      }
      
      // 业务失败，抛出错误
      throw new Error(res.msg || 'Request failed');
    },
  });

  return client;
}

export const requestClient = createRequestClient(apiURL);

export const baseRequestClient = new RequestClient({
  baseURL: apiURL,
});
