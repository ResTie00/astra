import { useMutation, useQueryClient } from "@tanstack/react-query"
import { ackAlert } from "@/api/alerts"
import { useAcked } from "@/store/alerts"

export function useAckAlert() {
  const qc = useQueryClient()
  const markAcked = useAcked((s) => s.markAcked)

  return useMutation({
    mutationFn: ackAlert,
    onSuccess: (_data, id) => {
      markAcked(id) // второй аргумент onSuccess это то, что передали в mutate(id)
      qc.invalidateQueries({ queryKey: ["alerts"] })
    },
  })
}