import { useState, type FormEvent } from "react"
import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Slider } from "@/components/ui/slider"
import { useSendCheckin } from "@/hooks/useSendCheckin"

import { useAuth } from "@/store/auth"

function ScaleField({
  label,
  value,
  onChange,
}: {
  label: string
  value: number
  onChange: (v: number) => void
}) {
  return (
    <div className="space-y-2">
      <div className="flex justify-between">
        <Label>{label}</Label>
        <span className="text-sm tabular-nums">{value}</span>
      </div>
      <Slider min={1} max={5} step={1} value={[value]} onValueChange={([v]) => onChange(v)} />
    </div>
  )
}

export function CheckinForm() {
  const crewId = useAuth((s) => s.user?.crewId ?? "")
  const { mutate, isPending, isSuccess, isError } = useSendCheckin()

  const [wellbeing, setWellbeing] = useState(4)
  const [mood, setMood] = useState(4)
  const [stress, setStress] = useState(2)
  const [reactionMeanMs, setReactionMeanMs] = useState(340)
  const [reactionLapses, setReactionLapses] = useState(2)
  const [symptoms, setSymptoms] = useState("")

  function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    mutate({
      crewId,
      wellbeing,
      mood,
      stress,
      reactionMeanMs,
      reactionLapses,
      symptoms: symptoms
        .split(",")
        .map((s) => s.trim())
        .filter(Boolean),
    })
  }

  return (
    <Card className="p-4">
      <form onSubmit={submit} className="space-y-4">
        <h3 className="font-semibold">Чекин</h3>

        <div className="grid gap-4 sm:grid-cols-3">
          <ScaleField label="Самочувствие" value={wellbeing} onChange={setWellbeing} />
          <ScaleField label="Настроение" value={mood} onChange={setMood} />
          <ScaleField label="Стресс" value={stress} onChange={setStress} />
        </div>

        <div className="grid gap-4 sm:grid-cols-3">
          <div className="space-y-2">
            <Label htmlFor="rt">Время реакции, мс</Label>
            <Input
              id="rt"
              type="number"
              value={reactionMeanMs}
              onChange={(e) => setReactionMeanMs(Number(e.target.value))}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="lapses">Пропуски реакции</Label>
            <Input
              id="lapses"
              type="number"
              value={reactionLapses}
              onChange={(e) => setReactionLapses(Number(e.target.value))}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="symptoms">Симптомы (через запятую)</Label>
            <Input id="symptoms" value={symptoms} onChange={(e) => setSymptoms(e.target.value)} />
          </div>
        </div>

        <div className="flex items-center gap-3">
          <Button type="submit" disabled={isPending}>
            {isPending ? "Отправка…" : "Отправить"}
          </Button>
          {isSuccess && <span className="text-sm text-green-500">Чекин отправлен</span>}
          {isError && <span className="text-sm text-red-500">Не удалось отправить</span>}
        </div>
      </form>
    </Card>
  )
}