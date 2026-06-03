import { createSlice } from '@reduxjs/toolkit';
import type { PayloadAction } from '@reduxjs/toolkit';

export interface DashboardItem {
    id: number;
    name: string;
    price: number;
    category: 'food' | 'drinks' | 'combo';
    isPopular: boolean;
}

export type DashboardCategoryTab = 'all' | 'food' | 'drinks' | 'combo' | 'popular';

interface DashboardState {
    items: DashboardItem[];
    activeTab: DashboardCategoryTab
}

const initialState: DashboardState = {
    activeTab: 'all',
    items: [
        {id: 1, name: 'Burger', price: 5500, category: 'food', isPopular: true},
        {id: 2, name: 'Pizza', price: 15000, category: 'food', isPopular: true},
        {id: 3, name: 'Igitoki', price: 4000, category: 'food', isPopular: false},
        {id: 4, name: 'Coke', price: 1000, category: 'drinks', isPopular: true},
        {id: 5, name: '5 Piece Chicken Combo', price: 2000, category: 'combo', isPopular: true},
        {id: 6, name: 'Coffee', price: 2500, category: 'drinks', isPopular: false},
        {id: 1, name: 'Seafood Mix', price: 50000, category: 'combo', isPopular: false},
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