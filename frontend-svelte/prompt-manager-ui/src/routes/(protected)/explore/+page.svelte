<script lang="ts">
    import { onMount } from "svelte";
    import { goto } from "$app/navigation";
    import { apiFetch } from "$lib/utils/api";

    // interface Prompt {
    //     id: string;
    //     title: string;
    //     description: string;
    //     aiTool: string;
    //     favorite?: boolean;
    // }

    interface Prompt {
        id: string;
        title: string;
        description: string;
        aiTool: string;

        predictedAiTool?: string;
        predictionConfidence?: number;

        similarityScore?: number;

        favorite?: boolean;
    }

    let prompts: Prompt[] = [];
    let favoriteIds: string[] = [];
    let loading = true;
    let errorMsg: string | null = null;
    let searchQuery = "";
    let searchMode: "keyword" | "semantic" = "keyword";

    const API_URL = import.meta.env.VITE_API_URL;

    async function loadPrompts() {
        loading = true;
        errorMsg = null;
        try {
            const [promptsRes, favsRes] = await Promise.all([
                apiFetch(`${API_URL}/prompts/all`),
                apiFetch(`${API_URL}/analytics/favorites`).catch(() => null),
            ]);

            const promptData: Prompt[] = await promptsRes.json();
            favoriteIds = favsRes ? await favsRes.json() : [];

            prompts = promptData.map((p) => ({
                ...p,
                favorite: favoriteIds.includes(p.id),
            }));
        } catch (e) {
            errorMsg = "Failed to load prompts.";
            console.error("loadPrompts error:", e);
        } finally {
            loading = false;
        }
    }

    // async function searchPrompts() {
    //     if (!searchQuery.trim()) {
    //         loadPrompts();
    //         return;
    //     }
    //     loading = true;
    //     errorMsg = null;
    //     try {
    //         const res = await apiFetch(
    //             `${API_URL}/prompts/search?query=${encodeURIComponent(searchQuery)}`,
    //         );
    //         const data: Prompt[] = await res.json();

    //         prompts = data.map((p) => ({
    //             ...p,
    //             favorite: favoriteIds.includes(p.id),
    //         }));
    //     } catch (e) {
    //         errorMsg = "Search failed.";
    //         console.error("searchPrompts error:", e);
    //     } finally {
    //         loading = false;
    //     }
    // }

    async function searchPrompts() {
        if (!searchQuery.trim()) {
            loadPrompts();
            return;
        }

        loading = true;
        errorMsg = null;

        try {
            /*
             * KEYWORD SEARCH
             */
            if (searchMode === "keyword") {
                const res = await apiFetch(
                    `${API_URL}/prompts/search?query=${encodeURIComponent(searchQuery)}`,
                );

                const data: Prompt[] = await res.json();

                prompts = data.map((p) => ({
                    ...p,
                    favorite: favoriteIds.includes(p.id),
                }));
            } else {
                /*
                 * SEMANTIC SEARCH
                 */
                const res = await apiFetch(
                    `${API_URL}/prompts/semantic-search`,
                    {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json",
                        },
                        body: JSON.stringify({
                            query: searchQuery,
                        }),
                    },
                );

                const data = await res.json();

                prompts = data.map((item: any) => ({
                    ...item.prompt,
                    similarityScore: item.similarityScore,
                    favorite: favoriteIds.includes(item.prompt.id),
                }));
            }
        } catch (e) {
            errorMsg = "Search failed.";

            console.error("searchPrompts error:", e);
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
            console.error("handleView failed:", e);
        }
    }

    async function handleCopy(p: Prompt) {
        try {
            await navigator.clipboard.writeText(`${p.description}`);
            await apiFetch(`${API_URL}/analytics/increment/copy/${p.id}`, {
                method: "POST",
            });
        } catch (e) {
            console.error("handleCopy failed:", e);
        }
    }

    async function toggleFavorite(p: Prompt, index: number) {
        try {
            const url = `${API_URL}/analytics/${p.favorite ? "decrement" : "increment"}/favorite/${p.id}`;
            const res = await apiFetch(url, { method: "POST" });
            if (!res.ok) throw new Error(await res.text());

            prompts[index].favorite = !p.favorite;

            const favRes = await apiFetch(
                `${API_URL}/analytics/favorites`,
            ).catch(() => null);
            if (favRes) {
                const favIds: string[] = await favRes.json();
                prompts = prompts.map((pr) => ({
                    ...pr,
                    favorite: favIds.includes(pr.id),
                }));
            }
        } catch (e) {
            console.error("toggleFavorite failed:", e);
        }
    }

    onMount(loadPrompts);
