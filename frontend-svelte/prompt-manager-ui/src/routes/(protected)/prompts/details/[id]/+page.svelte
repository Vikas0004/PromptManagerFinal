<script lang="ts">
  import { onMount } from "svelte";
  import { page } from "$app/stores";
  import { goto } from "$app/navigation";
  import { apiFetch } from "$lib/utils/api";

  // interface Prompt {
  //   id: string;
  //   userId: string;
  //   title: string;
  //   description: string;
  //   aiTool?: string;
  //   favorite?: boolean;
  //   createdAt?: string;
  //   updatedAt?: string;
  // }

  interface Prompt {
    id: string;
    userId: string;
    title: string;
    description: string;

    aiTool?: string;

    predictedAiTool?: string;
    predictionConfidence?: number;

    favorite?: boolean;

    createdAt?: string;
    updatedAt?: string;
  }

  interface ReferencePrompt {
    title: string;
    similarity: number;
  }

  interface ImproveResponse {
    improvedPrompt: string;
    explanation: string;
    referencePrompts: ReferencePrompt[];
  }

  let loading = true;
  let error: string | null = null;
  let prompt: Prompt | null = null;
  let improving = false;
  let aiResult: ImproveResponse | null = null;
  let aiError: string | null = null;
  let showAiPanel = false;

  const API_URL = import.meta.env.VITE_API_URL;
  const IMPROVE_WITH_AI_API_URL = import.meta.env.VITE_IMPROVE_API_URL;

  onMount(async () => {
    const id = $page.params.id;

    try {
      // // increment view count (ignore failure)
      // await apiFetch(`${API_URL}/analytics/increment/view/${id}`, {
      //   method: "POST",
      // }).catch(() => null);

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

  // Copy prompt content and record analytics
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

  async function improvePrompt() {
    if (!prompt) return;

    improving = true;

    aiError = null;

    try {
      const res = await apiFetch(
        `${IMPROVE_WITH_AI_API_URL}/prompt-assistant/improve`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },

          body: JSON.stringify({
            promptText: prompt.description,
          }),
        },
      );

      if (!res.ok) {
        throw new Error(await res.text());
      }

      aiResult = await res.json();

      showAiPanel = true;
    } catch (e: any) {
      aiError = e?.message ?? "AI improvement failed";

      console.error("Improve AI failed:", e);
    } finally {
      improving = false;
    }
  }

  async function copyImprovedPrompt() {
    if (!aiResult) return;

    try {
      await navigator.clipboard.writeText(aiResult.improvedPrompt);
    } catch (e) {
      console.error("Copy improved prompt failed:", e);
    }
  }

  // Toggle favorite with backend re-sync
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
        {#if prompt.predictedAiTool}
          <div class="mt-2 flex flex-wrap gap-2">
            <span
              class="text-xs bg-purple-100 text-purple-700 px-2 py-1 rounded-full"
            >
              Suggested Tool:
              {prompt.predictedAiTool}
            </span>

            {#if prompt.predictionConfidence}
              <span
                class="text-xs bg-gray-100 text-gray-700 px-2 py-1 rounded-full"
              >
                {(prompt.predictionConfidence * 100).toFixed(0)}% confidence
              </span>
            {/if}
          </div>
        {/if}
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
        <button
          on:click={improvePrompt}
          disabled={improving}
          class="px-3 py-1 text-sm bg-indigo-600 text-white rounded hover:bg-indigo-700 disabled:opacity-50"
        >
          {#if improving}
            Improving...
          {:else}
            Improve With AI
          {/if}
        </button>
      </div>
    </div>

    <div class="mt-4 text-sm text-gray-700 whitespace-pre-wrap">
      {prompt.description}
      {#if showAiPanel && aiResult}
        <div class="mt-8 border rounded-xl p-5 bg-indigo-50 space-y-5">
          <div class="flex justify-between items-center">
            <h2 class="text-lg font-semibold text-indigo-800">
              AI Improved Prompt
            </h2>

            <button
              on:click={copyImprovedPrompt}
              class="text-sm px-3 py-1 bg-indigo-600 text-white rounded hover:bg-indigo-700"
            >
              Copy
            </button>
          </div>

          <!-- Improved Prompt -->

          <div>
            <h3 class="font-medium text-gray-800 mb-2">Improved Prompt</h3>

            <div
              class="bg-white rounded-lg p-4 text-sm whitespace-pre-wrap border"
            >
              {aiResult.improvedPrompt}
            </div>
          </div>

          <!-- Explanation -->

          <div>
            <h3 class="font-medium text-gray-800 mb-2">Explanation</h3>

            <div class="bg-white rounded-lg p-4 text-sm border">
              {aiResult.explanation}
            </div>
          </div>

          <!-- Reference Prompts -->

          <div>
            <h3 class="font-medium text-gray-800 mb-2">Reference Prompts</h3>

            <div class="space-y-2">
              {#each aiResult.referencePrompts as ref}
                <div
                  class="bg-white border rounded-lg p-3 flex justify-between items-center"
                >
                  <span class="text-sm text-gray-700">
                    {ref.title}
                  </span>

                  <span
                    class="text-xs bg-green-100 text-green-700 px-2 py-1 rounded-full"
                  >
                    {(ref.similarity * 100).toFixed(1)}%
                  </span>
                </div>
              {/each}
            </div>
          </div>
        </div>
      {/if}
      {#if aiError}
        <div class="mt-4 text-red-500 text-sm">
          {aiError}
        </div>
      {/if}
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
