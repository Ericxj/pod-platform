<script lang="ts" setup>
import { VxeGridProps, useVbenVxeGrid } from '#/adapter/vxe-table';
import { Page } from '#/components/Page';
import { getWorkOrders, releaseWorkOrder, startOperation, finishOperation } from '#/api/mes';
import { message } from 'ant-design-vue';
import { usePermission } from '#/composables/usePermission';

const { hasPermission } = usePermission();

const gridOptions: VxeGridProps = {
  columns: [
    { field: 'workOrderNo', title: '工单号' },
    { field: 'status', title: '状态' },
    { type: 'expand', width: 60, slots: { content: 'expand_content' } },
    { title: '操作', slots: { default: 'action' }, width: 250 },
  ],
  proxyConfig: {
    ajax: {
      query: async ({ page, form }) => {
        return await getWorkOrders({
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

async function handleRelease(row: any) {
    try {
        await releaseWorkOrder(row.id);
        message.success('Release 成功');
        gridApi.reload();
    } catch (e) { }
}

async function handleStartOp(row: any, op: any) {
    try {
        await startOperation(row.id, op.id);
        message.success('Operation Started');
        gridApi.reload();
    } catch (e) { }
}

async function handleFinishOp(row: any, op: any) {
    try {
        await finishOperation(row.id, op.id);
        message.success('Operation Finished');
        gridApi.reload();
    } catch (e) { }
}
</script>

<template>
  <Page title="工单列表">
    <Grid>
      <template #expand_content="{ row }">
          <div class="p-4 bg-gray-50">
              <div v-for="op in row.operations" :key="op.id" class="flex items-center gap-4 mb-2">
                  <span>Step {{ op.stepNo }}: {{ op.opName }} ({{ op.status }})</span>
                  <a-button size="small" v-if="op.status === 'PENDING'" @click="handleStartOp(row, op)">Start</a-button>
                  <a-button size="small" v-if="op.status === 'IN_PROGRESS'" @click="handleFinishOp(row, op)">Finish</a-button>
              </div>
          </div>
      </template>
      <template #action="{ row }">
        <a-button 
            type="link" 
            v-if="hasPermission('mes:work_order:release')"
            @click="handleRelease(row)"
        >
            Release
        </a-button>
      </template>
    </Grid>
  </Page>
</template>
