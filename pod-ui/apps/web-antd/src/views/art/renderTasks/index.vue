<script lang="ts" setup>
import { VxeGridProps, useVbenVxeGrid } from '#/adapter/vxe-table';
import { Page } from '#/components/Page';
import { getRenderTasks, retryRenderTask } from '#/api/art';
import { message } from 'ant-design-vue';
import { usePermission } from '#/composables/usePermission';

const { hasPermission } = usePermission();

const gridOptions: VxeGridProps = {
  columns: [
    { field: 'taskNo', title: '任务号' },
    { field: 'status', title: '状态' },
    { title: '操作', slots: { default: 'action' }, width: 200 },
  ],
  proxyConfig: {
    ajax: {
      query: async ({ page, form }) => {
        return await getRenderTasks({
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

async function handleRetry(row: any) {
    try {
        await retryRenderTask(row.id);
        message.success('Retry 成功');
        gridApi.reload();
    } catch (e) {
        // Error handled by interceptor
    }
}
</script>

<template>
  <Page title="渲染任务列表">
    <Grid>
      <template #action="{ row }">
        <a-button 
            type="link" 
            v-if="hasPermission('art:render:retry')"
            @click="handleRetry(row)"
        >
            Retry
        </a-button>
      </template>
    </Grid>
  </Page>
</template>
