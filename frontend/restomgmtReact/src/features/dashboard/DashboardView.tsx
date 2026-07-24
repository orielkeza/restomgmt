import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { type RootState, type AppDispatch } from '../../store/store';
import { fetchAllOrders } from '../order/orderSlice';
import { fetchAllItemsAdmin } from '../menu/menuSlice';
import { fetchUsers } from '../users/userSlice';
import { PageLoader } from '../../components/PageLoader';
import { theme } from '../../theme';

export const DashboardView: React.FC = () => {
    const dispatch = useDispatch<AppDispatch>();
    const { allOrders, status: orderStatus } = useSelector((state: RootState) => state.orders);
    const { items, status: menuStatus } = useSelector((state: RootState) => state.menu);
    const { users, status: userStatus } = useSelector((state: RootState) => state.users);

    useEffect(() => {
        if (orderStatus === 'idle') dispatch(fetchAllOrders());
        if (menuStatus === 'idle') dispatch(fetchAllItemsAdmin());
        if (userStatus === 'idle') dispatch(fetchUsers());
    }, [orderStatus, menuStatus, userStatus, dispatch]);

    if (orderStatus === 'loading' || orderStatus === 'idle') {
        return <PageLoader label="Loading dashboard…" />;
    }

    const pendingOrders = allOrders.filter((o) => o.status === 'PENDING').length;
    const inProgress = allOrders.filter((o) => !['PENDING', 'DELIVERED', 'CANCELLED'].includes(o.status)).length;
    const unavailableItems = items.filter((i) => !i.available).length;
    const totalRevenue = allOrders
        .filter((o) => o.status !== 'CANCELLED')
        .reduce((sum, o) => sum + o.total, 0);

    const statCards = [
        { label: 'Pending Orders', value: pendingOrders, bg: theme.colors.warningBg, fg: theme.colors.warningText },
        { label: 'Orders In Progress', value: inProgress, bg: theme.colors.infoBg, fg: theme.colors.infoText },
        { label: 'Unavailable Items', value: unavailableItems, bg: theme.colors.dangerBg, fg: theme.colors.dangerText },
        { label: 'Total Users', value: users.length, bg: theme.colors.successBg, fg: theme.colors.successText },
    ];

    const recentOrders = [...allOrders]
        .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
        .slice(0, 6);

    return (
        <div style={{ fontFamily: theme.font }}>
            <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
                gap: '16px', marginBottom: '28px',
            }}>
                {statCards.map((c) => (
                    <div key={c.label} style={{ background: c.bg, borderRadius: theme.radius.md, padding: '20px' }}>
                        <div style={{ fontSize: '13px', color: c.fg, fontWeight: 600, opacity: 0.85 }}>{c.label}</div>
                        <div style={{ fontSize: '28px', fontWeight: 'bold', color: c.fg }}>{c.value}</div>
                    </div>
                ))}
            </div>

            <div style={{
                background: theme.colors.surface, borderRadius: theme.radius.md,
                boxShadow: theme.shadow.card, padding: '24px', marginBottom: '24px',
            }}>
                <div style={{ fontSize: '13px', color: theme.colors.textSecondary, marginBottom: '4px' }}>Total Revenue (non-cancelled)</div>
                <div style={{ fontSize: '26px', fontWeight: 700, color: theme.colors.textPrimary }}>{totalRevenue.toLocaleString()} RWF</div>
            </div>

            <div style={{ background: theme.colors.surface, borderRadius: theme.radius.md, boxShadow: theme.shadow.card, overflow: 'hidden' }}>
                <div style={{ padding: '16px 20px', borderBottom: `1px solid ${theme.colors.border}`, fontWeight: 700, color: theme.colors.textPrimary }}>
                    Recent Orders
                </div>
                {recentOrders.length === 0 ? (
                    <div style={{ padding: '32px', textAlign: 'center', color: theme.colors.textSecondary }}>No orders yet.</div>
                ) : (
                    <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                        <tbody>
                            {recentOrders.map((o) => (
                                <tr key={o.orderId} style={{ borderBottom: `1px solid ${theme.colors.border}`, fontSize: '13px' }}>
                                    <td style={{ padding: '12px 20px', fontWeight: 700 }}>#{o.orderId}</td>
                                    <td style={{ padding: '12px', color: theme.colors.textSecondary }}>{o.username}</td>
                                    <td style={{ padding: '12px', color: theme.colors.textSecondary }}>{new Date(o.createdAt).toLocaleString()}</td>
                                    <td style={{ padding: '12px', fontWeight: 600 }}>{o.total.toLocaleString()} RWF</td>
                                    <td style={{ padding: '12px 20px', textAlign: 'right', color: theme.colors.textSecondary }}>{o.status}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    );
};