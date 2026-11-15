import { request } from "./client"
import type { Assignment } from "@/types"

export const getSchedule = () => request<{ assignments: Assignment[] }>("/schedule")