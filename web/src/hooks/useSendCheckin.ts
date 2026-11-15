import { useMutation, useQueryClient } from "@tanstack/react-query"
import { sendCheckin } from "@/api/checkin"

export function useSendCheckin() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: sendCheckin,
    onSuccess: () => qc.invalidateQueries({ queryKey: ["crew"] }),
  })
}