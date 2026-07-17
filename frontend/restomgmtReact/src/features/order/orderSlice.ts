import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { orderApi, type OrderResponse, type OrderStatus } from '../../api/orderApi';
import type { RootState } from '../../store/store';

interface OrderState {
    myOrders: OrderResponse[];
    allOrders: OrderResponse[];
    status: 'idle' | 'loading' | 'succeeded' | 'failed';
    mutationStatus: 'idle' | 'loading' | 'failed';
    error: string | null;
    lastPlacedOrder: OrderResponse | null;
}

const initialState: OrderState = {
    myOrders: [],
    allOrders: [],
    status: 'idle',
    mutationStatus: 'idle',
    error: null,
    lastPlacedOrder: null,
};

type ThunkConfig = { state: RootState };

export const placeOrder = createAsyncThunk<OrderResponse, void, ThunkConfig>(
    'orders/placeOrder',
    async (_, { getState, rejectWithValue }) => {
        try {
            return await orderApi.placeOrder(getState().auth.token);
        } catch (err) {
            return rejectWithValue(err instanceof Error ? err.message : 'Failed to place order');
        }
    }
);

export const fetchMyOrders = createAsyncThunk<OrderResponse[], void, ThunkConfig>(
    'orders/fetchMyOrders',
    async (_, { getState, rejectWithValue }) => {
        try {
            return await orderApi.getMyOrders(getState().auth.token);
        } catch (err) {
            return rejectWithValue(err instanceof Error ? err.message : 'Failed to load orders');
        }
    }
);

export const cancelOrder = createAsyncThunk<OrderResponse, number, ThunkConfig>(
    'orders/cancelOrder',
    async (orderId, { getState, rejectWithValue }) => {
        try {
            return await orderApi.cancelOrder(getState().auth.token, orderId);
        } catch (err) {
            return rejectWithValue(err instanceof Error ? err.message : 'Failed to cancel order');
        }
    }
);

export const fetchAllOrders = createAsyncThunk<OrderResponse[], void, ThunkConfig>(
    'orders/fetchAllOrders',
    async (_, { getState, rejectWithValue }) => {
        try {
            return await orderApi.getAllOrders(getState().auth.token);
        } catch (err) {
            return rejectWithValue(err instanceof Error ? err.message : 'Failed to load orders');
        }
    }
);

export const advanceOrderStatus = createAsyncThunk<
    OrderResponse,
    { orderId: number; status: OrderStatus },
    ThunkConfig
>('orders/advanceOrderStatus', async ({ orderId, status }, { getState, rejectWithValue }) => {
    try {
        return await orderApi.advanceOrderStatus(getState().auth.token, orderId, status);
    } catch (err) {
        return rejectWithValue(err instanceof Error ? err.message : 'Failed to update status');
    }
});

export const orderSlice = createSlice({
    name: 'orders',
    initialState,
    reducers: {
        clearLastPlacedOrder: (state) => {
            state.lastPlacedOrder = null;
        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(placeOrder.pending, (state) => {
                state.mutationStatus = 'loading';
                state.error = null;
            })
            .addCase(placeOrder.fulfilled, (state, action) => {
                state.mutationStatus = 'idle';
                state.lastPlacedOrder = action.payload;
                state.myOrders.unshift(action.payload);
            })
            .addCase(placeOrder.rejected, (state, action) => {
                state.mutationStatus = 'failed';
                state.error = (action.payload as string) ?? 'Failed to place order';
            })
            .addCase(fetchMyOrders.pending, (state) => {
                state.status = 'loading';
                state.error = null;
            })
            .addCase(fetchMyOrders.fulfilled, (state, action) => {
                state.status = 'succeeded';
                state.myOrders = action.payload;
            })
            .addCase(fetchMyOrders.rejected, (state, action) => {
                state.status = 'failed';
                state.error = (action.payload as string) ?? 'Failed to load orders';
            })
            .addCase(fetchAllOrders.pending, (state) => {
                state.status = 'loading';
                state.error = null;
            })
            .addCase(fetchAllOrders.fulfilled, (state, action) => {
                state.status = 'succeeded';
                state.allOrders = action.payload;
            })
            .addCase(fetchAllOrders.rejected, (state, action) => {
                state.status = 'failed';
                state.error = (action.payload as string) ?? 'Failed to load orders';
            })
            .addCase(cancelOrder.fulfilled, (state, action) => {
                const idx = state.myOrders.findIndex((o) => o.orderId === action.payload.orderId);
                if (idx !== -1) state.myOrders[idx] = action.payload;
            })
            .addCase(advanceOrderStatus.fulfilled, (state, action) => {
                const idx = state.allOrders.findIndex((o) => o.orderId === action.payload.orderId);
                if (idx !== -1) state.allOrders[idx] = action.payload;
            });
    },
});

export const { clearLastPlacedOrder } = orderSlice.actions;
export default orderSlice.reducer;