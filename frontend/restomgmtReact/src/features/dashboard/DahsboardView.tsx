import React from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { type RootState } from '../../store/store';
import { setActiveTab, type DashboardCategoryTab, type DashboardItem } from './dashboardSlice';

export const DshboardView: React.FC = () => {
    const dispatch = useDispatch();

    //grabbing data from from global Redux dashboard cloud
    const { items, activeTab } = useSelector((state: RootState) => state.dashboard);

    //dynamic filtration of items based on the active tab
    const filteredItems = items.filter((item: DashboardItem) => {
        if (activeTab === 'all'){
            return true;
        } else if (activeTab === 'food') {
            return item.category === 'food';
        } else if (activeTab === 'drinks') {
            return item.category === 'drinks';
        } else if (activeTab === 'combo') {
            return item.category === 'combo';
        } 
    });

    //category tabs
    const tabs: { id: DashboardCategoryTab; label: string } [] = [
        {id: 'all', label: 'All Items'},
        {id: 'food', label: 'Food'},
        {id: 'drinks', label: 'Drinks'},
        {id: 'popular', label: 'Most Popular'},
    ];

    return (
        <div style={{ padding: '20px', fontFamily: 'sans-serif', maxWidth: '800px', margin: '0 auto'}}>
            <h1 style={{ color: '#333', marginBottom: '24px' }}>Menu</h1>

            {}
            <div style={{ display: 'flex', gap: '12px', marginBottom: '30px', borderBottom: '1px solid #eee', paddingBottom: '12px'}}>
                {tabs.map((tab) => {
                    const isActive = activeTab === tab.id;
                    return (
                        <button
                            key={tab.id}
                            onClick={() => dispatch(setActiveTab(tab.id))}
                            style={{
                                padding: '10px 20px',
                                borderRadius: '20px',
                                border: 'none',
                                cursor: 'pointer',
                                fontWeight: 'bold',
                                fontSize: '14px',
                                backgroundColor: isActive ? 'red' : '#f5f5f5',
                                color: isActive ? 'white' : '#555',
                                transition: 'all 0.2s ease'
                            }}
                        >
                            {tab.label}
                        </button>
                    );
                })}
            </div>

            {}
            <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))',
                gap: '20px'
            }}>
                {filteredItems.map((item) => (
                    <div
                        key={item.id}
                        style={{
                            border: '1px solid #e0e0e0',
                            borderRadius: '12px',
                            padding: '16px',
                            textAlign: 'center',
                            boxShadow: '0 4px 6px rgba(0,0,0,0.02)'
                        }}
                    >
                    <h3 style={{ margin: '12px 0 6px 0', fontSize: '16px', color: '#333' }}>{item.name}</h3>
                    <p style={{ color: '#E78B6D', fontWeight: 'bold', margin: '0 0 12px 0'}}>
                        {item.price.toLocaleString()} RWF
                    </p>

                    {}
                    <button style={{
                        width: '100%',
                        padding: '8px',
                        background: '#333',
                        color: 'white',
                        border: 'none',
                        borderRadius: '6px',
                        cursor: 'pointer',
                        fontWeight: 'bold'
                    }}>
                        Add to Order
                    </button>
                </div>
                ))}
            </div>
        </div>
    );
};