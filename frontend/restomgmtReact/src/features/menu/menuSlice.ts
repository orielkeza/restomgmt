import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import type { PayloadAction } from '@reduxjs/toolkit';
import { menuApi, type MenuItemResponse, type CategoryResponse, type MenuItemRequest } from '../../api/menuApi';
import type { RootState } from '../../store/store';

export type MenuCategoryTab = string; // categoryName, or 'all'

interface MenuState {
    items: MenuItemResponse[];
    categories: CategoryResponse[];
    activeTab: MenuCategoryTab;
    status: 'idle' | 'loading' | 'succeeded' | 'failed';
    pendingItemIds: number[];
    error: string | null;
}

const initialState: MenuState = {
    items: [],
    categories: [],
    activeTab: 'all',
    status: 'idle',
    pendingItemIds: [],
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

export const createMenuItem = createAsyncThunk<MenuItemResponse, MenuItemRequest, { state: RootState }>(
    'menu/createMenuItem',
    async (payload, { getState, rejectWithValue }) => {
        try { return await menuApi.createItem(getState().auth.token, payload); }
        catch (err) { return rejectWithValue(err instanceof Error ? err.message : 'Failed to create item'); }
    }
);

export const updateMenuItem = createAsyncThunk<MenuItemResponse, { id: number; payload: MenuItemRequest }, { state: RootState }>(
    'menu/updateMenuItem',
    async ({ id, payload }, { getState, rejectWithValue }) => {
        try { return await menuApi.updateItem(getState().auth.token, id, payload); }
        catch (err) { return rejectWithValue(err instanceof Error ? err.message : 'Failed to update item'); }
    }
);

export const deleteMenuItem = createAsyncThunk<number, number, { state: RootState }>(
    'menu/deleteMenuItem',
    async (id, { getState, rejectWithValue }) => {
        try { await menuApi.deleteItem(getState().auth.token, id); return id; }
        catch (err) { return rejectWithValue(err instanceof Error ? err.message : 'Failed to delete item'); }
    }
);

// Admin/staff-only: fetch ALL items including unavailable ones
export const fetchAllItemsAdmin = createAsyncThunk(
  'menu/fetchAllItemsAdmin', 
  async (_, { getState, rejectWithValue }) => {
    const stateCast = getState() as RootState;
    const token = stateCast.auth.token;
    if (!token) return rejectWithValue('Not authenticated');
    try {
      const [items, categories] = await Promise.all([
        menuApi.getAllItems(token),
        menuApi.getCategories(),
      ]);
      return { items, categories };
    } catch (err) {
      return rejectWithValue(err instanceof Error ? err.message : 'Failed to load items');
    }
  }
);

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

function addPending(state: MenuState, id: number) {
    if (!state.pendingItemIds.includes(id)) {
        state.pendingItemIds.push(id);
    }
}

function removePending(state: MenuState, id: number) {
    state.pendingItemIds = state.pendingItemIds.filter(
        (pendingId) => pendingId !== id
    );
}

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
                state.status = 'succeeded';
                state.items = action.payload.items;          // <-- Extract items
                state.categories = action.payload.categories; // <-- Extract categories
            })
            .addCase(fetchAllItemsAdmin.rejected, (state, action) => {
                state.error = (action.payload as string) ?? 'Failed to load items';
            })
            .addCase(toggleItemAvailability.pending, (state, action) => {
                addPending(state, action.meta.arg);
            })
            .addCase(toggleItemAvailability.fulfilled, (state, action) => {
                removePending(state, action.payload.id);

                const idx = state.items.findIndex((i) => i.id === action.payload.id);
                if (idx !== -1) {
                    state.items[idx] = action.payload;
                }
            })
            .addCase(toggleItemAvailability.rejected, (state, action) => {
                removePending(state, action.meta.arg);
                state.error = (action.payload as string) ?? 'Failed to update availability';
            })
            .addCase(createMenuItem.fulfilled, (state, action) => { state.items.push(action.payload); })
            .addCase(updateMenuItem.pending, (state, action) => {
                addPending(state, action.meta.arg.id);
            })
            .addCase(updateMenuItem.fulfilled, (state, action) => {
                removePending(state, action.payload.id);

                const idx = state.items.findIndex((i) => i.id === action.payload.id);
                if (idx !== -1) {
                    state.items[idx] = action.payload;
                }
            })
            .addCase(updateMenuItem.rejected, (state, action) => {
                removePending(state, action.meta.arg.id);
                state.error = (action.payload as string) ?? 'Failed to update item';
            })
            .addCase(deleteMenuItem.pending, (state, action) => {
                addPending(state, action.meta.arg);
            })
            .addCase(deleteMenuItem.fulfilled, (state, action) => {
                removePending(state, action.payload);

                state.items = state.items.filter(
                    (i) => i.id !== action.payload
                );
            })
            .addCase(deleteMenuItem.rejected, (state, action) => {
                removePending(state, action.meta.arg);
                state.error = (action.payload as string) ?? 'Failed to delete item';
            });
    },
});

export const { setActiveTab } = menuSlice.actions;
export default menuSlice.reducer;