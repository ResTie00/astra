import { LogOut } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import { useResetDemo } from "@/hooks/useResetDemo"
import { useAuth } from "@/store/auth"

export function SettingsPanel() {
  const user = useAuth((s) => s.user)
  const logout = useAuth((s) => s.logout)
  const reset = useResetDemo()

  return (
    <div className="space-y-4">
      <Card className="gap-3 p-4">
        <h3 className="font-semibold">Профиль</h3>
        <p className="text-sm text-muted-foreground">
          {user?.name} · {user?.crewId}
        </p>
        <Button variant="outline" className="w-fit" onClick={logout}>
          <LogOut /> Выйти
        </Button>
      </Card>

      <Card className="gap-3 p-4">
        <h3 className="font-semibold">Демо</h3>
        <p className="text-sm text-muted-foreground">
          Сбрасывает данные на сервере у всех, кто сейчас смотрит демо.
        </p>
        <div className="flex items-center gap-3">
          <Button
            variant="destructive"
            disabled={reset.isPending}
            onClick={() => {
              if (window.confirm("Сбросить демо-данные на сервере?")) reset.mutate()
            }}
          >
            {reset.isPending ? "Сбрасываю…" : "Сбросить демо"}
          </Button>
          {reset.isSuccess && <span className="text-sm text-green-500">Готово</span>}
          {reset.isError && <span className="text-sm text-red-500">Не удалось сбросить</span>}
        </div>
      </Card>
    </div>
  )
}