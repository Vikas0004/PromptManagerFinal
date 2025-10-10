<script lang="ts">
  import { goto } from "$app/navigation";
  let username = "";
  let password = "";
  let email = "";
  let message = "";
  let error = "";

  const handleRegister = async () => {
    error = "";
    try {
      const res = await fetch(
        `${import.meta.env.VITE_API_URL}/user/register?username=${username}&password=${password}&email=${email}`,
        { method: "POST" }
      );

      const data = await res.json();
      if (res.ok) {
        message = data.message || "User registered successfully!";
        setTimeout(() => goto("/login"), 1200);
      } else {
        error = data.error || "Registration failed.";
      }
    } catch (e) {
      error = "Server not reachable.";
    }
  };
</script>

<div class="flex flex-col items-center justify-center mt-20 bg-white p-8 rounded-xl shadow-lg w-96">
  <h1 class="text-2xl font-semibold mb-6 text-gray-800">Register</h1>

  <form on:submit|preventDefault={handleRegister} class="flex flex-col gap-4 w-full">
    <input class="p-2 border rounded focus:ring-2 focus:ring-green-400" placeholder="Username" bind:value={username} />
    <input class="p-2 border rounded focus:ring-2 focus:ring-green-400" placeholder="Email" bind:value={email} />
    <input class="p-2 border rounded focus:ring-2 focus:ring-green-400" type="password" placeholder="Password" bind:value={password} />
    <button class="bg-green-600 text-white py-2 rounded hover:bg-green-700 transition-all">
      Register
    </button>
  </form>

  {#if error}
    <p class="text-red-600 mt-3">{error}</p>
  {/if}
  {#if message}
    <p class="text-green-600 mt-3">{message}</p>
  {/if}

  <p class="mt-4 text-sm text-gray-600">
    Already have an account?
    <a href="/login" class="text-blue-600 hover:underline">Login</a>
  </p>
</div>
