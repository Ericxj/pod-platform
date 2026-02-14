<script lang="ts" setup>
import { ref } from 'vue';
import { message } from 'ant-design-vue';
import { getUserFactoryScopes, putUserFactoryScopes } from '#/api/system/user';
import { getFactoryAll } from '#/api/system/factories';

const visible = ref(false);
const userId = ref<number | null>(null);
const userName = ref('');
const loading = ref(false);
const saving = ref(false);
const factoryList = ref<{ id: number; factoryName: string }[]>([]);
const checkedIds = ref<number[]>([]);

const emit = defineEmits(['success']);

async function load() {
  if (userId.value == null) return;
  loading.value = true;
  try {
    const [scopesRes, allRes] = await Promise.all([
      getUserFactoryScopes(userId.value),
      getFactoryAll(),
    ]);
    const scopes = (scopesRes as any)?.data ?? scopesRes;
    const all = (allRes as any)?.data ?? allRes;
    const list = Array.isArray(all) ? all : (all?.records ?? []);
    factoryList.value = list.map((f: any) => ({ id: f.id, factoryName: f.factoryName || f.factory_code || `工厂${f.id}` }));
    checkedIds.value = [...(scopes?.factoryIds ?? [])];
  } finally {
    loading.value = false;
  }
}

function open(uid: number, name: string) {
  userId.value = uid;
  userName.value = name;
  visible.value = true;
  load();
}

function onCheck(id: number, checked: boolean) {
  if (checked) checkedIds.value = [...checkedIds.value, id].sort((a, b) => a - b);
  else checkedIds.value = checkedIds.value.filter((x) => x !== id);
}

async function save() {
  if (userId.value == null) return;
  saving.value = true;
  try {
    await putUserFactoryScopes(userId.value, { factoryIds: checkedIds.value });
    message.success('保存成功');
    emit('success');
    visible.value = false;
  } finally {
    saving.value = false;
  }
}

function close() {
  visible.value = false;
}

defineExpose({ open });
</script>

<template>
  <a-modal
    v-model:open="visible"
    title="工厂范围"
    :confirm-loading="saving"
    @ok="save"
    @cancel="close"
  >
    <p class="mb-2 text-gray-600">用户「{{ userName }}」可访问的工厂（多选）：</p>
    <a-spin :spinning="loading">
      <div class="max-h-80 overflow-y-auto rounded border p-2">
        <div
          v-for="f in factoryList"
          :key="f.id"
          class="flex items-center gap-2 py-1"
        >
          <a-checkbox
            :checked="checkedIds.includes(f.id)"
            @update:checked="(v: boolean) => onCheck(f.id, v)"
          />
          <span>{{ f.factoryName }} (ID: {{ f.id }})</span>
        </div>
        <div v-if="!loading && factoryList.length === 0" class="py-4 text-center text-gray-500">
          暂无可选工厂
        </div>
      </div>
    </a-spin>
  </a-modal>
</template>
