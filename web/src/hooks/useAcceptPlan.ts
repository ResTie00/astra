import { useMutation, useQueryClient } from "@tanstack/react-query"
import { acceptPlan } from "@/api/replan"

export function useAcceptPlan() {
  const qc = useQueryClient()

  return useMutation({
    mutationFn: ({ runId, optionId }: { runId: string; optionId: string }) =>
      acceptPlan(runId, optionId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["crew"] })
      qc.invalidateQueries({ queryKey: ["schedule"] })
    },
  })
}