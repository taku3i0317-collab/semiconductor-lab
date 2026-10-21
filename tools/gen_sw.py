"""オフラインで見るための service worker（S-026）。docs/sw.js を作る。

一度開いたら電波が無くても見られるように、ページ・データ・フォントを保存（キャッシュ）する。
版の番号は保存するファイルの中身から作る（データが変われば番号が変わり、古い保存は消える）。
見るときは保存した物をすぐ出し、裏で新しい物を取ってきて保存し直す（次に開いたとき新しくなる）。
使い方：python tools/gen_sw.py（tools/export_tech_network.py も書き出しのたびに呼ぶ）
"""
import hashlib
import json
import sys
from pathlib import Path

DOCS = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(__file__).resolve().parent.parent / "docs"
FILES = ["network.html", "node.html", "glossary.html", "people.html", "credits.html", "design.css",
         "tech_network_graph.js", "tech_network_detail.js", "glossary.js", "autolink.js", "furigana.js", "furigana_dict.js",
         "linebreak.js", "vendor/budoux-ja.min.js", "ui_icons.js", "film.js", "share.js", "pwa.js",
         "manifest.webmanifest", "app_icon_192.png", "app_icon_512.png", "apple-touch-icon.png"]


def main():
    files = [f for f in FILES if (DOCS / f).exists()]
    h = hashlib.sha256()
    for f in files:
        h.update(f.encode()); h.update((DOCS / f).read_bytes())
    ver = h.hexdigest()[:12]
    js = f"""// 自動生成：tools/gen_sw.py（手で直さない）。オフラインで見るための service worker（S-026）
const VERSION = 'tn-{ver}';
const CORE = {json.dumps(['./'] + files, ensure_ascii=False)};
self.addEventListener('install', e => {{
  e.waitUntil(caches.open(VERSION).then(c => Promise.all(CORE.map(u => c.add(u).catch(() => {{}})))).then(() => self.skipWaiting()));
}});
self.addEventListener('activate', e => {{
  e.waitUntil(caches.keys().then(ks => Promise.all(ks.filter(k => k.startsWith('tn-') && k !== VERSION).map(k => caches.delete(k)))).then(() => self.clients.claim()));
}});
// 保存した物をすぐ出し、裏で取り直して保存（次に開いたとき新しい）。フォント（Google Fonts）も同じ。そのほかの外のサイトはそのまま
self.addEventListener('fetch', e => {{
  const r = e.request, u = new URL(r.url);
  if (r.method !== 'GET') return;
  const ok = u.origin === location.origin || /fonts\.(googleapis|gstatic)\.com$/.test(u.hostname) || u.hostname === 'cdnjs.cloudflare.com';
  if (!ok) return;
  e.respondWith(caches.open(VERSION).then(async c => {{
    const key = u.origin === location.origin ? new Request(u.origin + u.pathname) : r;   // ?v= や #番号 は同じ物として保存
    const hit = await c.match(key);
    const net = fetch(r).then(res => {{ if (res && (res.ok || res.type === 'opaque')) c.put(key, res.clone()); return res; }}).catch(() => hit);
    return hit || net;
  }}));
}});
"""
    (DOCS / "sw.js").write_text(js, encoding="utf-8")
    print(f"sw.js: {VERSION if False else ver}, {len(files)} files")


if __name__ == "__main__":
    main()
