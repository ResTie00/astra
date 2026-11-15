import { request } from "./client"
import type { CrewStatus } from "@/types"

export const getCrew = () => request<CrewStatus[]>("/crew")