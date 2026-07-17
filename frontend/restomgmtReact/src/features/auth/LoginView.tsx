import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { loginUser } from './authSlice';
import { type RootState, type AppDispatch } from '../../store/store';
import { theme } from '../../theme';

interface LoginViewProps {
    onSwitchToRegister: () => void;
}

export const LoginView: React.FC<LoginViewProps> = ({ onSwitchToRegister }) => {
    const dispatch = useDispatch<AppDispatch>();
    const { loginStatus, loginError } = useSelector((state: RootState) => state.auth);

    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [formError, setFormError] = useState('');

    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();

        if (username.trim() === '' || password.trim() === '') {
            setFormError('Please fill in all the fields');
            return;
        }

        setFormError('');
        dispatch(loginUser({ username, password }));
    };

    const displayError = formError || loginError;

    return (
        <div style={{
            backgroundColor: theme.colors.bg,
            minHeight: '100vh',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            fontFamily: theme.font,
        }}>
            <div style={{
                backgroundColor: theme.colors.surface,
                padding: '40px',
                borderRadius: theme.radius.lg,
                width: '360px',
                boxShadow: theme.shadow.elevated,
                textAlign: 'center',
            }}>
                <h1 style={{ margin: '0 0 4px 0', fontSize: '24px', color: theme.colors.textPrimary }}>
                    Restaurant Management
                </h1>
                <h2 style={{ margin: '0 0 24px 0', fontSize: '18px', color: theme.colors.textSecondary }}>
                    Login
                </h2>

                {displayError && (
                    <p style={{ color: theme.colors.dangerText, fontSize: '13px', background: theme.colors.dangerBg, padding: '8px 12px', borderRadius: theme.radius.sm }}>
                        {displayError}
                    </p>
                )}

                <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px', marginTop: displayError ? '16px' : 0 }}>
                    <input
                        type="text"
                        placeholder="username"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        style={inputStyle}
                    />
                    <input
                        type="password"
                        placeholder="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        style={inputStyle}
                    />
                    <button
                        type="submit"
                        disabled={loginStatus === 'loading'}
                        style={{
                            backgroundColor: theme.colors.brand,
                            color: 'white',
                            border: 'none',
                            padding: '12px',
                            borderRadius: theme.radius.sm,
                            fontWeight: 'bold',
                            cursor: loginStatus === 'loading' ? 'not-allowed' : 'pointer',
                            opacity: loginStatus === 'loading' ? 0.7 : 1,
                            marginTop: '10px',
                        }}
                    >
                        {loginStatus === 'loading' ? 'Logging in…' : 'Log In'}
                    </button>
                </form>

                <p style={{ marginTop: '20px', fontSize: '14px', color: '#555' }}>
                    New here?{' '}
                    <span
                        onClick={onSwitchToRegister}
                        style={{ color: theme.colors.brand, cursor: 'pointer', fontWeight: 'bold' }}
                    >
                        Sign Up
                    </span>
                </p>
            </div>
        </div>
    );
};

const inputStyle: React.CSSProperties = {
    padding: '12px',
    borderRadius: '6px',
    border: '1px solid #ccc',
    fontSize: '14px',
    backgroundColor: '#f9f9f9',
};