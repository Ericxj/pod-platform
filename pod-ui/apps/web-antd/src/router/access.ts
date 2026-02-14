import type {
  ComponentRecordType,
  GenerateMenuAndRoutesOptions,
} from '@vben/types';

import { generateAccessible } from '@vben/access';
import { preferences } from '@vben/preferences';
import { useAccessStore } from '@vben/stores';

import { message } from 'ant-design-vue';

import { getUserMenusApi } from '#/api';
import { BasicLayout, IFrameView } from '#/layouts';
import { $t } from '#/locales';

const forbiddenComponent = () => import('#/views/_core/fallback/forbidden.vue');
const NotImplemented = () => import('#/views/_core/fallback/not-implemented.vue');

function resolveComponentFallback(menus: any[], pageMap: ComponentRecordType) {
  menus.forEach((menu) => {
    // 递归处理子菜单
    if (menu.children && menu.children.length > 0) {
      resolveComponentFallback(menu.children, pageMap);
    }
    
    // 1. 确保 path 以 / 开头
    if (menu.path && !menu.path.startsWith('/') && !menu.path.startsWith('http')) {
        menu.path = '/' + menu.path;
    }

    // 2. 如果是目录（有 children），强制 component 为 BasicLayout
    if (menu.children && menu.children.length > 0) {
        if (!menu.component || menu.component.toUpperCase() === 'LAYOUT') {
            menu.component = 'BasicLayout';
        }
        return;
    }
    
    // 3. 处理叶子节点组件
    // Skip Layouts and specialized components
    if (
      menu.component && 
      menu.component !== 'BasicLayout' && 
      menu.component !== 'IFrameView' &&
      menu.component.toUpperCase() !== 'LAYOUT'
    ) {
      // Normalize path: remove leading slash for lookup
      const cleanPath = menu.component.startsWith('/') ? menu.component.slice(1) : menu.component;
      const key = `../views/${cleanPath}.vue`;
      const indexKey = `../views/${cleanPath}/index.vue`;
      
      if (!pageMap[key] && !pageMap[indexKey]) {
        console.warn(`[Route Fallback] Component not found in map: ${menu.component}. Trying to guess from path...`);
        // Try to guess from route path if component path failed
        guessComponentFromPath(menu, pageMap);
      }
    } else if (!menu.component && !menu.children) {
      // Missing component for leaf node -> Guess from path
      guessComponentFromPath(menu, pageMap);
    }
  });
}

function guessComponentFromPath(menu: any, pageMap: ComponentRecordType) {
   if (!menu.path) return;
   
   const cleanPath = menu.path.startsWith('/') ? menu.path.slice(1) : menu.path;
   
   // Strategy 1: path/index.vue
   const tryIndex = `../views/${cleanPath}/index.vue`;
   // Strategy 2: path.vue
   const tryFile = `../views/${cleanPath}.vue`;
   
   if (pageMap[tryIndex]) {
      menu.component = `${cleanPath}/index`;
      console.log(`[Route Fallback] Guessed component: ${menu.component}`);
   } else if (pageMap[tryFile]) {
      menu.component = cleanPath;
      console.log(`[Route Fallback] Guessed component: ${menu.component}`);
   } else {
       console.error(`[Route Fallback] FAILED to resolve component for ${menu.path}. Marking as NotImplemented.`);
       menu.component = 'NotImplemented';
   }
}

async function generateAccess(options: GenerateMenuAndRoutesOptions) {
  const pageMap: ComponentRecordType = import.meta.glob('../views/**/*.vue');

  const layoutMap: ComponentRecordType = {
    BasicLayout,
    IFrameView,
    NotImplemented,
  };

  return await generateAccessible(preferences.app.accessMode, {
    ...options,
    fetchMenuListAsync: async () => {
      message.loading({
        content: `${$t('common.loadingMenu')}...`,
        duration: 1.5,
      });
      // Fetch menus AND permissions together
      const result = await getUserMenusApi();
      
      // Safety check
      if (!result) {
          console.error('[GenerateAccess] Failed to fetch menus: result is empty');
          return [];
      }

      // Store permissions
      const accessStore = useAccessStore();
      const codes = result.permissionCodes || [];
      accessStore.setAccessCodes(codes);
      
      const menus = result.menus || [];
      console.log('[GenerateAccess] Raw menus from backend:', JSON.parse(JSON.stringify(menus)));
      
      if (menus.length === 0) {
          console.warn('[GenerateAccess] No menus returned from backend. Check user permissions.');
          // TODO: Inject default workbench if needed, or rely on static routes
      }

      // Apply fallback logic
      resolveComponentFallback(menus, pageMap);
      
      console.log('[GenerateAccess] Processed menus:', JSON.parse(JSON.stringify(menus)));

      return menus;
    },
    // 可以指定没有权限跳转403页面
    forbiddenComponent,
    // 如果 route.meta.menuVisibleWithForbidden = true
    layoutMap,
    pageMap,
  });
}

export { generateAccess };
