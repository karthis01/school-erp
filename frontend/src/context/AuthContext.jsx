import { createContext, useContext, useState } from 'react';
import api from '../api/axios';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('erp_user');
    return stored ? JSON.parse(stored) : null;
  });

  // Tenant users log in as "username@schoolcode"; super-admin (platform) accounts use a
  // plain username. `force=true` tells the backend to kick out any existing session for
  // this account instead of returning a 409 conflict.
  async function login(username, password, force = false) {
    const { data } = await api.post('/auth/login', { username, password, force });
    localStorage.setItem('erp_token', data.token);
    const userInfo = {
      username: data.username,
      fullName: data.fullName,
      role: data.role,
      schoolCode: data.schoolCode || null,
      schoolName: data.schoolName || null,
    };
    localStorage.setItem('erp_user', JSON.stringify(userInfo));
    setUser(userInfo);
    return userInfo;
  }

  async function logout() {
    try {
      await api.post('/auth/logout');
    } catch {
      // even if the network call fails, still clear the local session below
    }
    localStorage.removeItem('erp_token');
    localStorage.removeItem('erp_user');
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
