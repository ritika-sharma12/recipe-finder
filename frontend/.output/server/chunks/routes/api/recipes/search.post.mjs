import { d as defineEventHandler, u as useRuntimeConfig, r as readBody, g as getQuery } from '../../../nitro/nitro.mjs';
import 'node:http';
import 'node:https';
import 'node:events';
import 'node:buffer';
import 'node:fs';
import 'node:path';
import 'node:crypto';
import 'node:url';

const search_post = defineEventHandler(async (event) => {
  var _a, _b;
  const config = useRuntimeConfig(event);
  const body = await readBody(event);
  const query = getQuery(event);
  const page = (_a = query.page) != null ? _a : 0;
  const size = (_b = query.size) != null ? _b : 20;
  const searchUrl = `${config.recipeApiBase.replace(/\/$/, "")}/recipes/search?page=${encodeURIComponent(String(page))}&size=${encodeURIComponent(String(size))}`;
  return await $fetch(searchUrl, {
    method: "POST",
    body
  });
});

export { search_post as default };
//# sourceMappingURL=search.post.mjs.map
