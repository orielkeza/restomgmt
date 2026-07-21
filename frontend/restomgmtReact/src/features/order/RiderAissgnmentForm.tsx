import React, { useState } from 'react';
import { useDispatch } from 'react-redux';
import { type AppDispatch } from '../../store/store';
import { assignRider } from './orderSlice';
import { theme } from '../../theme';
import { LoadingButton } from '../../components/LoadingButton';

export const RiderAssignmentForm: React.FC<{ orderId: number; onDone: () => void }> = ({ orderId, onDone }) => {
    const dispatch = useDispatch<AppDispatch>();
    const [phone, setPhone] = useState('');
    const [note, setNote] = useState('');
    const [error, setError] = useState('');
    const [submitting, setSubmitting] = useState(false);


    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!/^\+?[0-9]{7,15}$/.test(phone.trim())) { setError('Enter a valid phone number'); return; }
        setSubmitting(true);
        const result = await dispatch(assignRider({ orderId, riderPhone: phone.trim(), deliveryNote: note.trim() || undefined }));
        setSubmitting(false);
        if (assignRider.fulfilled.match(result)) onDone();
        else setError((result.payload as string) ?? 'Failed to assign rider');
    };

    return (
        <form onSubmit={handleSubmit} style={{ display: 'flex', gap: '6px', alignItems: 'center' }}>
            <input
                placeholder="Rider phone"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                style={{ width: '120px', padding: '6px 8px', borderRadius: '6px', border: `1px solid ${theme.colors.border}`, fontSize: '12px' }}
            />
            <input
                placeholder="Note (optional)"
                value={note}
                onChange={(e) => setNote(e.target.value)}
                style={{ width: '110px', padding: '6px 8px', borderRadius: '6px', border: `1px solid ${theme.colors.border}`, fontSize: '12px' }}
            />
            <LoadingButton type="submit" loading={submitting} style={{ padding: '6px 10px', borderRadius: '6px', border: 'none', background: theme.colors.brand, color: 'white', fontSize: '12px' }}>
                Assign
            </LoadingButton>
            {error && <span style={{ color: theme.colors.dangerText, fontSize: '11px' }}>{error}</span>}
        </form>
    );
};