import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

// Config
const CONFIG = {
  apiBase: process.env.VITE_API_BASE_URL || 'http://localhost:8080',
  viewsDir: path.resolve(process.cwd(), 'src/views'),
  reportFile: path.resolve(process.cwd(), 'reports/menu-audit-report.json'),
  tokenFile: path.resolve(process.cwd(), '.cache/dev-token.txt'),
};

// Types
interface Menu {
    path?: string;
    name?: string;
    component?: string;
    children?: Menu[];
    meta?: {
        title?: string;
        [key: string]: any;
    };
    [key: string]: any;
}

interface ComponentInfo {
    componentRaw: string;
    componentPath: string; // Normalized
    menuTitle: string;
    menuName: string;
    routePath: string;
    targetFile: string;
    reason?: string;
}

interface Report {
    timestamp: string;
    summary: {
        backendCount: number;
        frontendCount: number;
        missing: number;
        orphan: number;
        ignored: number;
        invalid: number;
        conflicts: number;
    };
    missing: ComponentInfo[];
    orphan: { file: string }[];
    ignored: ComponentInfo[];
    invalid: ComponentInfo[];
    conflicts: {
        path: any[];
        name: any[];
        component: any[];
    };
}

// Utils
function getArgs() {
  const args: Record<string, string> = {};
  process.argv.slice(2).forEach((arg) => {
    if (arg.startsWith('--')) {
      const parts = arg.slice(2).split('=');
      const key = parts[0];
      const value = parts[1] || 'true';
      args[key] = value;
    }
  });
  return args;
}

function getToken(args: Record<string, string>): string {
  if (args.mock) return 'mock-token'; // Skip token check in mock mode
  if (args.token) return args.token;
  if (fs.existsSync(CONFIG.tokenFile)) {
    const token = fs.readFileSync(CONFIG.tokenFile, 'utf-8').trim();
    if (token) return token;
  }
  throw new Error('Token not found. Provide --token=... or create .cache/dev-token.txt');
}

// P0: Normalization
function normalizeComponentPath(raw: string): string {
    if (!raw) return '';
    let normalized = raw.trim();
    
    // Remove prefixes
    if (normalized.startsWith('@/views/')) normalized = normalized.replace('@/views/', '');
    else if (normalized.startsWith('views/')) normalized = normalized.replace('views/', '');
    
    // Ensure leading slash
    if (!normalized.startsWith('/')) normalized = '/' + normalized;
    
    // Remove extension
    if (normalized.endsWith('.vue')) normalized = normalized.replace('.vue', '');
    
    return normalized; // e.g. /oms/order/list
}

// P0: Classification
function classifyComponent(raw: string): 'VALID' | 'IGNORED' | 'INVALID' {
    if (!raw) return 'IGNORED';
    const lower = raw.toLowerCase();
    
    // Ignored cases
    if (
        lower === 'layout' || 
        lower === 'iframeview' || 
        lower === 'iframe' ||
        lower === 'external' ||
        raw.startsWith('http') ||
        raw.startsWith('/_core') ||
        raw.startsWith('/_private')
    ) {
        return 'IGNORED';
    }

    // Invalid cases
    if (raw.includes('..') || raw.includes('\\') || raw.includes(':')) {
        return 'INVALID';
    }

    return 'VALID';
}

// File System
function getAllVueFiles(dir: string, fileList: string[] = [], rootDir: string = dir): string[] {
  if (!fs.existsSync(dir)) return [];
  const files = fs.readdirSync(dir);
  files.forEach((file) => {
    const filePath = path.join(dir, file);
    const stat = fs.statSync(filePath);
    if (stat.isDirectory()) {
      if (file !== '_core' && file !== '_fallback') {
         getAllVueFiles(filePath, fileList, rootDir);
      } else {
         getAllVueFiles(filePath, fileList, rootDir);
      }
    } else {
      if (file.endsWith('.vue')) {
        const relativePath = path.relative(rootDir, filePath).replace(/\\/g, '/').replace(/\.vue$/, '');
        fileList.push('/' + relativePath);
      }
    }
  });
  return fileList;
}

