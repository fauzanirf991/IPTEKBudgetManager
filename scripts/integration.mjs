import { spawnSync } from 'node:child_process';
const commands = [
  [process.execPath, ['--test','firebase/tests/rules.test.mjs']],
  [process.platform === 'win32' ? 'gradlew.bat' : './gradlew', [':budget-core:test', ':processor:test', ':processor:installDist', ':processor:admin', '-PadminArgs=seed-demo', '--rerun-tasks', '--console=plain']],
  [process.execPath, ['scripts/http-smoke.mjs']]
];
for (const [command,args] of commands) {
  const result = spawnSync(command,args,{stdio:'inherit',shell:process.platform === 'win32' && command.endsWith('.bat'), env:{...process.env,GOOGLE_CLOUD_PROJECT:'demo-iptek',STORAGE_EMULATOR_HOST:'http://127.0.0.1:9199'}});
  if (result.status !== 0) process.exit(result.status || 1);
}
