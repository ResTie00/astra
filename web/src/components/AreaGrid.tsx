import { Card } from "@/components/ui/card"
import { StatusBadge } from "@/components/StatusBadge"
import type { Area } from "@/types"

export function AreaGrid({ areas }: { areas: Area[] }) {
  return (
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: 12 }}>
      {areas.map(a => (
        <Card key={a.area} style={{ border: '1px solid #ddd', borderRadius: 8, padding: 12 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <strong>{a.area}</strong>
            <StatusBadge status={a.status} />
          </div>
          <p style={{ fontSize: 14, color: '#555' }}>{a.explanation}</p>
        </Card>
      ))}
    </div>
  )
}

