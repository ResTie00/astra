import { HttpError, request } from "./client"
import type { SpaceWeather } from "@/types"

export async function getSpaceWeather() {
  try {
    const data = await request<SpaceWeather | undefined>("/space-weather/current")
    return data ?? null
  } catch (e) {
    if (e instanceof HttpError && e.status === 404) return null
    throw e
  }
}