// API
async function fetchMenus(token: string, isMock = false) {
  if (isMock) {
    console.log('Using MOCK menu data...');
    return [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: '/dashboard/analytics/index',
        meta: { title: '仪表盘' }
      },
      {
        path: '/oms/orders',
        name: 'OmsOrderList',
        component: '/oms/orders/index',
        meta: { title: '订单列表' }
      },
      {
        path: '/oms/missing',
        name: 'MissingPage',
        component: '/oms/missing/page',
        meta: { title: '缺失页面示例' }
      },
      {
         path: '/invalid',
         name: 'InvalidPage',
         component: '../invalid/path',
         meta: { title: '非法路径示例' }
      },
      {
          path: '/conflict1',
          name: 'ConflictSamePath',
          component: '/common/page',
          meta: { title: '冲突示例1' }
      },
      {
          path: '/conflict1',
          name: 'ConflictSamePath2',
          component: '/common/page',
          meta: { title: '冲突示例2' }
      }
    ];
  }

  const url = `${CONFIG.apiBase}/api/iam/me/menus`;
  console.log(`Fetching menus from ${url}...`);
  try {
    const response = await fetch(url, {
      headers: {
        Authorization: token.startsWith('Bearer') ? token : `Bearer ${token}`,
      },
    });
    if (!response.ok) {
        throw new Error(`API Error: ${response.status} ${response.statusText}`);
    }
    const data = await response.json();
    return data.code === 0 || data.code === 200 ? (data.data || data) : data;
  } catch (error) {
    console.error('Failed to fetch menus:', error);
    process.exit(1);
  }
}

// Processing
function processMenus(
    menus: Menu[], 
    validComponents: Map<string, ComponentInfo>,
    report: Report,
    seen: { paths: Map<string, Menu>, names: Map<string, Menu>, components: Map<string, Menu[]> }
) {
  menus.forEach((menu) => {
    const menuTitle = menu.meta?.title || 'Untitled';
    const menuName = menu.name || 'Unnamed';
    const routePath = menu.path || 'NoPath';
    const componentRaw = menu.component || '';

    // P0: Conflict Detection
    if (menu.path) {
        if (seen.paths.has(menu.path)) {
            report.conflicts.path.push({
                value: menu.path,
                first: seen.paths.get(menu.path)?.name,
                second: menuName
            });
        } else {
            seen.paths.set(menu.path, menu);
        }
    }

    if (menu.name) {
        if (seen.names.has(menu.name)) {
            report.conflicts.name.push({
                value: menu.name,
                first: seen.names.get(menu.name)?.path,
                second: routePath
            });
        } else {
            seen.names.set(menu.name, menu);
        }
    }

    // Component Processing
    const type = classifyComponent(componentRaw);
    
    const info: ComponentInfo = {
        componentRaw,
        componentPath: normalizeComponentPath(componentRaw),
        menuTitle,
        menuName,
        routePath,
        targetFile: '',
        reason: ''
    };
    
    // Conflict for component usage
    if (info.componentPath && type === 'VALID') {
         if (!seen.components.has(info.componentPath)) {
             seen.components.set(info.componentPath, []);
         }
         seen.components.get(info.componentPath)?.push(menu);
    }

    if (type === 'IGNORED') {
        info.reason = 'Ignored Pattern';
        report.ignored.push(info);
    } else if (type === 'INVALID') {
        info.reason = 'Contains invalid characters';
        report.invalid.push(info);
    } else {
        // VALID
        info.targetFile = path.join(CONFIG.viewsDir, info.componentPath + '.vue');
        validComponents.set(info.componentPath, info);
    }

    if (menu.children && Array.isArray(menu.children)) {
      processMenus(menu.children, validComponents, report, seen);
    }
  });
}

