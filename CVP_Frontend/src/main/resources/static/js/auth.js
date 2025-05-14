// auth.js

function saveToken(token) {
    localStorage.setItem('jwtToken', token);
}

function removeToken() {
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('userType');
}

function getToken() {
    return localStorage.getItem('jwtToken');
}

function redirectToLogin() {
    const userType = localStorage.getItem('userType');
    window.location.href = userType === 'organizer' ? "/auth/organizer" : "/auth/volunteers/login";
}

async function fetchWithAuth(url, method = 'GET', data = null) {
    const token = getToken();
    const userType = localStorage.getItem('userType');

    if (!token || !userType) {
        redirectToLogin();
        return;
    }

    const headers = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };

    const options = {
        method: method.toUpperCase(),
        headers: headers
    };

    if (data && (method === 'POST' || method === 'PUT' || method === 'DELETE')) {
        options.body = JSON.stringify(data);
    }

    try {
        const response = await fetch(url, options);

        if (response.status === 401) {
            alert('Session expired. Please login again.');
            removeToken();
            redirectToLogin();
            return;
        }

        if (!response.ok) {
            throw new Error(`Request failed: ${response.status}`);
        }

        // If response has no content (204), don't try to parse JSON
        return response.status !== 204 ? await response.json() : null;
    } catch (err) {
        console.error("API Error:", err.message);
        throw err;
    }
}
