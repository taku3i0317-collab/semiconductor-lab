// S-047：ムービーの「あ！」の候補を docs/aha_candidates.md に書く（大事なモノ 10 個、候補を5つずつ）。
// 選び方（docs/film.js と同じ点数）：① 両端とも子どもが知っている（終わりは network_meta の aha.kid）② 部品 ↔ 原料 ③ 短い道（2〜4 個）④ 出典のある丸
// スマホ（B-10）は人が出典で確かめた5つ（CURATED）。ユーザーが選んだら network_meta.json の aha.chosen に入れる。
// 使い方：node tools/aha_candidates.mjs
import { readFileSync, writeFileSync } from 'node:fs';
const ROOT = new URL('..', import.meta.url).pathname.replace(/^\/([A-Za-z]:)/, '$1');
const js = f => { const s = readFileSync(ROOT + 'docs/' + f, 'utf8'); return JSON.parse(s.slice(s.indexOf('{'), s.lastIndexOf('}') + 1)); };
const G = js('tech_network_graph.js'), D = js('tech_network_detail.js').nodes;
const byId = new Map(G.nodes.map(n => [n.id, n])), KID = G.aha.kid;
const parents = new Map();
for (const [f, t, k] of G.edges) if (k === 'r' || k === 'p' || k === 'e') (parents.get(t) || parents.set(t, []).get(t)).push(f);
const anc = id => { const s = new Set(); const q = [id]; while (q.length) for (const p of parents.get(q.pop()) || []) if (!s.has(p)) { s.add(p); q.push(p); } return s; };
const bfs = (a, b, inS) => { const prev = new Map([[a, null]]); let q = [a]; while (q.length) { const nq = []; for (const u of q) for (const p of parents.get(u) || []) if (inS.has(p) && !prev.has(p)) { prev.set(p, u); if (p === b) { const path = [b]; let x = b; while ((x = prev.get(x)) !== null) path.push(x); return path.reverse(); } nq.push(p); } q = nq; } return null; };
const srcOf = k => { const d = D[k] || {}; const u = [...(d.sources || [])]; for (const r of d.refs || []) if (r.url) u.push(r.url); for (const m of ((d.real_full || '') + (d.hee1 || '')).matchAll(/https?:\/\/[^\s、）)」]+/g)) u.push(m[0]); return [...new Set(u)].slice(0, 2); };
const nm = k => byId.get(k).name;
const PRODUCTS = ['B-10', 'B-08', 'B-09', 'B-11', '15-10', '14-11', '15-01', 'B-01', 'P-02', 'B-12'];
// スマホ：出典で確かめた5つ（道はグラフの中にある物だけ。文は子ども向け）
const CURATED = { 'B-10': [
  { part: 'P-16', end: '1-46', path: ['P-16', '2-47', '2-14', '1-46'], text: 'あ！スマホの画面のガラスは、砂からできている', note: 'ふつうのガラスの重さの 70〜74% はシリカ（砂・石英）。画面の強化ガラスもガラスが元', src: ['https://en.wikipedia.org/wiki/Soda%E2%80%93lime_glass'], cert: '○' },
  { part: 'P-22', end: '1-07', text: 'あ！スマホの電池の中には、鉛筆のしんと同じ「黒鉛」が入っている', note: '黒鉛のいちばん大きな使い道は電池（リチウムイオン電池が増えている）。鉛筆のしんも黒鉛（と粘土）', src: ['https://pubs.usgs.gov/periodicals/mcs2024/mcs2024-graphite.pdf'], cert: '◎（電池）・○（鉛筆）' },
  { part: 'P-12', end: '1-46', path: ['P-12', '10-13', '9-23', '9-08', '1-17', '1-16', '1-15', '1-46'], text: 'あ！CPU のチップは、砂（石英）からとったケイ素でできている', note: 'ケイ素（金属シリコン）は石英（シリカ）から作る。半導体のチップは高い純度のケイ素から。道は長い（7 個）', src: ['https://pubs.usgs.gov/periodicals/mcs2024/mcs2024-silicon.pdf'], cert: '◎' },
  { part: 'P-21', end: '1-03', text: 'あ！アンテナの銅は、むかしは緑の石（孔雀石）からとっていた', note: '最初の銅の製錬は孔雀石から。今の銅は多くが黄銅鉱から（だから「むかしは」）', src: srcOf('1-03'), cert: '○' },
  { part: 'P-16', end: '2-02', path: ['P-16', '2-47', '2-02'], text: 'あ！画面のガラスは、木の灰と同じ「カリ」でかたくしている', note: 'スマホの画面のガラスは、とかした硝酸カリウム（カリの塩）につけて表面をかたくする（化学強化）。カリ（potash）は、むかしは木や植物の灰からとった。（スピーカー ← 天然磁石 はデータに道が無いので入れ替えた）', src: ['https://en.wikipedia.org/wiki/Chemically_strengthened_glass', 'https://en.wikipedia.org/wiki/Potash'], cert: '○' },
] };
const out = ['# 「あ！」の候補（S-047）', '', '自動生成：`node tools/aha_candidates.mjs`。ムービーの「あ！」に使う道の候補。**ユーザーが選んだ物を network_meta.json の aha.chosen に入れる**（モノごとに1〜2個）。選ばれていないモノは、下と同じ点数でムービーが自動で選ぶ。',
  '', '選び方：① 両端とも子どもが知っている物（終わりの呼び名は network_meta の aha.kid）② 見た目がかけ離れている（部品 ↔ 原料）③ 道が短い（2〜4 個）④ 出典のある丸だけ（◎○）', ''];
