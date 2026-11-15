import { User } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import { Skeleton } from "@/components/ui/skeleton"
import { StatusBadge } from "@/components/StatusBadge"
import { useCrew } from "@/hooks/useCrew"
import { useAuth } from "@/store/auth"
import { useUi } from "@/store/ui"

export function LoginPage() {
  const { data: crew, isLoading, isError } = useCrew()
  const login = useAuth((s) => s.login)
  const setCrew = useUi((s) => s.setCrew)

  return (
    <div className="flex min-h-screen items-center justify-center p-6">
      <Card className="w-full max-w-md gap-4 p-6">
        <div className="space-y-1">
          <h1 className="text-2xl font-bold">Astra</h1>
          <p className="text-sm text-muted-foreground">Выбери, кто ты</p>
        </div>

        {isLoading && <Skeleton className="h-40 w-full" />}
        {isError && <p className="text-sm text-red-500">Нет связи с API</p>}

        <div className="space-y-2">
          {crew?.map((c) => (
            <Button
              key={c.crewId}
              variant="outline"
              className="h-12 w-full justify-between"
              onClick={() => {
                login({ crewId: c.crewId, name: c.name })
                setCrew(c.crewId)
              }}
            >
              <span className="flex items-center gap-3">
                <User /> {c.name}
              </span>
              <StatusBadge status={c.overall} />
            </Button>
          ))}
        </div>
      </Card>
    </div>
  )
}