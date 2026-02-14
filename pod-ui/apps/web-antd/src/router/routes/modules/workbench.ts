import type { RouteRecordRaw } from 'vue-router';

import { BasicLayout } from '#/layouts';
import { $t } from '#/locales';

const routes: RouteRecordRaw[] = [
  {
    component: BasicLayout,
    meta: {
      icon: 'lucide:briefcase',
      order: 9999,
      title: 'Workbench',
    },
    name: 'Workbench',
    path: '/workbench',
    children: [
      {
        name: 'WorkbenchIndex',
        path: '/workbench/index',
        component: () => import('#/views/workbench/index.vue'),
        meta: {
          title: '工作台',
          icon: 'lucide:briefcase',
        },
      },
    ],
  },
];

export default routes;
