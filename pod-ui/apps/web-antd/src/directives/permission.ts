import type { App, Directive } from 'vue';
import { usePermission } from '../composables/usePermission';

const permission: Directive = {
  mounted(el, binding) {
    const { hasPermission } = usePermission();
    const value = binding.value;

    if (!value) return;

    if (!hasPermission(value)) {
      if (el.parentNode) {
        el.parentNode.removeChild(el);
      }
    }
  },
};

export function setupPermissionDirective(app: App) {
  app.directive('permission', permission);
}
