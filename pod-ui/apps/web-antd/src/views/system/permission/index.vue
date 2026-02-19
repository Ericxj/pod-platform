<script lang="ts" setup>
import { Page } from '@vben/common-ui';
import {
  getPermissionTree,
  pagePermissions,
  createPermission,
  updatePermission,
  deletePermission,
  validatePermission,
} from '#/api/system/permission';
import type {
  PermissionTreeDto,
  PermissionCreateDto,
  PermissionUpdateDto,
  PermissionPageQuery,
  PermissionValidateResult,
} from '#/api/system/permission';
import { message, Modal, Tabs, Input, Table, Button, Space, Tag } from 'ant-design-vue';
import { ref, onMounted, watch } from 'vue';
import type { TableColumnType } from 'ant-design-vue';
import PermissionDrawer from './permission-drawer.vue';
import { useVbenDrawer } from '@vben/common-ui';
import { usePermission } from '#/composables/usePermission';

const { hasPermission: hasPerm } = usePermission();

type TabKey = 'MENU' | 'BUTTON' | 'API';
const activeTab = ref<TabKey>('MENU');

// ----- MENU tab -----
const menuTreeData = ref<PermissionTreeDto[]>([]);
const menuLoading = ref(false);

async function loadMenuTree() {
  menuLoading.value = true;
  try {
    menuTreeData.value = await getPermissionTree('MENU', true);
  } catch (e: any) {
    message.error(e?.message || e?.msg || '加载菜单树失败');
  } finally {
    menuLoading.value = false;
  }
}

function findInTree(nodes: PermissionTreeDto[] | undefined, id: number): PermissionTreeDto | null {
  if (!nodes?.length) return null;
  for (const n of nodes) {
    if (n.id === id) return n;
    const found = findInTree(n.children, id);
    if (found) return found;
  }
  return null;
}

// ----- BUTTON tab -----
const buttonList = ref<any[]>([]);
const buttonTotal = ref(0);
const buttonLoading = ref(false);
const buttonPage = ref(1);
const buttonPageSize = ref(10);
const buttonKeyword = ref('');
const menuIdToName = ref<Record<number, string>>({});

async function loadMenuIdToName() {
  try {
    const tree = await getPermissionTree('MENU', true);
    const map: Record<number, string> = {};
    const walk = (nodes: PermissionTreeDto[] | undefined) => {
      nodes?.forEach((n) => {
        map[n.id] = n.permName || n.permCode || String(n.id);
        walk(n.children);
      });
    };
    walk(tree);
    menuIdToName.value = map;
  } catch {
    menuIdToName.value = {};
  }
}

async function loadButtonList() {
  buttonLoading.value = true;
  try {
    const res = await pagePermissions({
      permType: 'BUTTON',
      current: buttonPage.value,
      size: buttonPageSize.value,
      keyword: buttonKeyword.value || undefined,
    } as PermissionPageQuery);
    buttonList.value = (res as any)?.records ?? [];
    buttonTotal.value = (res as any)?.total ?? 0;
  } catch (e: any) {
    message.error(e?.message || e?.msg || '加载列表失败');
  } finally {
    buttonLoading.value = false;
  }
}

// ----- API tab -----
const apiList = ref<any[]>([]);
const apiTotal = ref(0);
const apiLoading = ref(false);
const apiPage = ref(1);
const apiPageSize = ref(10);
const apiKeyword = ref('');

async function loadApiList() {
  apiLoading.value = true;
  try {
    const res = await pagePermissions({
      permType: 'API',
      current: apiPage.value,
      size: apiPageSize.value,
      keyword: apiKeyword.value || undefined,
    } as PermissionPageQuery);
    apiList.value = (res as any)?.records ?? [];
    apiTotal.value = (res as any)?.total ?? 0;
  } catch (e: any) {
    message.error(e?.message || e?.msg || '加载列表失败');
  } finally {
    apiLoading.value = false;
  }
}

// ----- Drawer -----
const [Drawer, drawerApi] = useVbenDrawer({ connectedComponent: PermissionDrawer });

function openDrawerAdd(type: TabKey, parentId?: number) {
  drawerApi.setData({
    drawerType: type,
    parentId: parentId ?? 0,
    record: null,
    isUpdate: false,
  });
  drawerApi.open();
}

