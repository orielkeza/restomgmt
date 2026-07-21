import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { cartApi, type CartResponse } from '../../api/cartApi';
import type { RootState } from '../../store/store';

interface CartState {
    cart: CartResponse | null;
    status: 'idle' | 'loading' | 'succeeded' | 'failed';
    pendingItemIds: number[]; // menuItemIds with an in-flight add/update/remove
    clearStatus: 'idle' | 'loading' | 'failed';
    error: string | null;
}

const initialState: CartState = {
    cart: null,
    status: 'idle',
    pendingItemIds: [],
    clearStatus: 'idle',
    error: null,
};

type ThunkConfig = { state: RootState };

export const fetchCart = createAsyncThunk<CartResponse, void, ThunkConfig>(
    'cart/fetchCart',
    async (_, { getState, rejectWithValue }) => {
        try { return await cartApi.getCart(getState().auth.token); }
        catch (err) { return rejectWithValue(err instanceof Error ? err.message : 'Failed to load cart'); }
    }
);

export const addToCart = createAsyncThunk<CartResponse, { menuItemId: number; quantity: number }, ThunkConfig>(
    'cart/addToCart',
    async ({ menuItemId, quantity }, { getState, rejectWithValue }) => {
        try { return await cartApi.addItem(getState().auth.token, menuItemId, quantity); }
        catch (err) { return rejectWithValue(err instanceof Error ? err.message : 'Failed to add item'); }
    }
);

export const updateCartItemQuantity = createAsyncThunk<CartResponse, { menuItemId: number; quantity: number }, ThunkConfig>(
    'cart/updateCartItemQuantity',
    async ({ menuItemId, quantity }, { getState, rejectWithValue }) => {
        try { return await cartApi.updateItemQuantity(getState().auth.token, menuItemId, quantity); }
        catch (err) { return rejectWithValue(err instanceof Error ? err.message : 'Failed to update item'); }
    }
);

export const removeFromCart = createAsyncThunk<CartResponse, number, ThunkConfig>(
    'cart/removeFromCart',
    async (menuItemId, { getState, rejectWithValue }) => {
        try { return await cartApi.removeItem(getState().auth.token, menuItemId); }
        catch (err) { return rejectWithValue(err instanceof Error ? err.message : 'Failed to remove item'); }
    }
);

export const clearCart = createAsyncThunk<void, void, ThunkConfig>(
    'cart/clearCart',
    async (_, { getState, rejectWithValue }) => {
        try { await cartApi.clearCart(getState().auth.token); }
        catch (err) { return rejectWithValue(err instanceof Error ? err.message : 'Failed to clear cart'); }
    }
);

function addPending(state: CartState, id: number) {
    if (!state.pendingItemIds.includes(id)) state.pendingItemIds.push(id);
}
function removePending(state: CartState, id: number) {
    state.pendingItemIds = state.pendingItemIds.filter((i) => i !== id);
}

export const cartSlice = createSlice({
    name: 'cart',
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(fetchCart.pending, (state) => { state.status = 'loading'; state.error = null; })
            .addCase(fetchCart.fulfilled, (state, action) => { state.status = 'succeeded'; state.cart = action.payload; })
            .addCase(fetchCart.rejected, (state, action) => {
                state.status = 'failed';
                state.error = (action.payload as string) ?? 'Failed to load cart';
            })
            .addCase(addToCart.pending, (state, action) => addPending(state, action.meta.arg.menuItemId))
            .addCase(addToCart.fulfilled, (state, action) => {
                removePending(state, action.meta.arg.menuItemId);
                state.cart = action.payload;
            })
            .addCase(addToCart.rejected, (state, action) => {
                removePending(state, action.meta.arg.menuItemId);
                state.error = (action.payload as string) ?? 'Failed to add item';
            })
            .addCase(updateCartItemQuantity.pending, (state, action) => addPending(state, action.meta.arg.menuItemId))
            .addCase(updateCartItemQuantity.fulfilled, (state, action) => {
                removePending(state, action.meta.arg.menuItemId);
                state.cart = action.payload;
            })
            .addCase(updateCartItemQuantity.rejected, (state, action) => {
                removePending(state, action.meta.arg.menuItemId);
                state.error = (action.payload as string) ?? 'Failed to update item';
            })
            .addCase(removeFromCart.pending, (state, action) => addPending(state, action.meta.arg))
            .addCase(removeFromCart.fulfilled, (state, action) => {
                removePending(state, action.meta.arg);
                state.cart = action.payload;
            })
            .addCase(removeFromCart.rejected, (state, action) => {
                removePending(state, action.meta.arg);
                state.error = (action.payload as string) ?? 'Failed to remove item';
            })
            .addCase(clearCart.pending, (state) => { state.clearStatus = 'loading'; })
            .addCase(clearCart.fulfilled, (state) => {
                state.clearStatus = 'idle';
                if (state.cart) { state.cart.items = []; state.cart.total = 0; }
            })
            .addCase(clearCart.rejected, (state, action) => {
                state.clearStatus = 'failed';
                state.error = (action.payload as string) ?? 'Failed to clear cart';
            });
    },
});

export default cartSlice.reducer;