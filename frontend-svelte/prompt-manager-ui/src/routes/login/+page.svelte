<script lang="ts">
  import { loginUser } from "$lib/stores/auth";
  import { goto } from "$app/navigation";

  let username = "";
  let password = "";
  let error = "";

  const handleLogin = async () => {
    error = "";
    try {
      const res = await fetch(`${import.meta.env.VITE_API_URL}/user/login`, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ username, password }),
      });

      const data = await res.json();

      if (res.ok) {
        loginUser(username, data.role, data.token);
        goto("/dashboard");
      } else if (res.status === 401) {
        error = data.error || "Invalid username or password.";
      } else if (res.status === 503) {
        error = "Authentication service unavailable. Try again later.";
      } else {
        error = "Unexpected error occurred.";
      }
    } catch {
      error = "Server not reachable.";
    }
  };
</script>

<div
  class="flex flex-col items-center justify-center mt-20 bg-white p-8 rounded-xl shadow-lg w-96"
>
  <h1 class="text-2xl font-semibold mb-6 text-gray-800">Login</h1>
  <form
    on:submit|preventDefault={handleLogin}
    class="flex flex-col gap-4 w-full"
  >
    <input
      class="p-2 border rounded focus:ring-2 focus:ring-blue-400"
      placeholder="Username"
      bind:value={username}
    />
    <input
      class="p-2 border rounded focus:ring-2 focus:ring-blue-400"
      type="password"
      placeholder="Password"
      bind:value={password}
    />
    <button
      class="bg-blue-600 text-white py-2 rounded hover:bg-blue-700 transition-all"
    >
      Login
    </button>
  </form>
  {#if error}
    <p class="text-red-600 mt-3">{error}</p>
  {/if}
  <p class="mt-4 text-sm text-gray-600">
    Don’t have an account? <a
      href="/register"
      class="text-blue-600 hover:underline">Register</a
    >
  </p>
</div>
