import React, { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { type RootState, type AppDispatch } from '../../store/store';
import { fetchCart, updateCartItemQuantity, removeFromCart } from './cartSlice';
import { placeOrder, clearLastPlacedOrder } from '../order/orderSlice';
import { initiatePayment } from '../payments/paymentSlice';
import { PageLoader } from '../../components/PageLoader';
import { LoadingButton } from '../../components/LoadingButton';
import { showToast } from '../../components/toast/toastBus';
import { theme } from '../../theme';

type CheckoutStep = 'cart' | 'phone' | 'submitted';

export const CartView: React.FC = () => {
    const dispatch = useDispatch<AppDispatch>();
    const { cart, status, error } = useSelector((state: RootState) => state.cart);
    const pendingItemIds = useSelector((state: RootState) => state.cart.pendingItemIds);
    const { lastPlacedOrder, mutationStatus: orderMutationStatus } = useSelector((state: RootState) => state.orders);
    const { initiateStatus } = useSelector((state: RootState) => state.payments);

    const [step, setStep] = useState<CheckoutStep>('cart');
    const [phone, setPhone] = useState('');

    useEffect(() => {
        if (status === 'idle') dispatch(fetchCart());
    }, [status, dispatch]);

    const startCheckout = () => setStep('phone');

    const confirmPhoneAndPay = async () => {
        if (!/^\+?[0-9]{7,15}$/.test(phone.trim())) {
            showToast('Enter a valid phone number', 'error');
            return;
        }
        const orderResult = await dispatch(placeOrder());
        if (!placeOrder.fulfilled.match(orderResult)) {
            showToast((orderResult.payload as string) ?? 'Failed to place order', 'error');
            setStep('cart');
            return;
        }
        dispatch(fetchCart());

        // Fire the MTN request but don't block on its resolution — the order
        // stays PENDING until staff manually confirm or reject the payment.
        const paymentResult = await dispatch(initiatePayment({ orderId: orderResult.payload.orderId, payerPhone: phone.trim() }));
        if (initiatePayment.fulfilled.match(paymentResult)) {
            showToast('Order placed — payment request sent', 'success');
        } else {
            showToast('Order placed, but the payment request failed to send. Contact staff.', 'error');
        }
        setStep('submitted');
    };

    const resetCheckout = () => {
        setStep('cart');
        setPhone('');
        dispatch(clearLastPlacedOrder());
    };

    if (status === 'loading' || status === 'idle') {
        return <PageLoader label="Loading cart…" />;
    }

    if (step === 'phone') {
        return (
            <div style={{ fontFamily: theme.font, maxWidth: '400px' }}>
                <div style={{ background: theme.colors.surface, borderRadius: theme.radius.md, boxShadow: theme.shadow.card, padding: '32px' }}>
                    <h3 style={{ margin: '0 0 8px 0', color: theme.colors.textPrimary }}>Pay with MTN MoMo</h3>
                    <p style={{ fontSize: '13px', color: theme.colors.textSecondary, marginBottom: '20px' }}>
                        Enter the phone number to receive the payment prompt.
                    </p>
                    <input
                        type="tel"
                        placeholder="+250 7XX XXX XXX"
                        value={phone}
                        onChange={(e) => setPhone(e.target.value)}
                        style={{ width: '100%', padding: '12px', borderRadius: '6px', border: '1px solid #ccc', fontSize: '14px', marginBottom: '16px', boxSizing: 'border-box' }}
                    />
                    <div style={{ display: 'flex', gap: '10px' }}>
                        <button onClick={() => setStep('cart')} style={{ flex: 1, padding: '12px', borderRadius: theme.radius.sm, border: `1px solid ${theme.colors.border}`, background: 'white', cursor: 'pointer' }}>
                            Back
                        </button>
                        <LoadingButton
                            onClick={confirmPhoneAndPay}
                            loading={orderMutationStatus === 'loading' || initiateStatus === 'loading'}
                            loadingText="Placing order…"
                            style={{ flex: 2, padding: '12px', borderRadius: theme.radius.sm, border: 'none', background: theme.colors.brand, color: 'white', fontWeight: 'bold' }}
                        >
                            Request Payment
                        </LoadingButton>
                    </div>
                </div>
            </div>
        );
    }

    if (step === 'submitted') {
        return (
            <div style={{ fontFamily: theme.font, maxWidth: '400px', textAlign: 'center' }}>
                <div style={{ background: theme.colors.surface, borderRadius: theme.radius.md, boxShadow: theme.shadow.card, padding: '40px' }}>
                    <div style={{ fontSize: '32px', marginBottom: '12px' }}>📲</div>
                    <h3 style={{ margin: '0 0 8px 0', color: theme.colors.textPrimary }}>Order placed</h3>
                    <p style={{ fontSize: '13px', color: theme.colors.textSecondary, marginBottom: '20px' }}>
                        Order #{lastPlacedOrder?.orderId} is awaiting payment confirmation. Approve the MoMo prompt on {phone || 'your phone'} — staff will confirm once received.
                    </p>
                    <button onClick={resetCheckout} style={{ padding: '10px 20px', borderRadius: theme.radius.sm, border: 'none', background: theme.colors.brand, color: 'white', fontWeight: 'bold', cursor: 'pointer' }}>
                        Order More
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

            {error && (
                <p style={{ color: theme.colors.dangerText, fontSize: '13px', background: theme.colors.dangerBg, padding: '8px 12px', borderRadius: theme.radius.sm }}>
                    {error}
                </p>
            )}

            {items.length === 0 ? (
                <div style={{ background: theme.colors.surface, borderRadius: theme.radius.md, boxShadow: theme.shadow.card, padding: '40px', textAlign: 'center', color: theme.colors.textSecondary }}>
                    Your cart is empty.
                </div>
            ) : (
                <>
                    <div style={{ background: theme.colors.surface, borderRadius: theme.radius.md, boxShadow: theme.shadow.card, overflow: 'hidden', marginBottom: '20px' }}>
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
                                                <LoadingButton
                                                    loading={pendingItemIds.includes(row.menuItemId)}
                                                    disabled={row.quantity <= 1}
                                                    onClick={() => dispatch(updateCartItemQuantity({ menuItemId: row.menuItemId, quantity: row.quantity - 1 }))}
                                                    style={qtyBtnStyle}
                                                >
                                                    −
                                                </LoadingButton>
                                                <span>{row.quantity}</span>
                                                <LoadingButton
                                                    loading={pendingItemIds.includes(row.menuItemId)}
                                                    onClick={() => dispatch(updateCartItemQuantity({ menuItemId: row.menuItemId, quantity: row.quantity + 1 }))}
                                                    style={qtyBtnStyle}
                                                >
                                                    +
                                                </LoadingButton>
                                            </div>
                                        </td>
                                        <td style={{ padding: '16px', color: theme.colors.textSecondary }}>{row.itemPrice.toLocaleString()} RWF</td>
                                        <td style={{ padding: '16px', fontWeight: 700 }}>{row.subtotal.toLocaleString()} RWF</td>
                                        <td style={{ padding: '16px' }}>
                                            <LoadingButton
                                                loading={pendingItemIds.includes(row.menuItemId)}
                                                onClick={() => dispatch(removeFromCart(row.menuItemId))}
                                                style={{ border: 'none', background: 'none', color: theme.colors.dangerText, fontSize: '13px' }}
                                            >
                                                Remove
                                            </LoadingButton>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>

                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: theme.colors.surface, borderRadius: theme.radius.md, boxShadow: theme.shadow.card, padding: '20px 24px' }}>
                        <div>
                            <div style={{ fontSize: '13px', color: theme.colors.textSecondary }}>Total</div>
                            <div style={{ fontSize: '24px', fontWeight: 700, color: theme.colors.textPrimary }}>{total.toLocaleString()} RWF</div>
                        </div>
                        <button onClick={startCheckout} style={{ backgroundColor: theme.colors.brand, color: 'white', border: 'none', padding: '14px 28px', borderRadius: theme.radius.sm, fontWeight: 'bold', cursor: 'pointer', fontSize: '15px' }}>
                            Pay with Momo
                        </button>
                    </div>
                </>
            )}
        </div>
    );
};

const qtyBtnStyle: React.CSSProperties = {
    width: '24px', height: '24px', borderRadius: '6px',
    border: `1px solid ${theme.colors.border}`, background: theme.colors.surface,
    cursor: 'pointer', fontSize: '14px', lineHeight: 1,
};