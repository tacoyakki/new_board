document.head.insertAdjacentHTML('beforeend','<link rel="stylesheet" href="/shared-ui.css?v=4">');
const oauthResult=new URLSearchParams(location.hash.slice(1));
const oauthToken=oauthResult.get('oauth_token');
if(oauthToken){localStorage.setItem('demomo_token',oauthToken);history.replaceState(null,'',location.pathname+location.search)}
const oauthFailed=oauthResult.has('oauth_error');
if(oauthFailed)history.replaceState(null,'',location.pathname+location.search);
const state={boards:[],token:localStorage.getItem('demomo_token'),refreshToken:localStorage.getItem('demomo_refresh_token'),username:null,authMode:'login',activeBoard:null};
const $=(s,p=document)=>p.querySelector(s), $$=(s,p=document)=>[...p.querySelectorAll(s)];
const els={grid:$('#postGrid'),empty:$('#emptyState'),authModal:$('#authModal'),writeModal:$('#writeModal'),detail:$('#detailLayer'),drawer:$('#detailDrawer'),toast:$('#toast'),authButton:$('#authButton'),mypageButton:$('#mypageButton'),logoutButton:$('#logoutButton'),search:$('#searchInput'),sort:$('#sortSelect')};

function decodeUser(token){try{const payload=JSON.parse(atob(token.split('.')[1].replace(/-/g,'+').replace(/_/g,'/')));return !payload.exp||payload.exp*1000>Date.now()?payload.sub:null}catch{return null}}
state.username=state.token?decodeUser(state.token):null;
if(!state.username){state.token=null;localStorage.removeItem('demomo_token')}
function authHeader(){return state.token?{Authorization:`Bearer ${state.token}`}:{}}
async function api(path,options={}){const res=await fetch(path,{...options,headers:{'Content-Type':'application/json',...authHeader(),...options.headers}});if(!res.ok){let msg='요청을 처리하지 못했어요.';try{const body=await res.json();msg=body.message||body.error||msg}catch{}if(res.status===401||res.status===403) msg='로그인이 필요하거나 권한이 없어요.';throw new Error(msg)}const text=await res.text();try{return JSON.parse(text)}catch{return text}}
function escapeHtml(value=''){const el=document.createElement('div');el.textContent=value;return el.innerHTML}
function initials(name='?'){return [...name][0]?.toUpperCase()||'?'}
function displayName(item){return item.writerNickname||item.writer}
function profileHref(username){return `/profile.html?user=${encodeURIComponent(username)}`}
function avatarHtml(item){return item.writerProfileImageUrl?`<span class="avatar has-image"><img src="${escapeHtml(item.writerProfileImageUrl)}" alt=""></span>`:`<span class="avatar">${initials(displayName(item))}</span>`}
function profileLink(item){return `<a class="profile-link" href="${profileHref(item.writer)}">${avatarHtml(item)}<b>${escapeHtml(displayName(item))}</b></a>`}
function openLayer(el){el.classList.add('open');el.setAttribute('aria-hidden','false');document.body.style.overflow='hidden'}
function closeLayer(el){el.classList.remove('open');el.setAttribute('aria-hidden','true');if(!$('.open.modal-layer')&&!$('.drawer-layer.open'))document.body.style.overflow=''}
function toast(message){els.toast.textContent=message;els.toast.classList.add('show');clearTimeout(toast.timer);toast.timer=setTimeout(()=>els.toast.classList.remove('show'),2600)}
function updateAuthUI(){const signedIn=Boolean(state.username);els.authButton.hidden=signedIn;els.mypageButton.hidden=!signedIn;els.logoutButton.hidden=!signedIn}
function requireAuth(action){if(!state.token){openLayer(els.authModal);toast('먼저 로그인해 주세요.');return}action()}

