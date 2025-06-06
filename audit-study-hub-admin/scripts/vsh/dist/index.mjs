import { createJiti } from "../../../node_modules/.pnpm/jiti@2.4.2/node_modules/jiti/lib/jiti.mjs";

const jiti = createJiti(import.meta.url, {
  "interopDefault": true,
  "alias": {
    "@vben/vsh": "/Users/超级无敌巨重要的资料/大三下/JAVA综合实训/222090140_俞云烽_工程源代码/audit-study-hub-admin/scripts/vsh"
  },
  "transformOptions": {
    "babel": {
      "plugins": []
    }
  }
})

/** @type {import("/Users/超级无敌巨重要的资料/大三下/JAVA综合实训/222090140_俞云烽_工程源代码/audit-study-hub-admin/scripts/vsh/src/index.js")} */
const _module = await jiti.import("/Users/超级无敌巨重要的资料/大三下/JAVA综合实训/222090140_俞云烽_工程源代码/audit-study-hub-admin/scripts/vsh/src/index.ts");

export default _module?.default ?? _module;