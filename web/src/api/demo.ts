import { request } from "./client"

export const resetDemo = () => request<void>("/demo/reset", { method: "POST" })