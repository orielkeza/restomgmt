import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import {
    menuApi,
    type MenuItemResponse,
    type CategoryResponse,
    type MenuItemRequest,
    type CategoryRequest,
} from '../../api/menuApi';
import type { RootState } from '../../store/store';

interface MenuState {
    items: MenuItemResponse[];
    categories: CategoryResponse[];
    activeTab: string; // categoryName, or 'all'
    status: 'idle' | 'loading' | 'succeeded' | 'failed';
    createStatus: 'idle' | 'loading' | 'failed';
    categoryMutationStatus: 'idle' | 'loading' | 'failed';
    pendingItemIds: number[]; // items with an in-flight toggle/update/delete
    error: string | null;
}

const initialState: MenuState = {
    items: [],
    categories: [],
    activeTab: 'all',
    status: 'idle',
    createStatus: 'idle',
    categoryMutationStatus: 'idle',
    pendingItemIds: [],
    error: null,
};

type ThunkConfig = { state: RootState };

// ---- Reads ----

export const fetchMenuData = createAsyncThunk('menu/fetchMenuData', async () => {
    const [items, categories] = await Promise.all([
        menuApi.getAvailableItems(),
        menuApi.getCategories(),
    ]);
    return { items, categories };
});

export const fetchAllItemsAdmin = createAsyncThunk<MenuItemResponse[], void, ThunkConfig>(
    'menu/fetchAllItemsAdmin',
    async (_, { getState, rejectWithValue }) => {
        try {
            return await menuApi.getAllItems(getState().auth.token);
        } catch (err) {
            return rejectWithValue(err instanceof Error ? err.message : 'Failed to load items');
        }
    }
);

// ---- Menu item mutations ----

export const toggleItemAvailability = createAsyncThunk<MenuItemResponse, number, ThunkConfig>(
    'menu/toggleItemAvailability',
    async (itemId, { getState, rejectWithValue }) => {
        try {
            return await menuApi.toggleAvailability(itemId, getState().auth.token);
        } catch (err) {
            return rejectWithValue(err instanceof Error ? err.message : 'Failed to update item');
        }
    }
);

export const createMenuItem = createAsyncThunk<MenuItemResponse, MenuItemRequest, ThunkConfig>(
    'menu/createMenuItem',
    async (payload, { getState, rejectWithValue }) => {
        try {
            return await menuApi.createItem(getState().auth.token, payload);
        } catch (err) {
            return rejectWithValue(err instanceof Error ? err.message : 'Failed to create item');
        }
    }
);

export const updateMenuItem = createAsyncThunk <
    MenuItemResponse,
    { id: number; payload: MenuItemRequest },
    ThunkConfig
>('menu/updateMenuItem', async ({ id, payload }, { getState, rejectWithValue }) => {
    try {
        return await menuApi.updateItem(getState().auth.token, id, payload);
    } catch (err) {
        return rejectWithValue(err instanceof Error ? err.message : 'Failed to update item');
    }
});

export const deleteMenuItem = createAsyncThunk<number, number, ThunkConfig>(
    'menu/deleteMenuItem',
    async (id, { getState, rejectWithValue }) => {
        try {
            await menuApi.deleteItem(getState().auth.token, id);
            return id;
        } catch (err) {
            return rejectWithValue(err instanceof Error ? err.message : 'Failed to delete item');
        }
    }
);

// ---- Category mutations ----

export const createCategory = createAsyncThunk<CategoryResponse, CategoryRequest, ThunkConfig>(
    'menu/createCategory',
    async (payload, { getState, rejectWithValue }) => {
        try {
            return await menuApi.createCategory(getState().auth.token, payload);
        } catch (err) {
            return rejectWithValue(err instanceof Error ? err.message : 'Failed to create category');
        }
    }
);

export const updateCategory = createAsyncThunk <
    CategoryResponse,
    { id: number; payload: CategoryRequest },
    ThunkConfig
>('menu/updateCategory', async ({ id, payload }, { getState, rejectWithValue }) => {
    try {
        return await menuApi.updateCategory(getState().auth.token, id, payload);
    } catch (err) {
        return rejectWithValue(err instanceof Error ? err.message : 'Failed to update category');
    }
});

export const deleteCategory = createAsyncThunk<number, number, ThunkConfig>(
    'menu/deleteCategory',
    async (id, { getState, rejectWithValue }) => {
        try {
            await menuApi.deleteCategory(getState().auth.token, id);
            return id;
        } catch (err) {
            return rejectWithValue(err instanceof Error ? err.message : 'Failed to delete category');
        }
    }
);

