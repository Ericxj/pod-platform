<script lang="ts" setup>
import { Page } from '@vben/common-ui';
import { pageHolds, getHold, resolveHold } from '#/api/oms';
import { pageSku } from '#/api/prd';
import type { OrderHoldRecord } from '#/api/oms';
import type { SkuRecord } from '#/api/prd';
import { message, Table, Button, Input, Modal, Form, Select } from 'ant-design-vue';
import { ref, onMounted } from 'vue';
import { usePermission } from '#/composables/usePermission';

const hasPerm = usePermission().hasPermission;
const list = ref<OrderHoldRecord[]>([]);
const total = ref(0);
const loading = ref(false);
const current = ref(1);
const size = ref(10);
const holdType = ref('SKU_MAPPING');
const status = ref('OPEN');
const channel = ref('');
const shopId = ref('');

const resolveModalVisible = ref(false);
const resolvingHoldId = ref<number | null>(null);
const resolvingHold = ref<OrderHoldRecord | null>(null);
const skuSearchKeyword = ref('');
const skuOptions = ref<Array<{ id: number; skuCode: string; skuName?: string }>>([]);
const selectedSkuId = ref<number | undefined>();
const resolveLoading = ref(false);
const formRef = ref();

async function load() {
  loading.value = true;
  try {
    const res = await pageHolds({
      current: current.value,
      size: size.value,
      type: holdType.value || undefined,
      status: status.value || undefined,
      channel: channel.value || undefined,
      shopId: shopId.value || undefined,
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

async function searchSku() {
  if (!skuSearchKeyword.value.trim()) {
    skuOptions.value = [];
    return;
  }
  try {
    const res = await pageSku({ current: 1, size: 20, keyword: skuSearchKeyword.value, status: 'ACTIVE' });
    const d = res as any;
    skuOptions.value = (d?.records ?? []).map((r: SkuRecord) => ({ id: r.id!, skuCode: r.skuCode || '', skuName: r.skuName }));
  } catch {
    skuOptions.value = [];
  }
}

function openResolve(record: OrderHoldRecord) {
  resolvingHoldId.value = record.id ?? null;
  resolvingHold.value = record;
  selectedSkuId.value = undefined;
  skuSearchKeyword.value = '';
  skuOptions.value = [];
  resolveModalVisible.value = true;
}

async function submitResolve() {
  if (resolvingHoldId.value == null || selectedSkuId.value == null) {
    message.warning('请选择要绑定的 SKU');
    return;
  }
  resolveLoading.value = true;
  try {
    await resolveHold(resolvingHoldId.value, selectedSkuId.value);
    message.success('已绑定 SKU，异常已关闭');
    resolveModalVisible.value = false;
    load();
  } catch (e: any) {
    message.error(e?.message || '处理失败');
  } finally {
    resolveLoading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <Page title="异常队列（SKU映射失败）">
    <div class="mb-4 flex gap-2 flex-wrap">
      <Input v-model:value="holdType" placeholder="类型" allow-clear style="width: 120px" />
      <Input v-model:value="status" placeholder="状态" allow-clear style="width: 100px" />
      <Input v-model:value="channel" placeholder="渠道" allow-clear style="width: 100px" />
      <Input v-model:value="shopId" placeholder="店铺ID" allow-clear style="width: 100px" />
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
      <Table.Column title="类型" data-index="holdType" width="100" />
      <Table.Column title="状态" data-index="status" width="90" />
      <Table.Column title="渠道" data-index="channel" width="90" />
      <Table.Column title="店铺ID" data-index="shopId" width="90" />
      <Table.Column title="平台订单ID" data-index="externalOrderId" width="160" />
      <Table.Column title="外部SKU" data-index="externalSku" width="140" />
      <Table.Column title="原因" data-index="reasonMsg" ellipsis />
      <Table.Column title="创建时间" data-index="createdAt" width="170" />
      <Table.Column title="操作" key="action" width="100" fixed="right">
        <template #default="{ record }">
          <Button v-if="hasPerm('oms:hold:resolve') && record.status === 'OPEN'" type="link" size="small" @click="openResolve(record)">绑定SKU</Button>
        </template>
      </Table.Column>
    </Table>

    <Modal
      v-model:open="resolveModalVisible"
      title="绑定 SKU 并关闭异常"
      :confirm-loading="resolveLoading"
      @ok="submitResolve"
    >
      <div v-if="resolvingHold" class="mb-3 text-gray-600">
        订单 {{ resolvingHold.externalOrderId }} / 外部SKU {{ resolvingHold.externalSku }}
      </div>
      <Form ref="formRef" layout="vertical">
        <Form.Item label="搜索 SKU" name="skuKeyword">
          <div class="flex gap-2">
            <Input v-model:value="skuSearchKeyword" placeholder="输入 SKU 编码或名称" allow-clear @press-enter="searchSku" />
            <Button @click="searchSku">搜索</Button>
          </div>
        </Form.Item>
        <Form.Item label="选择内部 SKU" name="skuId" :rules="[{ required: true, message: '请选择 SKU' }]">
          <Select
            v-model:value="selectedSkuId"
            placeholder="先搜索再选择"
            show-search
            :filter-option="() => true"
            :options="skuOptions.map(o => ({ label: `${o.skuCode} ${o.skuName || ''}`, value: o.id }))"
            style="width: 100%"
          />
        </Form.Item>
      </Form>
    </Modal>
  </Page>
</template>
