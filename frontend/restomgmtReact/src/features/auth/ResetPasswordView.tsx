import React, { useState } from 'react';
import { authApi } from '../../api/authApi';
import { theme } from '../../theme';
import { LoadingButton } from '../../components/LoadingButton';

export const ResetPasswordView: React.FC<{ token: string; onDone: () => void }> = ({ token, onDone }) => {
    const [newPassword, setNewPassword] = useState('');
    const [status, setStatus] = useState<'idle' | 'loading' | 'done' | 'error'>('idle');
    const [message, setMessage] = useState('');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setStatus('loading');
        try {
            const msg = await authApi.resetPassword(token, newPassword);
            setMessage(msg);
            setStatus('done');
        } catch (err) {
            setMessage(err instanceof Error ? err.message : 'Reset failed');
            setStatus('error');
        }
    };

    return (
        <div style={{ backgroundColor: theme.colors.bg, minHeight: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center', fontFamily: theme.font }}>
            <div style={{ backgroundColor: theme.colors.surface, padding: '40px', borderRadius: theme.radius.lg, width: '360px', boxShadow: theme.shadow.elevated, textAlign: 'center' }}>
                <h2 style={{ margin: '0 0 16px 0', fontSize: '18px', color: theme.colors.textPrimary }}>Set a new password</h2>
                {status === 'done' ? (
                    <>
                        <p style={{ fontSize: '13px', color: theme.colors.textSecondary, marginBottom: '20px' }}>{message}</p>
                        <button onClick={onDone} style={submitBtn}>Go to Login</button>
                    </>
                ) : (
                    <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                        <input type="password" placeholder="new password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} required style={inputStyle} />
                        {status === 'error' && <p style={{ color: theme.colors.dangerText, fontSize: '13px', margin: 0 }}>{message}</p>}
                        <LoadingButton
                            type="submit"
                            loading={status === 'loading'}
                            loadingText="Saving..."
                            style={{ backgroundColor: theme.colors.brand, color: 'white', border: 'none', padding: '12px', borderRadius: theme.radius.sm, fontWeight: 'bold', marginTop: '10px' }}
                        >
                            Reset Password
                        </LoadingButton>
                    </form>
                )}
            </div>
        </div>
    );
};

const inputStyle: React.CSSProperties = { padding: '12px', borderRadius: '6px', border: '1px solid #ccc', fontSize: '14px', backgroundColor: '#f9f9f9' };
const submitBtn: React.CSSProperties = { backgroundColor: theme.colors.brand, color: 'white', border: 'none', padding: '12px', borderRadius: theme.radius.sm, fontWeight: 'bold', cursor: 'pointer' };