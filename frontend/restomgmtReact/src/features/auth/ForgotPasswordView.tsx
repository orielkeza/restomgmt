import React, { useState } from 'react';
import { authApi } from '../../api/authApi';
import { theme } from '../../theme';
import { LoadingButton } from '../../components/LoadingButton';

export const ForgotPasswordView: React.FC<{ onBack: () => void }> = ({ onBack }) => {
    const [email, setEmail] = useState('');
    const [status, setStatus] = useState<'idle' | 'loading' | 'sent' | 'error'>('idle');
    const [message, setMessage] = useState('');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setStatus('loading');
        try {
            const msg = await authApi.forgotPassword(email);
            setMessage(msg);
            setStatus('sent');
        } catch (err) {
            setMessage(err instanceof Error ? err.message : 'Something went wrong');
            setStatus('error');
        }
    };

    return (
        <div style={{ backgroundColor: theme.colors.bg, minHeight: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center', fontFamily: theme.font }}>
            <div style={{ backgroundColor: theme.colors.surface, padding: '40px', borderRadius: theme.radius.lg, width: '360px', boxShadow: theme.shadow.elevated, textAlign: 'center' }}>
                <h2 style={{ margin: '0 0 8px 0', fontSize: '18px', color: theme.colors.textPrimary }}>Reset your password</h2>
                {status === 'sent' ? (
                    <>
                        <p style={{ fontSize: '13px', color: theme.colors.textSecondary, margin: '16px 0 24px 0' }}>{message}</p>
                        <button onClick={onBack} style={submitBtn}>Back to Login</button>
                    </>
                ) : (
                    <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px', marginTop: '16px' }}>
                        <p style={{ fontSize: '13px', color: theme.colors.textSecondary, margin: 0 }}>
                            Enter your account email and we'll send a reset link.
                        </p>
                        <input type="email" placeholder="email" value={email} onChange={(e) => setEmail(e.target.value)} required style={inputStyle} />
                        {status === 'error' && <p style={{ color: theme.colors.dangerText, fontSize: '13px', margin: 0 }}>{message}</p>}
                        <LoadingButton
                            type="submit"
                            loading={status === 'loading'}
                            loadingText="Sending..."
                            style={{ backgroundColor: theme.colors.brand, color: 'white', border: 'none', padding: '12px', borderRadius: theme.radius.sm, fontWeight: 'bold', marginTop: '10px' }}
                        >
                            Send Reset Link
                        </LoadingButton>
                        <span onClick={onBack} style={{ color: theme.colors.brand, cursor: 'pointer', fontWeight: 'bold', fontSize: '13px' }}>
                            Back to Login
                        </span>
                    </form>
                )}
            </div>
        </div>
    );
};

const inputStyle: React.CSSProperties = { padding: '12px', borderRadius: '6px', border: '1px solid #ccc', fontSize: '14px', backgroundColor: '#f9f9f9' };
const submitBtn: React.CSSProperties = { backgroundColor: theme.colors.brand, color: 'white', border: 'none', padding: '12px', borderRadius: theme.radius.sm, fontWeight: 'bold', cursor: 'pointer' };