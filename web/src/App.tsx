import { Background } from "@/components/Background"
import { Dashboard } from "@/components/Dashboard"
import { LoginPage } from "@/components/LoginPage"
import { useAuth } from "@/store/auth"

export default function App() {
  const user = useAuth((s) => s.user)

  return (
    <div className="relative min-h-screen">
      <Background />
      <div className="relative z-10">{user ? <Dashboard user={user} /> : <LoginPage />}</div>
    </div>
  )
}