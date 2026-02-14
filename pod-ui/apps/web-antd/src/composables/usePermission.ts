import { useAccessStore } from '@vben/stores';

export function usePermission() {
  const accessStore = useAccessStore();

  /**
   * Determine whether there is permission
   */
  function hasPermission(value?: string | string[]): boolean {
    if (!value) {
      return true;
    }
    // If accessCodes is undefined/null, default to no permission (safe default)
    // unless you want to allow everything when no codes are present (insecure).
    if (!accessStore.accessCodes) {
      return false;
    }
    
    const codes = Array.isArray(value) ? value : [value];
    
    // Check if any of the required codes are in the user's access codes
    return codes.some(code => accessStore.accessCodes?.includes(code));
  }

  return { hasPermission };
}
