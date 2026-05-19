<script lang="ts">
  import "../app.css";

  import { afterNavigate } from "$app/navigation";
  import { loadUser } from "$lib/stores/auth";
  import { token, logoutUser } from "$lib/stores/auth";

  if (typeof window !== "undefined") {
    loadUser();
  }

  afterNavigate(() => {
    loadUser();
  });

  if (typeof window !== "undefined") {
    setInterval(async () => {
      const t = localStorage.getItem("auth")
        ? JSON.parse(localStorage.getItem("auth")!).token
        : null;
      if (!t) return;

      const res = await fetch(`${import.meta.env.VITE_API_URL}/user/validate`, {
        method: "POST",
        headers: { Authorization: `Bearer ${t}` },
      });

      if (!res.ok) {
        logoutUser();
        window.location.href = "/login";
      }
    }, 1200000); // every 2 min
  }
</script>

<div class="min-h-screen flex flex-col items-center justify-start">
  <slot />
</div>
