import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { registerUser, clearRegisterStatus } from './authSlice';
import { type RootState, type AppDispatch } from '../../store/store';
import { theme } from '../../theme';

interface RegistrationViewProps {
    onSwitchToLogin: () => void;
}

export const RegistrationView: React.FC<RegistrationViewProps> = ({ onSwitchToLogin }) => {
    const dispatch = useDispatch<AppDispatch>();
    const { registerStatus, registerError } = useSelector((state: RootState) => state.auth);

    const [fullName, setFullName] = useState('');
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [formError, setFormError] = useState('');

    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();

        if (username.trim() === '' || password.trim() === '' || email.trim() === '' || fullName.trim() === '') {
            setFormError('Please fill in all the fields');
            return;
        }

        setFormError('');
        dispatch(registerUser({ username, password, email, fullName }));
    };

    const displayError = formError || registerError;

    // Success state — backend requires email verification before login works
    if (registerStatus === 'succeeded') {
        return (
            <div style={{
                backgroundColor: theme.colors.bg, minHeight: '100vh',
                display: 'flex', justifyContent: 'center', alignItems: 'center', fontFamily: theme.font,
            }}>
                <div style={{
                    backgroundColor: theme.colors.surface, padding: '40px', borderRadius: theme.radius.lg,
                    width: '360px', boxShadow: theme.shadow.elevated, textAlign: 'center',
                }}>
                    <div style={{ fontSize: '40px', marginBottom: '12px' }}>📬</div>
                    <h2 style={{ margin: '0 0 12px 0', fontSize: '18px', color: theme.colors.textPrimary }}>
                        Check your inbox
                    </h2>
                    <p style={{ fontSize: '14px', color: theme.colors.textSecondary, marginBottom: '24px' }}>
                        We sent a verification link to <strong>{email}</strong>. Verify your email before logging in.
                    </p>
                    <button
                        onClick={() => { dispatch(clearRegisterStatus()); onSwitchToLogin(); }}
                        style={{
                            backgroundColor: theme.colors.brand, color: 'white', border: 'none',
                            padding: '12px', borderRadius: theme.radius.sm, fontWeight: 'bold',
                            cursor: 'pointer', width: '100%',
                        }}
                    >
                        Back to Login
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div style={{
            backgroundColor: theme.colors.bg, minHeight: '100vh',
            display: 'flex', justifyContent: 'center', alignItems: 'center', fontFamily: theme.font,
        }}>
            <div style={{
                backgroundColor: theme.colors.surface, padding: '40px', borderRadius: theme.radius.lg,
                width: '360px', boxShadow: theme.shadow.elevated, textAlign: 'center',
            }}>
                <h1 style={{ margin: '0 0 4px 0', fontSize: '24px', color: theme.colors.textPrimary }}>
                    Restaurant Management
                </h1>
                <h2 style={{ margin: '0 0 24px 0', fontSize: '18px', color: theme.colors.textSecondary }}>
                    Registration
                </h2>

                {displayError && (
                    <p style={{ color: theme.colors.dangerText, fontSize: '13px', background: theme.colors.dangerBg, padding: '8px 12px', borderRadius: theme.radius.sm }}>
                        {displayError}
                    </p>
                )}

                <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px', marginTop: displayError ? '16px' : 0 }}>
                    <input type="text" placeholder="full name" value={fullName} onChange={(e) => setFullName(e.target.value)} style={inputStyle} />
                    <input type="text" placeholder="email" value={email} onChange={(e) => setEmail(e.target.value)} style={inputStyle} />
                    <input type="text" placeholder="username" value={username} onChange={(e) => setUsername(e.target.value)} style={inputStyle} />
                    <input type="password" placeholder="password" value={password} onChange={(e) => setPassword(e.target.value)} style={inputStyle} />
                    <button
                        type="submit"
                        disabled={registerStatus === 'loading'}
                        style={{
                            backgroundColor: theme.colors.brand, color: 'white', border: 'none',
                            padding: '12px', borderRadius: theme.radius.sm, fontWeight: 'bold',
                            cursor: registerStatus === 'loading' ? 'not-allowed' : 'pointer',
                            opacity: registerStatus === 'loading' ? 0.7 : 1, marginTop: '10px',
                        }}
                    >
                        {registerStatus === 'loading' ? 'Creating account…' : 'Create account'}
                    </button>
                </form>

                <p style={{ marginTop: '20px', fontSize: '14px', color: '#555' }}>
                    Already have an account?{' '}
                    <span onClick={onSwitchToLogin} style={{ color: theme.colors.brand, cursor: 'pointer', fontWeight: 'bold' }}>
                        Sign In
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