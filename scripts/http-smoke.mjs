import { spawn } from 'node:child_process';
import { join } from 'node:path';
import assert from 'node:assert/strict';
import { randomUUID } from 'node:crypto';

const port=18081;
const java=join(process.env.JAVA_HOME,'bin',process.platform==='win32'?'java.exe':'java');
const child=spawn(java,['-cp','processor/build/install/processor/lib/*','com.fauzan.budget.server.Server'],{
  env:{...process.env,PORT:String(port),GOOGLE_CLOUD_PROJECT:'demo-iptek',STORAGE_EMULATOR_HOST:'http://127.0.0.1:9199'},stdio:['ignore','pipe','pipe']
});
let logs=''; child.stdout.on('data',s=>logs+=s);child.stderr.on('data',s=>logs+=s);
const base=`http://127.0.0.1:${port}`;
async function signIn(email){
  const result=await fetch(`http://${process.env.FIREBASE_AUTH_EMULATOR_HOST}/identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=demo-api-key`,{
    method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({email,password:'LocalDemo123!',returnSecureToken:true})
  });
  const json=await result.json();assert.equal(result.status,200,JSON.stringify(json));return json.idToken;
}
async function request(route,token,payload={}){
  const result=await fetch(base+route,{method:'POST',headers:{'Content-Type':'application/json',...(token?{Authorization:`Bearer ${token}`}:{})},body:JSON.stringify({organization:'iptek',...payload})});
  return {status:result.status,body:await result.json()};
}
try {
  let ready=false;
  for(let i=0;i<60;i++){
    try { if((await fetch(base+'/health')).ok){ready=true;break;} }catch{}
    if(child.exitCode!==null)throw new Error(logs);
    await new Promise(resolve=>setTimeout(resolve,500));
  }
  assert(ready,'Processor did not start: '+logs);
  const editor=await signIn('bendahara@iptek.test'),viewer=await signIn('ketua@iptek.test');
  assert.equal((await request('/v1/snapshot',null)).status,401);
  const date=new Date().toISOString().slice(0,10);
  const command={schemaVersion:1,id:randomUUID(),kind:'CASH',title:'HTTP smoke contribution',businessDate:date,periodId:date.slice(0,4),account:'KAS',amount:1000,quantity:0,unitPrice:0,eventId:'',targetId:'',receiptPath:''};
  assert.equal((await request('/v1/operations',viewer,{command})).status,403);
  const first=await request('/v1/operations',editor,{command});assert.equal(first.status,200,JSON.stringify(first.body));assert.equal(first.body.state,'ACCEPTED');
  assert.equal((await request('/v1/operations',editor,{command})).body.state,'ACCEPTED');
  assert.equal((await request('/v1/operations',editor,{command:{...command,amount:1000.5}})).status,400);
  const snapshot=await request('/v1/snapshot',viewer);assert.equal(snapshot.status,200);assert(snapshot.body.complete);assert.equal(snapshot.body.ownerUid,'ketua');
  assert.equal(snapshot.body.records.filter(r=>r.id===command.id).length,1);
  console.log('HTTP smoke passed: Firebase login, token/role checks, local-command replay, exact money, viewer snapshot.');
} finally { child.kill(); }
