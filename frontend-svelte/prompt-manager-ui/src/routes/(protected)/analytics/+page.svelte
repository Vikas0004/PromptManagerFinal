<script lang="ts">
    import { onMount } from "svelte";
    import { role } from "$lib/stores/auth";
    import { goto } from "$app/navigation";
    import { apiFetch } from "$lib/utils/api";

    const API_URL = import.meta.env.VITE_API_URL;

    // --- Data Models ---
    interface StatItem {
        promptId: string;
        views?: number;
        favorites?: number;
        copies?: number;
        prompt?: Prompt;
    }

    interface Summary {
        mostViewed: { promptId: string; views: number }[];
        mostFavorited: { promptId: string; favorites: number }[];
        mostCopied: { promptId: string; copies: number }[];
    }

    interface Prompt {
        id: string;
        userId: string;
        title: string;
        description: string;
        aiTool: string;
        favorite: boolean;
        createdAt: string;
        updatedAt: string;
    }

    // --- State ---
    let loading = true;
    let unauthorized = false;
    let viewed: StatItem[] = [];
    let favorited: StatItem[] = [];
    let copied: StatItem[] = [];
    let summary: Summary | null = null;

    const analyticsUrl = `${API_URL}/analytics`;
    const promptUrl = `${API_URL}/prompts/details`;

    // --- Attach full prompt details ---
    async function attachPromptDetails(stats: StatItem[]): Promise<StatItem[]> {
        const enriched = await Promise.all(
            stats.map(async (item) => {
                try {
                    const res = await apiFetch(`${promptUrl}/${item.promptId}`);
                    const prompt: Prompt = await res.json();
                    return { ...item, prompt };
                } catch {
                    console.warn("Prompt not found for", item.promptId);
                    return item;
                }
            }),
        );
        return enriched;
    }

    // --- Load analytics data ---
    onMount(async () => {
        if ($role !== "ADMIN") {
            goto("/analytics/my");
            return;
        }

        try {
            const [v, f, c, s] = await Promise.all([
                apiFetch(`${analyticsUrl}/most-viewed`).then((r) => r.json()),
                apiFetch(`${analyticsUrl}/most-favorited`).then((r) =>
                    r.json(),
                ),
                apiFetch(`${analyticsUrl}/most-copied`).then((r) => r.json()),
                apiFetch(`${analyticsUrl}/summary`).then((r) => r.json()),
            ]);

            viewed = await attachPromptDetails(v);
            favorited = await attachPromptDetails(f);
            copied = await attachPromptDetails(c);
            summary = s;
        } catch (e) {
            unauthorized = true;
            console.error("Analytics load failed:", e);
        } finally {
            loading = false;
        }
    });
</script>

