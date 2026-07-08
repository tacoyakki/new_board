document.head.insertAdjacentHTML('beforeend','<link rel="stylesheet" href="/shared-ui.css?v=4">');

function validToken(token) {
  if (!token) return false;
  try {
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
    return !payload.exp || payload.exp * 1000 > Date.now();
  } catch {
    return false;
  }
}

const headerToken = localStorage.getItem('demomo_token');
const headerSignedIn = validToken(headerToken);
if (!headerSignedIn) {
  localStorage.removeItem('demomo_token');
  localStorage.removeItem('demomo_refresh_token');
}

document.querySelectorAll('[data-logged-in]').forEach(element => element.hidden = !headerSignedIn);
document.querySelectorAll('[data-logged-out]').forEach(element => element.hidden = headerSignedIn);

document.querySelectorAll('[data-logout]').forEach(button => {
  button.addEventListener('click', async () => {
    try {
      await fetch('/api/members/logout', {
        method: 'POST',
        headers: {Authorization: `Bearer ${headerToken}`}
      });
    } catch {}
    localStorage.removeItem('demomo_token');
    localStorage.removeItem('demomo_refresh_token');
    location.href = '/boards.html';
  });
});
