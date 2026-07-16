import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { type RootState, type AppDispatch } from '../../store/store';
import { setActiveTab, fetchMenuData } from './menuSlice';
import { theme } from '../../theme';

export const MenuView: React.FC = () => {
    const dispatch = useDispatch<AppDispatch>();
    const { items, categories, activeTab, status, error } = useSelector((state: RootState) => state.menu);

    useEffect(() => {
        if (status === 'idle') {
            dispatch(fetchMenuData());
        }
    }, [status, dispatch]);

    const filteredItems = activeTab === 'all'
        ? items
        : items.filter((item) => item.categoryName === activeTab);

    if (status === 'loading' || status === 'idle') {
        return (
            <div style={{ padding: '40px', textAlign: 'center', color: theme.colors.textSecondary }}>
                Loading menu…
            </div>
        );
    }

    if (status === 'failed') {
        return (
            <div style={{ padding: '40px', textAlign: 'center', color: theme.colors.dangerText }}>
                Couldn't load the menu: {error}
                <div style={{ marginTop: '12px' }}>
                    <button
                        onClick={() => dispatch(fetchMenuData())}
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
            <div style={{ display: 'flex', gap: '10px', marginBottom: '28px', flexWrap: 'wrap' }}>
                <button
                    onClick={() => dispatch(setActiveTab('all'))}
                    style={tabStyle(activeTab === 'all')}
                >
                    All Items
                </button>
                {categories.map((cat) => (
                    <button
                        key={cat.id}
                        onClick={() => dispatch(setActiveTab(cat.name))}
                        style={tabStyle(activeTab === cat.name)}
                    >
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

                        <button
                            disabled={!item.available}
                            style={{
                                width: '100%', padding: '10px',
                                background: item.available ? theme.colors.brand : '#E5E7EB',
                                color: item.available ? 'white' : theme.colors.textMuted,
                                border: 'none', borderRadius: theme.radius.sm,
                                cursor: item.available ? 'pointer' : 'not-allowed',
                                fontWeight: 'bold', fontSize: '13px',
                            }}
                        >
                            {item.available ? 'Add to Order' : 'Unavailable'}
                        </button>
                    </div>
                ))}
            </div>
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