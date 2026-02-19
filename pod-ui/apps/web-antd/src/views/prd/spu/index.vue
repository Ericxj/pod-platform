<script lang="ts" setup>
import { Page } from '@vben/common-ui';
import { pageSpu, getSpu, createSpu, updateSpu } from '#/api/prd';
import type { SpuRecord } from '#/api/prd';
import { message, Table, Button, Input, Modal, Form } from 'ant-design-vue';
import { ref, onMounted } from 'vue';
import { usePermission } from '#/composables/usePermission';

const hasPerm = usePermission().hasPermission;
const list = ref<SpuRecord[]>([]);
const total = ref(0);
const loading = ref(false);
const current = ref(1);
const size = ref(10);
const keyword = ref('');
const modalVisible = ref(false);
const modalTitle = ref('新增SPU');
const editingId = ref<number | null>(null);
const formState = ref<Partial<SpuRecord>>({});
const saveLoading = ref(false);
const formRef = ref();

async function load() {
  loading.value = true;
  try {
    const res = await pageSpu({ current: current.value, size: size.value, keyword: keyword.value || undefined });
    const d = res as any;
    list.value = d?.records ?? [];
    total.value = d?.total ?? 0;
  } catch (e: any) {
    message.error(e?.message || '加载失败');
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  modalTitle.value = '新增SPU';
  editingId.value = null;
  formState.value = { spuCode: '', spuName: '', categoryCode: '', brand: '' };
  modalVisible.value = true;
}

async function openEdit(record: SpuRecord) {
  if (!record.id) return;
  modalTitle.value = '编辑SPU';
  editingId.value = record.id;
  try {
    const detail = await getSpu(record.id);
    formState.value = { ...(detail as any) };
  } catch (e: any) {
    message.error(e?.message || '加载详情失败');
    return;
  }
  modalVisible.value = true;
}

async function handleSave() {
  try {
    await formRef.value?.validate();
  } catch {
    return;
  }
  saveLoading.value = true;
  try {
    if (editingId.value != null) {
      await updateSpu(editingId.value, formState.value as SpuRecord);
      message.success('更新成功');
    } else {
      await createSpu(formState.value as SpuRecord);
      message.success('创建成功');
    }
    modalVisible.value = false;
    load();
  } catch (e: any) {
    message.error(e?.message || '操作失败');
  } finally {
    saveLoading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <Page title="SPU管理">
    <div class="mb-4 flex gap-2">
      <Input v-model:value="keyword" placeholder="SPU编码/名称" allow-clear style="width: 200px" />
      <Button type="primary" @click="load">查询</Button>
      <Button v-if="hasPerm('prd:spu:create')" type="primary" @click="openCreate">新增SPU</Button>
    </div>
    <Table
      :data-source="list"
      :loading="loading"
      row-key="id"
      :pagination="{ current, pageSize: size, total, showSizeChanger: true, showTotal: (t: number) => `共 ${t} 条` }"
      @change="(p: any) => { current = p.current; size = p.pageSize; load(); }"
    >
      <Table.Column title="ID" data-index="id" width="80" />
      <Table.Column title="SPU编码" data-index="spuCode" width="140" />
      <Table.Column title="SPU名称" data-index="spuName" />
      <Table.Column title="类目" data-index="categoryCode" width="100" />
      <Table.Column title="品牌" data-index="brand" width="100" />
      <Table.Column title="状态" data-index="status" width="90" />
      <Table.Column title="操作" key="action" width="100" fixed="right">
        <template #default="{ record }">
          <Button v-if="hasPerm('prd:spu:update')" type="link" size="small" @click="openEdit(record)">编辑</Button>
        </template>
      </Table.Column>
    </Table>
    <Modal v-model:open="modalVisible" :title="modalTitle" :confirm-loading="saveLoading" @ok="handleSave">
      <Form ref="formRef" :model="formState" layout="vertical">
        <Form.Item name="spuCode" label="SPU编码" :rules="[{ required: true }]">
          <Input v-model:value="formState.spuCode" :disabled="!!editingId" placeholder="唯一编码" />
        </Form.Item>
        <Form.Item name="spuName" label="SPU名称" :rules="[{ required: true }]">
          <Input v-model:value="formState.spuName" placeholder="名称" />
        </Form.Item>
        <Form.Item name="categoryCode" label="类目编码">
          <Input v-model:value="formState.categoryCode" placeholder="如 APPAREL" />
        </Form.Item>
        <Form.Item name="brand" label="品牌">
          <Input v-model:value="formState.brand" placeholder="品牌" />
        </Form.Item>
      </Form>
    </Modal>
  </Page>
</template>
