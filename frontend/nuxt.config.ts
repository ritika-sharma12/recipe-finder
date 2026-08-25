// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',
  devtools: { enabled: true },
  runtimeConfig: {
    recipeApiBase: process.env.NUXT_RECIPE_API_BASE || 'http://localhost:8181/api'
  }
})
