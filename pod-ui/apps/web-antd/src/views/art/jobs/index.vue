<script lang="ts" setup>
import { Page } from '@vben/common-ui';
import { pageArtJobs, getArtJob, retryArtJob, createArtJobsForFulfillment } from '#/api/art';
import type { ArtJobRecord } from '#/api/art';
import { message, Table, Button, Input, Modal, Descriptions, Tag } from 'ant-design-vue';
import { ref, onMounted } from 'vue';
import { usePermission } from '#/composables/usePermission';

const hasPerm = usePermission().hasPermission;
const list = ref<ArtJobRecord[]>([]);
const total = ref(0);
const loading = ref(false);
const current = ref(1);
const size = ref(10);
const status = ref('');

const detailVisible = ref(false);
const detailRecord = ref<ArtJobRecord | null>(null);
const detailLoading = ref(false);

const createFulfillmentId = ref<number | string>('');
const createSubmitting = ref(false);

async function load() {
  loading.value = true;
  try {
    const res = await pageArtJobs({
      current: current.value,
      size: size.value,
      status: status.value || undefined,
    });
    const d = res as any;
    list.value = d?.records ?? [];
    total.value = d?.total ?? 0;
  } catch (e: any) {
    message.error(e?.message || '加载失败');
  } finally {
    loading.value = false;
  }
}

async function openDetail(record: ArtJobRecord) {
  if (!record.id) return;
  detailVisible.value = true;
  detailRecord.value = null;
  detailLoading.value = true;
  try {
    detailRecord.value = (await getArtJob(record.id)) as ArtJobRecord;
  } catch (e: any) {
    message.error(e?.message || '加载详情失败');
  } finally {
    detailLoading.value = false;
  }
}

async function doRetry(record: ArtJobRecord) {
  if (!record.id) return;
  try {
    await retryArtJob(record.id);
    message.success('已提交重试');
    load();
    if (detailRecord.value?.id === record.id) openDetail(record);
  } catch (e: any) {
    message.error(e?.message || '重试失败');
  }
}

async function doCreateForFulfillment() {
  const id = typeof createFulfillmentId.value === 'string' ? Number(createFulfillmentId.value) : createFulfillmentId.value;
  if (!id || Number.isNaN(id)) {
    message.warning('请输入履约单ID');
    return;
  }
  createSubmitting.value = true;
  try {
    const ids = await createArtJobsForFulfillment(id);
    message.success(`已创建 ${Array.isArray(ids) ? ids.length : 0} 个任务`);
    createFulfillmentId.value = '';
    load();
  } catch (e: any) {
    message.error(e?.message || '创建失败');
  } finally {
    createSubmitting.value = false;
  }
}

function statusTag(s: string) {
  if (s === 'PENDING') return { color: 'blue', text: 'PENDING' };
  if (s === 'GENERATING') return { color: 'cyan', text: 'GENERATING' };
  if (s === 'READY') return { color: 'green', text: 'READY' };
  if (s === 'FAILED') return { color: 'red', text: 'FAILED' };
  return { color: 'default', text: s || '-' };
}

onMounted(load);
</script>

<template>
  <Page title="生产图任务">
    <div class="mb-4 flex gap-2 flex-wrap items-center">
      <Input v-model:value="status" placeholder="状态" allow-clear style="width: 120px" />
      <Button type="primary" @click="load">查询</Button>
      <span class="ml-4">按履约单创建：</span>
      <Input v-model:value="createFulfillmentId" placeholder="履约单ID" style="width: 120px" />
      <Button v-if="hasPerm('art:job:create')" :loading="createSubmitting" @click="doCreateForFulfillment">创建任务</Button>
    </div>
    <Table
      :data-source="list"
      :loading="loading"
      row-key="id"
      :pagination="{ current, pageSize: size, total, showSizeChanger: true, showTotal: (t: number) => `共 ${t} 条` }"
      @change="(p: any) => { current = p.current; size = p.pageSize; load(); }"
    >
      <Table.Column title="ID" data-index="id" width="80" />
      <Table.Column title="任务号" data-index="artJobNo" width="200" />
      <Table.Column title="履约单ID" data-index="fulfillmentId" width="100" />
      <Table.Column title="履约行ID" data-index="fulfillmentLineId" width="100" />
      <Table.Column title="状态" data-index="status" width="120">
        <template #default="{ record }">
          <Tag :color="statusTag(record.status).color">{{ statusTag(record.status).text }}</Tag>
        </template>
      </Table.Column>
      <Table.Column title="重试次数" data-index="retryCount" width="90" />
      <Table.Column title="创建时间" data-index="createdAt" width="170" />
      <Table.Column title="操作" key="action" width="180" fixed="right">
        <template #default="{ record }">
          <Button v-if="hasPerm('art:job:get')" type="link" size="small" @click="openDetail(record)">详情</Button>
          <Button v-if="hasPerm('art:job:retry') && record.status === 'FAILED'" type="link" size="small" @click="doRetry(record)">重试</Button>
        </template>
      </Table.Column>
    </Table>

    <Modal v-model:open="detailVisible" title="任务详情" width="640" :footer="null" destroy-on-close>
      <div v-if="detailLoading">加载中...</div>
      <template v-else-if="detailRecord">
        <Descriptions :column="2" bordered size="small">
          <Descriptions.Item label="ID">{{ detailRecord.id }}</Descriptions.Item>
          <Descriptions.Item label="任务号">{{ detailRecord.artJobNo }}</Descriptions.Item>
          <Descriptions.Item label="履约单ID">{{ detailRecord.fulfillmentId }}</Descriptions.Item>
          <Descriptions.Item label="履约行ID">{{ detailRecord.fulfillmentLineId }}</Descriptions.Item>
          <Descriptions.Item label="状态">
            <Tag :color="statusTag(detailRecord.status).color">{{ statusTag(detailRecord.status).text }}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="重试次数">{{ detailRecord.retryCount ?? 0 }}</Descriptions.Item>
          <Descriptions.Item label="错误码" :span="2">{{ detailRecord.lastErrorCode || '-' }}</Descriptions.Item>
          <Descriptions.Item label="错误信息" :span="2">{{ detailRecord.lastErrorMsg || '-' }}</Descriptions.Item>
          <Descriptions.Item label="创建时间">{{ detailRecord.createdAt }}</Descriptions.Item>
          <Descriptions.Item label="更新时间">{{ detailRecord.updatedAt }}</Descriptions.Item>
        </Descriptions>
        <div v-if="detailRecord.status === 'FAILED'" class="mt-4">
          <Button v-if="hasPerm('art:job:retry')" type="primary" @click="doRetry(detailRecord)">重试</Button>
        </div>
      </template>
    </Modal>
  </Page>
</template>
