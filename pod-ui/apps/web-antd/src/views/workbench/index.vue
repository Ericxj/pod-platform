<script lang="ts" setup>
import { computed } from 'vue';
import { useUserStore, useAccessStore } from '@vben/stores';
import { useFactoryStore } from '#/store';
import { Page } from '@vben/common-ui';
import { Card, Descriptions, Tag, Divider } from 'ant-design-vue';

const userStore = useUserStore();
const accessStore = useAccessStore();
const factoryStore = useFactoryStore();

const userInfo = computed(() => userStore.userInfo || {});
const accessCodes = computed(() => accessStore.accessCodes || []);
const accessMenus = computed(() => accessStore.accessMenus || []);
const currentFactoryId = computed(() => factoryStore.currentFactoryId);

</script>

<template>
  <Page title="系统工作台">
    <div class="p-4">
      <Card title="当前用户信息" class="mb-4">
        <Descriptions bordered :column="2">
          <Descriptions.Item label="用户名">{{ userInfo.username }}</Descriptions.Item>
          <Descriptions.Item label="真实姓名">{{ userInfo.realName }}</Descriptions.Item>
          <Descriptions.Item label="用户ID">{{ userInfo.id }}</Descriptions.Item>
          <Descriptions.Item label="租户ID">{{ userInfo.tenantId }}</Descriptions.Item>
          <Descriptions.Item label="当前工厂ID">
             <Tag color="blue">{{ currentFactoryId || 'N/A' }}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="角色">{{ userInfo.roles?.join(', ') }}</Descriptions.Item>
        </Descriptions>
      </Card>

      <Card title="权限与菜单概览" class="mb-4">
        <div class="flex gap-4 mb-4">
           <Card class="flex-1" :bordered="false" style="background: #f5f5f5;">
              <template #title>权限码数量</template>
              <div class="text-2xl font-bold text-green-600">{{ accessCodes.length }}</div>
           </Card>
           <Card class="flex-1" :bordered="false" style="background: #f5f5f5;">
              <template #title>菜单数量</template>
              <div class="text-2xl font-bold text-blue-600">{{ accessMenus.length }}</div>
           </Card>
        </div>
        
        <Divider orientation="left">权限码列表 (Top 20)</Divider>
        <div>
           <Tag v-for="code in accessCodes.slice(0, 20)" :key="code" class="mb-2">{{ code }}</Tag>
           <span v-if="accessCodes.length > 20">... (共 {{ accessCodes.length }} 个)</span>
        </div>
      </Card>
      
      <Card title="调试信息" size="small">
         <p>本页面用于验证系统后端对接状态。如果看到此页面，说明静态 Demo 路由已被移除，且系统已成功加载。</p>
      </Card>
    </div>
  </Page>
</template>
