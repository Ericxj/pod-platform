<script lang="ts" setup>
import { VxeGridProps, useVbenVxeGrid } from '#/adapter/vxe-table';
import { Page } from '#/components/Page';
import { getFulfillmentList, releaseFulfillment } from '#/api/oms';
import { message } from 'ant-design-vue';
import { usePermission } from '#/composables/usePermission';

const { hasPermission } = usePermission();

const gridOptions: VxeGridProps = {
  columns: [
    { field: 'fulfillmentNo', title: '履约单号' },
    { field: 'orderNo', title: '订单号' },
    { field: 'status', title: '状态' },
    { title: '操作', slots: { default: 'action' }, width: 200 },
  ],
  proxyConfig: {
    ajax: {
      query: async ({ page, form }) => {
        return await getFulfillmentList({
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
  formConfig: {
      items: [
          { field: 'fulfillmentNo', title: '履约单号', itemRender: { name: 'AInput' } },
      ]
  }
};

const [Grid, gridApi] = useVbenVxeGrid({ gridOptions });

async function handleRelease(row: any) {
    try {
        await releaseFulfillment(row.id);
        message.success('Release 成功');
        gridApi.reload();
    } catch (e) {
        // Error handled by interceptor
    }
}
</script>

<template>
  <Page title="履约单列表">
    <Grid>
      <template #action="{ row }">
        <a-button 
            type="link" 
            v-if="hasPermission('ful:fulfillment:release')"
            @click="handleRelease(row)"
        >
            Release
        </a-button>
      </template>
    </Grid>
  </Page>
</template>
