param([ValidateSet('build','emulators','seed','processor','test')][string]$Action = 'build')
$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
if (-not $env:JAVA_HOME) {
    $localJdk = Get-ChildItem .tools/jdk -Directory -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($localJdk) { $env:JAVA_HOME = $localJdk.FullName }
}
if (-not $env:JAVA_HOME) { throw 'Install JDK 21 and set JAVA_HOME.' }
$env:PATH = "$env:JAVA_HOME/bin;$env:PATH"
$env:GOOGLE_CLOUD_PROJECT = 'demo-iptek'
$env:FIREBASE_AUTH_EMULATOR_HOST = '127.0.0.1:9099'
$env:FIRESTORE_EMULATOR_HOST = '127.0.0.1:8180'
$env:STORAGE_EMULATOR_HOST = 'http://127.0.0.1:9199'
switch ($Action) {
    build { & ./gradlew.bat :app:assembleEmulatorDebug :app:lintEmulatorDebug :budget-core:test }
    emulators { & npx.cmd firebase emulators:start --project demo-iptek }
    seed { & ./gradlew.bat :processor:admin '-PadminArgs=seed-demo' }
    processor { & ./gradlew.bat :processor:run }
    test { & npx.cmd firebase emulators:exec --project demo-iptek --only auth,firestore,storage 'node scripts/integration.mjs' }
}
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
