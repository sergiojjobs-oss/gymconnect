document.getElementById('loginForm').addEventListener('submit', function (e) {
  e.preventDefault();
  const email = document.getElementById('email').value;
  const password = document.getElementById('password').value;

  // TODO: conectar con el backend (POST /api/auth/login)
  console.log('Login con:', email);
  alert('Login pendiente de conectar con el backend.');
});
