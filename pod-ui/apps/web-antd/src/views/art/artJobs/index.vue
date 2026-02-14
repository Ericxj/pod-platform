<script lang="ts" setup>
import { VxeGridProps, useVbenVxeGrid } from '#/adapter/vxe-table';
import { Page } from '#/components/Page';
import { getArtJobs } from '#/api/art';

const gridOptions: VxeGridProps = {
  columns: [
    { field: 'jobNo', title: '任务号' },
    { field: 'status', title: '状态' },
    { title: '操作', slots: { default: 'action' }, width: 200 },
  ],
  proxyConfig: {
    ajax: {
      query: async ({ page, form }) => {
        return await getArtJobs({
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

const [Grid] = useVbenVxeGrid({ gridOptions });
</script>

<template>
  <Page title="稿件任务列表">
    <Grid>
      <template #action="{ row }">
        <a-button type="link">查看详情</a-button>
        <a-button type="link">关联渲染任务</a-button>
      </template>
    </Grid>
  </Page>
</template>
