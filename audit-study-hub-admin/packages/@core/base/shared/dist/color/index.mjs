import { createJiti } from "../../../../../../node_modules/.pnpm/jiti@2.4.2/node_modules/jiti/lib/jiti.mjs";

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

/** @type {import("/Users/超级无敌巨重要的资料/大三下/JAVA综合实训/222090140_俞云烽_工程源代码/audit-study-hub-admin/packages/@core/base/shared/src/color/index.js")} */
const _module = await jiti.import("/Users/超级无敌巨重要的资料/大三下/JAVA综合实训/222090140_俞云烽_工程源代码/audit-study-hub-admin/packages/@core/base/shared/src/color/index.ts");

export const isDarkColor = _module.isDarkColor;
export const isLightColor = _module.isLightColor;
export const convertToHsl = _module.convertToHsl;
export const convertToHslCssVar = _module.convertToHslCssVar;
export const convertToRgb = _module.convertToRgb;
export const isValidColor = _module.isValidColor;
export const TinyColor = _module.TinyColor;
export const generatorColorVariables = _module.generatorColorVariables;