import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { type RootState, type AppDispatch } from '../../store/store';
import { fetchCart, updateCartItemQuantity, removeFromCart } from './cartSlice';
import { theme } from '../../theme';
import { placeOrder } from '../order/orderSlice';

export const CartView: React.FC = () => {
    const dispatch = useDispatch<AppDispatch>();
    const { cart, status, mutationStatus, error } = useSelector((state: RootState) => state.cart);
    const { mutationStatus: orderMutationStatus, lastPlacedOrder } = useSelector((state: RootState) => state.orders);

    useEffect(() => {
        if (status === 'idle') {
            dispatch(fetchCart());
        }
    }, [status, dispatch]);

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        const result = await dispatch(placeOrder());
        if (placeOrder.fulfilled.match(result)) {
            dispatch(fetchCart()); // cart is now empty server-side, resync
        }
    };

    if (status === 'loading' || status === 'idle') {
        return (
            <div style={{ padding: '40px', textAlign: 'center', color: theme.colors.textSecondary }}>
                Loading cart…
            </div>
        );
    }

    if (status === 'failed') {
        return (
            <div style={{ padding: '40px', textAlign: 'center', color: theme.colors.dangerText }}>
                Couldn't load your cart: {error}
                <div style={{ marginTop: '12px' }}>
                    <button
                        onClick={() => dispatch(fetchCart())}
                        style={{
                            padding: '8px 16px', borderRadius: theme.radius.sm, border: 'none',
                            background: theme.colors.brand, color: 'white', cursor: 'pointer', fontWeight: 'bold',
                        }}
                    >
                        Retry
                    </button>
                </div>
            </div>
        );
    }

    const items = cart?.items ?? [];
    const total = cart?.total ?? 0;

    return (
        <div style={{ fontFamily: theme.font, maxWidth: '760px' }}>
            <p style={{ margin: '0 0 20px 0', fontSize: '14px', color: theme.colors.textSecondary }}>
                Review items before sending to payment.
            </p>

            {mutationStatus === 'failed' && (
                <p style={{ color: theme.colors.dangerText, fontSize: '13px', background: theme.colors.dangerBg, padding: '8px 12px', borderRadius: theme.radius.sm }}>
                    {error}
                </p>
            )}

            {items.length === 0 ? (
                <div style={{
                    background: theme.colors.surface, borderRadius: theme.radius.md,
                    boxShadow: theme.shadow.card, padding: '40px', textAlign: 'center',
                    color: theme.colors.textSecondary,
                }}>
                    Your cart is empty.
                </div>
            ) : (
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
                                    <th style={{ padding: '14px 16px' }} />
                                </tr>
                            </thead>
                            <tbody>
                                {items.map((row) => (
                                    <tr key={row.menuItemId} style={{ borderBottom: `1px solid ${theme.colors.border}`, fontSize: '14px' }}>
                                        <td style={{ padding: '16px', fontWeight: 500 }}>{row.itemName}</td>
                                        <td style={{ padding: '16px' }}>
                                            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                                <button
                                                    type="button"
                                                    disabled={mutationStatus === 'loading' || row.quantity <= 1}
                                                    onClick={() => dispatch(updateCartItemQuantity({ menuItemId: row.menuItemId, quantity: row.quantity - 1 }))}
                                                    style={qtyBtnStyle}
                                                >
                                                    −
                                                </button>
                                                <span>{row.quantity}</span>
                                                <button
                                                    type="button"
                                                    disabled={mutationStatus === 'loading'}
                                                    onClick={() => dispatch(updateCartItemQuantity({ menuItemId: row.menuItemId, quantity: row.quantity + 1 }))}
                                                    style={qtyBtnStyle}
                                                >
                                                    +
                                                </button>
                                            </div>
                                        </td>
                                        <td style={{ padding: '16px', color: theme.colors.textSecondary }}>
                                            {row.itemPrice.toLocaleString()} RWF
                                        </td>
                                        <td style={{ padding: '16px', fontWeight: 700 }}>
                                            {row.subtotal.toLocaleString()} RWF
                                        </td>
                                        <td style={{ padding: '16px' }}>
                                            <button
                                                type="button"
                                                disabled={mutationStatus === 'loading'}
                                                onClick={() => dispatch(removeFromCart(row.menuItemId))}
                                                style={{ border: 'none', background: 'none', color: theme.colors.dangerText, cursor: 'pointer', fontSize: '13px' }}
                                            >
                                                Remove
                                            </button>
                                        </td>
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
                            <div style={{ fontSize: '24px', fontWeight: 700, color: theme.colors.textPrimary }}>
                                {total.toLocaleString()} RWF
                            </div>
                        </div>
                        <button type="submit"
                            disabled={orderMutationStatus === 'loading'}
                            style={{
                            backgroundColor: theme.colors.brand, color: 'white', border: 'none',
                            padding: '14px 28px', borderRadius: theme.radius.sm, fontWeight: 'bold',
                            cursor: 'pointer', fontSize: '15px',
                        }}>
                            {orderMutationStatus === 'loading' ? 'Placing order…' : 'Pay with Momo'}
                        </button>

                        {lastPlacedOrder && lastPlacedOrder.warnings.length > 0 && (
                            <div style={{ marginTop: '12px', padding: '12px', background: theme.colors.warningBg, borderRadius: theme.radius.sm, color: theme.colors.warningText, fontSize: '13px' }}>
                                {lastPlacedOrder.warnings.join(' ')}
                            </div>
                        )}

                    </div>
                </form>
            )}
        </div>
    );
};

const qtyBtnStyle: React.CSSProperties = {
    width: '24px', height: '24px', borderRadius: '6px',
    border: `1px solid ${theme.colors.border}`, background: theme.colors.surface,
    cursor: 'pointer', fontSize: '14px', lineHeight: 1,
};