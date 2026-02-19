<script lang="ts" setup>
import { useVbenDrawer } from '@vben/common-ui';
import { adjust } from '#/api/inv/commands';
import { message, Input, InputNumber } from 'ant-design-vue';
import { ref } from 'vue';

const emit = defineEmits(['success']);
const balanceId = ref<number>(0);
const delta = ref<number>(0);
const remark = ref('');

const [Drawer, drawerApi] = useVbenDrawer({
  onConfirm: async () => {
    if (delta.value === 0) {
      message.warning('调整量不能为0');
      return;
    }
    try {
      await adjust({
        balanceId: balanceId.value,
        delta: delta.value,
        remark: remark.value || undefined,
      });
      message.success('调整成功');
      drawerApi.close();
      emit('success');
    } catch (e: any) {
      message.error(e?.message || e?.msg || '调整失败');
    }
  },
  onOpenChange: (open: boolean) => {
    if (open) {
      const data = drawerApi.getData<{ balanceId?: number; skuId?: number; warehouseId?: number }>();
      balanceId.value = data?.balanceId ?? 0;
      delta.value = 0;
      remark.value = '';
    }
  },
});
</script>

<template>
  <Drawer title="库存调整">
    <div class="py-2">
      <div class="mb-2">余额ID: {{ balanceId }}</div>
      <div class="mb-2">
        <label>调整量（正数增加、负数减少）</label>
        <InputNumber v-model:value="delta" class="w-full" />
      </div>
      <div class="mb-2">
        <label>备注</label>
        <Input v-model:value="remark" placeholder="可选" />
      </div>
    </div>
  </Drawer>
</template>
