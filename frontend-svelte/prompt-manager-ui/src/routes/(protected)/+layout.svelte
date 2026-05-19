<script lang="ts">
  import { onMount } from "svelte";
  import { page } from "$app/stores";
  import { goto, afterNavigate } from "$app/navigation";
  import { user, role, logoutUser, loadUser } from "$lib/stores/auth";

  // Reactive store values
  $: username = $user;
  $: userRole = $role;
  $: path = $page.url.pathname;

  let mobileOpen = false;

  //  Hydrate store and redirect unauthenticated users
  onMount(() => {
    loadUser();
    const auth = localStorage.getItem("auth");
    if (!auth) goto("/login");
  });

  // Keep store synced across route changes
  afterNavigate(() => {
    loadUser();
  });

  function doLogout() {
    logoutUser();
    goto("/login");
  }

  // Active link detection
  function isActive(p: string) {
    return path === p || path.startsWith(p + "/");
  }
</script>

<nav class="w-full bg-white shadow sticky top-0 z-30">
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
    <div class="flex justify-between h-16 items-center">
      <!-- left: brand -->
      <div class="flex items-center">
        <a href="/dashboard" class="flex items-center gap-3">
          <div
            class="w-10 h-10 rounded-md bg-gradient-to-br from-purple-600 to-indigo-500 flex items-center justify-center text-white font-bold"
          >
            PM
          </div>
          <div class="hidden sm:block">
            <div class="text-lg font-semibold text-gray-800">
              Prompt Manager
            </div>
            <div class="text-xs text-gray-500">manage prompts • analytics</div>
          </div>
        </a>
      </div>

      <!-- center: nav links (desktop) -->
      <div class="hidden md:flex md:items-center md:space-x-6">
        <a
          href="/explore"
          class="px-3 py-2 rounded-md text-sm font-medium"
          class:text-gray-900={isActive("/explore")}
          class:text-gray-600={!isActive("/explore")}
          class:bg-gray-100={isActive("/explore")}
        >
          Explore
        </a>

        <a
          href="/prompts"
          class="px-3 py-2 rounded-md text-sm font-medium"
          class:text-gray-900={isActive("/prompts")}
          class:text-gray-600={!isActive("/prompts")}
          class:bg-gray-100={isActive("/prompts")}
        >
          My Prompts
        </a>

        <a
          href="/favorites"
          class="px-3 py-2 rounded-md text-sm font-medium"
          class:text-gray-900={isActive("/favorites")}
          class:text-gray-600={!isActive("/favorites")}
          class:bg-gray-100={isActive("/favorites")}
        >
          My Favorites
        </a>

        {#if userRole === "ADMIN"}
          <a
            href="/analytics"
            class="px-3 py-2 rounded-md text-sm font-medium"
            class:text-gray-900={isActive("/analytics")}
            class:text-gray-600={!isActive("/analytics")}
            class:bg-gray-100={isActive("/analytics")}
          >
            Analytics
          </a>
        {/if}

        {#if userRole === "USER"}
          <a
            href="/analytics/my"
            class="px-3 py-2 rounded-md text-sm font-medium"
            class:text-gray-900={isActive("/analytics/my")}
            class:text-gray-600={!isActive("/analytics/my")}
            class:bg-gray-100={isActive("/analytics/my")}
          >
            Analytics
          </a>
        {/if}
      </div>

      <!-- right: user actions -->
      <div class="flex items-center gap-3">
        <div class="hidden sm:flex sm:flex-col sm:items-end sm:mr-4">
          <div class="text-sm font-medium text-gray-800">{username}</div>
          <div class="text-xs text-gray-500">{userRole}</div>
        </div>

        <button
          on:click={doLogout}
          class="hidden sm:inline-flex items-center gap-2 bg-red-500 hover:bg-red-600 text-white text-sm px-3 py-1 rounded"
        >
          Logout
        </button>

        <!-- mobile menu button -->
        <button
          class="inline-flex md:hidden items-center justify-center p-2 rounded-md text-gray-700 hover:bg-gray-100"
          aria-label="Open menu"
          aria-expanded={mobileOpen}
          on:click={() => (mobileOpen = !mobileOpen)}
        >
          <svg
            class="h-6 w-6"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            {#if mobileOpen}
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M6 18L18 6M6 6l12 12"
              />
            {:else}
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M4 6h16M4 12h16M4 18h16"
              />
            {/if}
          </svg>
        </button>
      </div>
    </div>
  </div>

  <!-- mobile menu panel -->
  {#if mobileOpen}
    <div class="md:hidden border-t">
      <div class="px-4 py-3 space-y-1">
        <a
          href="/dashboard"
          class="block px-3 py-2 rounded-md text-base font-medium text-gray-700 hover:bg-gray-50"
          >Dashboard</a
        >
        <a
          href="/prompts"
          class="block px-3 py-2 rounded-md text-base font-medium text-gray-700 hover:bg-gray-50"
          >My Prompts</a
        >
        {#if userRole === "ADMIN"}
          <a
            href="/analytics"
            class="block px-3 py-2 rounded-md text-base font-medium text-gray-700 hover:bg-gray-50"
            >Analytics</a
          >
        {/if}

        <div class="pt-2 border-t">
          <div class="px-3 py-2 text-sm text-gray-600">
            Signed in as <strong class="text-gray-800">{username}</strong>
          </div>
          <button
            on:click={doLogout}
            class="w-full text-left px-3 py-2 rounded-md bg-red-500 text-white"
            >Logout</button
          >
        </div>
      </div>
    </div>
  {/if}
</nav>

<!-- main content area (centered, responsive) -->
<main class="w-full">
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <slot />
  </div>
</main>
