<script lang="ts" setup>
import { Page } from '@vben/common-ui';
import { pageUnifiedOrders } from '#/api/oms';
import type { UnifiedOrderRecord } from '#/api/oms';
import { message, Table, Button, Input } from 'ant-design-vue';
import { ref, onMounted } from 'vue';
import { usePermission } from '#/composables/usePermission';

const hasPerm = usePermission().hasPermission;
const list = ref<UnifiedOrderRecord[]>([]);
const total = ref(0);
const loading = ref(false);
const current = ref(1);
const size = ref(10);
const channel = ref('');
const shopId = ref('');
const externalOrderId = ref('');
const orderStatus = ref('');

async function load() {
  loading.value = true;
  try {
    const res = await pageUnifiedOrders({
      current: current.value,
      size: size.value,
      channel: channel.value || undefined,
      shopId: shopId.value || undefined,
      externalOrderId: externalOrderId.value || undefined,
      orderStatus: orderStatus.value || undefined,
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

onMounted(load);
</script>

<template>
  <Page title="统一订单">
    <div class="mb-4 flex gap-2 flex-wrap">
      <Input v-model:value="channel" placeholder="渠道" allow-clear style="width: 120px" />
      <Input v-model:value="shopId" placeholder="店铺ID" allow-clear style="width: 100px" />
      <Input v-model:value="externalOrderId" placeholder="平台订单ID" allow-clear style="width: 180px" />
      <Input v-model:value="orderStatus" placeholder="状态" allow-clear style="width: 100px" />
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
      <Table.Column title="统一订单号" data-index="unifiedOrderNo" width="160" />
      <Table.Column title="渠道" data-index="channel" width="90" />
      <Table.Column title="店铺ID" data-index="shopId" width="90" />
      <Table.Column title="平台订单ID" data-index="externalOrderId" width="160" />
      <Table.Column title="状态" data-index="orderStatus" width="90" />
      <Table.Column title="下单时间" data-index="orderCreatedAt" width="170" />
      <Table.Column title="买家" data-index="buyerName" width="120" />
      <Table.Column title="总金额" data-index="totalAmount" width="90" />
    </Table>
  </Page>
</template>
