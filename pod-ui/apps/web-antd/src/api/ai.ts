import { requestClient } from '#/api/request';

/**
 * 提交 AI 诊断请求
 * @param params 诊断参数 (type, businessKey)
 * @param requestId 幂等性 ID
 */
export function submitDiagnoseApi(params: { type: string; businessKey: string }, requestId: string) {
  return requestClient.post({
    url: '/iam/ai/diagnose',
    data: params,
    headers: {
      'X-Request-Id': requestId,
    },
  });
}

/**
 * 获取诊断结果
 * @param id 诊断记录 ID
 */
export function getDiagnoseResultApi(id: string) {
  return requestClient.get({
    url: `/iam/ai/diagnose/${id}`,
  });
}
