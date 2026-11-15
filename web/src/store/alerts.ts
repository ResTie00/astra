import { create } from "zustand"
import { persist } from "zustand/middleware"

interface AckedState {
  ids: number[]
  markAcked: (id: number) => void
  clear: () => void
}

export const useAcked = create<AckedState>()(
  persist(
    (set) => ({
      ids: [],
      markAcked: (id) => set((s) => (s.ids.includes(id) ? s : { ids: [...s.ids, id] })),
      clear: () => set({ ids: [] }),
    }),
    { name: "acked-alerts" },
  ),
)