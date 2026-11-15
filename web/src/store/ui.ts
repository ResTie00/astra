import { create } from "zustand"
import type { ReplanResponse } from "@/types"



interface UiState {
  selectedCrewId: string
  selectedOptionId: string | null
  appliedOptionId: string | null
  replanResult: ReplanResponse | null
  
  setCrew: (id: string) => void
  selectOption: (id: string | null) => void
  applyOption: (id: string) => void
  setReplanResult: (result: ReplanResponse | null) => void
  viewMode: "onboard" | "earth"
setViewMode: (m: "onboard" | "earth") => void
}

export const useUi = create<UiState>((set) => ({
  selectedCrewId: "c2",
  selectedOptionId: null,
  appliedOptionId: null,
  replanResult: null,
  setCrew: (id) => set({ selectedCrewId: id }),
  selectOption: (id) => set({ selectedOptionId: id }),
  applyOption: (id) => set({ appliedOptionId: id }),
  setReplanResult: (result) =>
    set({ replanResult: result, selectedOptionId: null, appliedOptionId: null }),
  viewMode: "onboard",
setViewMode: (m) => set({ viewMode: m }),
}))