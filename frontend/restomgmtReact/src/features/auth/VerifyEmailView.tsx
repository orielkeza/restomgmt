import React, { useEffect, useState } from 'react';
import { authApi } from '../../api/authApi';
import { theme } from '../../theme';

export const VerifyEmailView: React.FC<{ token: string; onDone: () => void }> = ({ token, onDone }) => {
    const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');
    const [message, setMessage] = useState('');

    useEffect(() => {
        authApi.verifyEmail(token)
            .then((msg) => { setMessage(msg); setStatus('success'); })
            .catch((err) => { setMessage(err instanceof Error ? err.message : 'Verification failed'); setStatus('error'); });
    }, [token]);

    return (
        <div style={{ backgroundColor: theme.colors.bg, minHeight: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center', fontFamily: theme.font }}>
            <div style={{ backgroundColor: theme.colors.surface, padding: '40px', borderRadius: theme.radius.lg, width: '360px', boxShadow: theme.shadow.elevated, textAlign: 'center' }}>
                <div style={{ fontSize: '32px', marginBottom: '12px' }}>{status === 'success' ? '✅' : status === 'error' ? '⚠️' : '⏳'}</div>
                <p style={{ fontSize: '14px', color: theme.colors.textSecondary, marginBottom: '20px' }}>
                    {status === 'loading' ? 'Verifying your email…' : message}
                </p>
                {status !== 'loading' && (
                    <button onClick={onDone} style={{ backgroundColor: theme.colors.brand, color: 'white', border: 'none', padding: '12px 24px', borderRadius: theme.radius.sm, fontWeight: 'bold', cursor: 'pointer' }}>
                        Go to Login
                    </button>
                )}
            </div>
        </div>
    );
};