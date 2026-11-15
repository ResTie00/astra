import { request } from "./client"
import type { AlertItem } from "@/types"

export const getAlerts = () => request<AlertItem[]>("/alerts")

export const ackAlert = (id: number) =>
  request<void>(`/alerts/${id}/ack`, { method: "POST" })