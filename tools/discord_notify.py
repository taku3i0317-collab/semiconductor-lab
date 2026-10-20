"""Discord の Webhook に完了通知を送る（CLAUDE.md「完了通知」）。

使い方: python tools/discord_notify.py <JSON ファイル>
  - URL はリポジトリ直下の .discord_webhook（git に入らない）から読む。URL を引数・画面に出さない
  - JSON の中身（content・embeds・tts）は呼ぶ側が Write で作る
  - .claude/settings.json で「聞かずに実行」に入っている（python tools/*）
"""
import pathlib
import sys
import urllib.request

root = pathlib.Path(__file__).resolve().parent.parent
hook = root / ".discord_webhook"
if len(sys.argv) != 2:
    sys.exit("使い方: python tools/discord_notify.py <JSON ファイル>")
if not hook.exists():
    sys.exit(".discord_webhook がない（このフォルダでは通知しない）")
body = pathlib.Path(sys.argv[1]).read_bytes()
req = urllib.request.Request(hook.read_text(encoding="utf-8").strip(), data=body, method="POST",
                             headers={"Content-Type": "application/json",
                                      "User-Agent": "semiconductor-lab-notify (python urllib)"})
with urllib.request.urlopen(req, timeout=20) as r:
    print("送った:", r.status)
