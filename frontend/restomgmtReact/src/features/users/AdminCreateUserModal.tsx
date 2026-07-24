import React, { useState } from 'react';
import { useDispatch } from 'react-redux';
import { type AppDispatch } from '../../store/store';
import { adminCreateUser } from './userSlice';
import { showToast } from '../../components/toast/toastBus';
import { LoadingButton } from '../../components/LoadingButton';
import { theme } from '../../theme';

export const AdminCreateUserModal: React.FC<{ onClose: () => void }> = ({ onClose }) => {
    const dispatch = useDispatch<AppDispatch>();
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [fullName, setFullName] = useState('');
    const [roleName, setRoleName] = useState('ROLE_USER');
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState('');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!username.trim() || !email.trim()) {
            setError('Username and email are required');
            return;
        }
        setSubmitting(true);
        setError('');

        const result = await dispatch(adminCreateUser({
            username: username.trim(),
            email: email.trim(),
            fullName: fullName.trim() || undefined,
            roleName,
        }));

        setSubmitting(false);

        if (adminCreateUser.fulfilled.match(result)) {
            showToast(`Account created — credentials emailed to ${email}`, 'success');
            onClose();
        } else {
            setError((result.payload as string) ?? 'Failed to create user');
        }
    };

    return (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
            <div style={{ background: theme.colors.surface, borderRadius: theme.radius.lg, padding: '32px', width: '420px', boxShadow: theme.shadow.elevated, fontFamily: theme.font }}>
                <h2 style={{ margin: '0 0 6px 0', fontSize: '18px', color: theme.colors.textPrimary }}>Create User</h2>
                <p style={{ margin: '0 0 20px 0', fontSize: '13px', color: theme.colors.textSecondary }}>
                    A temporary password will be generated and emailed to the user.
                </p>

                {error && (
                    <p style={{ color: theme.colors.dangerText, fontSize: '13px', background: theme.colors.dangerBg, padding: '8px 12px', borderRadius: theme.radius.sm, marginBottom: '16px' }}>
                        {error}
                    </p>
                )}

                <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                    <input placeholder="Username" value={username} onChange={(e) => setUsername(e.target.value)} style={inputStyle} />
                    <input placeholder="Email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} style={inputStyle} />
                    <input placeholder="Full name (optional)" value={fullName} onChange={(e) => setFullName(e.target.value)} style={inputStyle} />
                    <select value={roleName} onChange={(e) => setRoleName(e.target.value)} style={inputStyle}>
                        <option value="ROLE_USER">USER</option>
                        <option value="ROLE_STAFF">STAFF</option>
                        <option value="ROLE_ADMIN">ADMIN</option>
                    </select>

                    <div style={{ display: 'flex', gap: '10px', marginTop: '10px' }}>
                        <button type="button" onClick={onClose} style={{ flex: 1, padding: '11px', borderRadius: theme.radius.sm, border: `1px solid ${theme.colors.border}`, background: 'white', cursor: 'pointer' }}>
                            Cancel
                        </button>
                        <LoadingButton
                            type="submit"
                            loading={submitting}
                            loadingText="Creating…"
                            style={{ flex: 1, padding: '11px', borderRadius: theme.radius.sm, border: 'none', background: theme.colors.brand, color: 'white', fontWeight: 'bold' }}
                        >
                            Create User
                        </LoadingButton>
                    </div>
                </form>
            </div>
        </div>
    );
};

const inputStyle: React.CSSProperties = {
    padding: '11px', borderRadius: '6px', border: '1px solid #ccc', fontSize: '14px',
    backgroundColor: '#f9f9f9', width: '100%', boxSizing: 'border-box',
};