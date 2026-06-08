import React, { useState } from 'react';

interface Order {
    id: number;
    date: Date;
    customerName: string;
    items: Array<string>;
    price: number;
    status: 'order taken' | 'preparing' | 'done';
    tableNumber: number;
}

export const OrderView: React.FC = () => {

    const [orders] = useState<Order[]>([
        {id: 3, customerName: 'Sophia', date: new Date("2027-09-11"), status: 'order taken', tableNumber: 6, price: 47, items: ['burger', 'igitoki', 'virgin mojito'] },
        {id: 4, customerName: 'Laura', date: new Date("2026-05-19"), status: 'preparing', tableNumber: 4, price: 50,  items: ['burger', 'pizza', 'virgin mojito', 'coke'] },
        {id: 5, customerName: 'Jean', date: new Date("2026-12-01"), status: 'done', tableNumber: 1, price: 35,  items: ['burger', 'coke'] },
    ]);

    const getStatusStyle = (status: Order['status']) => {
        const baseBadge = { padding: '4px 8px', borderRadius: '12px', fontSize: '12px', fontWeight: 'bold' };
        switch(status) {
            case 'order taken': return { ...baseBadge, backgroundColor: '#E2FBE7', color: 'green' };
            case 'preparing': return { ...baseBadge, backgroundColor: '#E0F2FE', color: 'yellow' };
            case 'order taken': return { ...baseBadge, backgroundColor: '#FEF3C7', color: 'red' };
            default: return { ...baseBadge, backgroundColor: '#F3F4F6', color: '#374151' };
        }
    };

    return (
        <div style={{ 
            padding:'30px',
            fontFamily: 'sans-serif',
            backgroundColor: 'white',
            minHeight: '100vh'
        }}>
            <div style={{
                maxWidth: '1000px',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                marginBottom: '24px'
            }}>
                <div style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    marginBottom: '24px'
                }}>
                    <div>
                        <h1 style={{ margin: 0, fontSize: '22px', color: '#111827' }}>Order Management</h1>
                        <p style={{ margin: '4px 0 0 0', fontSize: '14px', color: '#6B7280' }}>Track, manage, and update live orders.</p>
                    </div>
                    <button style={{ backgroundColor: '#E70B0D', color: 'white', border: 'none', padding: '10px 16px', borderRadius: '6px', fontWeight: 'bold', cursor: 'pointer' }}>
                        + New Order
                    </button>
                </div>
                {/* Orders Table */}
                <div style={{ overflowX: 'auto' }}>
                    <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                        <thead>
                            <tr style={{ borderBottom: '2px solid #E5E7EB', color: '#374151', fontSize: '14px' }}>
                                <th style={{ padding: '12px 8px' }}>Order ID</th>
                                <th style={{ padding: '12px 8px' }}>Date</th>
                                <th style={{ padding: '12px 8px' }}>Customer</th>
                                <th style={{ padding: '12px 8px' }}>Items</th>
                                <th style={{ padding: '12px 8px' }}>Total</th>
                                <th style={{ padding: '12px 8px' }}>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            {orders.map((order) => (
                                <tr key={order.id} style={{ borderBottom: '1px solid #F3F4F6', fontSize: '14px', color: '#4B5563' }}>
                                    <td style={{ padding: '16px 8px', fontWeight: 'bold', color: '#111827' }}>{order.id}</td>
                                    <td style={{ padding: '16px 8px' }}>{order.date}</td>
                                    <td style={{ padding: '16px 8px', fontWeight: 500 }}>{order.customer}</td>
                                    <td style={{ padding: '16px 8px', fontStyle: 'italic' }}>{order.items}</td>
                                    <td style={{ padding: '16px 8px', fontWeight: 'bold' }}>${order.total.toFixed(2)}</td>
                                    <td style={{ padding: '16px 8px' }}>
                                        <span style={getStatusStyle(order.status)}>{order.status}</span>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    )