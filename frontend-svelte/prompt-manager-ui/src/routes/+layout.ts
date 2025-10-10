// src/routes/+layout.ts
import type { LayoutLoad } from "./$types";

export const load: LayoutLoad = async ({ route }) => {
  if (typeof window !== "undefined") {
    const authData = localStorage.getItem("auth");
    if (
      authData &&
      (route.id === "/login" || route.id === "/register")
    ) {
      window.location.href = "/dashboard";
    }
  }
  return {};
};
