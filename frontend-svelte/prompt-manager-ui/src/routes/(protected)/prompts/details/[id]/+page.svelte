<script lang="ts">
  import { onMount } from "svelte";
  import { page } from "$app/stores";
  import { goto } from "$app/navigation";
  import { apiFetch } from "$lib/utils/api";

  interface Prompt {
    id: string;
    userId: string;
    title: string;
    description: string;
    aiTool?: string;
    favorite?: boolean;
    createdAt?: string;
    updatedAt?: string;
  }

  let loading = true;
  let error: string | null = null;
  let prompt: Prompt | null = null;

  const API_URL = import.meta.env.VITE_API_URL;

  onMount(async () => {
    const id = $page.params.id;

    try {
      // increment view count (ignore failure)
      await apiFetch(`${API_URL}/analytics/increment/view/${id}`, {
        method: "POST",
      }).catch(() => null);

      // load prompt details
      const res = await apiFetch(`${API_URL}/prompts/details/${id}`);
      if (!res.ok) throw new Error(await res.text());
      prompt = await res.json();

      // load favorites to determine if this prompt is liked
      const favRes = await apiFetch(`${API_URL}/analytics/favorites`);
      if (favRes.ok && prompt) {
        const favIds: string[] = await favRes.json();
        prompt.favorite = favIds.includes(prompt.id);
      }
    } catch (e: any) {
      error = e?.message ?? String(e);
    } finally {
      loading = false;
    }
  });

  // 🧾 Copy prompt content and record analytics
  async function handleCopy() {
    if (!prompt) return;
    try {
      await navigator.clipboard.writeText(`${prompt.description}`);
      await apiFetch(`${API_URL}/analytics/increment/copy/${prompt.id}`, {
        method: "POST",
      });
    } catch (e) {
      console.warn("Copy or analytics failed:", e);
    }
  }

  // ⭐ Toggle favorite with backend re-sync
  async function toggleFavorite() {
    if (!prompt) return;
    try {
      const url = `${API_URL}/analytics/${prompt.favorite ? "decrement" : "increment"}/favorite/${prompt.id}`;
      const res = await apiFetch(url, { method: "POST" });
      if (!res.ok) throw new Error(await res.text());

      // optimistic toggle
      prompt.favorite = !prompt.favorite;

      // refetch authoritative favorite list
      const favRes = await apiFetch(`${API_URL}/analytics/favorites`);
      if (favRes.ok) {
        const favIds: string[] = await favRes.json();
        prompt.favorite = favIds.includes(prompt.id);
      }
    } catch (e) {
      console.error("Favorite toggle failed:", e);
    }
  }

  function goBack() {
    history.back();
  }
</script>

{#if loading}
  <div class="text-center text-gray-500 mt-8">Loading prompt...</div>
{:else if error}
  <div class="text-center text-red-500 mt-8">{error}</div>
{:else if prompt}
  <section class="max-w-3xl mx-auto p-6 bg-white rounded-xl shadow">
    <div class="flex items-start justify-between gap-4">
      <div>
        <h1 class="text-2xl font-semibold text-gray-800">{prompt.title}</h1>
        <p class="text-xs text-gray-500 mt-1">
          By {prompt.userId} • {prompt.aiTool ?? "—"}
        </p>
      </div>

      <div class="flex items-center gap-3">
        <button
          on:click={handleCopy}
          class="px-3 py-1 text-sm bg-green-50 text-green-700 rounded hover:bg-green-100"
        >
          Copy
        </button>

        <button
          on:click={toggleFavorite}
          class="px-3 py-1 text-sm rounded"
          aria-pressed={prompt.favorite ? "true" : "false"}
          title={prompt.favorite ? "Unfavorite" : "Favorite"}
        >
          {#if prompt.favorite}
            <span class="text-amber-500">♥ Unfavorite</span>
          {:else}
            <span class="text-gray-500">♡ Favorite</span>
          {/if}
        </button>
      </div>
    </div>

    <div class="mt-4 text-sm text-gray-700 whitespace-pre-wrap">
      {prompt.description}
    </div>

    <div class="mt-6 flex justify-between items-center text-xs text-gray-500">
      <div>
        Created: {prompt.createdAt
          ? new Date(prompt.createdAt).toLocaleString()
          : "—"}
        <span class="mx-2">•</span>
        Updated: {prompt.updatedAt
          ? new Date(prompt.updatedAt).toLocaleString()
          : "—"}
      </div>
      <button on:click={goBack} class="text-blue-600 hover:underline"
        >Back</button
      >
    </div>
  </section>
{:else}
  <div class="text-center text-gray-500 mt-8">Prompt not found.</div>
{/if}

<style>
  section {
    min-height: 200px;
  }
</style>
