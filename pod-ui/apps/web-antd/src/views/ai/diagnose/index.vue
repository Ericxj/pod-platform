<script lang="ts" setup>
import { reactive, ref, onUnmounted } from 'vue';
import { Page } from '#/components/Page';
import { submitDiagnose, getTaskStatus } from '#/api/ai';
import { message, Form, Input, Button, Card, Descriptions, Tag } from 'ant-design-vue';

const formState = reactive({
    bizType: '',
    bizNo: '',
    question: '',
});

const loading = ref(false);
const result = ref<any>(null);
const taskId = ref<string | null>(null);
let pollTimer: any = null;

async function handleSubmit() {
    loading.value = true;
    result.value = null;
    try {
        const res = await submitDiagnose(formState);
        taskId.value = res.taskNo; // Assuming backend returns taskNo
        startPolling(res.taskNo);
    } catch (e) {
        loading.value = false;
    }
}

function startPolling(taskNo: string) {
    if (pollTimer) clearInterval(pollTimer);
    
    pollTimer = setInterval(async () => {
        try {
            const res = await getTaskStatus(taskNo);
            if (res.status === 'COMPLETED' || res.status === 'FAILED') {
                clearInterval(pollTimer);
                result.value = res;
                loading.value = false;
            }
        } catch (e) {
            clearInterval(pollTimer);
            loading.value = false;
        }
    }, 2000);
}

onUnmounted(() => {
    if (pollTimer) clearInterval(pollTimer);
});
</script>

<template>
  <Page title="AI 智能诊断">
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <Card title="诊断请求">
            <Form :model="formState" @finish="handleSubmit" layout="vertical">
                <Form.Item label="业务类型" name="bizType" :rules="[{ required: true }]">
                    <Input v-model:value="formState.bizType" placeholder="例如: ORDER, OUTBOUND" />
                </Form.Item>
                <Form.Item label="业务编号" name="bizNo" :rules="[{ required: true }]">
                    <Input v-model:value="formState.bizNo" placeholder="例如: PO-2023001" />
                </Form.Item>
                <Form.Item label="问题描述" name="question" :rules="[{ required: true }]">
                    <Input.TextArea v-model:value="formState.question" :rows="4" placeholder="请描述您遇到的问题..." />
                </Form.Item>
                <Form.Item>
                    <Button type="primary" html-type="submit" :loading="loading">开始诊断</Button>
                </Form.Item>
            </Form>
        </Card>

        <Card title="诊断结果" v-if="result || loading">
            <div v-if="loading && !result" class="flex justify-center items-center h-40">
                <span>AI 正在分析中...</span>
            </div>
            <div v-else-if="result">
                <Descriptions bordered column="1">
                    <Descriptions.Item label="状态">
                        <Tag :color="result.status === 'COMPLETED' ? 'success' : 'error'">{{ result.status }}</Tag>
                    </Descriptions.Item>
                    <Descriptions.Item label="输出结果">
                        <pre class="whitespace-pre-wrap">{{ result.output }}</pre>
                    </Descriptions.Item>
                    <Descriptions.Item label="错误信息" v-if="result.error">
                        {{ result.error }}
                    </Descriptions.Item>
                </Descriptions>
            </div>
        </Card>
    </div>
  </Page>
</template>
