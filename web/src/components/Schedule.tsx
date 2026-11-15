import { Card } from "@/components/ui/card"
import { Skeleton } from "@/components/ui/skeleton"
import { useSchedule } from "@/hooks/useSchedule"
import { formatDateTime } from "@/lib/format"

export function Schedule() {
  const { data, isLoading, isError } = useSchedule()

  if (isLoading) return <Skeleton className="h-40 w-full" />
  if (isError) return <Card className="p-4 text-sm text-red-500">Не удалось загрузить расписание</Card>

  const list = [...(data?.assignments ?? [])].sort((a, b) => a.start.localeCompare(b.start))

  return (
    <Card className="gap-2 p-4">
      <h3 className="font-semibold">Расписание</h3>
      {list.length === 0 ? (
        <p className="text-sm text-muted-foreground">Задач нет</p>
      ) : (
        <ul className="divide-y text-sm">
          {list.map((a) => (
            <li key={a.taskId} className="flex flex-wrap justify-between gap-2 py-2">
              <span>
                {a.taskName} <span className="text-muted-foreground">· {a.crewName}</span>
              </span>
              <span className="tabular-nums text-muted-foreground">
                {formatDateTime(a.start)} → {formatDateTime(a.end)}
              </span>
            </li>
          ))}
        </ul>
      )}
    </Card>
  )
}