<script lang="ts" setup>
import { useVbenDrawer } from '@vben/common-ui';
import { reserve } from '#/api/inv/commands';
import { message, Input, InputNumber } from 'ant-design-vue';
import { ref } from 'vue';

const emit = defineEmits(['success']);
const bizType = ref('FULFILLMENT');
const bizNo = ref('');
const warehouseId = ref<number>(0);
const locationId = ref<number | undefined>();
const skuId = ref<number>(0);
const qty = ref<number>(1);
const remark = ref('');

const [Drawer, drawerApi] = useVbenDrawer({
  onConfirm: async () => {
    if (!bizNo.value?.trim()) {
      message.warning('请输入业务单号');
      return;
    }
    if (qty.value <= 0) {
      message.warning('预占数量必须大于0');
      return;
    }
    try {
      await reserve({
        bizType: bizType.value,
        bizNo: bizNo.value.trim(),
        warehouseId: warehouseId.value,
        locationId: locationId.value,
        skuId: skuId.value,
        qty: qty.value,
        remark: remark.value || undefined,
      });
      message.success('预占成功');
      drawerApi.close();
      emit('success');
    } catch (e: any) {
      message.error(e?.message || e?.msg || '预占失败');
    }
  },
  onOpenChange: (open: boolean) => {
    if (open) {
      const data = drawerApi.getData<{ warehouseId?: number; locationId?: number; skuId?: number }>();
      warehouseId.value = data?.warehouseId ?? 0;
      locationId.value = data?.locationId;
      skuId.value = data?.skuId ?? 0;
      bizNo.value = '';
      qty.value = 1;
      remark.value = '';
    }
  },
});
</script>

<template>
  <Drawer title="预占库存">
    <div class="py-2">
      <div class="mb-2">
        <label>业务类型</label>
        <Input v-model:value="bizType" placeholder="如 FULFILLMENT" />
      </div>
      <div class="mb-2">
        <label>业务单号</label>
        <Input v-model:value="bizNo" placeholder="必填" />
      </div>
      <div class="mb-2">
        <label>仓库ID</label>
        <InputNumber v-model:value="warehouseId" class="w-full" />
      </div>
      <div class="mb-2">
        <label>库位ID（可选）</label>
        <InputNumber v-model:value="locationId" class="w-full" placeholder="可空" />
      </div>
      <div class="mb-2">
        <label>SKU ID</label>
        <InputNumber v-model:value="skuId" class="w-full" />
      </div>
      <div class="mb-2">
        <label>预占数量</label>
        <InputNumber v-model:value="qty" class="w-full" :min="1" />
      </div>
      <div class="mb-2">
        <label>备注</label>
        <Input v-model:value="remark" placeholder="可选" />
      </div>
    </div>
  </Drawer>
</template>
