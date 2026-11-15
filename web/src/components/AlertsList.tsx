import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import { Skeleton } from "@/components/ui/skeleton"
import { useAckAlert } from "@/hooks/useAckAlert"
import { useAlerts } from "@/hooks/useAlerts"
import { formatDateTime } from "@/lib/format"
import { cn } from "@/lib/utils"
import { useAcked } from "@/store/alerts"
import type { Severity } from "@/types"

const SEVERITY_STYLE: Record<Severity, string> = {
  CRITICAL: "bg-red-600 text-white",
  WARNING: "bg-yellow-500 text-black",
  INFO: "bg-blue-600 text-white",
}

export function AlertsList() {
  const { data, isLoading, isError } = useAlerts()
  const acked = useAcked((s) => s.ids)
  const ack = useAckAlert()

  if (isLoading) return <Skeleton className="h-40 w-full" />
  if (isError) return <Card className="p-4 text-sm text-red-500">Не удалось загрузить тревоги</Card>

  const list = [...(data ?? [])].sort((a, b) => b.createdAt.localeCompare(a.createdAt))

  return (
    <Card className="gap-3 p-4">
      <h3 className="font-semibold">Тревоги и сообщения</h3>
      {list.length === 0 ? (
        <p className="text-sm text-muted-foreground">Тревог нет</p>
      ) : (
        <ul className="divide-y divide-white/10">
          {list.map((a) => {
            const isAcked = acked.includes(a.id)
            return (
              <li
                key={a.id}
                className={cn(
                  "flex flex-wrap items-center justify-between gap-3 py-3",
                  isAcked && "opacity-50",
                )}
              >
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <Badge className={SEVERITY_STYLE[a.severity]}>{a.severity}</Badge>
                    <span className="text-xs text-muted-foreground">
                      {a.crewId ?? "весь экипаж"} · {formatDateTime(a.createdAt)}
                    </span>
                  </div>
                  <p className="text-sm">{a.message}</p>
                </div>
                {isAcked ? (
                  <Badge variant="outline">Accepted</Badge>
                ) : (
                  <Button
                    size="sm"
                    variant="outline"
                    disabled={ack.isPending && ack.variables === a.id}
                    onClick={() => ack.mutate(a.id)}
                  >
                      Accept
                  </Button>
                )}
              </li>
            )
          })}
        </ul>
      )}
    </Card>
  )
}