const dtf = new Intl.DateTimeFormat("ru-RU", {
  day: "2-digit",
  month: "short",
  hour: "2-digit",
  minute: "2-digit",
  timeZone: "UTC",
})

// оставляем максимум 3 знака после секунды
const trim = (iso: string) => iso.replace(/(\.\d{3})\d+/, "$1")

export const formatDateTime = (iso: string) => `${dtf.format(new Date(trim(iso)))} UTC`

export const formatPercent = (x: number) => `${Math.round(x * 100)}%`