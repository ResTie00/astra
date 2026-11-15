const BASE = import.meta.env.VITE_API_URL as string

export class HttpError extends Error {
  status: number
  constructor(status: number) {
    super(`HTTP ${status}`)
    this.status = status
  }
}

export async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...init,
  })
  if (!res.ok) throw new HttpError(res.status)
  const text = await res.text()
  return (text ? JSON.parse(text) : undefined) as T
}
