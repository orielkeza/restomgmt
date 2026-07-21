import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { type RootState, type AppDispatch } from '../../store/store';
import { fetchMyOrders, fetchAllOrders, cancelOrder, advanceOrderStatus } from './orderSlice';
import { type OrderStatus, type OrderResponse } from '../../api/orderApi';
import { theme } from '../../theme';
import { RiderAssignmentForm } from './RiderAissgnmentForm';
import { flagRefund } from '../payments/paymentSlice';
import { PageLoader } from '../../components/PageLoader';
import { LoadingButton } from '../../components/LoadingButton';

const STATUS_ORDER: OrderStatus[] = ['PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'OUTFORDELIVERY', 'DELIVERED'];

function nextStatus(current: OrderStatus): OrderStatus | null {
    const idx = STATUS_ORDER.indexOf(current);
    if (idx === -1 || idx === STATUS_ORDER.length - 1) return null;
    return STATUS_ORDER[idx + 1];
}

function getStatusStyle(status: OrderStatus): React.CSSProperties {
    const base = { padding: '4px 10px', borderRadius: '12px', fontSize: '12px', fontWeight: 'bold' as const };
    switch (status) {
        case 'PENDING':        return { ...base, backgroundColor: theme.colors.warningBg, color: theme.colors.warningText };
        case 'CONFIRMED':      return { ...base, backgroundColor: theme.colors.infoBg, color: theme.colors.infoText };
        case 'PREPARING':      return { ...base, backgroundColor: theme.colors.purpleBg, color: theme.colors.purpleText };
        case 'READY':          return { ...base, backgroundColor: theme.colors.tealBg, color: theme.colors.tealText };
        case 'OUTFORDELIVERY': return { ...base, backgroundColor: theme.colors.indigoBg, color: theme.colors.indigoText };
        case 'DELIVERED':      return { ...base, backgroundColor: theme.colors.successBg, color: theme.colors.successText };
        case 'CANCELLED':      return { ...base, backgroundColor: theme.colors.dangerBg, color: theme.colors.dangerText };
        default:                 return { ...base, backgroundColor: '#F3F4F6', color: theme.colors.textSecondary };
    }
}

function statusLabel(status: OrderStatus): string {
    return status === 'OUTFORDELIVERY' ? 'Out for Delivery' : status.charAt(0) + status.slice(1).toLowerCase();
}

export const OrderView: React.FC = () => {
    const dispatch = useDispatch<AppDispatch>();
    const { myOrders, allOrders, status, error } = useSelector((state: RootState) => state.orders);
    const roles = useSelector((state: RootState) => state.auth.roles);
    const viewMode = useSelector((state: RootState) => state.auth.viewMode);
    const isStaff = viewMode === 'staff';
    const [assigningRiderFor, setAssigningRiderFor] = useState<number | null>(null);
    const pendingOrderIds = useSelector((state: RootState) => state.orders.pendingOrderIds);

    useEffect(() => {
        if (status === 'idle') {
            dispatch(isStaff ? fetchAllOrders() : fetchMyOrders());
        }
    }, [status, isStaff, dispatch]);

    const orders: OrderResponse[] = isStaff ? allOrders : myOrders;

    const canCancel = (order: OrderResponse) => {
        if (order.status !== 'PENDING') return false;
        const cutoff = new Date(order.createdAt).getTime() + 30 * 60 * 1000;
        return Date.now() < cutoff;
    };

    if (status === 'loading' || status === 'idle') {
        return <PageLoader label="Loading orders…" />;
    }

    if (status === 'failed') {
        return (
            <div style={{ padding: '40px', textAlign: 'center', color: theme.colors.dangerText }}>
                Couldn't load orders: {error}
            </div>
        );
    }

    return (
        <div style={{ fontFamily: theme.font }}>
            <p style={{ margin: '0 0 20px 0', fontSize: '14px', color: theme.colors.textSecondary }}>
                {isStaff ? 'Track, manage, and update live orders.' : 'Your order history.'}
            </p>

            {orders.length === 0 ? (
                <div style={{
                    background: theme.colors.surface, borderRadius: theme.radius.md,
                    boxShadow: theme.shadow.card, padding: '40px', textAlign: 'center', color: theme.colors.textSecondary,
                }}>
                    No orders yet.
                </div>
            ) : (
                <div style={{ background: theme.colors.surface, borderRadius: theme.radius.md, boxShadow: theme.shadow.card, overflow: 'hidden' }}>
                    <div style={{ overflowX: 'auto' }}>
                        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                            <thead>
                                <tr style={{ borderBottom: `1px solid ${theme.colors.border}`, color: theme.colors.textSecondary, fontSize: '13px' }}>
                                    <th style={{ padding: '14px 16px' }}>Order ID</th>
                                    <th style={{ padding: '14px 16px' }}>Date</th>
                                    {isStaff && <th style={{ padding: '14px 16px' }}>Customer</th>}
                                    <th style={{ padding: '14px 16px' }}>Items</th>
                                    <th style={{ padding: '14px 16px' }}>Total</th>
                                    <th style={{ padding: '14px 16px' }}>Status</th>
                                    <th style={{ padding: '14px 16px' }} />
                                </tr>
                            </thead>
                            <tbody>
                                {orders.map((order) => (
                                    <tr key={order.orderId} style={{ borderBottom: `1px solid ${theme.colors.border}`, fontSize: '14px', color: theme.colors.textPrimary }}>
                                        <td style={{ padding: '16px' }}>
                                            {isStaff && order.status === 'OUTFORDELIVERY' && !order.riderPhone && (
                                                assigningRiderFor === order.orderId ? (
                                                    <RiderAssignmentForm orderId={order.orderId} onDone={() => setAssigningRiderFor(null)} />
                                                ) : (
                                                    <button onClick={() => setAssigningRiderFor(order.orderId)} style={actionBtnStyle}>
                                                        Assign Rider
                                                    </button>
                                                )
                                            )}
                                            {isStaff && order.status === 'OUTFORDELIVERY' && order.riderPhone && (
                                                <span style={{ fontSize: '12px', color: theme.colors.textSecondary }}>Rider: {order.riderPhone}</span>
                                            )}
                                            {isStaff && order.status !== 'OUTFORDELIVERY' && nextStatus(order.status) && (
                                                // staff "Mark X" button:
                                                <LoadingButton
                                                    loading={pendingOrderIds.includes(order.orderId)}
                                                    onClick={() => dispatch(advanceOrderStatus({ orderId: order.orderId, status: nextStatus(order.status)! }))}
                                                    style={actionBtnStyle}
                                                >
                                                    Mark {statusLabel(nextStatus(order.status)!)}
                                                </LoadingButton>
                                            )}
                                            {isStaff && order.status === 'CANCELLED' && (
                                                <button onClick={() => dispatch(flagRefund(order.orderId))} style={{ ...actionBtnStyle, color: theme.colors.dangerText }}>
                                                    Flag Refund
                                                </button>
                                            )}
                                            {!isStaff && canCancel(order) && (
                                                // customer "Cancel" button:
                                                <LoadingButton
                                                    loading={pendingOrderIds.includes(order.orderId)}
                                                    onClick={() => dispatch(cancelOrder(order.orderId))}
                                                    style={{ ...actionBtnStyle, color: theme.colors.dangerText }}
                                                >
                                                    Cancel
                                                </LoadingButton>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}
        </div>
    );
};

const actionBtnStyle: React.CSSProperties = {
    border: `1px solid ${theme.colors.border}`, background: theme.colors.surface,
    padding: '6px 12px', borderRadius: theme.radius.sm, cursor: 'pointer',
    fontSize: '12px', fontWeight: 600, color: theme.colors.textSecondary,
};