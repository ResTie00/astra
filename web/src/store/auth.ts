import { create } from "zustand"
import { persist } from "zustand/middleware"

interface AuthUser {
  crewId: string
  name: string
}

interface AuthState {
  user: AuthUser | null
  login: (user: AuthUser) => void
  logout: () => void
}

export const useAuth = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      login: (user) => set({ user }),
      logout: () => set({ user: null }),
    }),
    { name: "auth" }, // ключ в localStorage
  ),
)