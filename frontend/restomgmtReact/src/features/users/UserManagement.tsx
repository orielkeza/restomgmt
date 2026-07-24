import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { type RootState, type AppDispatch } from '../../store/store';
import { fetchUsers, deleteUser, assignRole, removeRole } from './userSlice';
import { AdminCreateUserModal } from './AdminCreateUserModal';
import { showToast } from '../../components/toast/toastBus';
import { PageLoader } from '../../components/PageLoader';
import { LoadingButton } from '../../components/LoadingButton';
import { theme } from '../../theme';

const ASSIGNABLE_ROLES = ['ROLE_USER', 'ROLE_STAFF', 'ROLE_ADMIN'];

export const UserManagementView: React.FC = () => {
    const dispatch = useDispatch<AppDispatch>();
    const { users, status, pendingUserIds, error } = useSelector((state: RootState) => state.users);
    const [showCreateModal, setShowCreateModal] = useState(false);

    useEffect(() => {
        if (status === 'idle') dispatch(fetchUsers());
    }, [status, dispatch]);

    const handleAssign = async (userId: number, roleName: string) => {
        if (!roleName) return;
        const result = await dispatch(assignRole({ userId, roleName }));
        if (assignRole.fulfilled.match(result)) {
            showToast(`Role ${roleName.replace('ROLE_', '')} assigned`, 'success');
        } else {
            showToast((result.payload as string) ?? 'Failed to assign role', 'error');
        }
    };

    const handleRemove = async (userId: number, roleName: string) => {
        const result = await dispatch(removeRole({ userId, roleName }));
        if (removeRole.fulfilled.match(result)) {
            showToast(`Role ${roleName.replace('ROLE_', '')} removed`, 'success');
        } else {
            showToast((result.payload as string) ?? 'Failed to remove role', 'error');
        }
    };

    const handleDelete = async (id: number, username: string) => {
        if (!window.confirm(`Delete ${username}?`)) return;
        const result = await dispatch(deleteUser(id));
        if (deleteUser.fulfilled.match(result)) {
            showToast('User deleted', 'success');
        } else {
            showToast((result.payload as string) ?? 'Failed to delete user', 'error');
        }
    };

    if (status === 'loading' || status === 'idle') {
        return <PageLoader label="Loading users…" />;
    }
    if (status === 'failed') {
        return <div style={{ padding: '40px', textAlign: 'center', color: theme.colors.dangerText }}>Couldn't load users: {error}</div>;
    }

    return (
        <div style={{ fontFamily: theme.font }}>
            <button
                onClick={() => setShowCreateModal(true)}
                style={{ marginBottom: '16px', padding: '10px 18px', borderRadius: theme.radius.sm, border: 'none', background: theme.colors.brand, color: 'white', fontWeight: 'bold', cursor: 'pointer' }}
            >
                + Create User
            </button>

            <div style={{ background: theme.colors.surface, borderRadius: theme.radius.md, boxShadow: theme.shadow.card, overflow: 'hidden' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                    <thead>
                        <tr style={{ borderBottom: `1px solid ${theme.colors.border}`, color: theme.colors.textSecondary, fontSize: '13px' }}>
                            <th style={{ padding: '14px 16px' }}>Username</th>
                            <th style={{ padding: '14px 16px' }}>Full Name</th>
                            <th style={{ padding: '14px 16px' }}>Email</th>
                            <th style={{ padding: '14px 16px' }}>Status</th>
                            <th style={{ padding: '14px 16px' }}>Assign Role</th>
                            <th style={{ padding: '14px 16px' }}>Remove Role</th>
                            <th style={{ padding: '14px 16px' }} />
                        </tr>
                    </thead>
                    <tbody>
                        {users.map((u) => {
                            const isPending = pendingUserIds.includes(u.id);
                            return (
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
                                            disabled={isPending}
                                            defaultValue=""
                                            onChange={(e) => { handleAssign(u.id, e.target.value); e.target.value = ''; }}
                                            style={{ padding: '6px 8px', borderRadius: theme.radius.sm, border: `1px solid ${theme.colors.border}`, fontSize: '12px' }}
                                        >
                                            <option value="" disabled>Assign…</option>
                                            {ASSIGNABLE_ROLES.map((r) => (
                                                <option key={r} value={r}>{r.replace('ROLE_', '')}</option>
                                            ))}
                                        </select>
                                    </td>
                                    <td style={{ padding: '16px' }}>
                                        <select
                                            disabled={isPending}
                                            defaultValue=""
                                            onChange={(e) => { handleRemove(u.id, e.target.value); e.target.value = ''; }}
                                            style={{ padding: '6px 8px', borderRadius: theme.radius.sm, border: `1px solid ${theme.colors.border}`, fontSize: '12px' }}
                                        >
                                            <option value="" disabled>Remove…</option>
                                            {ASSIGNABLE_ROLES.map((r) => (
                                                <option key={r} value={r}>{r.replace('ROLE_', '')}</option>
                                            ))}
                                        </select>
                                    </td>
                                    <td style={{ padding: '16px' }}>
                                        <LoadingButton
                                            loading={isPending}
                                            onClick={() => handleDelete(u.id, u.username)}
                                            style={{ border: 'none', background: 'none', color: theme.colors.dangerText, fontSize: '13px' }}
                                        >
                                            Delete
                                        </LoadingButton>
                                    </td>
                                </tr>
                            );
                        })}
                    </tbody>
                </table>
            </div>

            {showCreateModal && <AdminCreateUserModal onClose={() => setShowCreateModal(false)} />}
        </div>
    );
};