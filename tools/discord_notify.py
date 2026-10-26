"""Discord の Webhook に完了通知を送る（CLAUDE.md「完了通知」）。

使い方: python tools/discord_notify.py <JSON ファイル> [絵のファイル]
  - 絵を付けると multipart で送る（payload_json ＋ files[0]。S-032 の見比べの写真など、リポジトリに入れない絵）
  - URL はリポジトリ直下の .discord_webhook（git に入らない）から読む。URL を引数・画面に出さない
  - JSON の中身（content・embeds・tts）は呼ぶ側が Write で作る
  - .claude/settings.json で「聞かずに実行」に入っている（python tools/*）
"""
import pathlib
import sys
import urllib.request

root = pathlib.Path(__file__).resolve().parent.parent
hook = root / ".discord_webhook"
if len(sys.argv) not in (2, 3):
    sys.exit("使い方: python tools/discord_notify.py <JSON ファイル> [絵のファイル]")
if not hook.exists():
    sys.exit(".discord_webhook がない（このフォルダでは通知しない）")
body = pathlib.Path(sys.argv[1]).read_bytes()
ctype = "application/json"
if len(sys.argv) == 3:
    img = pathlib.Path(sys.argv[2])
    b = "----semilab" + "0123456789abcdef"
    crlf = bytes([13, 10])
    parts = [f"--{b}".encode() + crlf + b'Content-Disposition: form-data; name="payload_json"' + crlf + b"Content-Type: application/json" + crlf + crlf + body + crlf,
             f"--{b}".encode() + crlf + f'Content-Disposition: form-data; name="files[0]"; filename="{img.name}"'.encode() + crlf + b"Content-Type: image/png" + crlf + crlf + img.read_bytes() + crlf,
             f"--{b}--".encode() + crlf]
    body = b"".join(parts)
    ctype = "multipart/form-data; boundary=" + b
req = urllib.request.Request(hook.read_text(encoding="utf-8").strip(), data=body, method="POST",
                             headers={"Content-Type": ctype,
                                      "User-Agent": "semiconductor-lab-notify (python urllib)"})
with urllib.request.urlopen(req, timeout=20) as r:
    print("送った:", r.status)
