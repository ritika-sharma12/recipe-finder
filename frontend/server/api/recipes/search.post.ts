export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event)
  const body = await readBody(event)
  const query = getQuery(event)
  const page = query.page ?? 0
  const size = query.size ?? 20
  const searchUrl = `${config.recipeApiBase.replace(/\/$/, '')}/recipes/search?page=${encodeURIComponent(String(page))}&size=${encodeURIComponent(String(size))}`

  return await $fetch(searchUrl, {
    method: 'POST',
    body
  })
})
