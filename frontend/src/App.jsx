import { useEffect, useMemo, useState } from 'react';

const API_BASE = '/api';
const initialAccountForm = {
  accNumber: '',
  firstName: '',
  lastName: '',
  balance: ''
};

function App() {
  const [token, setToken] = useState(() => localStorage.getItem('token') || '');
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('user');
    return stored ? JSON.parse(stored) : null;
  });
  const [loginForm, setLoginForm] = useState({ username: '', password: '' });
  const [registerForm, setRegisterForm] = useState({ username: '', email: '', password: '', role: 'CUSTOMER' });
  const [accountForm, setAccountForm] = useState(initialAccountForm);
  const [accounts, setAccounts] = useState([]);
  const [status, setStatus] = useState({ message: '', type: '' });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (token) {
      localStorage.setItem('token', token);
    } else {
      localStorage.removeItem('token');
    }
  }, [token]);

  useEffect(() => {
    if (user) {
      localStorage.setItem('user', JSON.stringify(user));
    } else {
      localStorage.removeItem('user');
    }
  }, [user]);

  const isAuthenticated = useMemo(() => Boolean(token), [token]);

  const headers = (json = true) => {
    const defaultHeaders = {};
    if (json) defaultHeaders['Content-Type'] = 'application/json';
    if (token) defaultHeaders['Authorization'] = `Bearer ${token}`;
    return defaultHeaders;
  };

  const resetStatus = () => setStatus({ message: '', type: '' });

  const handleResponse = async (response) => {
    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
      const errorMessage = data.error || data.message || 'Request failed';
      throw new Error(errorMessage);
    }
    return data;
  };

  const login = async (event) => {
    event.preventDefault();
    resetStatus();
    setLoading(true);

    try {
      const response = await fetch(`${API_BASE}/auth/login`, {
        method: 'POST',
        headers: headers(),
        body: JSON.stringify(loginForm)
      });
      const data = await handleResponse(response);
      setToken(data.token);
      setUser({ username: data.username, role: data.role });
      setStatus({ message: 'Login successful', type: 'success' });
      setLoginForm({ username: '', password: '' });
    } catch (error) {
      setStatus({ message: error.message, type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const register = async (event) => {
    event.preventDefault();
    resetStatus();
    setLoading(true);

    try {
      const response = await fetch(`${API_BASE}/auth/register`, {
        method: 'POST',
        headers: headers(),
        body: JSON.stringify(registerForm)
      });
      const data = await handleResponse(response);
      setStatus({ message: data.message || 'Registration successful', type: 'success' });
      setRegisterForm({ username: '', email: '', password: '', role: 'CUSTOMER' });
    } catch (error) {
      setStatus({ message: error.message, type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const fetchAccounts = async () => {
    resetStatus();
    setLoading(true);

    try {
      const response = await fetch(`${API_BASE}/accounts`, {
        method: 'GET',
        headers: headers(false)
      });
      const data = await handleResponse(response);
      setAccounts(Array.isArray(data) ? data : []);
      setStatus({ message: 'Accounts loaded', type: 'success' });
    } catch (error) {
      setStatus({ message: error.message, type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const createAccount = async (event) => {
    event.preventDefault();
    resetStatus();
    setLoading(true);

    const payload = {
      accNumber: Number(accountForm.accNumber),
      firstName: accountForm.firstName.trim(),
      lastName: accountForm.lastName.trim(),
      balance: Number(accountForm.balance)
    };

    try {
      const response = await fetch(`${API_BASE}/accounts`, {
        method: 'POST',
        headers: headers(),
        body: JSON.stringify(payload)
      });
      const data = await handleResponse(response);
      setStatus({ message: 'Account created successfully', type: 'success' });
      setAccountForm(initialAccountForm);
      fetchAccounts();
    } catch (error) {
      setStatus({ message: error.message, type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    setToken('');
    setUser(null);
    setAccounts([]);
    resetStatus();
  };

  return (
    <div className="page-shell">
      <header>
        <h1>Banking System React UI</h1>
        <div className="status-bar">
          <span>{isAuthenticated ? `Logged in as ${user?.username} (${user?.role})` : 'Not authenticated'}</span>
          {isAuthenticated && (
            <button className="secondary" onClick={logout} type="button">
              Logout
            </button>
          )}
        </div>
      </header>

      <main>
        <section className="card-grid">
          <article className="card">
            <h2>Login</h2>
            <form onSubmit={login}>
              <label>
                Username
                <input
                  value={loginForm.username}
                  onChange={(e) => setLoginForm({ ...loginForm, username: e.target.value })}
                  required
                />
              </label>
              <label>
                Password
                <input
                  type="password"
                  value={loginForm.password}
                  onChange={(e) => setLoginForm({ ...loginForm, password: e.target.value })}
                  required
                />
              </label>
              <button type="submit" disabled={loading}>
                Login
              </button>
            </form>
          </article>

          <article className="card">
            <h2>Register</h2>
            <form onSubmit={register}>
              <label>
                Username
                <input
                  value={registerForm.username}
                  onChange={(e) => setRegisterForm({ ...registerForm, username: e.target.value })}
                  required
                />
              </label>
              <label>
                Email
                <input
                  type="email"
                  value={registerForm.email}
                  onChange={(e) => setRegisterForm({ ...registerForm, email: e.target.value })}
                  required
                />
              </label>
              <label>
                Password
                <input
                  type="password"
                  value={registerForm.password}
                  onChange={(e) => setRegisterForm({ ...registerForm, password: e.target.value })}
                  required
                />
              </label>
              <label>
                Role
                <select
                  value={registerForm.role}
                  onChange={(e) => setRegisterForm({ ...registerForm, role: e.target.value })}
                >
                  <option value="CUSTOMER">Customer</option>
                  <option value="MANAGER">Manager</option>
                  <option value="ADMIN">Admin</option>
                </select>
              </label>
              <button type="submit" disabled={loading}>
                Register
              </button>
            </form>
          </article>
        </section>

        <section className="card">
          <h2>Account Actions</h2>
          <button type="button" onClick={fetchAccounts} disabled={!isAuthenticated || loading}>
            Load Accounts
          </button>
          <form onSubmit={createAccount} className="account-form">
            <label>
              Account Number
              <input
                type="number"
                value={accountForm.accNumber}
                onChange={(e) => setAccountForm({ ...accountForm, accNumber: e.target.value })}
                required
              />
            </label>
            <label>
              First Name
              <input
                value={accountForm.firstName}
                onChange={(e) => setAccountForm({ ...accountForm, firstName: e.target.value })}
                required
              />
            </label>
            <label>
              Last Name
              <input
                value={accountForm.lastName}
                onChange={(e) => setAccountForm({ ...accountForm, lastName: e.target.value })}
                required
              />
            </label>
            <label>
              Balance
              <input
                type="number"
                value={accountForm.balance}
                onChange={(e) => setAccountForm({ ...accountForm, balance: e.target.value })}
                required
              />
            </label>
            <button type="submit" disabled={!isAuthenticated || loading}>
              Create Account
            </button>
          </form>
        </section>

        {status.message && (
          <section className={`alert ${status.type === 'error' ? 'error' : 'success'}`}>
            {status.message}
          </section>
        )}

        <section className="card">
          <h2>Token & API Info</h2>
          <textarea value={token} readOnly placeholder="Token appears after login" />
        </section>

        <section className="card">
          <h2>Accounts</h2>
          {accounts.length === 0 ? (
            <p>No accounts loaded yet.</p>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Number</th>
                  <th>Name</th>
                  <th>Balance</th>
                </tr>
              </thead>
              <tbody>
                {accounts.map((account) => (
                  <tr key={account.id}>
                    <td>{account.id}</td>
                    <td>{account.accNumber}</td>
                    <td>{account.firstName} {account.lastName}</td>
                    <td>{account.balance}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>
      </main>
    </div>
  );
}

export default App;
