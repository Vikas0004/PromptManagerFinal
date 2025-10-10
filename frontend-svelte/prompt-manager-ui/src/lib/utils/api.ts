import { token, logoutUser } from "$lib/stores/auth";

export async function apiFetch(url: string, options: RequestInit = {}) {
  // Safely get current token value
  let t: string | null = null;
  token.subscribe((v) => (t = v))(); // immediately unsubscribe after reading

  const headers = new Headers(options.headers || {});
  if (t) headers.set("Authorization", `Bearer ${t}`);

  const res = await fetch(url, { ...options, headers });

  // Handle 401 (expired/invalid)
  if (res.status === 401) {
    logoutUser();
    window.location.href = "/login";
    return Promise.reject("Token expired or invalid");
  }

  // Handle 503 (user service down)
  if (res.status === 503) {
    alert("Authentication service unavailable. Please try again later.");
    return Promise.reject("User service unavailable");
  }

  return res;
}