for (const id of PRODUCTS) {
  const inS = anc(id); inS.add(id);
  out.push(`## [${id}] ${nm(id)}`, '');
  let list;
  if (CURATED[id]) {
    list = CURATED[id].map(c => ({ ...c, path: c.path && c.path.every((k, i) => i === 0 || (parents.get(c.path[i - 1]) || []).includes(k)) ? c.path : bfs(c.part, c.end, inS) }));   // 手で決めた道は、つながりが全部あるときだけ
  } else {
    const parts = (parents.get(id) || []).filter(p => byId.has(p)), cand = [];
    for (const p of parts.length ? parts : [id]) for (const e of Object.keys(KID)) if (inS.has(e) && e !== p) {
      const path = bfs(p, e, inS); if (!path) continue;
      const n = path.length, src = path.every(k => byId.get(k).src);
      cand.push({ part: p, end: e, path, score: (byId.get(p).kind === 'product' ? 2 : 0) + (n >= 2 && n <= 4 ? 3 : n <= 6 ? 1 : 0) + (src ? 2 : 0), src });
    }
    cand.sort((a, b) => b.score - a.score || a.path.length - b.path.length);
    const usedE = new Map(); list = [];
    for (const c of cand) { if ((usedE.get(c.end) || 0) >= 2) continue; usedE.set(c.end, (usedE.get(c.end) || 0) + 1); list.push(c); if (list.length >= 5) break; }
    list = list.map(c => ({ ...c, text: `あ！「${nm(c.part)}」をたどると「${KID[c.end]}」に行きつく`, srcs: [...new Set(c.path.flatMap(srcOf))].slice(0, 3) }));
  }
  if (!list.length) { out.push('（子どもが知っている原料までの道が無い。aha.kid に足すか、手で選ぶ）', ''); continue; }
  list.forEach((c, i) => {
    out.push(`${i + 1}. **${c.text}**`);
    out.push(`   - 道（${c.path ? c.path.length : '?'} 個）：${c.path ? c.path.map(nm).join(' ← ') : '（グラフに道が無い）'}`);
    if (c.note) out.push(`   - 確かめたこと：${c.note}（確かさ ${c.cert}）`);
    const ss = c.src || c.srcs || [];
    out.push(`   - 出典：${ss.length ? ss.join(' ') : '（丸の出典を見る）'}${c.score != null ? `（自動の点数 ${c.score}${c.src ? '' : '、出典の無い丸あり'}）` : ''}`);
    out.push(`   - 選ぶときの書き方：\`{"part": "${c.part}", "end": "${c.end}", "text": "${c.text}"${c.path && CURATED[id] ? `, "path": ${JSON.stringify(c.path)}` : ''}}\``);
  });
  out.push('');
}
writeFileSync(ROOT + 'docs/aha_candidates.md', out.join('\n') + '\n');
console.log('wrote docs/aha_candidates.md');
