import React, { type JSX } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { type RootState } from '../../store/store';
import { setActiveTab, type DashboardCategoryTab, type DashboardItem } from './dashboardSlice';
import { theme } from '../../theme';

export const DashboardView: React.FC = () => {
    const dispatch = useDispatch();

    //grabbing data from from global Redux dashboard cloud
    const { items, activeTab } = useSelector((state: RootState) => state.dashboard);

    //decides display of dashboard and what features are shown
    const categoryConfig: Record<string, { bg?: string; text?: string; renderDetails: (item: DashboardItem) => JSX.Element }> = {
        'bookings': {
            bg: 'white',
            renderDetails: (item: DashboardItem) => (
                <p style={{
                    color: '#E78B6D',
                    fontWeight: 'bold',
                    margin: '0 0 12px 0'}}>
                    <span>Reserved Under: {item.customerName}</span><br/>
                    <span>Meal: {item.meal}</span><br/>
                    <span>Table: {item.tableNumber}</span>
                </p>
            )
        },
        'orders': {
            renderDetails: (item: DashboardItem) => (
                <p style={{
                    color: '#E78B6D',
                    fontWeight: 'bold',
                    margin: '0 0 12px 0'}}>
                    <span>Table: {item.tableNumber}</span><br/>
                    <span>Order: {String(item.items)}</span><br/>
                    <span>Status: {item.status}</span>
                </p>
            )
        },
        'payments': {
            renderDetails: (item: DashboardItem) => (
                <p style={{
                    color: '#E78B6D',
                    fontWeight: 'bold',
                    margin: '0 0 12px 0'}}>
                    <span>Customer: {item.customerName}</span><br/>
                    <span>Amount: {item.price}</span><br/>
                    <span>Status: {item.payment}</span>
                </p>
            )
        }
    };

    //dynamic filtration of items based on the active tab
    const filteredItems = items.filter((item: DashboardItem) => {
        if (activeTab === 'bookings'){
            return item.category === 'bookings';
        } else if (activeTab === 'orders') {
            return item.category === 'orders';
        } else if (activeTab === 'payments') {
            return item.category === 'payments';
        }
        return false;
    });

    const statCards = [
        { label: 'Bookings', value: items.filter(i => i.category === 'bookings').length, bg: theme.colors.infoBg, fg: theme.colors.infoText },
        { label: 'Orders in Progress', value: items.filter(i => i.category === 'orders' && i.status !== 'done').length, bg: theme.colors.warningBg, fg: theme.colors.warningText },
        { label: 'Payments Pending', value: items.filter(i => i.category === 'payments' && i.payment === false).length, bg: theme.colors.successBg, fg: theme.colors.successText },
    ];

    //category tabs
    const tabs: { id: DashboardCategoryTab; label: string } [] = [
        {id: 'orders', label: 'Orders'},
        {id: 'bookings', label: 'Bookings'},
        {id: 'payments', label: 'Payments'},
    ];

    return (

        <div style={{ padding: '20px', fontFamily: 'sans-serif', maxWidth: '800px', margin: '0 auto'}}>
            <h1 style={{ color: '#333', marginBottom: '30px', textAlign: 'center', marginTop:'60px' }}>Dashboard</h1>
            
            {}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '16px', marginBottom: '24px' }}>
                {statCards.map((c) => (
                    <div key={c.label} style={{ background: c.bg, borderRadius: '12px', padding: '20px' }}>
                        <div style={{ fontSize: '13px', color: '#555', fontWeight: 600 }}>{c.label}</div>
                        <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#111' }}>{c.value}</div>
                    </div>
                ))}
            </div>

            {}
            <div style={{ 
                display: 'flex',
                gap: '12px',
                marginBottom: '30px',
                borderBottom: '1px solid #eee',
                paddingBottom: '12px'}}>
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
                                backgroundColor: theme.colors.brand,
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
                {filteredItems.map((item) => {

                    const currentStatus = item.category;
                    const config = categoryConfig[currentStatus];
                    return (

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
                    <h3 style={{ margin: '12px 0 6px 0', fontSize: '16px', color: '#333' }}>{item.customerName}</h3>
                    {}
                    <span style={{
                        //backgroundColor: config.bg,
                        //color: config.text,
                        padding: '4px 10px',
                        borderRadius: '20px',
                        fontSize: '11px',
                        display: 'inline-block',
                        marginBottom: '12px'
                    }}>
                        {config.renderDetails(item)}
                    </span>

                    
                    
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
                );
            })}
            </div>
        </div>
    )
};