function addPending(state: MenuState, id: number) {
    if (!state.pendingItemIds.includes(id)) state.pendingItemIds.push(id);
}
function removePending(state: MenuState, id: number) {
    state.pendingItemIds = state.pendingItemIds.filter((i) => i !== id);
}

export const menuSlice = createSlice({
    name: 'menu',
    initialState,
    reducers: {
        setActiveTab: (state, action) => {
            state.activeTab = action.payload;
        },
    },
    extraReducers: (builder) => {
        builder
            // fetchMenuData
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
            })
            // fetchAllItemsAdmin
            .addCase(fetchAllItemsAdmin.pending, (state) => {
                state.status = 'loading';
                state.error = null;
            })
            .addCase(fetchAllItemsAdmin.fulfilled, (state, action) => {
                state.items = action.payload;
                state.status = 'succeeded';
            })
            .addCase(fetchAllItemsAdmin.rejected, (state, action) => {
                state.status = 'failed';
                state.error = (action.payload as string) ?? 'Failed to load items';
            })
            // createMenuItem
            .addCase(createMenuItem.pending, (state) => {
                state.createStatus = 'loading';
                state.error = null;
            })
            .addCase(createMenuItem.fulfilled, (state, action) => {
                state.createStatus = 'idle';
                state.items.push(action.payload);
            })
            .addCase(createMenuItem.rejected, (state, action) => {
                state.createStatus = 'failed';
                state.error = (action.payload as string) ?? 'Failed to create item';
            })
            // toggleItemAvailability (per-item)
            .addCase(toggleItemAvailability.pending, (state, action) => addPending(state, action.meta.arg))
            .addCase(toggleItemAvailability.fulfilled, (state, action) => {
                removePending(state, action.payload.id);
                const idx = state.items.findIndex((i) => i.id === action.payload.id);
                if (idx !== -1) state.items[idx] = action.payload;
            })
            .addCase(toggleItemAvailability.rejected, (state, action) => {
                removePending(state, action.meta.arg);
                state.error = (action.payload as string) ?? 'Failed to update item';
            })
            // updateMenuItem (per-item)
            .addCase(updateMenuItem.pending, (state, action) => addPending(state, action.meta.arg.id))
            .addCase(updateMenuItem.fulfilled, (state, action) => {
                removePending(state, action.payload.id);
                const idx = state.items.findIndex((i) => i.id === action.payload.id);
                if (idx !== -1) state.items[idx] = action.payload;
            })
            .addCase(updateMenuItem.rejected, (state, action) => {
                removePending(state, action.meta.arg.id);
                state.error = (action.payload as string) ?? 'Failed to update item';
            })
            // deleteMenuItem (per-item)
            .addCase(deleteMenuItem.pending, (state, action) => addPending(state, action.meta.arg))
            .addCase(deleteMenuItem.fulfilled, (state, action) => {
                removePending(state, action.payload);
                state.items = state.items.filter((i) => i.id !== action.payload);
            })
            .addCase(deleteMenuItem.rejected, (state, action) => {
                removePending(state, action.meta.arg);
                state.error = (action.payload as string) ?? 'Failed to delete item';
            })
            // createCategory
            .addCase(createCategory.pending, (state) => {
                state.categoryMutationStatus = 'loading';
                state.error = null;
            })
            .addCase(createCategory.fulfilled, (state, action) => {
                state.categoryMutationStatus = 'idle';
                state.categories.push(action.payload);
            })
            .addCase(createCategory.rejected, (state, action) => {
                state.categoryMutationStatus = 'failed';
                state.error = (action.payload as string) ?? 'Failed to create category';
            })
            // updateCategory
            .addCase(updateCategory.fulfilled, (state, action) => {
                const idx = state.categories.findIndex((c) => c.id === action.payload.id);
                if (idx !== -1) state.categories[idx] = action.payload;
            })
            .addCase(updateCategory.rejected, (state, action) => {
                state.error = (action.payload as string) ?? 'Failed to update category';
            })
            // deleteCategory
            .addCase(deleteCategory.fulfilled, (state, action) => {
                state.categories = state.categories.filter((c) => c.id !== action.payload);
            })
            .addCase(deleteCategory.rejected, (state, action) => {
                state.error = (action.payload as string) ?? 'Failed to delete category';
            });
    },
});

export const { setActiveTab } = menuSlice.actions;
export default menuSlice.reducer;