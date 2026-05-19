<script lang="ts">
  import { goto } from "$app/navigation";
  import { apiFetch } from "$lib/utils/api";

  let title = "";
  let description = "";
  let aiTool = "ChatGPT";
  let message = "";
  let error = "";
  let loading = false;

  const API_URL = import.meta.env.VITE_API_URL;

  async function createPrompt() {
    error = "";
    message = "";
    if (!title.trim() || !description.trim()) {
      error = "Please fill in all required fields.";
      return;
    }

    loading = true;
    try {
      const res = await apiFetch(`${API_URL}/prompts/add`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ title, description, aiTool }),
      });

      if (!res.ok) throw new Error(await res.text());

      message = "Prompt created successfully!";
      setTimeout(() => goto("/prompts"), 1000);
    } catch (e) {
      console.error("Prompt creation failed:", e);
      error = "Failed to create prompt. Please try again.";
    } finally {
      loading = false;
    }
  }

  function cancel() {
    // goto("/prompts");
    history.back();
  }
</script>

<section class="max-w-3xl mx-auto bg-white p-8 rounded-xl shadow space-y-6">
  <h1 class="text-2xl font-bold text-gray-800 text-center">
    Create New Prompt
  </h1>

  <form on:submit|preventDefault={createPrompt} class="space-y-5">
    <!-- Title -->
    <div>
      <!-- svelte-ignore a11y_label_has_associated_control -->
      <label class="block text-sm font-medium text-gray-700 mb-1">Title</label>
      <input
        bind:value={title}
        class="w-full border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
        placeholder="Enter prompt title"
        maxlength="255"
        required
      />
    </div>

    <!-- Description -->
    <div>
      <!-- svelte-ignore a11y_label_has_associated_control -->
      <label class="block text-sm font-medium text-gray-700 mb-1"
        >Description</label
      >
      <textarea
        bind:value={description}
        class="w-full border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
        rows="8"
        placeholder="Write your prompt details..."
        required
      ></textarea>
    </div>

    
    <div>
      <!-- svelte-ignore a11y_label_has_associated_control -->
      <label class="block text-sm font-medium text-gray-700 mb-1">AI Tool</label
      >
      <select
        bind:value={aiTool}
        class="w-full border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
      >
        <option>ChatGPT</option>
        <option>Gemini</option>
        <option>Claude</option>
        <option>Perplexity</option>
        <option>Midjourney</option>
      </select>
    </div>

    <!-- Buttons -->
    <div class="flex justify-between items-center mt-6">
      <button
        type="button"
        on:click={cancel}
        class="px-4 py-2 text-sm rounded bg-gray-100 hover:bg-gray-200"
      >
        Cancel
      </button>

      <button
        type="submit"
        disabled={loading}
        class="px-4 py-2 text-sm rounded bg-indigo-600 text-white hover:bg-indigo-700 disabled:opacity-50"
      >
        {loading ? "Creating..." : "Create Prompt"}
      </button>
    </div>

    {#if message}
      <p class="text-green-600 text-sm text-center mt-2">{message}</p>
    {/if}
    {#if error}
      <p class="text-red-500 text-sm text-center mt-2">{error}</p>
    {/if}
  </form>
</section>

<style>
  section {
    max-width: 700px;
  }
</style>
