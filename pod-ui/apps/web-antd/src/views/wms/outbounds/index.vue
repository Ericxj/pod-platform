<script lang="ts" setup>
import { VxeGridProps, useVbenVxeGrid } from '#/adapter/vxe-table';
import { Page } from '#/components/Page';
import { getOutbounds, createPickTask, packOutbound, shipOutbound } from '#/api/wms';
import { message } from 'ant-design-vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const gridOptions: VxeGridProps = {
  columns: [
    { field: 'outboundNo', title: '出库单号' },
    { field: 'status', title: '状态' },
    { title: '操作', slots: { default: 'action' }, width: 300 },
  ],
  proxyConfig: {
    ajax: {
      query: async ({ page, form }) => {
        return await getOutbounds({
          page: page.currentPage,
          pageSize: page.pageSize,
          ...form,
        });
      },
    },
  },
  toolbarConfig: {
      refresh: true,
  },
};

const [Grid, gridApi] = useVbenVxeGrid({ gridOptions });

async function handleCreatePickTask(row: any) {
    try {
        await createPickTask(row.id);
        message.success('拣货任务创建成功');
        gridApi.reload();
    } catch (e) { }
}

async function handlePack(row: any) {
    try {
        await packOutbound(row.id);
        message.success('打包成功');
        gridApi.reload();
    } catch (e) { }
}

async function handleShip(row: any) {
    try {
        await shipOutbound(row.id);
        message.success('发货成功');
        gridApi.reload();
    } catch (e) { }
}

function handleDetail(row: any) {
    // Navigate to detail page (not implemented in this batch, using modal or separate route)
    // For now assuming we stay on list or have a separate detail view
}
</script>

<template>
  <Page title="出库单列表">
    <Grid>
      <template #action="{ row }">
        <a-button type="link" @click="handleDetail(row)">详情</a-button>
        <a-button type="link" @click="handleCreatePickTask(row)">生成拣货</a-button>
        <a-button type="link" @click="handlePack(row)">打包</a-button>
        <a-button type="link" @click="handleShip(row)">发货</a-button>
      </template>
    </Grid>
  </Page>
</template>
