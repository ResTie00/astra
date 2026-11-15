import type { ReactNode } from "react"
import { cn } from "@/lib/utils"

export function Pill({
  active,
  onClick,
  children,
}: {
  active: boolean
  onClick: () => void
  children: ReactNode
}) {
  return (
    <button
      onClick={onClick}
      className={cn(
        "rounded-full border px-3 py-1 text-xs transition",
        active
          ? "border-orange-500 bg-orange-500/20 text-orange-300"
          : "border-white/20 text-muted-foreground hover:text-foreground",
      )}
    >
      {children}
    </button>
  )
}