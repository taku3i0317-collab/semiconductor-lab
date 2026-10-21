// インスタのストーリー用の縦の動画（S-023）：ミニムービー（docs/film.js）を 1080×1920・30fps・15秒の MP4（H.264、音なし）に書き出す。
// ヘッドレスの Chrome で docs/mockups/S017_opening.html を 360×640（3倍の細かさ＝1080×1920）で開き、ムービーの時計（window.FILM_CLOCK）を
// 1コマずつ進めて画面を写し、ffmpeg でつなぐ。最後の2秒は「技術ネットワーク」と URL。上下の端（約 250px）には大事な物を置かない（film.js の story）。
// 使い方：docs をローカルのサーバー（例 python -m http.server 8791 -d docs）で出してから
//   node tools/make_story.mjs [番号=B-10] [出力=build/instagram/story_smartphone.mp4]
// Chrome は自分で起動し、終わったら自分が起動した PID だけを止める。ffmpeg が要る
import { spawn, spawnSync } from 'node:child_process';
import { mkdirSync, writeFileSync, mkdtempSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join, dirname } from 'node:path';

const id = process.argv[2] || 'B-10', out = process.argv[3] || 'build/instagram/story_smartphone.mp4';
const base = 'http://127.0.0.1:8791/', FPS = 30, SECONDS = 15, FILM_SEC = 12.5, CENTER_SEC = 0.5;   // 0〜12.5秒：ムービー、〜13秒：中心図、〜15秒：終わりの画面
const URL = 'https://taku3i0317-collab.github.io/tech-network/';
const CHROME = process.env.CHROME || 'C:/Program Files/Google/Chrome/Application/chrome.exe';
const PORT = 9670 + Math.floor(Math.random() * 20);
const frames = mkdtempSync(join(tmpdir(), 'tn-story-'));
mkdirSync(dirname(out), { recursive: true });
const prof = mkdtempSync(join(tmpdir(), 'tn-story-prof-'));
const chrome = spawn(CHROME, ['--headless=new', `--remote-debugging-port=${PORT}`, `--user-data-dir=${prof}`, '--no-first-run', '--disable-extensions', 'about:blank'], { stdio: 'ignore' });
const sleep = ms => new Promise(r => setTimeout(r, ms));
let list;
for (let i = 0; i < 100; i++) { try { list = await (await fetch(`http://127.0.0.1:${PORT}/json/list`)).json(); if (list.some(t => t.type === 'page')) break; } catch {} await sleep(200); }
const page = list.find(t => t.type === 'page');
const ws = new WebSocket(page.webSocketDebuggerUrl); await new Promise(r => ws.onopen = r);
let n = 0; const pend = new Map(), ev = [];
ws.onmessage = m => { const d = JSON.parse(m.data); if (d.id && pend.has(d.id)) { pend.get(d.id)(d); pend.delete(d.id); } else ev.push(d); };
const send = (method, params = {}) => new Promise(r => { const i = ++n; pend.set(i, r); ws.send(JSON.stringify({ id: i, method, params })); });
const js = async x => (await send('Runtime.evaluate', { expression: x, returnByValue: true, awaitPromise: true })).result.result?.value;
await send('Page.enable'); await send('Runtime.enable');
await send('Emulation.setEmulatedMedia', { features: [{ name: 'prefers-color-scheme', value: 'light' }, { name: 'prefers-reduced-motion', value: 'no-preference' }] });
await send('Emulation.setDeviceMetricsOverride', { width: 360, height: 640, deviceScaleFactor: 3, mobile: true });
await send('Page.addScriptToEvaluateOnNewDocument', { source: 'window.FILM_STORY = true; window.FILM_CLOCK = 0; try { localStorage.setItem("tn-lang", "ja"); } catch (e) {}' });
await send('Page.navigate', { url: `${base}mockups/S017_opening.html?id=${encodeURIComponent(id)}` });
for (let i = 0; i < 300 && !ev.some(e => e.method === 'Page.loadEventFired'); i++) await sleep(50);
// 人物のデータとフォントとアイコンを読み終わるまで待つ
await js('new Promise(r => { const c = () => window.TECH_DETAIL ? document.fonts.ready.then(() => setTimeout(r, 1500)) : setTimeout(c, 100); c(); })');
const total = await js(`window.TECH_GRAPH.nodes.length`);
// ムービーの長さ（film.js と同じ時間割）：t5 まで
const t5 = await js(`(() => { const G = window.TECH_GRAPH, par = new Map(); for (const [f, t, k] of G.edges) if ('rep'.includes(k)) (par.get(t) || par.set(t, []).get(t)).push(f);
  const by = new Set(G.nodes.map(x => x.id)), d = new Map([['${id}', 0]]); let q = ['${id}'], L = 0;
  while (q.length) { const nq = []; for (const u of q) for (const p of par.get(u) || []) if (!d.has(p) && by.has(p)) { d.set(p, d.get(u) + 1); L = Math.max(L, d.get(p)); nq.push(p); } q = nq; }
  const P2 = Math.max(1800, Math.min(6500, L * 750)); return 1400 + P2 + 700 + 1800 + 2400 + 2300; })()`);