</script>

{#if loading}
    <div class="text-center text-gray-500 mt-8">Loading prompts...</div>
{:else if errorMsg}
    <div class="text-center text-red-500 mt-8">{errorMsg}</div>
{:else}
    <section class="p-8 space-y-6">
        <h1 class="text-2xl font-bold text-center">Explore Prompts</h1>

        <!-- 🔍 Search input -->
        <!-- <div class="flex justify-center mt-4"> -->
        <div class="flex flex-col items-center gap-3 mt-4">
            <div class="flex gap-2">
                <button
                    on:click={() => (searchMode = "keyword")}
                    class={`px-4 py-2 rounded text-sm ${
                        searchMode === "keyword"
                            ? "bg-indigo-600 text-white"
                            : "bg-gray-200 text-gray-700"
                    }`}
                >
                    Keyword Search
                </button>

                <button
                    on:click={() => (searchMode = "semantic")}
                    class={`px-4 py-2 rounded text-sm ${
                        searchMode === "semantic"
                            ? "bg-indigo-600 text-white"
                            : "bg-gray-200 text-gray-700"
                    }`}
                >
                    Semantic Search
                </button>
            </div>
            <input
                type="text"
                placeholder={searchMode === "keyword"
                    ? "Search by keywords..."
                    : "Describe what you are looking for..."}
                bind:value={searchQuery}
                on:keyup={(e) => e.key === "Enter" && searchPrompts()}
                class="border rounded px-3 py-2 w-full max-w-md"
            />
            <button
                on:click={searchPrompts}
                class="ml-2 px-4 py-2 bg-indigo-600 text-white rounded"
            >
                Search
            </button>
            <button
                on:click={() => {
                    searchQuery = "";
                    loadPrompts();
                }}
                class="ml-2 px-4 py-2 bg-gray-300 text-gray-800 rounded"
            >
                Reset
            </button>
        </div>

        {#if prompts.length === 0}
            <p class="text-center text-gray-500 mt-6">No prompts available.</p>
        {:else}
            <div class="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
                {#each prompts as p, i}
                    <div
                        class="bg-white rounded-xl shadow hover:shadow-md transition p-4 flex flex-col justify-between border border-gray-100"
                    >
                        <div>
                            <h2
                                class="text-lg font-semibold text-gray-800 mb-1"
                            >
                                {p.title}
                            </h2>
                            <p class="text-sm text-gray-600 line-clamp-3">
                                {p.description}
                            </p>
                            <p class="text-xs text-indigo-500 mt-2">
                                {p.aiTool}
                            </p>
                            {#if p.predictedAiTool}
                                <div class="mt-2 flex flex-wrap gap-2">
                                    <span
                                        class="text-xs bg-purple-100 text-purple-700 px-2 py-1 rounded-full"
                                    >
                                        Suggested: {p.predictedAiTool}
                                    </span>

                                    {#if p.predictionConfidence}
                                        <span
                                            class="text-xs bg-gray-100 text-gray-700 px-2 py-1 rounded-full"
                                        >
                                            {(
                                                p.predictionConfidence * 100
                                            ).toFixed(0)}%
                                        </span>
                                    {/if}
                                </div>
                            {/if}
                            {#if p.similarityScore !== undefined}
                                <div class="mt-2">
                                    <span
                                        class="text-xs bg-green-100 text-green-700 px-2 py-1 rounded-full"
                                    >
                                        Similarity:
                                        {(p.similarityScore * 100).toFixed(1)}%
                                    </span>
                                </div>
                            {/if}
                        </div>

                        <div class="flex justify-between items-center mt-4">
                            <div class="flex gap-3">
                                <button
                                    on:click={() => handleView(p.id)}
                                    class="text-blue-600 hover:underline text-sm"
                                    >View</button
                                >
                                <button
                                    on:click={() => handleCopy(p)}
                                    class="text-green-600 hover:underline text-sm"
                                    >Copy</button
                                >
                            </div>

                            <button
                                on:click={() => toggleFavorite(p, i)}
                                class="text-lg"
                                title={p.favorite ? "Unfavorite" : "Favorite"}
                            >
                                {#if p.favorite}
                                    <span class="text-amber-500">♥</span>
                                {:else}
                                    <span
                                        class="text-gray-400 hover:text-amber-400"
                                        >♡</span
                                    >
                                {/if}
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
