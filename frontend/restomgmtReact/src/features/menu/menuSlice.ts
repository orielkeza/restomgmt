import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import type { PayloadAction } from '@reduxjs/toolkit';
import { menuApi, type MenuItemResponse, type CategoryResponse } from '../../api/menuApi';
import type { RootState } from '../../store/store';

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

// Admin/staff-only: fetch ALL items including unavailable ones
export const fetchAllItemsAdmin = createAsyncThunk<
    MenuItemResponse[],
    void,
    { state: RootState }
>('menu/fetchAllItemsAdmin', async (_, { getState, rejectWithValue }) => {
    const token = getState().auth.token;
    if (!token) {
        return rejectWithValue('Not authenticated');
    }
    try {
        return await menuApi.getAllItems(token);
    } catch (err) {
        return rejectWithValue(err instanceof Error ? err.message : 'Failed to load items');
    }
});

// Toggle a single item's availability, then patch it into state directly
// (avoids refetching the whole list just to reflect one flipped boolean)
export const toggleItemAvailability = createAsyncThunk<
    MenuItemResponse,
    number, // itemId
    { state: RootState }
>('menu/toggleItemAvailability', async (itemId, { getState, rejectWithValue }) => {
    const token = getState().auth.token;
    if (!token) {
        return rejectWithValue('Not authenticated');
    }
    try {
        return await menuApi.toggleAvailability(itemId, token);
    } catch (err) {
        return rejectWithValue(err instanceof Error ? err.message : 'Failed to update item');
    }
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
            .addCase(fetchMenuData.pending, (state) => { state.status = 'loading'; state.error = null; })
            .addCase(fetchMenuData.fulfilled, (state, action) => {
                state.status = 'succeeded';
                state.items = action.payload.items;
                state.categories = action.payload.categories;
            })
            .addCase(fetchMenuData.rejected, (state, action) => {
                state.status = 'failed';
                state.error = action.error.message ?? 'Failed to load menu';
            })
            .addCase(fetchAllItemsAdmin.fulfilled, (state, action) => {
                state.items = action.payload;
                state.status = 'succeeded';
            })
            .addCase(fetchAllItemsAdmin.rejected, (state, action) => {
                state.error = (action.payload as string) ?? 'Failed to load items';
            })
            .addCase(toggleItemAvailability.fulfilled, (state, action) => {
                // swap just the one item that changed, in place
                const idx = state.items.findIndex((i) => i.id === action.payload.id);
                if (idx !== -1) state.items[idx] = action.payload;
            })
            .addCase(toggleItemAvailability.rejected, (state, action) => {
                state.error = (action.payload as string) ?? 'Failed to update availability';
            });
    },
});

export const { setActiveTab } = menuSlice.actions;
export default menuSlice.reducer;