function openDrawerEdit(type: TabKey, record: any) {
  drawerApi.setData({
    drawerType: type,
    parentId: record.parentId ?? 0,
    record: { ...record },
    isUpdate: true,
  });
  drawerApi.open();
}

function handleDrawerSuccess() {
  if (activeTab.value === 'MENU') loadMenuTree();
  else if (activeTab.value === 'BUTTON') {
    loadMenuIdToName();
    loadButtonList();
  } else {
    loadMenuIdToName();
    loadApiList();
  }
}

// ----- Delete -----
function handleDelete(record: PermissionTreeDto | any, tab: TabKey) {
  Modal.confirm({
    title: '确认删除',
    content: `确认删除「${record.permName || record.permCode}」吗？`,
    onOk: async () => {
      try {
        await deletePermission(record.id);
        message.success('删除成功');
        if (tab === 'MENU') loadMenuTree();
        else if (tab === 'BUTTON') loadButtonList();
        else loadApiList();
      } catch (e: any) {
        message.error(e?.message || e?.msg || '删除失败');
      }
    },
  });
}

// ----- MENU table columns -----
const menuColumns: TableColumnType<PermissionTreeDto>[] = [
  { title: '标题', dataIndex: 'permName', key: 'permName', width: 140 },
  { title: '路径', dataIndex: 'menuPath', key: 'menuPath', width: 160 },
  { title: '组件', dataIndex: 'component', key: 'component', width: 180 },
  { title: '图标', dataIndex: 'icon', key: 'icon', width: 80 },
  { title: '排序', dataIndex: 'sortNo', key: 'sortNo', width: 70 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '隐藏', dataIndex: 'hidden', key: 'hidden', width: 70 },
  { title: '缓存', dataIndex: 'keepAlive', key: 'keepAlive', width: 70 },
  { title: '操作', key: 'action', width: 260, fixed: 'right' },
];

watch(activeTab, (tab) => {
  if (tab === 'MENU') loadMenuTree();
  else if (tab === 'BUTTON') {
    loadMenuIdToName();
    loadButtonList();
  } else if (tab === 'API') {
    loadMenuIdToName();
    loadApiList();
  }
});

onMounted(() => {
  loadMenuTree();
});
</script>

