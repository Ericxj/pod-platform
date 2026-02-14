<script lang="ts" setup>
import { VxeGridProps, useVbenVxeGrid } from '#/adapter/vxe-table';
import { Page } from '#/components/Page';
import { getShipments, generateLabel, ackPlatform } from '#/api/tms';
import { message } from 'ant-design-vue';

const gridOptions: VxeGridProps = {
  columns: [
    { field: 'shipmentNo', title: '运单号' },
    { field: 'trackingNo', title: '物流单号' },
    { field: 'status', title: '状态' },
    { title: '操作', slots: { default: 'action' }, width: 250 },
  ],
  proxyConfig: {
    ajax: {
      query: async ({ page, form }) => {
        return await getShipments({
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

async function handleGenerateLabel(row: any) {
    try {
        await generateLabel(row.id);
        message.success('面单生成成功');
        gridApi.reload();
    } catch (e) { }
}

async function handleAck(row: any) {
    try {
        await ackPlatform(row.id);
        message.success('已回传平台');
        gridApi.reload();
    } catch (e) { }
}
</script>

<template>
  <Page title="运单列表">
    <Grid>
      <template #action="{ row }">
        <a-button type="link" @click="handleGenerateLabel(row)">生成面单</a-button>
        <a-button type="link" @click="handleAck(row)">回传平台</a-button>
      </template>
    </Grid>
  </Page>
</template>
