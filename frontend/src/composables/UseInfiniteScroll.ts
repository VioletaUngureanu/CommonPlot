// ============================================================
//  composables/useInfiniteScroll.ts
//  Folosește IntersectionObserver pe un element sentinel
//  plasat după listă — când devine vizibil, încarcă mai mult.
// ============================================================

import { ref, watch, onUnmounted, type Ref } from 'vue'

interface InfiniteScrollOptions {
  onLoadMore: () => Promise<void>
  onPrefetch?: () => Promise<void>
}

export function useInfiniteScroll(
  sentinelRef: Ref<HTMLElement | null>,   // ← ref pe elementul sentinel
  options: InfiniteScrollOptions
) {
  const { onLoadMore, onPrefetch } = options
  const loading    = ref(false)
  const prefetched = ref(false)

  let observer: IntersectionObserver | null = null

  function setupObserver(sentinel: HTMLElement) {
    observer = new IntersectionObserver(
      async (entries) => {
        const entry = entries[0]
        if (!entry?.isIntersecting || loading.value) return

        // Prefetch la prima apariție
        if (onPrefetch && !prefetched.value) {
          prefetched.value = true
          onPrefetch().catch(() => { prefetched.value = false })
        }

        // Load more
        loading.value    = true
        prefetched.value = false
        try {
          await onLoadMore()
        } finally {
          loading.value = false
        }
      },
      {
        root: null,        // viewport ca root
        rootMargin: '200px', // declanșează cu 200px înainte ca sentinel să fie vizibil
        threshold: 0,
      }
    )
    observer.observe(sentinel)
  }

  function teardown() {
    observer?.disconnect()
    observer = null
  }

  watch(sentinelRef, (el) => {
    teardown()
    if (el) setupObserver(el)
  }, { immediate: true })

  onUnmounted(teardown)

  return { loading }
}
