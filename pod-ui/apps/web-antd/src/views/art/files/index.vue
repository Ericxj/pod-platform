<script lang="ts" setup>
import { Page } from '@vben/common-ui';
import { pageProductionFiles, getProductionFile } from '#/api/art';
import type { ProductionFileRecord } from '#/api/art';
import { message, Table, Button, Input, Modal, Descriptions } from 'ant-design-vue';
import { ref, onMounted } from 'vue';
import { usePermission } from '#/composables/usePermission';

const hasPerm = usePermission().hasPermission;
const list = ref<ProductionFileRecord[]>([]);
const total = ref(0);
const loading = ref(false);
const current = ref(1);
const size = ref(10);
const jobId = ref<number | string>('');

const detailVisible = ref(false);
const detailRecord = ref<ProductionFileRecord | null>(null);
const detailLoading = ref(false);

async function load() {
  loading.value = true;
  try {
    const id = typeof jobId.value === 'string' ? (jobId.value ? Number(jobId.value) : undefined) : jobId.value;
    const res = await pageProductionFiles({
      current: current.value,
      size: size.value,
      jobId: id,
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

async function openDetail(record: ProductionFileRecord) {
  if (!record.id) return;
  detailVisible.value = true;
  detailRecord.value = null;
  detailLoading.value = true;
  try {
    detailRecord.value = (await getProductionFile(record.id)) as ProductionFileRecord;
  } catch (e: any) {
    message.error(e?.message || '加载详情失败');
  } finally {
    detailLoading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <Page title="生产文件">
    <div class="mb-4 flex gap-2 flex-wrap items-center">
      <Input v-model:value="jobId" placeholder="任务ID" allow-clear style="width: 140px" />
      <Button type="primary" @click="load">查询</Button>
    </div>
    <Table
      :data-source="list"
      :loading="loading"
      row-key="id"
      :pagination="{ current, pageSize: size, total, showSizeChanger: true, showTotal: (t: number) => `共 ${t} 条` }"
      @change="(p: any) => { current = p.current; size = p.pageSize; load(); }"
    >
      <Table.Column title="ID" data-index="id" width="80" />
      <Table.Column title="任务ID" data-index="artJobId" width="100" />
      <Table.Column title="文件编号" data-index="fileNo" width="180" />
      <Table.Column title="类型" data-index="fileType" width="80" />
      <Table.Column title="格式" data-index="format" width="80" />
      <Table.Column title="宽(px)" data-index="widthPx" width="80" />
      <Table.Column title="高(px)" data-index="heightPx" width="80" />
      <Table.Column title="DPI" data-index="dpi" width="70" />
      <Table.Column title="状态" data-index="status" width="80" />
      <Table.Column title="创建时间" data-index="createdAt" width="170" />
      <Table.Column title="操作" key="action" width="100" fixed="right">
        <template #default="{ record }">
          <Button v-if="hasPerm('art:file:get')" type="link" size="small" @click="openDetail(record)">详情</Button>
          <a v-if="record.fileUrl" :href="record.fileUrl" target="_blank" rel="noopener" class="ml-1">下载</a>
        </template>
      </Table.Column>
    </Table>

    <Modal v-model:open="detailVisible" title="文件详情" width="560" :footer="null" destroy-on-close>
      <div v-if="detailLoading">加载中...</div>
      <template v-else-if="detailRecord">
        <Descriptions :column="2" bordered size="small">
          <Descriptions.Item label="ID">{{ detailRecord.id }}</Descriptions.Item>
          <Descriptions.Item label="任务ID">{{ detailRecord.artJobId }}</Descriptions.Item>
          <Descriptions.Item label="文件编号">{{ detailRecord.fileNo }}</Descriptions.Item>
          <Descriptions.Item label="类型">{{ detailRecord.fileType }}</Descriptions.Item>
          <Descriptions.Item label="格式">{{ detailRecord.format }}</Descriptions.Item>
          <Descriptions.Item label="状态">{{ detailRecord.status }}</Descriptions.Item>
          <Descriptions.Item label="宽(px)">{{ detailRecord.widthPx }}</Descriptions.Item>
          <Descriptions.Item label="高(px)">{{ detailRecord.heightPx }}</Descriptions.Item>
          <Descriptions.Item label="DPI">{{ detailRecord.dpi }}</Descriptions.Item>
          <Descriptions.Item label="文件哈希">{{ detailRecord.fileHash || '-' }}</Descriptions.Item>
          <Descriptions.Item label="URL" :span="2">
            <a v-if="detailRecord.fileUrl" :href="detailRecord.fileUrl" target="_blank" rel="noopener">{{ detailRecord.fileUrl }}</a>
            <span v-else>-</span>
          </Descriptions.Item>
        </Descriptions>
      </template>
    </Modal>
  </Page>
</template>
