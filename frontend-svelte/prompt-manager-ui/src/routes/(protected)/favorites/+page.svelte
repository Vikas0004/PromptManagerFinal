<script lang="ts">
  import { onMount } from "svelte";
  import { goto } from "$app/navigation";
  import { apiFetch } from "$lib/utils/api";

  interface Prompt {
    id: string;
    title: string;
    description: string;
    aiTool?: string;
    favorite?: boolean;
  }

  let loading = true;
  let errorMsg: string | null = null;
  let prompts: Prompt[] = [];

  const API_URL = import.meta.env.VITE_API_URL;

  async function loadFavorites() {
    loading = true;
    errorMsg = null;
    try {
      // get list of prompt IDs the user favorited
      const favRes = await apiFetch(`${API_URL}/analytics/favorites`);
      if (!favRes.ok) throw new Error(await favRes.text());
      const favIds: string[] = await favRes.json();

      if (!favIds.length) {
        prompts = [];
        return;
      }

      // fetch each prompt's full details in parallel
      const detailCalls = favIds.map(async (id) => {
        try {
          const res = await apiFetch(`${API_URL}/prompts/details/${id}`);
          return res.ok ? await res.json() : null;
        } catch {
          return null;
        }
      });

      const results = await Promise.all(detailCalls);
      prompts = results
        .filter(Boolean)
        .map((p: any) => ({ ...p, favorite: true }));
    } catch (e) {
      console.error("Failed to load favorites:", e);
      errorMsg = "Failed to load favorites.";
    } finally {
      loading = false;
    }
  }

  async function handleView(id: string) {
    try {
      await apiFetch(`${API_URL}/analytics/increment/view/${id}`, {
        method: "POST",
      });
      goto(`/prompts/details/${id}`);
    } catch (e) {
      console.error("View increment failed:", e);
    }
  }

  async function handleCopy(p: Prompt) {
    try {
      await navigator.clipboard.writeText(`${p.title}\n\n${p.description}`);
      await apiFetch(`${API_URL}/analytics/increment/copy/${p.id}`, {
        method: "POST",
      });
    } catch (e) {
      console.error("Copy failed:", e);
    }
  }

  async function removeFavorite(p: Prompt) {
    try {
      await apiFetch(`${API_URL}/analytics/decrement/favorite/${p.id}`, {
        method: "POST",
      });
      await loadFavorites();
    } catch (e) {
      console.error("Unfavorite failed:", e);
    }
  }

  onMount(loadFavorites);
</script>

{#if loading}
  <div class="text-center text-gray-500 mt-8">Loading favorites...</div>
{:else if errorMsg}
  <div class="text-center text-red-500 mt-8">{errorMsg}</div>
{:else}
  <section class="p-8 space-y-6">
    <h1 class="text-2xl font-bold text-center">My Favorite Prompts</h1>

    {#if prompts.length === 0}
      <p class="text-center text-gray-500 mt-6">
        You haven't marked any favorites yet.
      </p>
    {:else}
      <div class="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
        {#each prompts as p}
          <div
            class="bg-white rounded-xl shadow hover:shadow-md transition p-4 flex flex-col justify-between border border-gray-100"
          >
            <div>
              <h2 class="text-lg font-semibold text-gray-800 mb-1">
                {p.title}
              </h2>
              <p class="text-sm text-gray-600 line-clamp-3">{p.description}</p>
              <p class="text-xs text-indigo-500 mt-2">{p.aiTool}</p>
            </div>

            <div class="flex justify-between items-center mt-4">
              <div class="flex gap-3">
                <button
                  on:click={() => handleView(p.id)}
                  class="text-blue-600 hover:underline text-sm"
                >
                  View
                </button>
                <button
                  on:click={() => handleCopy(p)}
                  class="text-green-600 hover:underline text-sm"
                >
                  Copy
                </button>
              </div>

              <button
                on:click={() => removeFavorite(p)}
                class="text-lg"
                title="Remove from favorites"
              >
                <span class="text-amber-500">♥</span>
              </button>
            </div>
          </div>
        {/each}
      </div>
    {/if}
  </section>
{/if}

<style>
  .line-clamp-3 {
    display: -webkit-box;
    line-clamp: 3;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
</style>
