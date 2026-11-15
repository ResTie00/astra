import { useMutation, useQueryClient } from "@tanstack/react-query"
import { resetDemo } from "@/api/demo"
import { useUi } from "@/store/ui"
import { useAcked } from "@/store/alerts"

export function useResetDemo() {
  const qc = useQueryClient()
  const setReplanResult = useUi((s) => s.setReplanResult)

  return useMutation({
    mutationFn: resetDemo,
    onSuccess: () => {
        useAcked.getState().clear()
      setReplanResult(null)
      qc.invalidateQueries() // без аргументов: перезапросить всё
    },
  })
}