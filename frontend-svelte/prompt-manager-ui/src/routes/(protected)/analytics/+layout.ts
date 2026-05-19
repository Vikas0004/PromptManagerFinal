import type { LayoutLoad } from "./$types";

export const load: LayoutLoad = async () => {
  if (typeof window === "undefined") return {};

  const auth = localStorage.getItem("auth");
  if (!auth) {
    return { userRole: null };
  }

  const { role } = JSON.parse(auth);
  return { userRole: role };
};
