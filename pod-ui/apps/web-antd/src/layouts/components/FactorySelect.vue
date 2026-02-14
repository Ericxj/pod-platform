<script lang="ts" setup>
import { computed } from 'vue';
import { Select } from 'ant-design-vue';
import { useFactoryStore } from '#/store';
import { useRefresh } from '@vben/hooks';

const factoryStore = useFactoryStore();
const { refresh } = useRefresh();

const factoryOptions = computed(() => {
  return factoryStore.factoryIds.map(id => ({
    label: `工厂 ${id}`, // In real world, we might need a map for Factory Names
    value: id,
  }));
});

function handleFactoryChange(value: number) {
  factoryStore.setCurrentFactoryId(value);
  // Reload current page to reflect factory change
  refresh();
}
</script>

<template>
  <div class="flex items-center px-2" v-if="factoryStore.factoryIds.length > 0">
    <Select
      v-model:value="factoryStore.currentFactoryId"
      :options="factoryOptions"
      style="width: 120px"
      placeholder="选择工厂"
      size="small"
      @change="handleFactoryChange"
    />
  </div>
</template>
