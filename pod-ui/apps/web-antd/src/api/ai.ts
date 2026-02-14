import { requestClient } from '#/api/request';

/**
 * 提交 AI 诊断请求
 * @param params 诊断参数 (type, businessKey)
 * @param requestId 幂等性 ID
 */
export function submitDiagnoseApi(params: { type: string; businessKey: string }, requestId: string) {
  return requestClient.post<{ id: number; status: string }>({
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
  return requestClient.get<{
    id?: number;
    status?: string;
    resultJson?: string;
    diagnosisType?: string;
    businessKey?: string;
  }>({
    url: `/iam/ai/diagnose/${id}`,
  });
}

/** 诊断表单参数（与页面 formState 一致） */
export interface DiagnoseFormState {
  bizType: string;
  bizNo: string;
  question?: string;
}

/** 提交诊断：供 diagnose 页面使用，返回带 taskNo 的结果 */
export async function submitDiagnose(params: DiagnoseFormState) {
  const requestId = crypto.randomUUID?.() ?? `${Date.now()}-${Math.random().toString(36).slice(2)}`;
  const res = await submitDiagnoseApi(
    { type: params.bizType, businessKey: params.bizNo },
    requestId,
  );
  const data = (res as any)?.data ?? res;
  const id = data?.id;
  if (id == null) throw new Error('诊断任务创建失败：未返回任务 ID');
  return { taskNo: String(id), status: data?.status ?? 'PENDING' };
}

/** 查询诊断任务状态：供 diagnose 页面轮询使用 */
export async function getTaskStatus(taskNo: string) {
  const res = await getDiagnoseResultApi(taskNo);
  const data = (res as any)?.data ?? res;
  return {
    status: data?.status ?? 'PENDING',
    resultJson: data?.resultJson,
    diagnosisType: data?.diagnosisType,
    businessKey: data?.businessKey,
  };
}
