import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { type RootState, type AppDispatch } from '../../store/store';
import { fetchUsers, deleteUser, assignRole } from './userSlice';
import { theme } from '../../theme';
import { LoadingButton } from '../../components/LoadingButton';
import { PageLoader } from '../../components/PageLoader';

export const UserManagementView: React.FC = () => {
    const dispatch = useDispatch<AppDispatch>();
    const { users, status, error } = useSelector((state: RootState) => state.users);
    const pendingUserIds = useSelector((state: RootState) => state.users.pendingUserIds);

    useEffect(() => { if (status === 'idle') dispatch(fetchUsers()); }, [status, dispatch]);

    if (status === 'loading' || status === 'idle') {
        return <PageLoader label="Loading users…" />;
    }
    if (status === 'failed') {
        return <div style={{ padding: '40px', textAlign: 'center', color: theme.colors.dangerText }}>Couldn't load users: {error}</div>;
    }

    return (
        <div style={{ fontFamily: theme.font }}>
            <div style={{ background: theme.colors.surface, borderRadius: theme.radius.md, boxShadow: theme.shadow.card, overflow: 'hidden' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                    <thead>
                        <tr style={{ borderBottom: `1px solid ${theme.colors.border}`, color: theme.colors.textSecondary, fontSize: '13px' }}>
                            <th style={{ padding: '14px 16px' }}>Username</th>
                            <th style={{ padding: '14px 16px' }}>Full Name</th>
                            <th style={{ padding: '14px 16px' }}>Email</th>
                            <th style={{ padding: '14px 16px' }}>Status</th>
                            <th style={{ padding: '14px 16px' }}>Role</th>
                            <th style={{ padding: '14px 16px' }} />
                        </tr>
                    </thead>
                    <tbody>
                        {users.map((u) => (
                            <tr key={u.id} style={{ borderBottom: `1px solid ${theme.colors.border}`, fontSize: '14px' }}>
                                <td style={{ padding: '16px', fontWeight: 600 }}>{u.username}</td>
                                <td style={{ padding: '16px' }}>{u.fullName}</td>
                                <td style={{ padding: '16px', color: theme.colors.textSecondary }}>{u.email}</td>
                                <td style={{ padding: '16px' }}>
                                    <span style={{
                                        padding: '4px 10px', borderRadius: '12px', fontSize: '12px', fontWeight: 'bold',
                                        background: u.enabled ? theme.colors.successBg : theme.colors.warningBg,
                                        color: u.enabled ? theme.colors.successText : theme.colors.warningText,
                                    }}>
                                        {u.enabled ? 'Verified' : 'Unverified'}
                                    </span>
                                </td>
                                <td style={{ padding: '16px' }}>
                                    <select
                                        disabled={pendingUserIds.includes(u.id)}
                                        defaultValue=""
                                        onChange={(e) => { if (e.target.value) dispatch(assignRole({ userId: u.id, roleName: e.target.value })); }}
                                        style={{ padding: '6px 8px', borderRadius: theme.radius.sm, border: `1px solid ${theme.colors.border}`, fontSize: '12px' }}
                                    >
                                        <option value="" disabled>Assign role…</option>
                                        <option value="STAFF">STAFF</option>
                                        <option value="ADMIN">ADMIN</option>
                                    </select>
                                </td>
                                <td style={{ padding: '16px' }}>
                                    // Delete button:
                                    <LoadingButton
                                        loading={pendingUserIds.includes(u.id)}
                                        onClick={() => { if (window.confirm(`Delete ${u.username}?`)) dispatch(deleteUser(u.id)); }}
                                        style={{ border: 'none', background: 'none', color: theme.colors.dangerText, fontSize: '13px' }}
                                    >
                                        Delete
                                    </LoadingButton>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};