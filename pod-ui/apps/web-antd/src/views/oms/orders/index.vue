<script lang="ts" setup>
import type { VxeGridProps } from '#/adapter/vxe-table';
import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { Page } from '#/components/Page';
import { getOrderList, createFulfillment } from '#/api/oms';
import { message } from 'ant-design-vue';
import { usePermission } from '#/composables/usePermission';

const { hasPermission } = usePermission();

const gridOptions: VxeGridProps = {
  columns: [
    { field: 'orderNo', title: '订单号' },
    { field: 'status', title: '状态' },
    { field: 'createdAt', title: '创建时间' },
    { title: '操作', slots: { default: 'action' }, width: 200 },
  ],
  proxyConfig: {
    ajax: {
      query: async ({ page, form }) => {
        return await getOrderList({
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
          { field: 'orderNo', title: '订单号', itemRender: { name: 'AInput' } },
          { field: 'status', title: '状态', itemRender: { name: 'AInput' } },
      ]
  }
};

const [Grid, gridApi] = useVbenVxeGrid({ gridOptions });

async function handleCreateFulfillment(row: any) {
    try {
        await createFulfillment(row.id);
        message.success('创建履约单成功');
        gridApi.reload();
    } catch (e) {
        // Error handled by interceptor
    }
}
</script>

<template>
  <Page title="订单列表">
    <Grid>
      <template #action="{ row }">
        <a-button type="link">查看详情</a-button>
        <a-button 
            type="link" 
            v-if="hasPermission('oms:order:create_fulfillment')"
            @click="handleCreateFulfillment(row)"
        >
            创建履约
        </a-button>
      </template>
    </Grid>
  </Page>
</template>
