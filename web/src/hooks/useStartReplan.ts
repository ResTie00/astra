import { useMutation } from "@tanstack/react-query"
import { startReplan } from "@/api/replan"
import { useUi } from "@/store/ui"

export function useStartReplan() {
  const setReplanResult = useUi((s) => s.setReplanResult)

  return useMutation({
    mutationFn: () => startReplan(),
    onSuccess: (data) => setReplanResult(data),
  })
}