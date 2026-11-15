import { request } from "./client"
import type { Trends } from "@/types"

export const getTrends = (crewId: string) => request<Trends>(`/crew/${crewId}/trends`)