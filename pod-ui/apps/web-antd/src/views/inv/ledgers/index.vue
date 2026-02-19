<!--
  库存台账 - GET /api/inv/ledgers (current, size, bizType, bizNo, action, skuId)
  权限：inv:ledger:page 查询
-->
<script lang="ts" setup>
import { Page } from '@vben/common-ui';
import { pageLedgers } from '#/api/inv/query';
import type { LedgerRecord } from '#/api/inv/query';
import { message, Table, Button, Input, InputNumber } from 'ant-design-vue';
import { ref, onMounted } from 'vue';
import { usePermission } from '#/composables/usePermission';

const { hasPermission: hasPerm } = usePermission();

const list = ref<LedgerRecord[]>([]);
const total = ref(0);
const loading = ref(false);
const current = ref(1);
const size = ref(10);
const bizType = ref('');
const bizNo = ref('');
const action = ref('');
const skuId = ref<number | undefined>();

async function load() {
  loading.value = true;
  try {
    const res = await pageLedgers({
      current: current.value,
      size: size.value,
      bizType: bizType.value || undefined,
      bizNo: bizNo.value || undefined,
      action: action.value || undefined,
      skuId: skuId.value,
    });
    list.value = (res as any)?.records ?? [];
    total.value = (res as any)?.total ?? 0;
  } catch (e: any) {
    message.error(e?.message || e?.msg || '加载失败');
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <Page title="库存台账">
    <div class="mb-4 flex gap-2">
      <Input v-model:value="bizType" placeholder="业务类型" allow-clear style="width: 110px" />
      <Input v-model:value="bizNo" placeholder="业务单号" allow-clear style="width: 130px" />
      <Input v-model:value="action" placeholder="动作" allow-clear style="width: 90px" />
      <InputNumber v-model:value="skuId" placeholder="SKU ID" allow-clear style="width: 100px" />
      <Button type="primary" @click="load">查询</Button>
    </div>
    <Table
      :data-source="list"
      :loading="loading"
      row-key="id"
      :pagination="{
        current,
        pageSize: size,
        total,
        showSizeChanger: true,
        showTotal: (t: number) => `共 ${t} 条`,
      }"
      @change="(p: any) => { current = p.current; size = p.pageSize; load(); }"
    >
      <Table.Column title="动作(action)" data-index="txnType" width="90" />
      <Table.Column title="业务类型" data-index="bizType" width="100" />
      <Table.Column title="业务单号" data-index="bizNo" width="120" />
      <Table.Column title="行号" width="70">
        <template #default="{ record }">{{ record.id }}</template>
      </Table.Column>
      <Table.Column title="仓库ID" data-index="warehouseId" width="90" />
      <Table.Column title="SKU ID" data-index="skuId" width="80" />
      <Table.Column title="变更量(qty)" data-index="deltaQty" width="90" />
      <Table.Column title="变更前现存" data-index="beforeOnHand" width="100" />
      <Table.Column title="变更前预占" data-index="beforeAllocated" width="100" />
      <Table.Column title="变更后现存" data-index="afterOnHand" width="100" />
      <Table.Column title="变更后预占" data-index="afterAllocated" width="100" />
      <Table.Column title="创建时间" data-index="createdAt" width="170" />
    </Table>
  </Page>
</template>
