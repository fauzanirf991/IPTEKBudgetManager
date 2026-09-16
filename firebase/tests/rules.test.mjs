import { readFile } from 'node:fs/promises';
import { before, after, test } from 'node:test';
import { initializeTestEnvironment, assertFails, assertSucceeds } from '@firebase/rules-unit-testing';
import { doc, getDoc, setDoc } from 'firebase/firestore';
import { ref, uploadBytes, getBytes } from 'firebase/storage';
let env;
before(async () => {
  env = await initializeTestEnvironment({projectId:'demo-iptek', firestore:{rules:await readFile('firebase/firestore.rules','utf8')}, storage:{rules:await readFile('firebase/storage.rules','utf8')}});
  await env.withSecurityRulesDisabled(async c => {
    for (const [uid,role] of [['editor','BENDAHARA'],['viewer','KETUA']]) await setDoc(doc(c.firestore(),`organizations/rules/members/${uid}`),{role,active:true});
    await setDoc(doc(c.firestore(),'organizations/rules/records/accepted'),{actorUid:'editor',kind:'CASH'});
  });
});
after(async () => { await env?.cleanup(); });
test('members can read records, anonymous and outsiders cannot',async()=>{
  for (const uid of ['editor','viewer']) await assertSucceeds(getDoc(doc(env.authenticatedContext(uid).firestore(),'organizations/rules/records/accepted')));
  await assertFails(getDoc(doc(env.unauthenticatedContext().firestore(),'organizations/rules/records/accepted')));
  await assertFails(getDoc(doc(env.authenticatedContext('outsider').firestore(),'organizations/rules/records/accepted')));
});
test('all client ledger, balance, audit and role writes are denied',async()=>{
  for(const uid of ['viewer','editor']) for(const path of ['records/forged','accounts/KAS','auditEvents/forged','members/viewer'])
    await assertFails(setDoc(doc(env.authenticatedContext(uid).firestore(),`organizations/rules/${path}`),{role:'BENDAHARA',balance:999,active:true}));
});
test('only editor may create their own bounded image, cannot overwrite',async()=>{
  const path='organizations/rules/receipts/editor/upload/receipt';
  await assertSucceeds(uploadBytes(ref(env.authenticatedContext('editor').storage(),path),new Uint8Array([1,2,3]),{contentType:'image/jpeg'}));
  await assertFails(uploadBytes(ref(env.authenticatedContext('editor').storage(),path),new Uint8Array([4]),{contentType:'image/jpeg'}));
  await assertFails(uploadBytes(ref(env.authenticatedContext('viewer').storage(),'organizations/rules/receipts/viewer/no/receipt'),new Uint8Array([1]),{contentType:'image/jpeg'}));
  await assertFails(uploadBytes(ref(env.authenticatedContext('editor').storage(),'organizations/rules/receipts/editor/exe/receipt'),new Uint8Array([1]),{contentType:'application/octet-stream'}));
  await assertFails(getBytes(ref(env.authenticatedContext('viewer').storage(),path)));
});