async function loadBoards(){els.grid.innerHTML=Array(6).fill('<article class="post-card skeleton"></article>').join('');try{state.boards=await api('/api/boards');renderBoards()}catch(e){els.grid.innerHTML='';els.empty.classList.remove('hidden');els.empty.querySelector('h3').textContent='이야기를 불러오지 못했어요';els.empty.querySelector('p').textContent='서버 연결을 확인한 뒤 다시 시도해 주세요.'}}
function renderBoards(){const q=els.search.value.trim().toLowerCase();let list=state.boards.filter(b=>`${b.title} ${b.content} ${b.writer} ${b.writerNickname||''}`.toLowerCase().includes(q));if(els.sort.value==='views')list.sort((a,b)=>(b.viewCount||0)-(a.viewCount||0));if(els.sort.value==='comments')list.sort((a,b)=>(b.commentCount||0)-(a.commentCount||0));if(els.sort.value==='new')list.sort((a,b)=>b.id-a.id);els.grid.innerHTML=list.map((b,i)=>`<article class="post-card compact-post" data-id="${b.id}" tabindex="0"><span class="post-number">STORY ${String(i+1).padStart(2,'0')}</span><h3>${escapeHtml(b.title)}</h3><span class="post-stat">조회 ${b.viewCount||0}</span><span class="post-stat">댓글 ${b.commentCount||0}</span><div class="post-author">${profileLink(b)}</div></article>`).join('');els.empty.classList.toggle('hidden',list.length>0);$$('.post-card[data-id]').forEach(card=>{card.onclick=()=>openDetail(card.dataset.id);card.onkeydown=e=>{if(e.key==='Enter')openDetail(card.dataset.id)}});$$('.profile-link').forEach(link=>link.onclick=e=>e.stopPropagation())}

async function openDetail(id){openLayer(els.detail);els.drawer.innerHTML='<button class="close-button detail-close" data-close>×</button><div class="skeleton" style="margin-top:70px"></div>';try{const b=await api(`/api/boards/${id}`);state.activeBoard=b;renderDetail(b);const cached=state.boards.find(x=>x.id===b.id);if(cached)cached.viewCount=b.viewCount}catch(e){toast(e.message);closeLayer(els.detail)}}
function renderDetail(b){const own=state.username===b.writer;els.drawer.innerHTML=`<button class="close-button detail-close" data-close aria-label="닫기">×</button><span class="detail-label">DEMOMO STORY · ${String(b.id).padStart(3,'0')}</span><h1>${escapeHtml(b.title)}</h1><div class="detail-author">${profileLink(b)}<span>${escapeHtml(b.createdAt||'')}</span><span>조회 ${b.viewCount||0}</span>${own?'<span class="detail-actions"><button class="mini-button" data-edit>수정</button><button class="mini-button" data-delete>삭제</button></span>':''}</div><div class="detail-content">${escapeHtml(b.content)}</div><section class="comments"><h3>댓글 <span>${b.comments?.length||0}</span></h3><form class="comment-form" id="commentForm"><input name="content" required maxlength="500" placeholder="다정한 한마디를 남겨주세요"><button class="button primary">등록</button></form><div>${(b.comments||[]).map(commentHtml).join('')||'<p class="modal-copy">아직 댓글이 없어요. 첫 댓글을 남겨보세요.</p>'}</div></section>`;els.drawer.querySelector('[data-close]').onclick=()=>closeLayer(els.detail);$('#commentForm').onsubmit=submitComment;if(own){$('[data-delete]',els.drawer).onclick=deleteBoard;$('[data-edit]',els.drawer).onclick=editBoard}}
function commentHtml(c){const own=state.username===c.writer;return `<div class="comment"><div class="comment-head">${profileLink(c)}<time>${escapeHtml(c.createdAt||'')}</time>${own?`<button class="mini-button" data-delete-comment="${c.id}">삭제</button>`:''}</div><p>${escapeHtml(c.content)}</p></div>`}
async function submitComment(e){e.preventDefault();if(!state.token){closeLayer(els.detail);openLayer(els.authModal);return}const content=new FormData(e.currentTarget).get('content').trim();if(!content)return;try{await api(`/api/comments/${state.activeBoard.id}`,{method:'POST',body:JSON.stringify({content})});toast('댓글을 남겼어요.');await openDetail(state.activeBoard.id)}catch(err){toast(err.message)}}
async function deleteBoard(){if(!confirm('이 이야기를 정말 삭제할까요?'))return;try{await api(`/api/boards/${state.activeBoard.id}`,{method:'DELETE'});closeLayer(els.detail);toast('이야기를 삭제했어요.');loadBoards()}catch(e){toast(e.message)}}
function editBoard(){closeLayer(els.detail);openWrite(state.activeBoard)}

