import { useCrew } from "@/hooks/useCrew"
import { cn } from "@/lib/utils"

const STATES = {
  online: { dot: "bg-emerald-400 shadow-emerald-400", title: "Systems Online", text: "All systems nominal" },
  offline: { dot: "bg-red-500 shadow-red-500", title: "Connection Lost", text: "Showing last known data" },
  connecting: { dot: "bg-yellow-400 shadow-yellow-400", title: "Connecting…", text: "Waiting for API" },
}

export function SystemStatus({ className }: { className?: string }) {
  const { isLoading, isError } = useCrew()
  const key = isLoading ? "connecting" : isError ? "offline" : "online"
  const s = STATES[key]

  return (
    <div className={cn("flex items-center gap-3 rounded-2xl border border-white/10 bg-white/5 px-4 py-3", className)}>
      <span className={cn("size-3 shrink-0 rounded-full shadow-[0_0_10px]", s.dot)} />
      <div>
        <p className="text-sm font-semibold">{s.title}</p>
        <p className="text-xs text-muted-foreground">{s.text}</p>
      </div>
    </div>
  )
}