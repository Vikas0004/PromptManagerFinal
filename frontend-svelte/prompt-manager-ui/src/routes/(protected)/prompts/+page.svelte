<script lang="ts">
  import { onMount } from "svelte";
  import { apiFetch } from "$lib/utils/api";
  import { goto } from "$app/navigation";

  let prompts: any[] = [];
  let error = "";
  let message = "";

  const API_URL = import.meta.env.VITE_API_URL;

  const fetchPrompts = async () => {
    error = "";
    try {
      const res = await apiFetch(`${API_URL}/prompts/my`);
      if (!res.ok) throw new Error("Failed to fetch prompts");
      prompts = await res.json();
    } catch (e) {
      console.error("Failed to fetch prompts:", e);
      error = "Unable to fetch prompts. Try again later.";
    }
  };

  const canDeletePrompt = async (id: string) => {
    try {
      const res = await apiFetch(`${API_URL}/analytics/check-favorite/${id}`);
      const data = await res.json();

      if (res.status === 200 && data.canDelete) {
        return true;
      } else if (res.status === 409) {
        error =
          data.message || "Prompt is favorited by users and cannot be deleted.";
        return false;
      } else if (res.status === 404) {
        error = data.message || "Prompt not found.";
        return false;
      } else {
        error = "Unable to verify deletion status.";
        return false;
      }
    } catch (err) {
      console.error("Check favorite failed:", err);
      error = "Error verifying prompt deletion.";
      return false;
    }
  };

  const deletePrompt = async (id: string) => {
    if (!confirm("Are you sure you want to delete this prompt?")) return;
    error = "";
    message = "";

    const allowed = await canDeletePrompt(id);
    if (!allowed) return;

    try {
      const promptRes = await apiFetch(`${API_URL}/prompts/${id}`, {
        method: "DELETE",
      });
      if (!promptRes.ok) throw new Error("Prompt delete failed");

      const statsRes = await apiFetch(`${API_URL}/analytics/delete/${id}`, {
        method: "DELETE",
      });
      const statsData = await statsRes.json();

      if (statsRes.status === 200) {
        message =
          statsData.message || "Prompt and analytics deleted successfully.";
        prompts = prompts.filter((p) => p.id !== id);
      } else {
        error =
          statsData.message || "Prompt deleted, but analytics cleanup failed.";
      }
    } catch (e) {
      console.error("Delete failed:", e);
      error = "Error deleting prompt. Try again later.";
    }
  };

  onMount(fetchPrompts);
</script>

<div class="space-y-6">
  <div class="flex justify-between items-center">
    <h1 class="text-2xl font-bold text-gray-800">My Prompts</h1>
    <button
      on:click={() => goto("/prompts/create")}
      class="bg-indigo-600 text-white px-4 py-2 rounded hover:bg-indigo-700 transition"
    >
      + New Prompt
    </button>
  </div>

  {#if error}
    <p class="text-red-600">{error}</p>
  {/if}
  {#if message}
    <p class="text-green-600">{message}</p>
  {/if}

  {#if prompts.length === 0}
    <div class="p-6 bg-white rounded-lg shadow text-center text-gray-600">
      No prompts found. Click “New Prompt” to add one!
    </div>
  {:else}
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
      {#each prompts as p (p.id)}
        <article
          class="bg-white p-5 rounded-lg shadow hover:shadow-lg transition"
        >
          <h2 class="text-lg font-semibold text-gray-800">{p.title}</h2>
          <p class="text-gray-600 mt-2 line-clamp-3">{p.description}</p>
          <div class="text-sm text-gray-500 mt-3">AI Tool: {p.aiTool}</div>

          <div class="mt-4 flex justify-between items-center">
            <button
              on:click={() => goto(`/prompts/edit/${p.id}`)}
              class="text-indigo-600 hover:text-indigo-800 text-sm"
            >
              Edit
            </button>
            <button
              on:click={() => deletePrompt(p.id)}
              class="text-red-500 hover:text-red-700 text-sm"
            >
              Delete
            </button>
          </div>
        </article>
      {/each}
    </div>
  {/if}
</div>
