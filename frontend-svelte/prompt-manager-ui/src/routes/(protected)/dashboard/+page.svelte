<script lang="ts">
  import { user, role } from "$lib/stores/auth";
  import { goto } from "$app/navigation";

  // Svelte auto-subscription shorthand — no manual subscribe/unsubscribe
  $: username = $user;
  $: userRole = $role;

  function navigate(path: string) {
    goto(path);
  }
</script>

<div class="w-full px-6 sm:px-10 py-8 space-y-10">
  <!-- Welcome Banner -->
  <section
    class="bg-gradient-to-r from-indigo-500 via-purple-500 to-pink-500 text-white rounded-2xl p-8 shadow-lg"
  >
    <h1 class="text-3xl font-bold tracking-tight">
      Welcome, {username || "User"} 👋
    </h1>
    <p class="mt-2 text-sm opacity-90">
      Role: <span class="font-semibold uppercase">{userRole}</span>
    </p>
    <p class="mt-3 text-sm opacity-90">
      Ready to craft your next powerful AI prompt?
    </p>
  </section>

  <!-- Create Prompt Call-to-Action -->
  <!-- svelte-ignore a11y_click_events_have_key_events -->
  <div
    role="button"
    tabindex="0"
    class="group relative overflow-hidden bg-white rounded-2xl shadow-md hover:shadow-xl transition-all border border-gray-100 cursor-pointer"
    on:click={() => navigate("/prompts/create")}
  >
    <!-- Accent stripe -->
    <div
      class="absolute inset-0 bg-gradient-to-r from-indigo-500 to-purple-500 opacity-0 group-hover:opacity-10 transition-opacity"
    ></div>

    <div
      class="relative z-10 flex flex-col sm:flex-row items-start sm:items-center justify-between p-8"
    >
      <div class="flex flex-col space-y-3 max-w-xl">
        <h2 class="text-2xl font-semibold text-gray-800">
          ✨ Create Your Next Prompt
        </h2>
        <p class="text-gray-600 text-sm leading-relaxed">
          Draft, refine, and save your AI prompts effortlessly. Capture ideas,
          test variations, and keep them organized for reuse.
        </p>
      </div>

      <button
        class="mt-6 sm:mt-0 inline-flex items-center gap-2 bg-indigo-600 text-white px-5 py-2.5 rounded-lg font-medium hover:bg-indigo-700 transition"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="w-5 h-5"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          stroke-width="2"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="M12 4v16m8-8H4"
          />
        </svg>
        New Prompt
      </button>
    </div>
  </div>
</div>
