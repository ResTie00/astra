import { request } from "./client"
import type { CrewStatus } from "@/types"

export interface CheckinBody {
  crewId: string
  wellbeing: number
  mood: number
  stress: number
  symptoms: string[]
  reactionMeanMs: number
  reactionLapses: number
}

export const sendCheckin = (body: CheckinBody) =>
  request<{ checkinId: number; status: CrewStatus }>("/checkin", {
    method: "POST",
    body: JSON.stringify({ ...body, timestamp: new Date().toISOString() }),
  })