<script lang="ts" setup>
import { ref, watch } from 'vue';
import { Page } from '#/components/Page';
import { getDataScopes, putDataScopes } from '#/api/system/scopes';
import { getFactoriesForScope, getFactoryAll } from '#/api/system/factories';
import { usePermission } from '#/composables/usePermission';
import { message, Card, Select, Button, Spin } from 'ant-design-vue';

const { hasPermission: hasPerm } = usePermission();

const subjectType = ref<'USER' | 'ROLE'>('USER');
const subjectId = ref<number | null>(null);
const scopeType = ref('FACTORY');
const scopeIds = ref<number[]>([]);
const factoryOptions = ref<{ id: number; factoryName: string }[]>([]);
const loading = ref(false);
const saving = ref(false);

function parseFactoryList(res: any): { id: number; factoryName: string }[] {
  const data = (res as any)?.data ?? res;
  const arr = Array.isArray(data) ? data : (data?.records ?? []);
  return arr.map((f: any) => ({ id: f.id, factoryName: f.factoryName || f.factoryCode || `工厂${f.id}` }));
}

async function loadOptions() {
  try {
    const res = await getFactoriesForScope();
    factoryOptions.value = parseFactoryList(res);
  } catch (_first) {
    try {
      const res = await getFactoryAll();
      factoryOptions.value = parseFactoryList(res);
    } catch (_second) {
      message.error('加载工厂列表失败');
    }
  }
}

async function loadScopes() {
  if (subjectId.value == null) {
    scopeIds.value = [];
    return;
  }
  loading.value = true;
  try {
    const res = await getDataScopes({
      subjectType: subjectType.value,
      subjectId: subjectId.value,
      scopeType: scopeType.value,
    });
    const data = (res as any)?.data ?? res;
    scopeIds.value = data?.scopeIds ?? [];
  } catch (e) {
    message.error('加载数据范围失败');
    scopeIds.value = [];
  } finally {
    loading.value = false;
  }
}

watch([subjectType, subjectId, scopeType], () => {
  loadScopes();
}, { immediate: true });

async function save() {
  if (subjectId.value == null) {
    message.warning('请选择主体');
    return;
  }
  saving.value = true;
  try {
    await putDataScopes({
      subjectType: subjectType.value,
      subjectId: subjectId.value,
      scopeType: scopeType.value,
      scopeIds: scopeIds.value,
    });
    message.success('保存成功');
  } catch (e) {
    message.error('保存失败');
  } finally {
    saving.value = false;
  }
}

loadOptions();
</script>

<template>
  <Page title="数据权限管理">
    <Card title="范围配置">
      <div class="space-y-4">
        <div class="flex flex-wrap gap-4 items-center">
          <span>主体类型：</span>
          <Select v-model:value="subjectType" style="width: 120px" :options="[{ label: '用户', value: 'USER' }, { label: '角色', value: 'ROLE' }]" />
          <span>主体ID：</span>
          <input v-model.number="subjectId" type="number" placeholder="用户ID或角色ID" class="border px-2 py-1 w-32" />
          <span>范围类型：</span>
          <Select v-model:value="scopeType" style="width: 120px" :options="[{ label: '工厂', value: 'FACTORY' }]" />
        </div>
        <Spin :spinning="loading">
          <div v-if="subjectId != null" class="space-y-2">
            <span>可访问工厂：</span>
            <Select
              v-model:value="scopeIds"
              mode="multiple"
              placeholder="选择工厂"
              style="width: 100%; max-width: 400px"
              :options="factoryOptions.map(f => ({ label: f.factoryName, value: f.id }))"
            />
            <Button v-if="hasPerm('iam:scope:update')" type="primary" :loading="saving" @click="save">保存</Button>
          </div>
        </Spin>
      </div>
    </Card>
  </Page>
</template>
