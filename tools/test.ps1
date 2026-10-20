# tools/test.ps1 — 自動テスト（GameTest）を走らせて結果を要約する
#
# 使い方（プロジェクトのフォルダで）:
#   powershell -ExecutionPolicy Bypass -File tools\test.ps1
#   powershell -ExecutionPolicy Bypass -File tools\test.ps1 -Full    # 抽出前の全ログも最後に表示
#
# 中身:
#   1. ./gradlew.bat runGameTest を実行（画面なしの専用サーバーが起動→テスト→自動終了。1〜3分）
#   2. 全ログを build/gametest.log に保存
#   3. 大事な行だけ抜き出して表示（エラー・テスト結果・[LAB] 行）。401/authlib/Realms のノイズは除外
#   4. 終了コード：0＝全部成功、1＝失敗あり（Gradle の終了コードをそのまま返す）
param([switch]$Full)

$ErrorActionPreference = 'Continue'
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root
$log = Join-Path $root 'build\gametest.log'
New-Item -ItemType Directory -Force (Join-Path $root 'build') | Out-Null

# lang のまとめたファイルが部品（src/main/lang/<幹>/）と同じか（IMP-28。違えば python tools/lang_build.py）
python (Join-Path $root 'tools\lang_build.py') --check
if ($LASTEXITCODE -ne 0) { Write-Host '== 失敗：lang が部品と違う（python tools/lang_build.py を実行）' -ForegroundColor Red; exit 1 }

Write-Host '== runGameTest を実行中（1〜3分）...' -ForegroundColor Cyan
$start = Get-Date
# PowerShell 5.1 で native の stderr を 2>&1 すると赤いエラー扱いになるので、
# Start-Process で標準出力・標準エラーを別々のファイルに書かせ、あとでつなげる
$errLog = Join-Path $root 'build\gametest.err.log'
$proc = Start-Process -FilePath (Join-Path $root 'gradlew.bat') -ArgumentList 'runGameTest', '--console=plain' `
    -WorkingDirectory $root -RedirectStandardOutput $log -RedirectStandardError $errLog -NoNewWindow -PassThru
# -Wait は子のプロセス（新しく起きた Gradle デーモン）の終わりまで待って止まるので、gradlew 本体だけを待つ（IMP-30）
$null = $proc.Handle   # これを先に読むと、あとで ExitCode が取れる（PowerShell 5.1）
$proc.WaitForExit()
$code = $proc.ExitCode
if (Test-Path $errLog) { Get-Content $errLog -Encoding UTF8 | Add-Content $log -Encoding UTF8; Remove-Item $errLog }
$sec = [int]((Get-Date) - $start).TotalSeconds

$pattern = 'Caused by|Unbound|No key|Failed to parse|ERROR.*semiconductor-lab|WARN.*semiconductor-lab|validation problem|GameTest.*(FAILED|PASSED)|\[LAB\]|GAME TESTS COMPLETE|tests? (passed|failed)|\)\s+- \S+:|BUILD (SUCCESSFUL|FAILED)|FAILURE:|error:'
$noise = '401|authlib|Realms|server\.properties|eula\.txt'

$lines = Get-Content $log -Encoding UTF8
$hits = $lines | Where-Object { $_ -match $pattern -and $_ -notmatch $noise }

Write-Host ''
Write-Host "== 抽出結果（$($hits.Count) 行 / 全 $($lines.Count) 行、$sec 秒）" -ForegroundColor Cyan
foreach ($l in $hits) {
    if ($l -match 'FAILED|failed|Caused by|ERROR|error:|Unbound|No key|Failed to parse|GameTestAssert|FAILURE') { Write-Host $l -ForegroundColor Red }
    elseif ($l -match '\[VANILLA\]') { Write-Host $l -ForegroundColor Yellow }
    else { Write-Host $l -ForegroundColor Green }
}

if ($Full) { Write-Host ''; Write-Host '== 全ログ' -ForegroundColor Cyan; $lines }

Write-Host ''
if ($code -eq 0) { Write-Host "== 成功（終了コード 0）。全ログ: $log" -ForegroundColor Green }
else { Write-Host "== 失敗（終了コード $code）。全ログ: $log" -ForegroundColor Red }
exit $code
