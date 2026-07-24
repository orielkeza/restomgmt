import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { type RootState, type AppDispatch } from '../../store/store';
import { fetchOrderById, clearSelectedOrder } from './orderSlice';
import { fetchPaymentForOrder, setPaymentStatus } from '../payments/paymentSlice';
import { LoadingButton } from '../../components/LoadingButton';
import { PageLoader } from '../../components/PageLoader';
import { showToast } from '../../components/toast/toastBus';
import { theme } from '../../theme';

interface OrderDetailModalProps {
    orderId: number;
    onClose: () => void;
}

export const OrderDetailModal: React.FC<OrderDetailModalProps> = ({ orderId, onClose }) => {
    const dispatch = useDispatch<AppDispatch>();
    const { selectedOrder, selectedOrderStatus } = useSelector((state: RootState) => state.orders);
    const payment = useSelector((state: RootState) => state.payments.paymentsByOrderId[orderId]);
    const { fetchStatus, updateStatus } = useSelector((state: RootState) => state.payments);
    const viewMode = useSelector((state: RootState) => state.auth.viewMode);
    const isStaff = viewMode === 'staff';

    useEffect(() => {
        dispatch(fetchOrderById(orderId));
        if (isStaff) {
            dispatch(fetchPaymentForOrder(orderId));
        }
    }, [orderId, isStaff, dispatch]);

    const handleClose = () => {
        dispatch(clearSelectedOrder());
        onClose();
    };

    const handleSetStatus = async (status: 'SUCCESSFUL' | 'PENDING') => {
        const result = await dispatch(setPaymentStatus({ orderId, status }));
        if (setPaymentStatus.fulfilled.match(result)) {
            showToast(`Payment marked ${status === 'SUCCESSFUL' ? 'Paid' : 'Pending'}`, 'success');
        } else {
            showToast((result.payload as string) ?? 'Failed to update payment', 'error');
        }
    };

    const isCurrentOrderLoaded = selectedOrder && selectedOrder.orderId === orderId;

    return (
        <div
            style={{
                position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)',
                display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000,
            }}
            onClick={(e) => { if (e.target === e.currentTarget) handleClose(); }}
        >
            <div style={{
                background: theme.colors.surface, borderRadius: theme.radius.lg,
                padding: '32px', width: '460px', maxHeight: '80vh', overflowY: 'auto',
                boxShadow: theme.shadow.elevated, fontFamily: theme.font,
            }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                    <h2 style={{ margin: 0, fontSize: '18px', color: theme.colors.textPrimary }}>
                        Order #{orderId}
                    </h2>
                    <button
                        onClick={handleClose}
                        style={{ border: 'none', background: 'none', fontSize: '18px', cursor: 'pointer', color: theme.colors.textMuted }}
                    >
                        ✕
                    </button>
                </div>

                {(selectedOrderStatus === 'loading' || selectedOrderStatus === 'idle') && !isCurrentOrderLoaded && (
                    <PageLoader compact label="Loading order…" />
                )}

                {selectedOrderStatus === 'failed' && !isCurrentOrderLoaded && (
                    <p style={{ color: theme.colors.dangerText, fontSize: '13px' }}>Couldn't load this order.</p>
                )}

                {isCurrentOrderLoaded && selectedOrder && (
                    <>
                        <div style={{ fontSize: '13px', color: theme.colors.textSecondary, marginBottom: '16px', lineHeight: 1.7 }}>
                            <div>Customer: <strong style={{ color: theme.colors.textPrimary }}>{selectedOrder.username}</strong></div>
                            <div>Placed: {new Date(selectedOrder.createdAt).toLocaleString()}</div>
                            <div>Status: <strong style={{ color: theme.colors.textPrimary }}>{selectedOrder.status}</strong></div>
                            {selectedOrder.riderPhone && <div>Rider: {selectedOrder.riderPhone}</div>}
                            {selectedOrder.deliveryNote && <div>Delivery note: {selectedOrder.deliveryNote}</div>}
                        </div>

                        <div style={{ borderTop: `1px solid ${theme.colors.border}`, paddingTop: '16px', marginBottom: '16px' }}>
                            {selectedOrder.items.map((item) => (
                                <div key={item.menuItemId} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px', padding: '6px 0' }}>
                                    <span>{item.itemName} × {item.quantity}</span>
                                    <span style={{ fontWeight: 600 }}>{item.subtotal.toLocaleString()} RWF</span>
                                </div>
                            ))}
                            <div style={{
                                display: 'flex', justifyContent: 'space-between', fontWeight: 700, fontSize: '15px',
                                paddingTop: '10px', marginTop: '6px', borderTop: `1px solid ${theme.colors.border}`,
                            }}>
                                <span>Total</span>
                                <span>{selectedOrder.total.toLocaleString()} RWF</span>
                            </div>
                        </div>

                        {selectedOrder.warnings.length > 0 && (
                            <div style={{
                                background: theme.colors.warningBg, color: theme.colors.warningText,
                                padding: '10px 14px', borderRadius: theme.radius.sm, fontSize: '12px', marginBottom: '16px',
                            }}>
                                {selectedOrder.warnings.join(' ')}
                            </div>
                        )}

                        {isStaff && (
                            <div style={{ borderTop: `1px solid ${theme.colors.border}`, paddingTop: '16px' }}>
                                <h3 style={{ margin: '0 0 10px 0', fontSize: '14px', color: theme.colors.textPrimary }}>
                                    Payment
                                </h3>

                                {fetchStatus === 'loading' && <PageLoader compact label="Loading payment…" />}

                                {payment ? (
                                    <>
                                        <div style={{ fontSize: '13px', color: theme.colors.textSecondary, marginBottom: '12px', lineHeight: 1.7 }}>
                                            <div>Reference: {payment.momoReferenceId}</div>
                                            <div>Payer: {payment.payerPhone}</div>
                                            <div>Amount: {payment.amount.toLocaleString()} RWF</div>
                                            <div>
                                                Current status:{' '}
                                                <span style={{
                                                    fontWeight: 700,
                                                    color:
                                                        payment.status === 'SUCCESSFUL' ? theme.colors.successText :
                                                        payment.status === 'FAILED' ? theme.colors.dangerText :
                                                        theme.colors.warningText,
                                                }}>
                                                    {payment.status}
                                                </span>
                                            </div>
                                            {payment.refundFlagged && (
                                                <div style={{ color: theme.colors.dangerText }}>⚠ Refund flagged</div>
                                            )}
                                        </div>
                                        <div style={{ display: 'flex', gap: '10px' }}>
                                            <LoadingButton
                                                loading={updateStatus === 'loading'}
                                                disabled={payment.status === 'SUCCESSFUL'}
                                                onClick={() => handleSetStatus('SUCCESSFUL')}
                                                style={{
                                                    flex: 1, padding: '10px', borderRadius: theme.radius.sm, border: 'none',
                                                    background: theme.colors.successBg, color: theme.colors.successText, fontWeight: 700,
                                                }}
                                            >
                                                Mark Paid
                                            </LoadingButton>
                                            <LoadingButton
                                                loading={updateStatus === 'loading'}
                                                disabled={payment.status === 'PENDING'}
                                                onClick={() => handleSetStatus('PENDING')}
                                                style={{
                                                    flex: 1, padding: '10px', borderRadius: theme.radius.sm, border: 'none',
                                                    background: theme.colors.warningBg, color: theme.colors.warningText, fontWeight: 700,
                                                }}
                                            >
                                                Mark Pending
                                            </LoadingButton>
                                        </div>
                                    </>
                                ) : (
                                    fetchStatus !== 'loading' && (
                                        <p style={{ fontSize: '13px', color: theme.colors.textSecondary }}>
                                            No payment record for this order yet.
                                        </p>
                                    )
                                )}
                            </div>
                        )}
                    </>
                )}
            </div>
        </div>
    );
};