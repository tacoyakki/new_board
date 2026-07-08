const token = localStorage.getItem('demomo_token');
const content = document.querySelector('#accountContent');
const toastEl = document.querySelector('#toast');

const escapeHtml = value => {
  const el = document.createElement('div');
  el.textContent = value ?? '';
  return el.innerHTML;
};
const initials = name => [...name][0]?.toUpperCase() || '?';

function toast(message) {
  toastEl.textContent = message;
  toastEl.classList.add('show');
  setTimeout(() => toastEl.classList.remove('show'), 2500);
}

async function api(path, options = {}) {
  const res = await fetch(path, {
    ...options,
    headers: {'Content-Type': 'application/json', Authorization: `Bearer ${token}`, ...options.headers}
  });
  if (!res.ok) {
    let message = '요청을 처리하지 못했어요.';
    try { message = (await res.json()).message || message; } catch {}
    throw new Error(message);
  }
  return res.status === 204 ? null : res.json();
}

async function uploadImage(file) {
  const formData = new FormData();
  formData.append('file', file);
  const res = await fetch('/api/members/me/profile-image', {
    method: 'POST',
    headers: {Authorization: `Bearer ${token}`},
    body: formData
  });
  if (!res.ok) {
    let message = '프로필 사진을 업로드하지 못했어요.';
    try { message = (await res.json()).message || message; } catch {}
    throw new Error(message);
  }
  return res.json();
}

function avatar(profile) {
  return profile.profileImageUrl
    ? `<img src="${escapeHtml(profile.profileImageUrl)}" alt="${escapeHtml(profile.nickname)}의 프로필 사진">`
    : initials(profile.nickname);
}

function render(profile) {
  content.innerHTML = `<div class="profile-panel">
    <div class="profile-visual">
      <div class="profile-avatar-large" id="profilePreview">${avatar(profile)}</div>
      <p class="profile-handle">@${escapeHtml(profile.username)}</p>
      <p class="profile-preview-note">JPG, PNG, GIF, WebP<br>최대 5MB까지 업로드할 수 있어요.</p>
    </div>
    <form class="profile-form" id="profileForm">
      <label>프로필 사진
        <input name="profileImage" type="file" accept="image/jpeg,image/png,image/gif,image/webp">
      </label>
      <label>닉네임
        <input name="nickname" maxlength="30" placeholder="사람들에게 보일 이름" value="${escapeHtml(profile.nickname || '')}">
      </label>
      <label>소개
        <textarea name="bio" maxlength="300" placeholder="나를 소개하는 짧은 문장을 적어주세요.">${escapeHtml(profile.bio || '')}</textarea>
      </label>
      <button class="button primary" type="submit">프로필 저장하기</button>
    </form>
  </div>
  <section style="display:flex;align-items:center;justify-content:space-between;gap:28px;margin-top:30px;padding:28px 32px;border:1px solid rgba(231,95,95,.35);border-radius:18px;background:rgba(120,35,35,.08)">
    <div><h2 style="margin:0 0 8px;font-size:17px">회원 탈퇴</h2><p style="margin:0;color:var(--muted);font-size:12px;line-height:1.7">작성한 게시글과 댓글을 포함한 Demomo 데이터가 모두 삭제되며 되돌릴 수 없습니다.</p></div>
    <button class="button" style="border-color:rgba(231,95,95,.55);color:#ff9d9d;white-space:nowrap" id="withdrawButton" type="button">회원 탈퇴</button>
  </section>`;

  const form = document.querySelector('#profileForm');
  const preview = document.querySelector('#profilePreview');
  let previewUrl = null;

  form.profileImage.addEventListener('change', () => {
    const file = form.profileImage.files[0];
    if (!file) return;
    if (file.size > 5 * 1024 * 1024) {
      form.profileImage.value = '';
      toast('프로필 사진은 5MB 이하만 선택해 주세요.');
      return;
    }
    if (previewUrl) URL.revokeObjectURL(previewUrl);
    previewUrl = URL.createObjectURL(file);
    preview.innerHTML = `<img src="${previewUrl}" alt="프로필 사진 미리보기">`;
  });

  form.nickname.addEventListener('input', () => {
    if (!profile.profileImageUrl && !form.profileImage.files[0]) {
      preview.textContent = initials(form.nickname.value || profile.username);
    }
  });

  form.addEventListener('submit', async event => {
    event.preventDefault();
    const button = form.querySelector('button[type="submit"]');
    button.disabled = true;
    button.textContent = '저장 중...';
    try {
      let imageUrl = profile.profileImageUrl || '';
      const imageFile = form.profileImage.files[0];
      if (imageFile) imageUrl = (await uploadImage(imageFile)).profileImageUrl;
      const updated = await api('/api/members/me', {
        method: 'PUT',
        body: JSON.stringify({nickname: form.nickname.value, bio: form.bio.value, profileImageUrl: imageUrl})
      });
      if (previewUrl) URL.revokeObjectURL(previewUrl);
      render(updated);
      toast('프로필을 저장했어요.');
    } catch (error) {
      toast(error.message);
      button.disabled = false;
      button.textContent = '프로필 저장하기';
    }
  });

  document.querySelector('#withdrawButton').addEventListener('click', async () => {
    if (prompt('탈퇴하려면 “탈퇴”라고 입력해 주세요.') !== '탈퇴') return;
    const button = document.querySelector('#withdrawButton');
    button.disabled = true;
    button.textContent = '탈퇴 처리 중...';
    try {
      await api('/api/members/me', {method: 'DELETE'});
      localStorage.removeItem('demomo_token');
      localStorage.removeItem('demomo_refresh_token');
      alert('Demomo 회원 탈퇴가 완료되었습니다. Google 연결 권한은 Google 계정의 서드 파티 연결에서 별도로 해제할 수 있습니다.');
      location.href = '/boards.html';
    } catch (error) {
      toast(error.message);
      button.disabled = false;
      button.textContent = '회원 탈퇴';
    }
  });
}

document.querySelector('#pageLogout').addEventListener('click', async () => {
  try { await api('/api/members/logout', {method: 'POST'}); } catch {}
  localStorage.removeItem('demomo_token');
  localStorage.removeItem('demomo_refresh_token');
  location.href = '/boards.html';
});

if (!token) {
  content.innerHTML = '<div class="account-message"><h2>로그인이 필요해요</h2><p>마이페이지는 로그인 후 이용할 수 있습니다.</p><a class="button primary" href="/boards.html?login=true">로그인하러 가기</a></div>';
} else {
  api('/api/members/me').then(render).catch(error => {
    content.innerHTML = `<div class="account-message"><h2>프로필을 불러오지 못했어요</h2><p>${escapeHtml(error.message)}</p><a class="button primary" href="/boards.html">게시글로 돌아가기</a></div>`;
  });
}
