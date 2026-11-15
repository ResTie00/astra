import { Badge } from "@/components/ui/badge"
import { Card } from "@/components/ui/card"
import type { CrewStatus } from "@/types"

export function ActionCard({ action }: { action: CrewStatus["action"] }) {
  return (
    <Card className="gap-2 p-4 min-w-[280px] h-[100px] mt-auto">
      <div className="flex items-center justify-between">
        <h3 className="font-semibold">Рекомендация</h3>
        <Badge variant="outline">{action.type}</Badge>
      </div>
      <p className="text-sm">{action.text}</p>
    </Card>
  )
}