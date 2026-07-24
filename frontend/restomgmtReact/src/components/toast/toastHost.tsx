import React, { useEffect, useState } from 'react';
import { subscribeToasts, type ToastMessage } from './toastBus';
import { theme } from '../../theme';

const DURATION_MS = 4000;

export const ToastHost: React.FC = () => {
    const [toasts, setToasts] = useState<ToastMessage[]>([]);

    useEffect(() => {
        return subscribeToasts((toast) => {
            setToasts((prev) => [...prev, toast]);
            setTimeout(() => {
                setToasts((prev) => prev.filter((t) => t.id !== toast.id));
            }, DURATION_MS);
        });
    }, []);

    if (toasts.length === 0) return null;

    return (
        <div style={{
            position: 'fixed', bottom: '24px', right: '24px', zIndex: 2000,
            display: 'flex', flexDirection: 'column', gap: '10px', fontFamily: theme.font,
        }}>
            {toasts.map((toast) => (
                <div
                    key={toast.id}
                    style={{
                        padding: '12px 18px', borderRadius: theme.radius.sm, minWidth: '240px',
                        boxShadow: theme.shadow.elevated, fontSize: '13px', fontWeight: 500,
                        background: toast.kind === 'success' ? theme.colors.successBg : toast.kind === 'error' ? theme.colors.dangerBg : theme.colors.infoBg,
                        color: toast.kind === 'success' ? theme.colors.successText : toast.kind === 'error' ? theme.colors.dangerText : theme.colors.infoText,
                    }}
                >
                    {toast.text}
                </div>
            ))}
        </div>
    );
};