<script lang="ts">
  import { onMount } from "svelte";
  import { page } from "$app/stores";
  import { goto } from "$app/navigation";
  import { apiFetch } from "$lib/utils/api";

  interface Prompt {
    id: string;
    title: string;
    description: string;
    aiTool?: string;
  }

  let prompt: Prompt | null = null;
  let loading = true;
  let errorMsg: string | null = null;
  let saving = false;
  let success = false;

  const API_URL = import.meta.env.VITE_API_URL;

  // Load current prompt
  onMount(async () => {
    const id = $page.params.id;
    try {
      const res = await apiFetch(`${API_URL}/prompts/details/${id}`);
      if (!res.ok) throw new Error(await res.text());
      prompt = await res.json();
    } catch (e) {
      errorMsg = String(e);
      console.error("Failed to load prompt:", e);
    } finally {
      loading = false;
    }
  });

  // Update handler
  async function savePrompt() {
    if (!prompt) return;
    saving = true;
    success = false;
    errorMsg = null;

    try {
      const res = await apiFetch(`${API_URL}/prompts/${prompt.id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          title: prompt.title,
          description: prompt.description,
          aiTool: prompt.aiTool,
        }),
      });

      if (!res.ok) throw new Error(await res.text());

      success = true;
      setTimeout(() => goto("/prompts"), 1000);
    } catch (e) {
      errorMsg = String(e);
      console.error("Save failed:", e);
    } finally {
      saving = false;
    }
  }

  function cancelEdit() {
    history.back();
  }
</script>

{#if loading}
  <div class="text-center text-gray-500 mt-8">Loading prompt...</div>
{:else if errorMsg}
  <div class="text-center text-red-500 mt-8">{errorMsg}</div>
{:else if prompt}
  <section class="max-w-3xl mx-auto bg-white p-6 rounded-xl shadow space-y-6">
    <h1 class="text-2xl font-bold text-gray-800 text-center">Edit Prompt</h1>

    <div class="space-y-4">
      <div>
        <!-- svelte-ignore a11y_label_has_associated_control -->
        <label class="block text-sm font-medium text-gray-700 mb-1">Title</label
        >
        <input
          class="w-full border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
          bind:value={prompt.title}
          maxlength="255"
          required
        />
      </div>

      <div>
        <!-- svelte-ignore a11y_label_has_associated_control -->
        <label class="block text-sm font-medium text-gray-700 mb-1"
          >Description</label
        >
        <textarea
          class="w-full border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
          bind:value={prompt.description}
          rows="8"
          required
        ></textarea>
      </div>

      <div>
        <!-- svelte-ignore a11y_label_has_associated_control -->
        <label class="block text-sm font-medium text-gray-700 mb-1"
          >AI Tool</label
        >
        <input
          class="w-full border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
          bind:value={prompt.aiTool}
          placeholder="e.g. ChatGPT, Midjourney"
        />
      </div>
    </div>

    <div class="flex justify-between items-center mt-6">
      <button
        on:click={cancelEdit}
        class="px-4 py-2 text-sm rounded bg-gray-100 hover:bg-gray-200"
      >
        Cancel
      </button>

      <button
        on:click={savePrompt}
        class="px-4 py-2 text-sm rounded bg-indigo-600 text-white hover:bg-indigo-700 disabled:opacity-50"
        disabled={saving}
      >
        {saving ? "Saving..." : "Save Changes"}
      </button>
    </div>

    {#if success}
      <p class="text-green-600 text-sm text-center mt-2">
        Prompt updated successfully!
      </p>
    {/if}
    {#if errorMsg}
      <p class="text-red-500 text-sm text-center mt-2">{errorMsg}</p>
    {/if}
  </section>
{:else}
  <div class="text-center text-gray-500 mt-8">Prompt not found.</div>
{/if}

<style>
  section {
    max-width: 700px;
  }
</style>
