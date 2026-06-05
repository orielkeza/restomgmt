import { createSlice } from '@reduxjs/toolkit';
import type { PayloadAction } from '@reduxjs/toolkit';

export interface DashboardItem {
    /*id: number | null;
    name: string |null;
    customerName: string | null;
    date: Date | null;
    meal: string | null;
    price: number| null;
    payment: boolean | null;
    orderProcessed: boolean | null;
    items: Array<string> | null; */
    id: number;
    name?: string;
    customerName?: string;
    date?: Date;
    meal?: string;
    price?: number;
    payment?: boolean;
    status?: 'order taken' | 'preparing' | 'done';
    items?: Array<string>;
    category: 'bookings' | 'orders' | 'payments';
    tableNumber?: number;
}

export type DashboardCategoryTab = 'bookings' | 'orders' | 'payments';

interface DashboardState {
    items: DashboardItem[];
    activeTab: DashboardCategoryTab
}

const initialState: DashboardState = {
    activeTab: 'bookings',
    items: [
        {id: 1, customerName: 'Charles', date: new Date("2026-06-04"), items: ["Seafood Platter"], tableNumber: 1, category: 'bookings' },
        {id: 2, customerName: 'James', date: new Date("2026-11-24"), items: ["5 Piece Chicken Combo", "Virgin Mojito"], tableNumber: 9, category: 'bookings' }, 
        {id: 3, customerName: 'Sophia', date: new Date("2027-09-11"), status: 'order taken', tableNumber: 6, category: 'orders' },
        {id: 4, customerName: 'Laura', date: new Date("2026-05-19"), status: 'preparing', tableNumber: 4, category: 'orders' },
        {id: 5, customerName: 'Jean', date: new Date("2026-12-01"), status: 'done', tableNumber: 1, category: 'orders' },
        {id: 6, customerName: 'Luka', date: new Date("2026-04-15"), price: 60, payment: true, category: 'payments' },
        {id: 7, customerName: 'Shaq', date: new Date("2026-10-06"), price: 30, payment: false, category: 'payments' },
    ],
};

export const dashboardSlice = createSlice({
    name: 'dashboard',
    initialState,
    reducers: {
        setActiveTab: (state, action: PayloadAction<DashboardCategoryTab>) => {
            state.activeTab = action.payload;
        },
    },
});

export const { setActiveTab } = dashboardSlice.actions;
export default dashboardSlice.reducer;