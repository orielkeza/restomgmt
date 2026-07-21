import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { type RootState, type AppDispatch } from '../../store/store';
import { setActiveTab, fetchMenuData, fetchAllItemsAdmin, toggleItemAvailability, deleteMenuItem } from './menuSlice';
import { addToCart } from '../cart/cartSlice';
import { type MenuItemResponse } from '../../api/menuApi';
import { MenuItemFormModal } from './MenuItemFormModal';
import { theme } from '../../theme';
import { PageLoader } from '../../components/PageLoader';
import { LoadingButton } from '../../components/LoadingButton';

export const MenuView: React.FC = () => {
    const dispatch = useDispatch<AppDispatch>();
    const { items, categories, activeTab, status, error } = useSelector((state: RootState) => state.menu);
    const roles = useSelector((state: RootState) => state.auth.roles);
    const viewMode = useSelector((state: RootState) => state.auth.viewMode);

    const canManageMenu = viewMode === 'staff' && (roles.includes('ROLE_ADMIN') || roles.includes('STAFF'));
    const [modalItem, setModalItem] = useState<MenuItemResponse | 'new' | null>(null);

    const pendingItemIds = useSelector((state: RootState) => state.menu.pendingItemIds);

    useEffect(() => {
        if (status === 'idle') {
            dispatch(canManageMenu ? fetchAllItemsAdmin() : fetchMenuData());
        }
    }, [status, canManageMenu, dispatch]);

    const filteredItems = activeTab === 'all'
        ? items
        : items.filter((item) => item.categoryName === activeTab);

    if (status === 'loading' || status === 'idle') {
        return <PageLoader label="Loading menu…" />;
    }

    if (status === 'failed') {
        return (
            <div style={{ padding: '40px', textAlign: 'center', color: theme.colors.dangerText }}>
                Couldn't load the menu: {error}
                <div style={{ marginTop: '12px' }}>
                    <button
                        onClick={() => dispatch(canManageMenu ? fetchAllItemsAdmin() : fetchMenuData())}
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

    return (
        <div style={{ fontFamily: theme.font }}>
            {canManageMenu && (
                <button
                    onClick={() => setModalItem('new')}
                    style={{
                        marginBottom: '16px', padding: '10px 18px', borderRadius: theme.radius.sm,
                        border: 'none', background: theme.colors.brand, color: 'white',
                        fontWeight: 'bold', cursor: 'pointer',
                    }}
                >
                    + Add Menu Item
                </button>
            )}

            <div style={{ display: 'flex', gap: '10px', marginBottom: '28px', flexWrap: 'wrap' }}>
                <button onClick={() => dispatch(setActiveTab('all'))} style={tabStyle(activeTab === 'all')}>
                    All Items
                </button>
                {categories.map((cat) => (
                    <button key={cat.id} onClick={() => dispatch(setActiveTab(cat.name))} style={tabStyle(activeTab === cat.name)}>
                        {cat.name}
                    </button>
                ))}
            </div>

            <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))',
                gap: '20px',
            }}>
                {filteredItems.map((item) => (
                    <div
                        key={item.id}
                        style={{
                            background: theme.colors.surface,
                            border: `1px solid ${theme.colors.border}`,
                            borderRadius: theme.radius.md,
                            padding: '20px',
                            boxShadow: theme.shadow.card,
                            opacity: item.available ? 1 : 0.55,
                        }}
                    >
                        <div style={{
                            width: '100%', height: '120px', borderRadius: theme.radius.sm,
                            background: '#FFF3EC', marginBottom: '16px',
                            display: 'flex', alignItems: 'center', justifyContent: 'center',
                            fontSize: '32px',
                        }}>
                            🍽
                        </div>

                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                            <h3 style={{ margin: '0 0 6px 0', fontSize: '15px', color: theme.colors.textPrimary }}>
                                {item.name}
                            </h3>
                            {!item.available && (
                                <span style={{
                                    fontSize: '10px', fontWeight: 700, color: theme.colors.dangerText,
                                    background: theme.colors.dangerBg, padding: '3px 8px', borderRadius: '10px',
                                }}>
                                    UNAVAILABLE
                                </span>
                            )}
                        </div>

                        {item.description && (
                            <p style={{ fontSize: '12px', color: theme.colors.textMuted, margin: '0 0 8px 0' }}>
                                {item.description}
                            </p>
                        )}

                        <p style={{ color: theme.colors.textSecondary, fontWeight: 700, margin: '0 0 16px 0', fontSize: '14px' }}>
                            {item.cost.toLocaleString()} RWF
                        </p>

                        {!canManageMenu && (
                            // Add to Order:
                            <LoadingButton
                                loading={pendingItemIds.includes(item.id)}
                                disabled={!item.available}
                                onClick={() => dispatch(addToCart({ menuItemId: item.id, quantity: 1 }))}
                                style={{ width: '100%', padding: '10px', background: item.available ? theme.colors.brand : '#E5E7EB', color: item.available ? 'white' : theme.colors.textMuted, border: 'none', borderRadius: theme.radius.sm, fontWeight: 'bold', fontSize: '13px' }}
                            >
                                {item.available ? 'Add to Order' : 'Unavailable'}
                            </LoadingButton>
                        )}

                        {canManageMenu && (
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                                <LoadingButton
                                    loading={pendingItemIds.includes(item.id)}
                                    onClick={() => dispatch(toggleItemAvailability(item.id))}
                                    style={{ width: '100%', padding: '8px', border: `1px solid ${theme.colors.border}`, background: 'white', borderRadius: theme.radius.sm, fontSize: '12px', color: theme.colors.textSecondary }}
                                >
                                    {item.available ? 'Mark Unavailable' : 'Mark Available'}
                                </LoadingButton>
                                <div style={{ display: 'flex', gap: '8px' }}>
                                    <button onClick={() => setModalItem(item)} style={smallBtnStyle}>Edit</button>
                                    <button
                                        onClick={() => { if (window.confirm(`Delete ${item.name}?`)) dispatch(deleteMenuItem(item.id)); }}
                                        style={{ ...smallBtnStyle, color: theme.colors.dangerText }}
                                    >
                                        Delete
                                    </button>
                                </div>
                            </div>
                        )}
                    </div>
                ))}
            </div>

            {modalItem && (
                <MenuItemFormModal
                    editingItem={modalItem === 'new' ? null : modalItem}
                    onClose={() => setModalItem(null)}
                />
            )}
        </div>
    );
};

function tabStyle(isActive: boolean): React.CSSProperties {
    return {
        padding: '9px 18px',
        borderRadius: '20px',
        border: isActive ? 'none' : `1px solid ${theme.colors.border}`,
        cursor: 'pointer',
        fontWeight: 600,
        fontSize: '13px',
        backgroundColor: isActive ? theme.colors.brand : theme.colors.surface,
        color: isActive ? 'white' : theme.colors.textSecondary,
    };
}

const smallBtnStyle: React.CSSProperties = {
    flex: 1, padding: '6px', fontSize: '11px', fontWeight: 600,
    border: `1px solid ${theme.colors.border}`, background: 'white',
    borderRadius: theme.radius.sm, cursor: 'pointer', color: theme.colors.textSecondary,
};