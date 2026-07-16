import React, { useState } from 'react';
import { theme } from '../../theme';

interface RowData {
    id: number;
    item: string;
    quantity: number;
    price: number;
}

export const CartView: React.FC = () => {
    const [error, setError] = useState('');

    const sampleData: RowData[] = [
        { id: 1, item: 'Chicken Wings', quantity: 1, price: 25 },
        { id: 2, item: 'Virgin Mojito', quantity: 2, price: 8 },
        { id: 3, item: 'Fruit Salad', quantity: 2, price: 12 },
    ];

    const total = sampleData.reduce((sum, row) => sum + row.quantity * row.price, 0);

    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setError('');
        // TODO: dispatch checkout/payment action
    };

    return (
        <div style={{ fontFamily: theme.font, maxWidth: '760px' }}>
            <p style={{ margin: '0 0 20px 0', fontSize: '14px', color: theme.colors.textSecondary }}>
                Review items before sending to payment.
            </p>

            {error && <p style={{ color: theme.colors.dangerText, fontSize: '14px' }}>{error}</p>}

            <form onSubmit={handleSubmit}>
                <div style={{
                    background: theme.colors.surface, borderRadius: theme.radius.md,
                    boxShadow: theme.shadow.card, overflow: 'hidden', marginBottom: '20px',
                }}>
                    <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                        <thead>
                            <tr style={{ borderBottom: `1px solid ${theme.colors.border}`, color: theme.colors.textSecondary, fontSize: '13px' }}>
                                <th style={{ padding: '14px 16px' }}>Item</th>
                                <th style={{ padding: '14px 16px' }}>Quantity</th>
                                <th style={{ padding: '14px 16px' }}>Price</th>
                                <th style={{ padding: '14px 16px' }}>Subtotal</th>
                            </tr>
                        </thead>
                        <tbody>
                            {sampleData.map((row) => (
                                <tr key={row.id} style={{ borderBottom: `1px solid ${theme.colors.border}`, fontSize: '14px' }}>
                                    <td style={{ padding: '16px', fontWeight: 500 }}>{row.item}</td>
                                    <td style={{ padding: '16px', color: theme.colors.textSecondary }}>{row.quantity}</td>
                                    <td style={{ padding: '16px', color: theme.colors.textSecondary }}>${row.price.toFixed(2)}</td>
                                    <td style={{ padding: '16px', fontWeight: 700 }}>${(row.quantity * row.price).toFixed(2)}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>

                <div style={{
                    display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                    background: theme.colors.surface, borderRadius: theme.radius.md,
                    boxShadow: theme.shadow.card, padding: '20px 24px',
                }}>
                    <div>
                        <div style={{ fontSize: '13px', color: theme.colors.textSecondary }}>Total</div>
                        <div style={{ fontSize: '24px', fontWeight: 700, color: theme.colors.textPrimary }}>${total.toFixed(2)}</div>
                    </div>
                    <button type="submit" style={{
                        backgroundColor: theme.colors.brand, color: 'white', border: 'none',
                        padding: '14px 28px', borderRadius: theme.radius.sm, fontWeight: 'bold',
                        cursor: 'pointer', fontSize: '15px',
                    }}>
                        Pay with Momo
                    </button>
                </div>
            </form>
        </div>
    );
};