function openWrite(board=null){requireAuth(()=>{const form=$('#writeForm');form.dataset.editId=board?.id||'';form.title.value=board?.title||'';form.content.value=board?.content||'';$('#writeTitle').textContent=board?'이야기를 다듬어볼까요?':'무슨 생각을 하고 있나요?';$('#titleCount').textContent=form.title.value.length;openLayer(els.writeModal);setTimeout(()=>form.title.focus(),100)})}
$('#writeForm').onsubmit=async e=>{e.preventDefault();const form=e.currentTarget,data=Object.fromEntries(new FormData(form));const id=form.dataset.editId;try{await api(id?`/api/boards/${id}`:'/api/boards',{method:id?'PUT':'POST',body:JSON.stringify(data)});closeLayer(els.writeModal);form.reset();toast(id?'이야기를 수정했어요.':'이야기를 올렸어요.');loadBoards()}catch(err){toast(err.message)}};
$('#writeForm').title.oninput=e=>$('#titleCount').textContent=e.target.value.length;

let nicknameCheckStarted=false;
async function checkNicknameSetup(showWelcome=false){
  if(!state.token||nicknameCheckStarted)return;
  nicknameCheckStarted=true;
  try{
    const profile=await api('/api/members/me');
    if(profile.nicknameConfigured){
      if(showWelcome)toast(`${profile.nickname}님, 반가워요.`);
      return;
    }
    document.body.insertAdjacentHTML('beforeend',`<div class="modal-layer" id="nicknameModal" aria-hidden="true"><div class="modal auth-modal" role="dialog" aria-modal="true" aria-labelledby="nicknameTitle"><div class="modal-symbol">✦</div><p class="section-kicker">ONE MORE STEP</p><h2 id="nicknameTitle">어떻게 불러드릴까요?</h2><p class="modal-copy">이야기와 댓글에 표시할 닉네임을 정해 주세요.</p><form id="nicknameForm"><label>닉네임<input name="nickname" required maxlength="30" autocomplete="nickname" placeholder="닉네임을 입력하세요"></label><button class="button primary wide" type="submit">시작하기</button></form></div></div>`);
    const modal=$('#nicknameModal');
    openLayer(modal);
    $('#nicknameForm').onsubmit=async event=>{
      event.preventDefault();
      const nickname=event.currentTarget.nickname.value.trim();
      if(!nickname)return;
      try{
        await api('/api/members/me',{method:'PUT',body:JSON.stringify({nickname,bio:profile.bio||'',profileImageUrl:profile.profileImageUrl||''})});
        closeLayer(modal);modal.remove();toast(`${nickname}님, 반가워요.`);
      }catch(error){toast(error.message)}
    };
    setTimeout(()=>$('#nicknameForm').nickname.focus(),100);
  }catch{}
}

