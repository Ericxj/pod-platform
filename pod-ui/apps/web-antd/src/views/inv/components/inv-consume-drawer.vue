<script lang="ts" setup>
import { useVbenDrawer } from '@vben/common-ui';
import { consumeReserved } from '#/api/inv/commands';
import { message, Input, InputNumber } from 'ant-design-vue';
import { ref } from 'vue';

const emit = defineEmits(['success']);
const reservationId = ref<number>(0);
const qty = ref<number>(1);
const remark = ref('');
const maxQty = ref(0);

const [Drawer, drawerApi] = useVbenDrawer({
  onConfirm: async () => {
    if (qty.value <= 0) {
      message.warning('核销数量必须大于0');
      return;
    }
    if (qty.value > maxQty.value) {
      message.warning('核销数量不能超过剩余预占');
      return;
    }
    try {
      await consumeReserved({
        reservationId: reservationId.value,
        qty: qty.value,
        remark: remark.value || undefined,
      });
      message.success('核销成功');
      drawerApi.close();
      emit('success');
    } catch (e: any) {
      message.error(e?.message || e?.msg || '核销失败');
    }
  },
  onOpenChange: (open: boolean) => {
    if (open) {
      const data = drawerApi.getData<{ reservationId?: number; remainingQty?: number }>();
      reservationId.value = data?.reservationId ?? 0;
      maxQty.value = data?.remainingQty ?? 0;
      qty.value = Math.min(1, maxQty.value) || 1;
      remark.value = '';
    }
  },
});
</script>

<template>
  <Drawer title="核销预占">
    <div class="py-2">
      <div class="mb-2">预占ID: {{ reservationId }}，可核销数量: {{ maxQty }}</div>
      <div class="mb-2">
        <label>核销数量</label>
        <InputNumber v-model:value="qty" class="w-full" :min="1" :max="maxQty" />
      </div>
      <div class="mb-2">
        <label>备注</label>
        <Input v-model:value="remark" placeholder="可选" />
      </div>
    </div>
  </Drawer>
</template>
