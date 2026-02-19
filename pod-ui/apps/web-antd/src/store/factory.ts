import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import { useUserStore } from '@vben/stores';

interface FactoryState {
  currentFactoryId: number | null;
  factoryIds: number[];
}

export const useFactoryStore = defineStore('factory', () => {
  const userStore = useUserStore();
  
  const currentFactoryId = ref<number | null>(null);
  const factoryIds = ref<number[]>([]);

  // Persistent key based on user to isolate settings
  const storageKey = computed(() => {
    return `factory-settings-${userStore.userInfo?.userId || 'guest'}`;
  });

  function setFactoryIds(ids: number[]) {
    factoryIds.value = ids;
  }

  function setCurrentFactoryId(id: number) {
    currentFactoryId.value = id;
    // Persist to localStorage
    try {
        localStorage.setItem(storageKey.value, JSON.stringify({ currentFactoryId: id }));
    } catch (e) {
        console.error('Failed to persist factory settings', e);
    }
  }

  function initFactorySettings(defaultId?: number) {
    // Try to load from localStorage
    try {
        const stored = localStorage.getItem(storageKey.value);
        if (stored) {
            const parsed = JSON.parse(stored);
            if (parsed.currentFactoryId && factoryIds.value.includes(parsed.currentFactoryId)) {
                currentFactoryId.value = parsed.currentFactoryId;
                return;
            }
        }
    } catch (e) {
        // ignore
    }

    // Fallback to default
    if (defaultId && factoryIds.value.includes(defaultId)) {
        currentFactoryId.value = defaultId;
    } else if (factoryIds.value.length > 0) {
        currentFactoryId.value = factoryIds.value[0];
    }
  }
  
  function clear() {
      currentFactoryId.value = null;
      factoryIds.value = [];
  }

  /** 供 resetAllStores 调用（setup 语法 store 无 $reset） */
  function reset() {
    clear();
  }

  return {
    currentFactoryId,
    factoryIds,
    setFactoryIds,
    setCurrentFactoryId,
    initFactorySettings,
    clear,
    reset,
  };
});
