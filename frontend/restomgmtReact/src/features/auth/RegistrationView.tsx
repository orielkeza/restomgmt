import React, { useState } from 'react';
import { useDispatch } from 'react-redux';
import { loginSuccess } from './authSlice';
import { theme } from '../../theme';

interface RegistrationViewProps {
    onSwitchToLogin: () => void;
}

export const RegistrationView: React.FC<RegistrationViewProps> = ({ onSwitchToLogin }) => {
    const dispatch = useDispatch();

    // keeps track of what the user types in real-time
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [email, setEmail] = useState('');
    const [error, setError] = useState('');

    // handles submission
    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault(); // prevent from reloading the entire page

        if (username.trim() === '' || password.trim() === '' || email.trim() === '') {
            setError('Please fill in all the fields');
            return;
        }

        setError('');

        // sends the action to redux so that the user is logged in globally
        dispatch(loginSuccess(username));
    };

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
                    Registration
                </h2>

                <button style={{
                    width: '100%',
                    padding: '10px',
                    backgroundColor: '#f5f5f5',
                    border: '1px solid #ddd',
                    borderRadius: theme.radius.sm,
                    cursor: 'pointer',
                    marginBottom: '20px',
                    fontSize: '14px',
                }}>
                    Login with Google
                </button>

                {error && <p style={{ color: 'red', fontSize: '14px' }}>{error}</p>}

                <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                    <input
                        type="text"
                        placeholder="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        style={inputStyle}
                    />
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
                    <button type="submit" style={{
                        backgroundColor: theme.colors.brand,
                        color: 'white',
                        border: 'none',
                        padding: '12px',
                        borderRadius: theme.radius.sm,
                        fontWeight: 'bold',
                        cursor: 'pointer',
                        marginTop: '10px',
                    }}>
                        Create account
                    </button>
                </form>

                <p style={{ marginTop: '20px', fontSize: '14px', color: '#555' }}>
                    Already have an account?{' '}
                    <span
                        onClick={onSwitchToLogin}
                        style={{ color: theme.colors.brand, cursor: 'pointer', fontWeight: 'bold' }}
                    >
                        Sign In
                    </span>
                </p>
            </div>
        </div>
    );
};

// reusable basic css object for inputs
const inputStyle: React.CSSProperties = {
    padding: '12px',
    borderRadius: '6px',
    border: '1px solid #ccc',
    fontSize: '14px',
    backgroundColor: '#f9f9f9',
};