function setAuthMode(mode){state.authMode=mode;const password=$('#authForm').password;password.minLength=mode==='signup'?8:1;password.autocomplete=mode==='signup'?'new-password':'current-password';password.setCustomValidity('');$$('[data-auth-tab]').forEach(b=>b.classList.toggle('active',b.dataset.authTab===mode));$('#authTitle').textContent=mode==='login'?'다시 만나 반가워요.':'우리, 처음 만나네요.';$('#authForm button[type="submit"]').textContent=mode==='login'?'로그인':'회원가입';$('.form-hint').innerHTML=mode==='login'?'처음이신가요? <button type="button" data-switch-auth>회원가입</button>':'이미 함께하고 있나요? <button type="button" data-switch-auth>로그인</button>';$('.form-hint button').onclick=()=>setAuthMode(mode==='login'?'signup':'login')}
const authPassword=$('#authForm').password;
authPassword.addEventListener('invalid',()=>{if(state.authMode==='signup'&&authPassword.validity.tooShort)authPassword.setCustomValidity('비밀번호는 8자 이상 입력해 주세요.')});
authPassword.addEventListener('input',()=>authPassword.setCustomValidity(''));
$('#authForm').onsubmit=async e=>{e.preventDefault();const form=e.currentTarget;const data=Object.fromEntries(new FormData(form));try{if(state.authMode==='signup'){await api('/api/members/signup',{method:'POST',body:JSON.stringify(data)});form.password.value='';toast('회원가입이 완료됐어요. 비밀번호를 다시 입력해 로그인해 주세요.');setAuthMode('login');form.password.focus();return}const tokens=await api('/api/members/login',{method:'POST',body:JSON.stringify(data)});state.token=tokens.accessToken;state.refreshToken=tokens.refreshToken;state.username=decodeUser(state.token)||data.username;localStorage.setItem('demomo_token',state.token);localStorage.setItem('demomo_refresh_token',state.refreshToken);updateAuthUI();closeLayer(els.authModal);form.reset();nicknameCheckStarted=false;await checkNicknameSetup(true)}catch(err){toast(err.message)}};

els.authButton.onclick=()=>openLayer(els.authModal);
els.logoutButton.onclick=async()=>{try{await api('/api/members/logout',{method:'POST'})}catch(e){toast(e.message);return}state.token=null;state.refreshToken=null;state.username=null;localStorage.removeItem('demomo_token');localStorage.removeItem('demomo_refresh_token');updateAuthUI();toast('로그아웃했어요.')};
$('#writeButton').onclick=()=>openWrite();$('#heroWrite').onclick=()=>openWrite();$$('[data-open-write]').forEach(b=>b.onclick=()=>openWrite());$('#searchToggle').onclick=()=>{document.querySelector('.content-shell').scrollIntoView();setTimeout(()=>els.search.focus(),350)};els.search.oninput=renderBoards;els.sort.onchange=renderBoards;$$('[data-auth-tab]').forEach(b=>b.onclick=()=>setAuthMode(b.dataset.authTab));$('[data-switch-auth]').onclick=()=>setAuthMode('signup');$$('.modal-layer [data-close]').forEach(b=>b.onclick=()=>closeLayer(b.closest('.modal-layer')));$('.drawer-backdrop').onclick=()=>closeLayer(els.detail);document.addEventListener('keydown',e=>{if(e.key==='Escape')$$('.open').forEach(closeLayer)});els.drawer.addEventListener('click',async e=>{const id=e.target.dataset.deleteComment;if(!id)return;if(!confirm('댓글을 삭제할까요?'))return;try{await api(`/api/comments/${id}`,{method:'DELETE'});toast('댓글을 삭제했어요.');openDetail(state.activeBoard.id)}catch(err){toast(err.message)}});
updateAuthUI();loadBoards();
if(state.token)checkNicknameSetup(Boolean(oauthToken));
if(oauthFailed)setTimeout(()=>toast('Google 로그인에 실패했어요. 다시 시도해 주세요.'),100);
if(new URLSearchParams(location.search).get('login')==='true'){
  history.replaceState({},'',location.pathname);
  if(!state.token)setTimeout(()=>openLayer(els.authModal),100);
}
if(new URLSearchParams(location.search).get('write')==='true'){
  history.replaceState({},'',location.pathname);
  setTimeout(()=>openWrite(),150);
}
