import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { type RootState, type AppDispatch } from '../../store/store';
import { createMenuItem, updateMenuItem } from './menuSlice';
import { type MenuItemResponse } from '../../api/menuApi';
import { theme } from '../../theme';
import { LoadingButton } from '../../components/LoadingButton';

interface Props {
    editingItem: MenuItemResponse | null; // null = create mode
    onClose: () => void;
}

export const MenuItemFormModal: React.FC<Props> = ({ editingItem, onClose }) => {
    const dispatch = useDispatch<AppDispatch>();
    const categories = useSelector((state: RootState) => state.menu.categories);

    const [name, setName] = useState(editingItem?.name ?? '');
    const [description, setDescription] = useState(editingItem?.description ?? '');
    const [cost, setCost] = useState(editingItem?.cost?.toString() ?? '');
    const [categoryId, setCategoryId] = useState<number | ''>(
        categories.find((c) => c.name === editingItem?.categoryName)?.id ?? ''
    );
    const [available, setAvailable] = useState(editingItem?.available ?? true);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState('');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!name.trim() || !cost || categoryId === '') {
            setError('Name, cost, and category are required');
            return;
        }
        const parsedCost = parseFloat(cost);
        if (isNaN(parsedCost) || parsedCost <= 0) {
            setError('Cost must be a positive number');
            return;
        }

        setSubmitting(true);
        setError('');

        const payload = {
            name: name.trim(),
            description: description.trim() || undefined,
            cost: parsedCost,
            categoryId: categoryId as number,
            available,
        };

        const result = editingItem
            ? await dispatch(updateMenuItem({ id: editingItem.id, payload }))
            : await dispatch(createMenuItem(payload));

        setSubmitting(false);

        if (createMenuItem.fulfilled.match(result) || updateMenuItem.fulfilled.match(result)) {
            onClose();
        } else {
            setError((result.payload as string) ?? 'Something went wrong');
        }
    };

    return (
        <div style={{
            position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)',
            display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000,
        }}>
            <div style={{
                background: theme.colors.surface, borderRadius: theme.radius.lg,
                padding: '32px', width: '420px', boxShadow: theme.shadow.elevated,
                fontFamily: theme.font,
            }}>
                <h2 style={{ margin: '0 0 20px 0', fontSize: '18px', color: theme.colors.textPrimary }}>
                    {editingItem ? 'Edit Item' : 'Add Menu Item'}
                </h2>

                {error && (
                    <p style={{ color: theme.colors.dangerText, fontSize: '13px', background: theme.colors.dangerBg, padding: '8px 12px', borderRadius: theme.radius.sm, marginBottom: '16px' }}>
                        {error}
                    </p>
                )}

                <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                    <input placeholder="Item name" value={name} onChange={(e) => setName(e.target.value)} style={inputStyle} />
                    <textarea placeholder="Description (optional)" value={description} onChange={(e) => setDescription(e.target.value)} rows={2} style={{ ...inputStyle, resize: 'vertical' as const }} />
                    <input placeholder="Cost (RWF)" type="number" step="0.01" value={cost} onChange={(e) => setCost(e.target.value)} style={inputStyle} />
                    <select value={categoryId} onChange={(e) => setCategoryId(e.target.value ? Number(e.target.value) : '')} style={inputStyle}>
                        <option value="">Select category…</option>
                        {categories.map((c) => (
                            <option key={c.id} value={c.id}>{c.name}</option>
                        ))}
                    </select>
                    <label style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px', color: theme.colors.textSecondary }}>
                        <input type="checkbox" checked={available} onChange={(e) => setAvailable(e.target.checked)} />
                        Available for order
                    </label>

                    <div style={{ display: 'flex', gap: '10px', marginTop: '10px' }}>
                        <button type="button" onClick={onClose} style={{ flex: 1, padding: '11px', borderRadius: theme.radius.sm, border: `1px solid ${theme.colors.border}`, background: 'white', cursor: 'pointer' }}>
                            Cancel
                        </button>
                        <LoadingButton type="submit" loading={submitting} loadingText="Saving…" style={{ flex: 1, padding: '11px', borderRadius: theme.radius.sm, border: 'none', background: theme.colors.brand, color: 'white', fontWeight: 'bold' }}>
                            {editingItem ? 'Save Changes' : 'Create Item'}
                        </LoadingButton>
                    </div>
                </form>
            </div>
        </div>
    );
};

const inputStyle: React.CSSProperties = {
    padding: '11px', borderRadius: '6px', border: '1px solid #ccc', fontSize: '14px', backgroundColor: '#f9f9f9', width: '100%', boxSizing: 'border-box',
};