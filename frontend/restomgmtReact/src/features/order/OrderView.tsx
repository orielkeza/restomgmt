import React, { useState } from 'react';
import { theme } from '../../theme';

interface Order {
    id: number;
    date: Date;
    customerName: string;
    items: Array<string>;
    price: number;
    status: 'PENDING' | 'CONFIRMED' | 'PREPARING' | 'READY' | 'OUTFORDELIVERY' | 'DELIVERED' | 'CANCELLED';
    tableNumber: number;
}

export const OrderView: React.FC = () => {
    const [orders] = useState<Order[]>([
        { id: 3, customerName: 'Sophia', date: new Date('2027-09-11'), status: 'PENDING', tableNumber: 6, price: 47, items: ['Burger', 'Igitoki', 'Virgin Mojito'] },
        { id: 4, customerName: 'Laura', date: new Date('2026-05-19'), status: 'PREPARING', tableNumber: 4, price: 50, items: ['Burger', 'Pizza', 'Virgin Mojito', 'Coke'] },
        { id: 5, customerName: 'Jean', date: new Date('2026-12-01'), status: 'DELIVERED', tableNumber: 1, price: 35, items: ['Burger', 'Coke'] },
        { id: 6, customerName: 'Marc', date: new Date('2026-12-02'), status: 'CANCELLED', tableNumber: 3, price: 20, items: ['Fruit Salad'] },
    ]);

    const getStatusStyle = (status: Order['status']) => {
        const baseBadge = { padding: '4px 10px', borderRadius: '12px', fontSize: '12px', fontWeight: 'bold' as const };
        switch (status) {
            case 'PENDING':        return { ...baseBadge, backgroundColor: theme.colors.warningBg, color: theme.colors.warningText };
            case 'CONFIRMED':      return { ...baseBadge, backgroundColor: theme.colors.infoBg, color: theme.colors.infoText };
            case 'PREPARING':      return { ...baseBadge, backgroundColor: theme.colors.purpleBg, color: theme.colors.purpleText };
            case 'READY':          return { ...baseBadge, backgroundColor: theme.colors.tealBg, color: theme.colors.tealText };
            case 'OUTFORDELIVERY': return { ...baseBadge, backgroundColor: theme.colors.indigoBg, color: theme.colors.indigoText };
            case 'DELIVERED':      return { ...baseBadge, backgroundColor: theme.colors.successBg, color: theme.colors.successText };
            case 'CANCELLED':      return { ...baseBadge, backgroundColor: theme.colors.dangerBg, color: theme.colors.dangerText };
            default:                 return { ...baseBadge, backgroundColor: '#F3F4F6', color: theme.colors.textSecondary };
        }
    };

    const statusLabel = (status: Order['status']) =>
        status.charAt(0) + status.slice(1).toLowerCase().replace('outfordelivery', 'Out for Delivery');

    return (
        <div style={{ fontFamily: theme.font }}>
            <div style={{
                display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px',
            }}>
                <p style={{ margin: 0, fontSize: '14px', color: theme.colors.textSecondary }}>
                    Track, manage, and update live orders.
                </p>
                <button style={{
                    backgroundColor: theme.colors.brand, color: 'white', border: 'none',
                    padding: '10px 18px', borderRadius: theme.radius.sm, fontWeight: 'bold', cursor: 'pointer',
                }}>
                    + New Order
                </button>
            </div>

            <div style={{
                background: theme.colors.surface, borderRadius: theme.radius.md,
                boxShadow: theme.shadow.card, overflow: 'hidden',
            }}>
                <div style={{ overflowX: 'auto' }}>
                    <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                        <thead>
                            <tr style={{ borderBottom: `1px solid ${theme.colors.border}`, color: theme.colors.textSecondary, fontSize: '13px' }}>
                                <th style={{ padding: '14px 16px' }}>Order ID</th>
                                <th style={{ padding: '14px 16px' }}>Date</th>
                                <th style={{ padding: '14px 16px' }}>Customer</th>
                                <th style={{ padding: '14px 16px' }}>Items</th>
                                <th style={{ padding: '14px 16px' }}>Table</th>
                                <th style={{ padding: '14px 16px' }}>Total</th>
                                <th style={{ padding: '14px 16px' }}>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            {orders.map((order) => (
                                <tr key={order.id} style={{ borderBottom: `1px solid ${theme.colors.border}`, fontSize: '14px', color: theme.colors.textPrimary }}>
                                    <td style={{ padding: '16px', fontWeight: 700 }}>#{order.id}</td>
                                    <td style={{ padding: '16px', color: theme.colors.textSecondary }}>{order.date.toLocaleDateString()}</td>
                                    <td style={{ padding: '16px', fontWeight: 500 }}>{order.customerName}</td>
                                    <td style={{ padding: '16px', color: theme.colors.textSecondary }}>{order.items.join(', ')}</td>
                                    <td style={{ padding: '16px' }}>{order.tableNumber}</td>
                                    <td style={{ padding: '16px', fontWeight: 700 }}>${order.price.toFixed(2)}</td>
                                    <td style={{ padding: '16px' }}>
                                        <span style={getStatusStyle(order.status)}>{statusLabel(order.status)}</span>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};