<script lang="ts" setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import { Empty, Alert } from 'ant-design-vue';

const route = useRoute();

const reason = computed(() => (route.query?.reason as string) || '');
const messageText = computed(() => {
  const raw = route.query?.message as string;
  return raw ? decodeURIComponent(raw) : '';
});

const isBuildFailed = computed(() => reason.value === 'build-failed');
const isNoMenus = computed(() => reason.value === 'no-menus' || !reason.value);

const title = computed(() =>
  isBuildFailed.value ? '菜单配置错误 / 组件缺失' : '权限未配置',
);
const description = computed(() =>
  isBuildFailed.value
    ? '后端菜单的 component 与前端 src/views 下文件不匹配，请检查后端菜单配置或补充对应页面组件。'
    : '当前账号未分配任何菜单权限，请联系管理员在系统中配置角色与菜单后再登录。',
);
</script>

<template>
  <div class="flex h-full min-h-[320px] flex-col items-center justify-center gap-4 p-6">
    <Empty
      :description="title"
      :image-style="{ height: '80px' }"
    />
    <p class="text-muted-foreground text-sm">
      {{ description }}
    </p>
    <Alert
      v-if="isBuildFailed && messageText"
      type="error"
      :message="'错误详情'"
      :description="messageText"
      show-icon
      class="max-w-2xl"
    />
  </div>
</template>
