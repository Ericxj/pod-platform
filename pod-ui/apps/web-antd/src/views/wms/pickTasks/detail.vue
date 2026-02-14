<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { Page } from '#/components/Page';
import { getPickTaskDetail, confirmPickLine, completePickTask } from '#/api/wms';
import { message, InputNumber } from 'ant-design-vue';

const route = useRoute();
const pickTask = ref<any>(null);
const loading = ref(false);

async function loadData() {
    loading.value = true;
    try {
        const id = Number(route.params.id);
        pickTask.value = await getPickTaskDetail(id);
    } finally {
        loading.value = false;
    }
}

async function handleConfirmLine(line: any) {
    try {
        await confirmPickLine(pickTask.value.id, line.id, line.qtyActual);
        message.success('已确认');
        // Refresh local state if needed or reload
    } catch(e) {}
}

async function handleComplete() {
    try {
        await completePickTask(pickTask.value.id);
        message.success('拣货任务已完成');
        loadData();
    } catch(e) {}
}

onMounted(() => {
    loadData();
});
</script>

<template>
  <Page title="拣货任务详情">
    <div v-if="pickTask" class="p-4">
        <div class="mb-4 flex justify-between">
            <h2 class="text-lg font-bold">任务号: {{ pickTask.taskNo }}</h2>
            <a-button type="primary" @click="handleComplete">完成拣货</a-button>
        </div>
        
        <div class="bg-white p-4 rounded shadow">
            <a-table :dataSource="pickTask.pickLines" :pagination="false" rowKey="id">
                <a-table-column title="SKU" dataIndex="sku" />
                <a-table-column title="应拣数量" dataIndex="qtyRequired" />
                <a-table-column title="实拣数量">
                    <template #default="{ record }">
                        <InputNumber v-model:value="record.qtyActual" :min="0" />
                    </template>
                </a-table-column>
                <a-table-column title="操作">
                    <template #default="{ record }">
                        <a-button type="link" @click="handleConfirmLine(record)">确认</a-button>
                    </template>
                </a-table-column>
            </a-table>
        </div>
    </div>
  </Page>
</template>
