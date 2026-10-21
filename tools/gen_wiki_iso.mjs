// S-057：ウィキの「斜め上から見た絵」を作る（wiki/iso/<名前>.png）。絵を描くのは wiki/viewer.js と同じ（3D で見る と同じ見た目）。
// ヘッドレスの Chrome（CDP_PORT）で wiki のページを開き、V3D.png() を呼んで保存する。Mojang の絵は使わない（バニラの所は単色）
// 使い方：CDP_PORT=<番号> node tools/gen_wiki_iso.mjs（docs を http://127.0.0.1:8791/ で出しているとき）
import { writeFileSync, mkdirSync, readFileSync } from 'node:fs';
const port = +process.env.CDP_PORT, base = 'http://127.0.0.1:8791/';
const sleep = ms => new Promise(r => setTimeout(r, ms));
let list; for (let i = 0; i < 100; i++) { try { list = await (await fetch(`http://127.0.0.1:${port}/json/list`)).json(); if (list.some(t => t.type === 'page')) break; } catch {} await sleep(200); }
const page = list.find(t => t.type === 'page'); const ws = new WebSocket(page.webSocketDebuggerUrl); await new Promise(r => ws.onopen = r);
let id = 0; const pend = new Map(), ev = [];
ws.onmessage = m => { const d = JSON.parse(m.data); if (d.id && pend.has(d.id)) { pend.get(d.id)(d); pend.delete(d.id); } else ev.push(d); };
const send = (method, params = {}) => new Promise(r => { const i = ++id; pend.set(i, r); ws.send(JSON.stringify({ id: i, method, params })); });
await send('Page.enable'); await send('Runtime.enable');
await send('Page.navigate', { url: base + 'wiki/machines/acheson_furnace.html' }); await sleep(2500);
const models = JSON.parse(readFileSync('docs/wiki/models.json', 'utf8'));
const ids = [...Object.keys(models.models), ...models.multiblocks.map(m => m.controller_block).filter(b => !models.models[b])];   // マルチブロックの制御のブロックも（組み上がった形）
mkdirSync('docs/wiki/iso', { recursive: true });
let n = 0;
for (const b of ids) {
  const r = await send('Runtime.evaluate', { expression: `V3D.png(${JSON.stringify(b)}, 256)`, awaitPromise: true, returnByValue: true });
  const url = r.result && r.result.result && r.result.result.value;
  if (url && url.startsWith('data:image/png')) { writeFileSync(`docs/wiki/iso/${b.split(':')[1]}.png`, Buffer.from(url.split(',')[1], 'base64')); n++; }
}
console.log(`iso: ${n} / ${ids.length}`);
ws.close(); process.exit(0);
