import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import type { PayloadAction } from '@reduxjs/toolkit';
import { menuApi, type MenuItemResponse, type CategoryResponse } from '../../api/menuApi';

export type MenuCategoryTab = string; // categoryName, or 'all'

interface MenuState {
    items: MenuItemResponse[];
    categories: CategoryResponse[];
    activeTab: MenuCategoryTab;
    status: 'idle' | 'loading' | 'succeeded' | 'failed';
    error: string | null;
}

const initialState: MenuState = {
    items: [],
    categories: [],
    activeTab: 'all',
    status: 'idle',
    error: null,
};

// fetches both items and categories together since the menu screen always needs both
export const fetchMenuData = createAsyncThunk('menu/fetchMenuData', async () => {
    const [items, categories] = await Promise.all([
        menuApi.getAvailableItems(),
        menuApi.getCategories(),
    ]);
    return { items, categories };
});

export const menuSlice = createSlice({
    name: 'menu',
    initialState,
    reducers: {
        setActiveTab: (state, action: PayloadAction<MenuCategoryTab>) => {
            state.activeTab = action.payload;
        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchMenuData.pending, (state) => {
                state.status = 'loading';
                state.error = null;
            })
            .addCase(fetchMenuData.fulfilled, (state, action) => {
                state.status = 'succeeded';
                state.items = action.payload.items;
                state.categories = action.payload.categories;
            })
            .addCase(fetchMenuData.rejected, (state, action) => {
                state.status = 'failed';
                state.error = action.error.message ?? 'Failed to load menu';
            });
    },
});

export const { setActiveTab } = menuSlice.actions;
export default menuSlice.reducer;