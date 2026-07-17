import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { cartApi, type CartResponse } from '../../api/cartApi';
import type { RootState } from '../../store/store';

interface CartState {
    cart: CartResponse | null;
    status: 'idle' | 'loading' | 'succeeded' | 'failed';
    mutationStatus: 'idle' | 'loading' | 'failed';
    error: string | null;
}

const initialState: CartState = {
    cart: null,
    status: 'idle',
    mutationStatus: 'idle',
    error: null,
};

type ThunkConfig = { state: RootState };

export const fetchCart = createAsyncThunk<CartResponse, void, ThunkConfig>(
    'cart/fetchCart',
    async (_, { getState, rejectWithValue }) => {
        try {
            return await cartApi.getCart(getState().auth.token);
        } catch (err) {
            return rejectWithValue(err instanceof Error ? err.message : 'Failed to load cart');
        }
    }
);

export const addToCart = createAsyncThunk<
    CartResponse,
    { menuItemId: number; quantity: number },
    ThunkConfig
>('cart/addToCart', async ({ menuItemId, quantity }, { getState, rejectWithValue }) => {
    try {
        return await cartApi.addItem(getState().auth.token, menuItemId, quantity);
    } catch (err) {
        return rejectWithValue(err instanceof Error ? err.message : 'Failed to add item');
    }
});

export const updateCartItemQuantity = createAsyncThunk<
    CartResponse,
    { menuItemId: number; quantity: number },
    ThunkConfig
>('cart/updateCartItemQuantity', async ({ menuItemId, quantity }, { getState, rejectWithValue }) => {
    try {
        return await cartApi.updateItemQuantity(getState().auth.token, menuItemId, quantity);
    } catch (err) {
        return rejectWithValue(err instanceof Error ? err.message : 'Failed to update item');
    }
});

export const removeFromCart = createAsyncThunk<CartResponse, number, ThunkConfig>(
    'cart/removeFromCart',
    async (menuItemId, { getState, rejectWithValue }) => {
        try {
            return await cartApi.removeItem(getState().auth.token, menuItemId);
        } catch (err) {
            return rejectWithValue(err instanceof Error ? err.message : 'Failed to remove item');
        }
    }
);

export const clearCart = createAsyncThunk<void, void, ThunkConfig>(
    'cart/clearCart',
    async (_, { getState, rejectWithValue }) => {
        try {
            await cartApi.clearCart(getState().auth.token);
        } catch (err) {
            return rejectWithValue(err instanceof Error ? err.message : 'Failed to clear cart');
        }
    }
);

export const cartSlice = createSlice({
    name: 'cart',
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder
            // --- all addCase calls first ---
            .addCase(fetchCart.pending, (state) => {
                state.status = 'loading';
                state.error = null;
            })
            .addCase(fetchCart.fulfilled, (state, action) => {
                state.status = 'succeeded';
                state.cart = action.payload;
            })
            .addCase(fetchCart.rejected, (state, action) => {
                state.status = 'failed';
                state.error = (action.payload as string) ?? 'Failed to load cart';
            })
            .addCase(clearCart.fulfilled, (state) => {
                state.mutationStatus = 'idle';
                if (state.cart) {
                    state.cart.items = [];
                    state.cart.total = 0;
                }
            })
            // --- addMatcher calls after, for the thunks that share identical handling ---
            .addMatcher(
                (action) =>
                    [addToCart.pending, updateCartItemQuantity.pending, removeFromCart.pending, clearCart.pending].some(
                        (a) => a.match(action)
                    ),
                (state) => {
                    state.mutationStatus = 'loading';
                    state.error = null;
                }
            )
            .addMatcher(
                (action): action is ReturnType<typeof addToCart.fulfilled> =>
                    [addToCart.fulfilled, updateCartItemQuantity.fulfilled, removeFromCart.fulfilled].some((a) =>
                        a.match(action)
                    ),
                (state, action) => {
                    state.mutationStatus = 'idle';
                    state.cart = action.payload;
                }
            )
            .addMatcher(
                (action): action is ReturnType <
                            typeof addToCart.rejected |
                            typeof updateCartItemQuantity.rejected |
                            typeof removeFromCart.rejected |
                            typeof clearCart.rejected
                > =>
                    [addToCart.rejected, updateCartItemQuantity.rejected, removeFromCart.rejected, clearCart.rejected].some(
                        (a) => a.match(action)
                    ),
                (state, action) => {
                    state.mutationStatus = 'failed';
                    state.error = (action.payload as string) ?? 'Cart update failed';
                }
            );
    },
});

export default cartSlice.reducer;