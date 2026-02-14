<script lang="ts" setup>
import { useRoute } from 'vue-router';
import { Result, Button, Descriptions } from 'ant-design-vue';

interface Props {
  title?: string;
  componentPath?: string;
  routePath?: string;
  menuName?: string;
}

const props = withDefaults(defineProps<Props>(), {
  title: '',
  componentPath: '',
  routePath: '',
  menuName: '',
});

const route = useRoute();
const displayComponent = props.componentPath || route.meta?.originalComponent || route.path;
const displayTitle = props.title || route.meta?.title || '未知功能';
</script>

<template>
  <div class="flex flex-col items-center justify-center h-full p-8">
    <Result
      status="404"
      title="功能待实现"
      :sub-title="`组件占位符: ${displayTitle}`"
    >
      <template #extra>
        <div class="w-full max-w-2xl mx-auto text-left bg-gray-50 p-4 rounded-md border border-gray-200 mb-6">
          <Descriptions title="调试信息" bordered size="small" :column="1">
            <Descriptions.Item label="Menu Title">{{ displayTitle }}</Descriptions.Item>
            <Descriptions.Item label="Component Path">{{ displayComponent }}</Descriptions.Item>
            <Descriptions.Item label="Route Path">{{ props.routePath || route.path }}</Descriptions.Item>
            <Descriptions.Item label="Menu Name">{{ props.menuName || route.name }}</Descriptions.Item>
          </Descriptions>
        </div>
        <Button type="primary" @click="$router.back()">返回上一页</Button>
      </template>
    </Result>
  </div>
</template>