const K = t5 / (FILM_SEC * 1000);   // ムービーを少し速めて 12.5 秒に入れる
console.log(`film ${id}: ${t5} ms -> ${FILM_SEC} s (x${K.toFixed(2)}), nodes ${total}`);
// 終わりの画面（最後の2秒）：真ん中に「技術ネットワーク」と URL。上下の端は空ける
await js(`(() => { const d = document.createElement('div'); d.id = 'endcard';
  d.style.cssText = 'position:fixed;inset:0;z-index:200;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:14px;text-align:center;background:color-mix(in srgb, var(--bg) 88%, transparent);opacity:0;padding:90px 20px';
  d.innerHTML = '<div style="font-family:var(--font-pixel);font-size:38px;color:var(--ink)">技術ネットワーク</div>' +
    '<div style="font-size:17px;color:var(--sub)">石器から AI まで、${total} の技術・モノ・発見のつながり</div>' +
    '<div style="font-size:15px;font-weight:700;color:var(--accent);word-break:break-all">${URL}</div>';
  document.body.appendChild(d); })()`);
const N = FPS * SECONDS;
for (let f = 0; f < N; f++) {
  const v = f / FPS;
  const clock = v < FILM_SEC ? v * 1000 * K : t5 + 100 + (v - FILM_SEC) * 1000;
  const card = v < FILM_SEC + CENTER_SEC ? 0 : Math.min(1, (v - FILM_SEC - CENTER_SEC) / 0.4);
  await js(`window.FILM_CLOCK = ${clock}; document.getElementById('endcard').style.opacity = ${card};
    document.querySelectorAll('.film .cap').forEach(c => c.style.animation = 'none');
    new Promise(r => requestAnimationFrame(() => requestAnimationFrame(r)))`);
  const shot = await send('Page.captureScreenshot', { format: 'png' });
  writeFileSync(join(frames, `f${String(f).padStart(4, '0')}.png`), Buffer.from(shot.result.data, 'base64'));
  if (f % 60 === 0) console.log(`frame ${f}/${N}`);
}
ws.close();
try { process.kill(chrome.pid); } catch {}
const r = spawnSync('ffmpeg', ['-y', '-loglevel', 'error', '-framerate', String(FPS), '-i', join(frames, 'f%04d.png'),
  '-c:v', 'libx264', '-pix_fmt', 'yuv420p', '-crf', '18', '-preset', 'slow', '-movflags', '+faststart', '-an', out], { stdio: 'inherit' });
await sleep(500);
try { rmSync(frames, { recursive: true, force: true }); rmSync(prof, { recursive: true, force: true }); } catch {}
console.log(r.status === 0 ? `wrote ${out}` : 'ffmpeg failed');
process.exit(r.status || 0);
