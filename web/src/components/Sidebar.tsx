import { SystemStatus } from "@/components/SystemStatus"
import { cn } from "@/lib/utils"
import { NAV, type Section } from "@/lib/sections"
import icon from "./images/argus-logo.svg"

export function Sidebar({
  section,
  onChange,
}: {
  section: Section
  onChange: (s: Section) => void
}) {
  return (
    <aside className="sticky top-6 flex h-[calc(100vh-3rem)] w-60 shrink-0 flex-col gap-6">
      <div className="flex items-center gap-3 px-2">
        <img src={icon} alt="ARGUS" className="size-10 w-full" />
        {/* <div>
          <p className="text-xl font-bold tracking-wide">ARGUS</p>
          <p className="text-xs text-muted-foreground">Human in Space</p>
        </div> */}
      </div>

      <nav className="flex flex-col gap-3 rounded-2xl border border-white/10 bg-black/40 p-4 backdrop-blur-md">
        {NAV.map((item) => (
          <button
            key={item.id}
            onClick={() => onChange(item.id)}
            className={cn(
              "h-12 rounded-lg border text-sm font-medium transition",
              section === item.id
                ? "border-transparent bg-linear-to-b from-amber-500 to-orange-700 text-white"
                : "border-white/20 text-foreground/80 hover:bg-white/10",
            )}
          >
            {item.label}
          </button>
        ))}
      </nav>

      <SystemStatus className="pointer-events-none mt-auto select-none" />
    </aside>
  )
}