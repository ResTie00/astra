import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group"
import { useCrew } from "@/hooks/useCrew"
import { useUi } from "@/store/ui"

export function CrewSwitcher() {
  const { data: crew } = useCrew()
  const selected = useUi((s) => s.selectedCrewId)
  const setCrew = useUi((s) => s.setCrew)

  return (
    <ToggleGroup
      type="single"
      variant="outline"
      value={selected}
      onValueChange={(v) => v && setCrew(v)}
      className="ml-auto"
    >
      {crew?.map((c) => (
        <ToggleGroupItem key={c.crewId} value={c.crewId}>
          {c.name}
        </ToggleGroupItem>
      ))}
    </ToggleGroup>
  )
}