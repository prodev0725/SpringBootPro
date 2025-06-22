document.getElementById('loginForm').addEventListener('submit', async function (e) {
    e.preventDefault();

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    try {
        const response = await fetch('/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });

        if (response.ok) {
            const data = await response.json();
            document.getElementById('message').innerText = 'Login successful!';
            // Save token to localStorage if using JWT
            // localStorage.setItem('token', data.token);
        } else {
            const errorData = await response.json();
            document.getElementById('message').innerText = errorData.message || 'Login failed';
        }
    } catch (err) {
        console.error(err);
        document.getElementById('message').innerText = 'Error connecting to server';
    }
});
