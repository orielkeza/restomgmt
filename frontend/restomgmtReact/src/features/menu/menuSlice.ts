import { createSlice } from '@reduxjs/toolkit';
import type { PayloadAction } from '@reduxjs/toolkit';

export interface MenuItem {
    id: number;
    name: string;
    price: number;
    category: 'food' | 'drinks' | 'combo';
    isPopular: boolean;
}

export type MenuCategoryTab = 'all' | 'food' | 'drinks' | 'combo' | 'popular';

interface MenuState {
    items: MenuItem[];
    activeTab: MenuCategoryTab
}

const initialState: MenuState = {
    activeTab: 'all',
    items: [
        {id: 1, name: 'Burger', price: 5500, category: 'food', isPopular: true},
        {id: 2, name: 'Pizza', price: 15000, category: 'food', isPopular: true},
        {id: 3, name: 'Igitoki', price: 4000, category: 'food', isPopular: false},
        {id: 4, name: 'Coke', price: 1000, category: 'drinks', isPopular: true},
        {id: 5, name: '5 Piece Chicken Combo', price: 2000, category: 'combo', isPopular: true},
        {id: 6, name: 'Coffee', price: 2500, category: 'drinks', isPopular: false},
        {id: 7, name: 'Seafood Mix', price: 50000, category: 'combo', isPopular: false},
    ],
};

export const menuSlice = createSlice({
    name: 'menu',
    initialState,
    reducers: {
        setActiveTab: (state, action: PayloadAction<MenuCategoryTab>) => {
            state.activeTab = action.payload;
        },
    },
});

export const { setActiveTab } = menuSlice.actions;
export default menuSlice.reducer;