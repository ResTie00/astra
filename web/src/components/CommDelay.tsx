import { Card } from "@/components/ui/card"
import { Pill } from "@/components/Pill"
import { useUi } from "@/store/ui"

export function CommDelay() {
  const mode = useUi((s) => s.viewMode)
  const setMode = useUi((s) => s.setViewMode)

  return (
    <Card className="items-center gap-3 p-5 text-center">
      <h3 className="text-sm font-semibold">Communication Delay</h3>
      <p className="text-xs text-muted-foreground">Earth view is delayed by ~20 min</p>
      <div className="flex gap-2">
        <Pill active={mode === "onboard"} onClick={() => setMode("onboard")}>
          Onboard
        </Pill>
        <Pill active={mode === "earth"} onClick={() => setMode("earth")}>
          Earth
        </Pill>
      </div>
    </Card>
  )
}