{#if loading}
    <div class="text-center text-gray-500 mt-8">Loading analytics…</div>
{:else if unauthorized}
    <div class="text-center text-red-500 mt-8">Access denied.</div>
{:else}
    <section class="p-8 space-y-10">
        <h1 class="text-2xl font-bold text-center">
            Admin Analytics Dashboard
        </h1>

        <!-- Summary -->
        <div class="grid md:grid-cols-3 gap-6 text-center">
            <div class="bg-white shadow-sm rounded-xl p-6">
                <p class="text-gray-500 text-sm">Total Views</p>
                <p class="text-3xl font-semibold">
                    {summary?.mostViewed?.[0]?.views ?? 0}
                </p>
            </div>
            <div class="bg-white shadow-sm rounded-xl p-6">
                <p class="text-gray-500 text-sm">Total Favorites</p>
                <p class="text-3xl font-semibold">
                    {summary?.mostFavorited?.[0]?.favorites ?? 0}
                </p>
            </div>
            <div class="bg-white shadow-sm rounded-xl p-6">
                <p class="text-gray-500 text-sm">Total Copies</p>
                <p class="text-3xl font-semibold">
                    {summary?.mostCopied?.[0]?.copies ?? 0}
                </p>
            </div>
        </div>

        <!-- Most Viewed -->
        <div class="bg-white shadow-sm rounded-xl p-4 space-y-3">
            <h2 class="font-semibold mb-2 text-lg">Most Viewed Prompts</h2>
            {#if viewed.length === 0}
                <p class="text-gray-500 text-sm text-center py-3">
                    No viewed prompts yet.
                </p>
            {:else}
                <div
                    class="flex justify-between items-center text-xs uppercase tracking-wide text-gray-500 mb-1 px-1"
                >
                    <span>Prompt</span>
                    <span class="min-w-[50px] text-right">Views</span>
                </div>
                <ul class="space-y-2">
                    {#each viewed as item}
                        <li
                            class="p-3 flex justify-between items-start bg-gray-50 hover:bg-gray-100 rounded-lg transition"
                        >
                            <div>
                                <div class="font-semibold text-gray-800">
                                    {item.prompt?.title ?? item.promptId}
                                </div>
                                <div
                                    class="text-xs text-gray-500 line-clamp-2 mt-0.5"
                                >
                                    {item.prompt?.description ??
                                        "No description"}
                                </div>
                                <div
                                    class="text-xs text-indigo-600 mt-1 flex items-center gap-1"
                                >
                                    {item.prompt?.aiTool ?? ""}
                                    {#if item.prompt?.favorite}
                                        <span class="text-amber-500">★</span>
                                    {/if}
                                </div>
                            </div>
                            <div
                                class="text-right font-medium text-gray-700 text-sm min-w-[50px]"
                            >
                                {item.views ?? 0}
                            </div>
                        </li>
                    {/each}
                </ul>
            {/if}
        </div>

        <!-- Most Favorited -->
        <div class="bg-white shadow-sm rounded-xl p-4 space-y-3">
            <h2 class="font-semibold mb-2 text-lg">Most Favorited Prompts</h2>
            {#if favorited.length === 0}
                <p class="text-gray-500 text-sm text-center py-3">
                    No favorites yet.
                </p>
            {:else}
                <div
                    class="flex justify-between items-center text-xs uppercase tracking-wide text-gray-500 mb-1 px-1"
                >
                    <span>Prompt</span>
                    <span class="min-w-[50px] text-right">Favorites</span>
                </div>
                <ul class="space-y-2">
                    {#each favorited as item}
                        <li
                            class="p-3 flex justify-between items-start bg-gray-50 hover:bg-gray-100 rounded-lg transition"
                        >
                            <div>
                                <div class="font-semibold text-gray-800">
                                    {item.prompt?.title ?? item.promptId}
                                </div>
                                <div
                                    class="text-xs text-gray-500 line-clamp-2 mt-0.5"
                                >
                                    {item.prompt?.description ??
                                        "No description"}
                                </div>
                                <div
                                    class="text-xs text-indigo-600 mt-1 flex items-center gap-1"
                                >
                                    {item.prompt?.aiTool ?? ""}
                                    {#if item.prompt?.favorite}
                                        <span class="text-amber-500">★</span>
                                    {/if}
                                </div>
                            </div>
                            <div
                                class="text-right font-medium text-gray-700 text-sm min-w-[50px]"
                            >
                                {item.favorites ?? 0}
                            </div>
                        </li>
                    {/each}
                </ul>
            {/if}
        </div>

        <!-- Most Copied -->
        <div class="bg-white shadow-sm rounded-xl p-4 space-y-3">
            <h2 class="font-semibold mb-2 text-lg">Most Copied Prompts</h2>
            {#if copied.length === 0}
                <p class="text-gray-500 text-sm text-center py-3">
                    No copied prompts yet.
                </p>
            {:else}
                <div
                    class="flex justify-between items-center text-xs uppercase tracking-wide text-gray-500 mb-1 px-1"
                >
                    <span>Prompt</span>
                    <span class="min-w-[50px] text-right">Copies</span>
                </div>
                <ul class="space-y-2">
                    {#each copied as item}
                        <li
                            class="p-3 flex justify-between items-start bg-gray-50 hover:bg-gray-100 rounded-lg transition"
                        >
                            <div>
                                <div class="font-semibold text-gray-800">
                                    {item.prompt?.title ?? item.promptId}
                                </div>
                                <div
                                    class="text-xs text-gray-500 line-clamp-2 mt-0.5"
                                >
                                    {item.prompt?.description ??
                                        "No description"}
                                </div>
                                <div
                                    class="text-xs text-indigo-600 mt-1 flex items-center gap-1"
                                >
                                    {item.prompt?.aiTool ?? ""}
                                    {#if item.prompt?.favorite}
                                        <span class="text-amber-500">★</span>
                                    {/if}
                                </div>
                            </div>
                            <div
                                class="text-right font-medium text-gray-700 text-sm min-w-[50px]"
                            >
                                {item.copies ?? 0}
                            </div>
                        </li>
                    {/each}
                </ul>
            {/if}
        </div>
    </section>
{/if}