<template>
  <Page title="权限管理">
    <Tabs v-model:activeKey="activeTab">
      <Tabs.TabPane key="MENU" tab="菜单">
        <div class="mb-4 flex gap-2">
          <Button v-if="hasPerm('iam:perm:create')" type="primary" @click="openDrawerAdd('MENU', 0)">
            新增根菜单
          </Button>
        </div>
        <Table
          :columns="menuColumns"
          :data-source="menuTreeData"
          :loading="menuLoading"
          row-key="id"
          :pagination="false"
          :expandable="{ defaultExpandAllRows: true }"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'status'">
              <Tag :color="record.status === 'ENABLED' ? 'green' : 'red'">
                {{ record.status === 'ENABLED' ? '启用' : '禁用' }}
              </Tag>
            </template>
            <template v-else-if="column.key === 'hidden'">
              {{ record.hidden ? '是' : '否' }}
            </template>
            <template v-else-if="column.key === 'keepAlive'">
              {{ record.keepAlive !== false ? '是' : '否' }}
            </template>
            <template v-else-if="column.key === 'action'">
              <Space>
                <Button v-if="hasPerm('iam:perm:create')" type="link" size="small" @click="openDrawerAdd('MENU', record.id)">
                  新增子级
                </Button>
                <Button v-if="hasPerm('iam:perm:create')" type="link" size="small" @click="openDrawerAdd('MENU', record.parentId ?? 0)">
                  新增同级
                </Button>
                <Button v-if="hasPerm('iam:perm:update')" type="link" size="small" @click="openDrawerEdit('MENU', record)">
                  编辑
                </Button>
                <Button v-if="hasPerm('iam:perm:delete')" type="link" size="small" danger @click="handleDelete(record, 'MENU')">
                  删除
                </Button>
              </Space>
            </template>
          </template>
        </Table>
      </Tabs.TabPane>

      <Tabs.TabPane key="BUTTON" tab="按钮">
        <div class="mb-4 flex gap-2">
          <Input v-model:value="buttonKeyword" placeholder="权限码/名称" allow-clear style="width: 200px" @press-enter="loadButtonList" />
          <Button @click="loadButtonList">查询</Button>
          <Button v-if="hasPerm('iam:perm:create')" type="primary" @click="openDrawerAdd('BUTTON')">
            新增按钮权限
          </Button>
        </div>
        <Table
          :columns="[
            { title: '权限码', dataIndex: 'permCode', key: 'permCode', width: 160 },
            { title: '名称', dataIndex: 'permName', key: 'permName', width: 140 },
            { title: '所属菜单', key: 'parentName', width: 140 },
            { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
            { title: '排序', dataIndex: 'sortNo', key: 'sortNo', width: 70 },
            { title: '操作', key: 'action', width: 120, fixed: 'right' },
          ]"
          :data-source="buttonList"
          :loading="buttonLoading"
          row-key="id"
          :pagination="{
            current: buttonPage,
            pageSize: buttonPageSize,
            total: buttonTotal,
            showSizeChanger: true,
            showTotal: (t: number) => `共 ${t} 条`,
          }"
          @change="(pag: any) => { buttonPage = pag.current; buttonPageSize = pag.pageSize; loadButtonList(); }"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'parentName'">
              {{ menuIdToName[record.parentId] ?? '-' }}
            </template>
            <template v-else-if="column.key === 'status'">
              <Tag :color="record.status === 'ENABLED' ? 'green' : 'red'">
                {{ record.status === 'ENABLED' ? '启用' : '禁用' }}
              </Tag>
            </template>
            <template v-else-if="column.key === 'action'">
              <Space>
                <Button v-if="hasPerm('iam:perm:update')" type="link" size="small" @click="openDrawerEdit('BUTTON', record)">编辑</Button>
                <Button v-if="hasPerm('iam:perm:delete')" type="link" size="small" danger @click="handleDelete(record, 'BUTTON')">删除</Button>
              </Space>
            </template>
          </template>
        </Table>
      </Tabs.TabPane>

      <Tabs.TabPane key="API" tab="API">
        <div class="mb-4 flex gap-2">
          <Input v-model:value="apiKeyword" placeholder="权限码/名称/路径" allow-clear style="width: 200px" @press-enter="loadApiList" />
          <Button @click="loadApiList">查询</Button>
          <Button v-if="hasPerm('iam:perm:create')" type="primary" @click="openDrawerAdd('API')">
            新增接口权限
          </Button>
        </div>
        <Table
          :columns="[
            { title: '权限码', dataIndex: 'permCode', key: 'permCode', width: 160 },
            { title: '名称', dataIndex: 'permName', key: 'permName', width: 140 },
            { title: '方法', dataIndex: 'apiMethod', key: 'apiMethod', width: 80 },
            { title: '路径', dataIndex: 'apiPath', key: 'apiPath', ellipsis: true },
            { title: '所属菜单', key: 'parentName', width: 120 },
            { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
            { title: '操作', key: 'action', width: 120, fixed: 'right' },
          ]"
          :data-source="apiList"
          :loading="apiLoading"
          row-key="id"
          :pagination="{
            current: apiPage,
            pageSize: apiPageSize,
            total: apiTotal,
            showSizeChanger: true,
            showTotal: (t: number) => `共 ${t} 条`,
          }"
          @change="(pag: any) => { apiPage = pag.current; apiPageSize = pag.pageSize; loadApiList(); }"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'parentName'">
              {{ menuIdToName[record.parentId] ?? '-' }}
            </template>
            <template v-else-if="column.key === 'status'">
              <Tag :color="record.status === 'ENABLED' ? 'green' : 'red'">
                {{ record.status === 'ENABLED' ? '启用' : '禁用' }}
              </Tag>
            </template>
            <template v-else-if="column.key === 'action'">
              <Space>
                <Button v-if="hasPerm('iam:perm:update')" type="link" size="small" @click="openDrawerEdit('API', record)">编辑</Button>
                <Button v-if="hasPerm('iam:perm:delete')" type="link" size="small" danger @click="handleDelete(record, 'API')">删除</Button>
              </Space>
            </template>
          </template>
        </Table>
      </Tabs.TabPane>
    </Tabs>

    <Drawer @success="handleDrawerSuccess" />
  </Page>
</template>
