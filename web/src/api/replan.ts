import { request } from "./client"
import type { AcceptPlanResponse, ReplanResponse } from "@/types"

export const startReplan = (reason?: string) =>
  request<ReplanResponse>("/replan", {
    method: "POST",
    body: JSON.stringify({ reason }),
  })

export const acceptPlan = (runId: string, optionId: string) =>
  request<AcceptPlanResponse>(`/replan/${runId}/accept`, {
    method: "POST",
    body: JSON.stringify({ optionId }),
  })