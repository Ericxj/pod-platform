/**
 * 纯后端动态菜单：/api/iam/me/menus 菜单树 -> RouteRecordRaw -> router.addRoute
 * component 解析：LAYOUT -> Layout(基础布局)；其他 -> views 下 .vue 动态加载
 */
import type {
  ComponentRecordType,
  GenerateMenuAndRoutesOptions,
} from '@vben/types';

import { generateAccessible } from '@vben/access';
import { preferences } from '@vben/preferences';
import { useAccessStore } from '@vben/stores';

import { message } from 'ant-design-vue';

import { getMenusApi, getPermsApi } from '#/api';
import { BasicLayout, IFrameView } from '#/layouts';
import { $t } from '#/locales';

const forbiddenComponent = () => import('#/views/_core/fallback/forbidden.vue');

/** 与 @vben/utils generate-routes-backend 中 normalizeViewPath 一致，用于匹配 pageMap 键 */
function normalizeViewPathForMatch(path: string): string {
  const normalizedPath = path.replace(/^(\.\/|\.\.\/)+/, '');
  const viewPath = normalizedPath.startsWith('/') ? normalizedPath : `/${normalizedPath}`;
  return viewPath.replace(/^\/views/, '');
}

/**
 * 归一化后端 component：
 * - trim
 * - LAYOUT -> 'Layout'
 * - 去掉前导 '/'：'/system/user/index' -> 'system/user/index'
 */
export function normalizeComponent(raw: string | undefined): string {
  if (!raw || typeof raw !== 'string') return raw ?? '';
  const t = raw.trim();
  if (t.toUpperCase() === 'LAYOUT') return 'Layout';
  return t.startsWith('/') ? t.slice(1) : t;
}

/**
 * 解析视图 component：用 import.meta.glob 的 pageMap 精确匹配，找不到则抛出含期望 key 的错误
 * 支持 ${comp}.vue 与 ${comp}/index.vue（与 generate-routes-backend 查找的 key 一致）
 */
export function resolveView(
  component: string,
  pageMap: ComponentRecordType,
  raw?: string,
): void {
  const normalized = normalizeComponent(component || raw);
  if (!normalized) return;

  const expectedKey = `${normalized.endsWith('.vue') ? normalized : `${normalized}.vue`}`;
  const expectedNormalized = expectedKey.startsWith('/') ? expectedKey : `/${expectedKey}`;

  const normalizedToOriginal: Record<string, string> = {};
  for (const key of Object.keys(pageMap)) {
    const n = normalizeViewPathForMatch(key);
    normalizedToOriginal[n] = key;
  }

  if (import.meta.env.DEV && Object.keys(normalizedToOriginal).length > 0) {
    const sample = Object.keys(normalizedToOriginal).slice(0, 3).join(', ');
    console.info('[Access] pageMap sample keys (normalized):', sample);
  }

  if (normalizedToOriginal[expectedNormalized]) return;
  const withIndex = expectedNormalized.replace(/\.vue$/, '/index.vue');
  if (normalizedToOriginal[withIndex]) return;

  const expectedKeys = `"${expectedNormalized}" or "${withIndex}" (views path: ${normalized})`;
  throw new Error(
    `View component not found: ${raw ?? component} -> ${normalized} (expected key: ${expectedKeys})`,
  );
}

/**
 * 菜单树 -> RouteRecordRaw 前的 component 解析（与 generate-routes-backend 的 pageMap 键一致）
 */
function resolveComponentFallback(menus: any[], pageMap: ComponentRecordType) {
  menus.forEach((menu) => {
    if (menu.children && menu.children.length > 0) {
      resolveComponentFallback(menu.children, pageMap);
    }

    if (menu.path && !menu.path.startsWith('/') && !menu.path.startsWith('http')) {
      menu.path = '/' + menu.path;
    }

    const raw = menu.component;
    const normalized = normalizeComponent(raw ?? '');

    if (menu.children && menu.children.length > 0) {
      menu.component = !normalized || normalized === 'Layout' ? 'Layout' : normalized;
      return;
    }

    if (normalized === 'Layout' || normalized === 'IFrameView') return;

    if (normalized) {
      try {
        resolveView(normalized, pageMap, raw);
        menu.component = normalized;
      } catch (e) {
        throw e;
      }
      return;
    }

    const cleanPath = menu.path ? (menu.path.startsWith('/') ? menu.path.slice(1) : menu.path) : '';
    const tryIndex = `${cleanPath}/index`;
    const tryFile = cleanPath;
    if (tryIndex) {
      try {
        resolveView(tryIndex, pageMap);
        menu.component = tryIndex;
        return;
      } catch {
        // ignore
      }
    }
    if (tryFile) {
      try {
        resolveView(tryFile, pageMap);
        menu.component = tryFile;
        return;
      } catch {
        // ignore
      }
    }
    throw new Error(`View component not found for path: ${menu.path} (tried: ${tryIndex}, ${tryFile})`);
  });
}

async function generateAccess(options: GenerateMenuAndRoutesOptions) {
  const pageMap: ComponentRecordType = import.meta.glob('../views/**/*.vue');

  const layoutMap: ComponentRecordType = {
    Layout: BasicLayout,
    BasicLayout,
    IFrameView,
  };

  return await generateAccessible(preferences.app.accessMode, {
    ...options,
    fetchMenuListAsync: async () => {
      message.loading({
        content: `${$t('common.loadingMenu')}...`,
        duration: 1.5,
      });
      const accessStore = useAccessStore();

      if (import.meta.env.DEV) console.info('[Access] Fetching perms...');
      let codes: string[] = [];
      try {
        codes = await getPermsApi();
        accessStore.setAccessCodes(codes);
        if (import.meta.env.DEV) console.info('[Access] perms count:', codes.length);
      } catch (e) {
        console.error('[Access] getPermsApi failed', e);
      }

      if (import.meta.env.DEV) console.info('[Access] Fetching menus...');
      let menus: any[] = [];
      try {
        menus = await getMenusApi();
      } catch (e) {
        console.error('[Access] getMenusApi failed', e);
        return [];
      }

      if (menus.length === 0) {
        if (import.meta.env.DEV) console.warn('[Access] No menus from backend.');
        return [];
      }

      try {
        resolveComponentFallback(menus, pageMap);
      } catch (e) {
        console.error('[Access] resolveComponentFallback failed:', e);
        throw e;
      }
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
