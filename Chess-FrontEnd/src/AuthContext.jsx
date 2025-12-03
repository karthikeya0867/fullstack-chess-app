import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [authToken, setAuthToken] = useState(localStorage.getItem('authToken'));
    const [userId, setUserId] = useState(localStorage.getItem('userId')); // Assuming backend provides userId

    useEffect(() => {
        if (authToken) {
            localStorage.setItem('authToken', authToken);
        } else {
            localStorage.removeItem('authToken');
        }
        if (userId) {
            localStorage.setItem('userId', userId);
        } else {
            localStorage.removeItem('userId');
        }
    }, [authToken, userId]);

    const login = (token, id) => {
        setAuthToken(token);
        setUserId(id);
    };

    const logout = () => {
        setAuthToken(null);
        setUserId(null);
    };

    return (
        <AuthContext.Provider value={{ authToken, userId, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    return useContext(AuthContext);
};
