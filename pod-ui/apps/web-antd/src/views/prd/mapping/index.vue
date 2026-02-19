<!-- 商品中心 - 平台 SKU 映射：列表 + 新增/编辑 + 占位导入导出 -->
<script lang="ts" setup>
import { Page } from '@vben/common-ui';
import { pageSkuMapping, createSkuMapping, updateSkuMapping, deleteSkuMapping } from '#/api/prd';
import type { SkuMappingRecord } from '#/api/prd';
import { message, Table, Button, Input, Modal, Form, Space } from 'ant-design-vue';
import { ref, onMounted } from 'vue';
import { usePermission } from '#/composables/usePermission';

const { hasPermission: hasPerm } = usePermission();

const list = ref<SkuMappingRecord[]>([]);
const total = ref(0);
const loading = ref(false);
const current = ref(1);
const size = ref(10);
const channel = ref('');
const shopId = ref('');
const externalSku = ref('');
const skuCode = ref('');

const modalVisible = ref(false);
const modalTitle = ref('新增映射');
const editingId = ref<number | null>(null);
const formState = ref<Partial<SkuMappingRecord> & { skuCode?: string }>({});
const saveLoading = ref(false);
const formRef = ref();

async function load() {
  loading.value = true;
  try {
    const res = await pageSkuMapping({
      current: current.value,
      size: size.value,
      channel: channel.value || undefined,
      shopId: shopId.value || undefined,
      externalSku: externalSku.value || undefined,
      skuCode: skuCode.value || undefined,
    });
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
  modalTitle.value = '新增映射';
  editingId.value = null;
  formState.value = { channel: '', shopId: '', externalSku: '', externalName: '', skuCode: '', remark: '' };
  modalVisible.value = true;
}

function openEdit(record: SkuMappingRecord) {
  if (!record.id) return;
  modalTitle.value = '编辑映射';
  editingId.value = record.id;
  formState.value = {
    externalName: record.externalName,
    remark: (record as any).remark,
    skuCode: record.skuCode,
  };
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
      await updateSkuMapping(editingId.value, {
        externalName: formState.value?.externalName,
        remark: formState.value?.remark,
      });
      message.success('更新成功');
    } else {
      await createSkuMapping({
        channel: formState.value?.channel!,
        shopId: formState.value?.shopId,
        externalSku: formState.value?.externalSku!,
        externalName: formState.value?.externalName,
        skuCode: formState.value?.skuCode!,
        remark: formState.value?.remark,
      });
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

function handleDelete(record: SkuMappingRecord) {
  if (!record.id) return;
  Modal.confirm({
    title: '确认删除',
    content: `确认删除映射 ${record.channel}/${record.externalSku} 吗？`,
    onOk: async () => {
      try {
        await deleteSkuMapping(record.id!);
        message.success('已删除');
        load();
      } catch (e: any) {
        message.error(e?.message || '删除失败');
      }
    },
  });
}

function doExport() {
  message.info('导出 CSV 功能可在此对接后端 /api/prd/mapping/export');
}

function doImport() {
  message.info('导入 CSV 功能可在此对接后端 /api/prd/mapping/import');
}

onMounted(load);
</script>

<template>
  <Page title="平台SKU映射">
    <div class="mb-4 flex gap-2 flex-wrap">
      <Input v-model:value="channel" placeholder="渠道" allow-clear style="width: 120px" />
      <Input v-model:value="shopId" placeholder="店铺ID" allow-clear style="width: 120px" />
      <Input v-model:value="externalSku" placeholder="外部SKU" allow-clear style="width: 140px" />
      <Input v-model:value="skuCode" placeholder="内部SKU编码" allow-clear style="width: 140px" />
      <Button type="primary" @click="load">查询</Button>
      <Button v-if="hasPerm('prd:mapping:create')" type="primary" @click="openCreate">新增映射</Button>
      <Button @click="doExport">导出CSV</Button>
      <Button @click="doImport">导入CSV</Button>
    </div>
    <Table
      :data-source="list"
      :loading="loading"
      row-key="id"
      :pagination="{
        current,
        pageSize: size,
        total,
        showSizeChanger: true,
        showTotal: (t: number) => `共 ${t} 条`,
      }"
      @change="(p: any) => { current = p.current; size = p.pageSize; load(); }"
    >
      <Table.Column title="内部SKU" data-index="skuCode" width="140" />
      <Table.Column title="渠道" data-index="channel" width="100" />
      <Table.Column title="店铺ID" data-index="shopId" width="100" />
      <Table.Column title="外部SKU" data-index="externalSku" width="140" />
      <Table.Column title="外部名称" data-index="externalName" />
      <Table.Column title="操作" key="action" width="160" fixed="right">
        <template #default="{ record }">
          <Space>
            <Button v-if="hasPerm('prd:mapping:update')" type="link" size="small" @click="openEdit(record)">编辑</Button>
            <Button v-if="hasPerm('prd:mapping:delete')" type="link" size="small" danger @click="handleDelete(record)">删除</Button>
          </Space>
        </template>
      </Table.Column>
    </Table>

    <Modal
      v-model:open="modalVisible"
      :title="modalTitle"
      :confirm-loading="saveLoading"
      @ok="handleSave"
    >
      <Form ref="formRef" :model="formState" layout="vertical">
        <Form.Item v-if="!editingId" name="channel" label="渠道" :rules="[{ required: true }]">
          <Input v-model:value="formState.channel" placeholder="如 AMAZON, TEMU, SHEIN" />
        </Form.Item>
        <Form.Item v-if="!editingId" name="shopId" label="店铺ID">
          <Input v-model:value="formState.shopId" placeholder="店铺标识" />
        </Form.Item>
        <Form.Item v-if="!editingId" name="externalSku" label="外部SKU" :rules="[{ required: true }]">
          <Input v-model:value="formState.externalSku" placeholder="平台SKU" />
        </Form.Item>
        <Form.Item name="externalName" label="外部名称">
          <Input v-model:value="formState.externalName" placeholder="平台商品名" />
        </Form.Item>
        <Form.Item v-if="!editingId" name="skuCode" label="内部SKU编码" :rules="[{ required: true }]">
          <Input v-model:value="formState.skuCode" placeholder="已激活的SKU编码" />
        </Form.Item>
        <Form.Item name="remark" label="备注">
          <Input v-model:value="formState.remark" placeholder="备注" />
        </Form.Item>
      </Form>
    </Modal>
  </Page>
</template>
