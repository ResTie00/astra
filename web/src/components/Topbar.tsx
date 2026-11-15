import { useEffect, useState } from "react"
import { cn } from "@/lib/utils"
import { useUi } from "@/store/ui"
import type { Status } from "@/types"

const SUBTITLE: Record<Status, string> = {
  NORMAL: "Your health status is stable and within personal norms.",
  WATCH: "Some indicators need attention. Review the details below.",
  ACT: "Action required. Check the recommendations below.",
}

function greeting(hour: number) {
  if (hour < 12) return "Good morning"
  if (hour < 18) return "Good afternoon"
  return "Good evening"
}

export function TopBar({
  name,
  overall,
  onAvatar,
}: {
  name: string
  overall: Status
  onAvatar: () => void
}) {
  const viewMode = useUi((s) => s.viewMode)
  const [now, setNow] = useState(() => new Date())

  useEffect(() => {
    const t = setInterval(() => setNow(new Date()), 1000)
    return () => clearInterval(t) // без этого таймер жил бы после ухода со страницы
  }, [])

  const delayed = viewMode === "earth"
  const shown = new Date(now.getTime() - (delayed ? 20 * 60_000 : 0))
  const time = shown.toISOString().slice(11, 16) // "12:42"
  const firstName = name.split(" ")[0]

  return (
    <header className="flex items-center justify-between gap-4">
      <div className="border-l-2 border-orange-500 pl-4">
        <h1 className="text-2xl font-semibold">
          {greeting(now.getHours())}, {firstName}
        </h1>
        <p className="text-sm text-muted-foreground">{SUBTITLE[overall]}</p>
      </div>

      <div className="flex items-center gap-4">
        <div className="flex items-center gap-2 rounded-full border border-white/15 bg-black/40 px-4 py-2 text-sm">
          <span
            className={cn(
              "size-2 rounded-full",
              delayed ? "bg-amber-400" : "animate-pulse bg-emerald-400",
            )}
          />
          {delayed ? "Delayed" : "Live"}
          <span className="tabular-nums">{time} UTC</span>
        </div>
        <button
          onClick={onAvatar}
          aria-label="Settings"
          className="flex size-14 items-center justify-center rounded-full bg-white/90 text-xl font-semibold text-black"
        >
          {firstName[0]}
        </button>
      </div>
    </header>
  )
}