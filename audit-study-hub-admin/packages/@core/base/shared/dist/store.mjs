import { createJiti } from "../../../../../node_modules/.pnpm/jiti@2.4.2/node_modules/jiti/lib/jiti.mjs";

const jiti = createJiti(import.meta.url, {
  "interopDefault": true,
  "alias": {
    "@vben-core/shared": "/Users/超级无敌巨重要的资料/大三下/JAVA综合实训/222090140_俞云烽_工程源代码/audit-study-hub-admin/packages/@core/base/shared"
  },
  "transformOptions": {
    "babel": {
      "plugins": []
    }
  }
})

/** @type {import("/Users/超级无敌巨重要的资料/大三下/JAVA综合实训/222090140_俞云烽_工程源代码/audit-study-hub-admin/packages/@core/base/shared/src/store.js")} */
const _module = await jiti.import("/Users/超级无敌巨重要的资料/大三下/JAVA综合实训/222090140_俞云烽_工程源代码/audit-study-hub-admin/packages/@core/base/shared/src/store.ts");

export const shallow = _module.shallow;
export const useStore = _module.useStore;
export const Derived = _module.Derived;
export const Effect = _module.Effect;
export const Store = _module.Store;
export const __depsThatHaveWrittenThisTick = _module.__depsThatHaveWrittenThisTick;
export const __derivedToStore = _module.__derivedToStore;
export const __flush = _module.__flush;
export const __storeToDerived = _module.__storeToDerived;
export const batch = _module.batch;