// Main
async function main() {
  console.log('Starting Production-Grade Menu Audit...');
  
  try {
    const args = getArgs();
    const token = getToken(args);
    
    // 1. Fetch
    const menuData = await fetchMenus(token, args.mock === 'true');
    const menus = Array.isArray(menuData) ? menuData : (menuData.items || []);
    
    // 2. Initialize Report
    const report: Report = {
        timestamp: new Date().toISOString(),
        summary: {
            backendCount: 0,
            frontendCount: 0,
            missing: 0,
            orphan: 0,
            ignored: 0,
            invalid: 0,
            conflicts: 0
        },
        missing: [],
        orphan: [],
        ignored: [],
        invalid: [],
        conflicts: { path: [], name: [], component: [] }
    };

    const validComponents = new Map<string, ComponentInfo>();
    const seen = {
        paths: new Map<string, Menu>(),
        names: new Map<string, Menu>(),
        components: new Map<string, Menu[]>()
    };

    // 3. Process
    processMenus(menus, validComponents, report, seen);
    
    // Post-process component conflicts
    seen.components.forEach((menuList, compPath) => {
        if (menuList.length > 1) {
            report.conflicts.component.push({
                component: compPath,
                menus: menuList.map(m => ({ name: m.name, path: m.path }))
            });
        }
    });

    report.summary.backendCount = validComponents.size + report.ignored.length + report.invalid.length;
    report.summary.conflicts = report.conflicts.path.length + report.conflicts.name.length + report.conflicts.component.length;

    // 4. Compare with Frontend
    const frontendFiles = new Set(getAllVueFiles(CONFIG.viewsDir));
    report.summary.frontendCount = frontendFiles.size;

    validComponents.forEach((info, compPath) => {
        if (!frontendFiles.has(compPath)) {
            report.missing.push(info);
        }
    });

    frontendFiles.forEach((file) => {
        if (!validComponents.has(file)) {
             // Optional: Ignore some core directories
             if (!file.startsWith('/_core') && !file.startsWith('/demos')) {
                report.orphan.push({ file });
             }
        }
    });

    report.summary.missing = report.missing.length;
    report.summary.orphan = report.orphan.length;
    report.summary.ignored = report.ignored.length;
    report.summary.invalid = report.invalid.length;

    // 5. Output Console Report
    console.log('\n=== Audit Report ===');
    console.log(`Summary:`);
    console.log(`  Backend Valid Components: ${validComponents.size}`);
    console.log(`  Frontend Views: ${frontendFiles.size}`);
    console.log(`  Missing: ${report.summary.missing}`);
    console.log(`  Orphan: ${report.summary.orphan}`);
    console.log(`  Ignored: ${report.summary.ignored}`);
    console.log(`  Invalid: ${report.summary.invalid}`);
    console.log(`  Conflicts: ${report.summary.conflicts}`);

    if (report.missing.length > 0) {
        console.log('\n[!] Missing Components:');
        report.missing.forEach(c => console.log(`  ${c.componentPath} (for menu: ${c.menuTitle})`));
    }

    if (report.conflicts.component.length > 0 || report.conflicts.path.length > 0 || report.conflicts.name.length > 0) {
        console.log('\n[!] Conflicts Detected: Check report JSON for details.');
    }

    fs.writeFileSync(CONFIG.reportFile, JSON.stringify(report, null, 2));
    console.log(`\nDetailed report saved to ${CONFIG.reportFile}`);

    // 6. Auto Fix
    if (report.missing.length > 0) {
        console.log('\n=== Auto Fixing Missing Components ===');
        let createdCount = 0;
        
        for (const info of report.missing) {
            const fullPath = info.targetFile;
            const dir = path.dirname(fullPath);
            
            if (!fs.existsSync(dir)) {
                fs.mkdirSync(dir, { recursive: true });
            }
            
            if (!fs.existsSync(fullPath)) {
                // P1: Pass Props
                const content = `<script lang="ts" setup>
/**
 * Auto-generated fallback component
 * Original Path: ${info.componentRaw}
 * Menu: ${info.menuTitle} (${info.menuName})
 * Generated at: ${new Date().toISOString()}
 */
import NotImplemented from '#/views/_core/fallback/not-implemented.vue';

const props = {
  title: "${info.menuTitle}",
  componentPath: "${info.componentPath}",
  routePath: "${info.routePath}",
  menuName: "${info.menuName}"
};
</script>

<template>
  <NotImplemented v-bind="props" />
</template>
`;
                fs.writeFileSync(fullPath, content);
                console.log(`  [+] Created: ${info.componentPath}.vue`);
                createdCount++;
            }
        }
        console.log(`Fixed ${createdCount} components.`);
    }

  } catch (error: any) {
    console.error('Error:', error.message);
    process.exit(1);
  }
}

main();
