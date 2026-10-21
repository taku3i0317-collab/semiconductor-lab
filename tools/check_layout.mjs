// 画面のはみ出しの自動検査（S-016）。ヘッドレスの Chrome で、5つの画面の大きさ × いくつかのページを開き、
//  ① 横スクロールが出ていないか（ページの幅 > 画面の幅）
//  ② 画面の右にはみ出す要素が無いか（横にスクロールする箱・図の canvas の中は除く）
// を調べ、画面写真を <出力フォルダ>/<大きさ>_<ページ>.png に残す（言葉の途中の改行は写真を目で見る）。
// 使い方：docs をローカルのサーバー（例 python -m http.server 8791 -d docs）で出してから
//   node tools/check_layout.mjs [出力フォルダ] [ベース URL]
// 見つかったら終了コード 1。Chrome は自分で起動し、終わったら自分が起動した PID だけを止める
import { spawn } from 'node:child_process';
import { mkdirSync, writeFileSync, mkdtempSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';

const out = process.argv[2] || 'build/layout';
const base = (process.argv[3] || 'http://127.0.0.1:8791/').replace(/\/?$/, '/');
const SIZES = [[360, 640, 'android_s'], [390, 844, 'iphone'], [430, 932, 'iphone_max'], [768, 1024, 'tablet'], [1366, 768, 'laptop']];
const PAGES = [['map_start', 'network.html'], ['map_focus', 'network.html#9-23'], ['map_whole', 'network.html#map/9-23'],
  ['node', 'node.html#9-23'], ['node_long', 'node.html#B-10'], ['glossary', 'glossary.html'], ['people', 'people.html']];
const CHROME = process.env.CHROME || 'C:/Program Files/Google/Chrome/Application/chrome.exe';
const PORT = 9620 + Math.floor(Math.random() * 50);
mkdirSync(out, { recursive: true });
const prof = mkdtempSync(join(tmpdir(), 'tn-layout-'));
const chrome = spawn(CHROME, ['--headless=new', `--remote-debugging-port=${PORT}`, `--user-data-dir=${prof}`, '--no-first-run', '--disable-extensions', 'about:blank'], { stdio: 'ignore' });
const sleep = ms => new Promise(r => setTimeout(r, ms));
let list;
for (let i = 0; i < 100; i++) { try { list = await (await fetch(`http://127.0.0.1:${PORT}/json/list`)).json(); if (list.some(t => t.type === 'page')) break; } catch {} await sleep(200); }
const page = list.find(t => t.type === 'page');
const ws = new WebSocket(page.webSocketDebuggerUrl); await new Promise(r => ws.onopen = r);
let id = 0; const pending = new Map(), events = [];
ws.onmessage = m => { const d = JSON.parse(m.data); if (d.id && pending.has(d.id)) { pending.get(d.id)(d); pending.delete(d.id); } else events.push(d); };
const send = (method, params = {}) => new Promise(r => { const i = ++id; pending.set(i, r); ws.send(JSON.stringify({ id: i, method, params })); });
await send('Page.enable'); await send('Runtime.enable');
await send('Emulation.setEmulatedMedia', { features: [{ name: 'prefers-color-scheme', value: 'light' }] });
// ページの中で調べる式：はみ出した要素を「タグ.クラス 右端」で最大 5 つ返す
const PROBE = `(() => {
  const W = document.documentElement.clientWidth, bad = [];
  const scroller = el => { for (let p = el.parentElement; p; p = p.parentElement) { const s = getComputedStyle(p); if (/(auto|scroll|hidden)/.test(s.overflowX) && p !== document.body && p !== document.documentElement) return true; } return false; };
  for (const el of document.body.querySelectorAll('*')) {
    if (el.closest('canvas, svg') || el.closest('[hidden]')) continue;
    const r = el.getBoundingClientRect();
    if (!r.width || !r.height || getComputedStyle(el).visibility === 'hidden') continue;
    if (r.right > W + 1 && !scroller(el)) bad.push(el.tagName.toLowerCase() + (el.id ? '#' + el.id : '') + (el.className && typeof el.className === 'string' ? '.' + el.className.split(' ')[0] : '') + ' ' + Math.round(r.right));
    if (bad.length >= 5) break;
  }
  return JSON.stringify({ hscroll: document.documentElement.scrollWidth > W + 1, W, sw: document.documentElement.scrollWidth, bad });
})()`;
let fails = 0;
for (const [w, h, sname] of SIZES) {
  const mobile = w < 768;
  await send('Emulation.setDeviceMetricsOverride', { width: w, height: h, deviceScaleFactor: mobile ? 2 : 1, mobile });
  await send('Emulation.setTouchEmulationEnabled', { enabled: mobile, maxTouchPoints: mobile ? 5 : 0 });
  for (const [pname, url] of PAGES) {
    events.length = 0;
    await send('Page.navigate', { url: 'about:blank' }); await sleep(150);
    await send('Page.navigate', { url: base + url });
    for (let i = 0; i < 200 && !events.some(e => e.method === 'Page.loadEventFired'); i++) await sleep(50);
    await sleep(2500);
    const r = JSON.parse((await send('Runtime.evaluate', { expression: PROBE, returnByValue: true })).result.result.value);
    const shot = await send('Page.captureScreenshot', { format: 'png' });
    writeFileSync(join(out, `${sname}_${pname}.png`), Buffer.from(shot.result.data, 'base64'));
    const ok = !r.hscroll && !r.bad.length;
    if (!ok) fails++;
    console.log(`${ok ? 'ok  ' : 'FAIL'} ${sname} ${w}x${h} ${pname}${r.hscroll ? ` 横スクロール（幅 ${r.sw} > ${r.W}）` : ''}${r.bad.length ? ' はみ出し: ' + r.bad.join(', ') : ''}`);
  }
}
ws.close();
try { process.kill(chrome.pid); } catch {}
await sleep(500);
try { rmSync(prof, { recursive: true, force: true }); } catch {}
console.log(fails ? `はみ出し ${fails} 件` : 'はみ出し 0 件');
process.exit(fails ? 1 : 0);
