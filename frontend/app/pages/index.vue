<script setup lang="ts">
import normalizedIngredients from '../data/normalized-ingredients.json'

interface IngredientDetail {
  id?: number
  name?: string
  baseIngredient?: string
  quantityWithUnit?: string
  category?: string
}

interface RecipeDTO {
  id: number
  title?: string
  cookTimeMinutes?: number
  prepTimeMinutes?: number
  instructions?: string
  author?: string
  category?: string
  cuisine?: string
  ratings?: number
  imageUrl?: string
  ingredients?: IngredientDetail[]
}

interface PageResponseRecipeDTO {
  content: RecipeDTO[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

const loading = ref(false)
const errorMessage = ref('')
const recipes = ref<RecipeDTO[]>([])
const totalElements = ref(0)
const totalPages = ref(0)

const form = reactive({
  maxCookTime: '',
  exactIngredientsMatch: true
})

const page = ref(0)
const size = ref(20)
const ingredientQuery = ref('')
const showSuggestions = ref(false)
const selectedIngredients = ref<string[]>([])
const ingredientControlRef = ref<HTMLElement | null>(null)
const resultSort = ref('')

const ingredientOptions = (normalizedIngredients as string[]).filter(Boolean)
const ingredientOptionsSet = new Set(ingredientOptions)

const filteredIngredients = computed(() => {
  const query = ingredientQuery.value.trim().toLowerCase()
  const selectedSet = new Set(selectedIngredients.value)

  return ingredientOptions
    .filter((item) => !selectedSet.has(item))
    .filter((item) => (query ? item.toLowerCase().includes(query) : true))
})

const visibleStart = computed(() => {
  if (totalElements.value === 0) return 0
  return page.value * size.value + 1
})

const visibleEnd = computed(() => {
  if (totalElements.value === 0) return 0
  return Math.min((page.value + 1) * size.value, totalElements.value)
})

const displayedRecipes = computed(() => {
  const list = [...recipes.value]
  if (!resultSort.value) return list

  return list.sort((a, b) => {
    const aTime = a.cookTimeMinutes ?? Number.MAX_SAFE_INTEGER
    const bTime = b.cookTimeMinutes ?? Number.MAX_SAFE_INTEGER
    return resultSort.value === 'desc' ? bTime - aTime : aTime - bTime
  })
})

function addIngredient(candidate: string) {
  if (!ingredientOptionsSet.has(candidate)) return
  if (selectedIngredients.value.includes(candidate)) return
  selectedIngredients.value.push(candidate)
  ingredientQuery.value = ''
}

function addFirstSuggestion() {
  if (filteredIngredients.value.length > 0) {
    addIngredient(filteredIngredients.value[0])
  }
}

function removeIngredient(candidate: string) {
  selectedIngredients.value = selectedIngredients.value.filter((item) => item !== candidate)
}

function handleDocumentPointerDown(event: Event) {
  const root = ingredientControlRef.value
  const target = event.target

  if (!root || !(target instanceof Node)) return
  if (!root.contains(target)) {
    showSuggestions.value = false
  }
}

async function runSearch(resetPage = false) {
  if (resetPage) {
    page.value = 0
  }

  loading.value = true
  errorMessage.value = ''

  try {
    const searchRequest: Record<string, unknown> = {}

    if (selectedIngredients.value.length > 0) {
      searchRequest.availableIngredients = [...selectedIngredients.value]
    }
    if (form.maxCookTime !== '') searchRequest.maxCookTime = Number(form.maxCookTime)
    searchRequest.exactIngredientsMatch = form.exactIngredientsMatch

    const response = await $fetch<PageResponseRecipeDTO>('/api/recipes/search', {
      method: 'POST',
      body: searchRequest,
      query: {
        page: page.value,
        size: size.value
      }
    })

    recipes.value = response.content || []
    totalElements.value = response.totalElements || 0
    totalPages.value = response.totalPages || 0
  } catch (error) {
    recipes.value = []
    totalElements.value = 0
    totalPages.value = 0
    errorMessage.value =
      error instanceof Error ? error.message : 'Search failed. Verify the backend is running on port 8181.'
  } finally {
    loading.value = false
  }
}

function nextPage() {
  if (page.value + 1 < totalPages.value) {
    page.value += 1
    runSearch()
  }
}

function previousPage() {
  if (page.value > 0) {
    page.value -= 1
    runSearch()
  }
}

onMounted(() => {
  document.addEventListener('pointerdown', handleDocumentPointerDown)
})

onBeforeUnmount(() => {
  document.removeEventListener('pointerdown', handleDocumentPointerDown)
})
</script>

<template>
  <main class="page">
    <section class="hero">
      <p class="kicker">Recipe Finder</p>
      <h1>Cook With What You Have</h1>
      <p class="subhead">Select ingredients from the list, then optionally filter by cook time.</p>
    </section>

    <section class="panel">
      <form class="grid" @submit.prevent="runSearch(true)">
        <label>
          Base Ingredients (multi-select)
          <div ref="ingredientControlRef" class="ingredients-control">
            <input
              v-model="ingredientQuery"
              placeholder="Type to search ingredients"
              type="text"
              @focus="showSuggestions = true"
              @blur="setTimeout(() => (showSuggestions = false), 120)"
              @keydown.enter.prevent="addFirstSuggestion"
            />

            <ul v-if="showSuggestions && filteredIngredients.length" class="suggestions">
              <li v-for="option in filteredIngredients" :key="option">
                <button type="button" @mousedown.prevent="addIngredient(option)">
                  {{ option }}
                </button>
              </li>
            </ul>

            <div v-if="selectedIngredients.length" class="chips">
              <button
                v-for="item in selectedIngredients"
                :key="item"
                class="chip"
                type="button"
                @click="removeIngredient(item)"
              >
                {{ item }} ×
              </button>
            </div>
          </div>
           <p class="pantry-note">
             Note: Salt, pepper, and oil are assumed to be available and do not need to be selected.
           </p>
        </label>

        <label>
          Max Cook Time (minutes)
          <input v-model="form.maxCookTime" min="0" type="number" />
        </label>

        <label class="checkbox-control">
          <input v-model="form.exactIngredientsMatch" type="checkbox" />
          <span>Show results with only matching exact ingredients</span>
        </label>

        <div class="actions">
          <button :disabled="loading" type="submit">
            {{ loading ? 'Searching...' : 'Search Recipes' }}
          </button>
        </div>
      </form>
    </section>

    <section class="results">
      <div class="results-header">
        <p>
          <strong>{{ totalElements }}</strong> recipes found
          <span class="page-details">
            • Page {{ page + 1 }} of {{ totalPages || 1 }} • Showing {{ visibleStart }}-{{ visibleEnd }}
          </span>
        </p>
        <div class="results-controls">
          <label class="sort-control">
            Sort by cook time
            <select v-model="resultSort">
              <option value="">Default</option>
              <option value="asc">Low to High</option>
              <option value="desc">High to Low</option>
            </select>
          </label>

          <div class="pager">
          <button :disabled="loading || page === 0" type="button" @click="previousPage">Prev</button>
          <span>Page {{ page + 1 }} / {{ totalPages || 1 }}</span>
          <button :disabled="loading || page + 1 >= totalPages" type="button" @click="nextPage">Next</button>
          </div>
        </div>
      </div>

      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

      <div v-if="!loading && recipes.length === 0" class="empty">No recipes match your filters.</div>

      <article v-for="recipe in displayedRecipes" :key="recipe.id" class="card">
        <div class="title-row">
          <h2>{{ recipe.title || 'Untitled recipe' }}</h2>
        </div>

        <p class="meta">
          {{ recipe.cuisine || 'Unknown cuisine' }}
          •
          {{ recipe.category || 'Uncategorized' }}
          •
          {{ recipe.prepTimeMinutes ?? '-' }} min prep time
          •
          {{ recipe.cookTimeMinutes ?? '-' }} min cook time
        </p>

        <p class="meta">
          Rating: {{ recipe.ratings ?? 'N/A' }}
          •
          Author: {{ recipe.author || 'Unknown' }}
        </p>

        <ul v-if="recipe.ingredients?.length" class="ingredients">
          <li v-for="ingredient in recipe.ingredients" :key="ingredient.id || ingredient.name">
            {{ ingredient.name || ingredient.baseIngredient }}
          </li>
        </ul>
      </article>
    </section>
  </main>
</template>

<style scoped>
:global(body) {
  margin: 0;
  font-family: "Avenir Next", "Segoe UI", sans-serif;
  background:
    radial-gradient(circle at 10% 10%, #f2efe5 0, #f2efe500 50%),
    radial-gradient(circle at 90% 20%, #f8d8bb 0, #f8d8bb00 45%),
    linear-gradient(135deg, #fff8f1 0%, #fdf3e6 100%);
  color: #2b1f14;
}

.page {
  max-width: 1040px;
  margin: 0 auto;
  padding: 2rem 1rem 4rem;
}

.hero {
  margin-bottom: 1.5rem;
  animation: rise 450ms ease-out;
}

.kicker {
  text-transform: uppercase;
  letter-spacing: 0.18em;
  font-weight: 700;
  font-size: 0.75rem;
  color: #845830;
}

h1 {
  margin: 0.2rem 0;
  font-size: clamp(2rem, 4vw, 3rem);
  font-family: Georgia, "Times New Roman", serif;
}

.subhead {
  max-width: 66ch;
  line-height: 1.5;
}

.panel,
.results {
  background: #fffefa;
  border: 1px solid #e8d8c8;
  border-radius: 18px;
  box-shadow: 0 10px 24px #694b2b14;
}

.panel {
  margin-bottom: 1rem;
  padding: 1rem;
}

.grid {
  display: grid;
  gap: 0.8rem;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

label {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  font-size: 0.92rem;
  font-weight: 600;
}

.checkbox-control {
  flex-direction: row;
  align-items: center;
  gap: 0.5rem;
}

.pantry-note {
  margin: -0.35rem 0 0;
  color: #765f4b;
  font-size: 0.82rem;
  font-weight: 400;
}

input,
select,
button {
  font: inherit;
}

input,
select {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid #cfb59a;
  border-radius: 10px;
  padding: 0.6rem 0.7rem;
  background: #fff;
}

.checkbox-control input {
  width: auto;
}

button {
  border: 0;
  border-radius: 999px;
  background: linear-gradient(135deg, #91572a 0%, #c57639 100%);
  color: #fff;
  padding: 0.65rem 1.15rem;
  cursor: pointer;
  font-weight: 700;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.actions {
  display: flex;
  align-items: end;
  grid-column: 1 / -1;
}

.ingredients-control {
  position: relative;
}

.chips {
  display: flex;
  gap: 0.4rem;
  flex-wrap: wrap;
  margin: 0.45rem 0;
}

.chip {
  border: 0;
  border-radius: 999px;
  padding: 0.3rem 0.65rem;
  background: #f2e0cf;
  color: #5d3a1f;
  cursor: pointer;
  font-weight: 600;
}

.suggestions {
  position: absolute;
  z-index: 20;
  margin: 0.3rem 0 0;
  padding: 0.35rem;
  width: 100%;
  max-height: 220px;
  overflow: auto;
  list-style: none;
  border: 1px solid #cfb59a;
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 12px 22px #0000001a;
}

.suggestions button {
  width: 100%;
  text-align: left;
  border-radius: 8px;
  padding: 0.45rem 0.55rem;
  color: #2b1f14;
  background: transparent;
}

.suggestions button:hover {
  background: #f5e7d8;
}

.results {
  padding: 1rem;
}

.results-header {
  display: flex;
  gap: 0.8rem;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
}

.pager {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.results-controls {
  display: flex;
  gap: 0.8rem;
  align-items: end;
  flex-wrap: wrap;
}

.sort-control {
  font-size: 0.85rem;
  color: #684a30;
}

.card {
  border: 1px solid #ead9c7;
  border-radius: 14px;
  padding: 0.9rem;
  margin-top: 0.8rem;
  background: #fff;
  animation: rise 280ms ease-out;
}

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.8rem;
}

h2 {
  margin: 0;
  font-size: 1.2rem;
}

.meta {
  color: #684a30;
  font-size: 0.92rem;
}

.page-details {
  font-weight: 500;
}

.ingredients {
  margin: 0.5rem 0 0;
  padding-left: 1.2rem;
}

.error {
  margin: 0.5rem 0;
  color: #b00020;
  font-weight: 700;
}

.empty {
  color: #4d3826;
  font-style: italic;
}

@keyframes rise {
  from {
    transform: translateY(6px);
    opacity: 0;
  }

  to {
    transform: translateY(0);
    opacity: 1;
  }
}

@media (max-width: 720px) {
  .grid {
    grid-template-columns: 1fr;
  }

  .page {
    padding: 1rem 0.75rem 2rem;
  }

  .panel,
  .results {
    border-radius: 14px;
  }
}
</style>
