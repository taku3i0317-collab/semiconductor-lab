// 中心図の輪の決まりのテスト（S-039）：どのノードの中心図（作る順番の輪）でも、前提は必ずその物より外の輪にあり、
// いちばん外の輪は前提の無い始まりだけ。docs/film.js の Film.scene を node で動かす。使い方：node tools/test_rings.mjs
import { readFileSync } from 'node:fs';
import vm from 'node:vm';
const ctx = { window: {}, document: { documentElement: { lang: 'ja' } }, matchMedia: () => ({ matches: false }), getComputedStyle: () => ({ getPropertyValue: () => '' }) };
ctx.window = ctx; vm.createContext(ctx);
vm.runInContext(readFileSync('docs/tech_network_graph.js', 'utf8'), ctx);
vm.runInContext(readFileSync('docs/film.js', 'utf8'), ctx);
const { Film, TECH_GRAPH: G } = ctx;
let bad = 0, outer = 0, n = 0, worst = 0;
for (const node of G.nodes) {
  const t0 = performance.now(), S = Film.scene(node.id, 'down'); worst = Math.max(worst, performance.now() - t0); n++;
  for (const [f, t, kk] of S.all) if (kk !== 'u' && S.dist.has(f) && S.dist.has(t) && !(S.dist.get(f) > S.dist.get(t))) { if (bad < 5) console.log('NG 前提が外でない', node.id, f, '->', t, S.dist.get(f), S.dist.get(t)); bad++; }
  const R = S.nL; for (const [k, d] of S.dist) if (d === R && R > 0 && !S.roots.includes(k)) { if (outer < 5) console.log('NG 外の輪に始まりでない物', node.id, k); outer++; }
}
console.log(`中心図 ${n} 枚：前提が外でない線 ${bad}、外の輪の始まりでない物 ${outer}、1枚を作るのにいちばん長くかかった時間 ${worst.toFixed(1)}ms`);
process.exit(bad || outer ? 1 : 0);
