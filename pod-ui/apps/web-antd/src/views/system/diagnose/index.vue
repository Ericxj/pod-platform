<script lang="ts" setup>
import { ref } from 'vue';
import { Page } from '@vben/common-ui';
import { Card, Button, Steps, Alert, Descriptions, message } from 'ant-design-vue';
import { submitDiagnoseApi, getDiagnoseResultApi } from '#/api/ai';
import { useUserStore } from '@vben/stores';

const userStore = useUserStore();
const loading = ref(false);
const currentStep = ref(0);
const diagnosisResult = ref<any>(null);
  const errorMsg = ref('');

  function generateUUID() {
    if (typeof crypto !== 'undefined' && crypto.randomUUID) {
      return crypto.randomUUID();
    }
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
      var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
      return v.toString(16);
    });
  }

  async function handleDiagnose() {
    loading.value = true;
    currentStep.value = 1;
    errorMsg.value = '';
    diagnosisResult.value = null;

    try {
      const requestId = generateUUID();
      const { id } = await submitDiagnoseApi({
        type: 'PERMISSION_CHECK',
        businessKey: String(userStore.userInfo?.userId || 'unknown'),
      }, requestId);

      // Start Polling
      pollResult(id);
    } catch (error: any) {
      loading.value = false;
      currentStep.value = 0;
      const msg = error.message || 'Failed to submit diagnosis';
      message.error(msg);
      errorMsg.value = msg;
    }
  }

  async function pollResult(id: string) {
    if (!loading.value) return;

    try {
      const result = await getDiagnoseResultApi(id);
      if (result.status === 'COMPLETED') {
        loading.value = false;
        currentStep.value = 2;
        try {
          diagnosisResult.value = JSON.parse(result.resultJson || '{}');
        } catch (e) {
          diagnosisResult.value = { suggestions: result.resultJson };
        }
      } else if (result.status === 'FAILED') {
        loading.value = false;
        currentStep.value = 2;
        errorMsg.value = result.resultJson || 'Diagnosis failed';
      } else {
        // Continue polling
        setTimeout(() => pollResult(id), 1000);
      }
    } catch (e) {
      loading.value = false;
      errorMsg.value = 'Polling failed';
    }
  }
</script>

<template>
  <Page title="AI 智能诊断">
    <div class="p-4">
      <Card title="系统权限健康诊断">
        <div class="mb-8">
            <Steps :current="currentStep" :items="[
                { title: '准备', description: '点击开始' },
                { title: '诊断中', description: 'AI 正在分析...' },
                { title: '完成', description: '查看报告' }
            ]" />
        </div>

        <div class="text-center mb-8" v-if="currentStep !== 2">
             <Button type="primary" size="large" :loading="loading" @click="handleDiagnose">
                {{ loading ? 'AI 正在思考...' : '开始一键诊断' }}
             </Button>
        </div>

        <div v-if="currentStep === 2">
             <Alert v-if="errorMsg" type="error" :message="errorMsg" show-icon class="mb-4" />
             <Alert v-else type="success" message="诊断完成，系统运行正常" show-icon class="mb-4" />

             <Descriptions title="诊断报告" bordered v-if="diagnosisResult">
                <Descriptions.Item label="AI 模型">{{ diagnosisResult.ai_model }}</Descriptions.Item>
                <Descriptions.Item label="健康评分">{{ diagnosisResult.score }}</Descriptions.Item>
                <Descriptions.Item label="建议" :span="3">{{ diagnosisResult.suggestions }}</Descriptions.Item>
             </Descriptions>
             
             <div class="text-center mt-4">
                <Button @click="currentStep = 0">重新诊断</Button>
             </div>
        </div>
      </Card>
    </div>
  </Page>
</template>
