import { writable } from "svelte/store";

export const user = writable<string | null>(null);
export const role = writable<string | null>(null);
export const token = writable<string | null>(null);

export function loginUser(u: string, r: string, t: string) {
  user.set(u);
  role.set(r);
  token.set(t);
  localStorage.setItem("auth", JSON.stringify({ user: u, role: r, token: t }));
}

export function logoutUser() {
  user.set(null);
  role.set(null);
  token.set(null);
  localStorage.removeItem("auth");
}

export function loadUser() {
  const data = localStorage.getItem("auth");
  if (data) {
    try {
      const { user: u, role: r, token: t } = JSON.parse(data);
      user.set(u);
      role.set(r);
      token.set(t);
    } catch {
      localStorage.removeItem("auth");
    }
  }
}
